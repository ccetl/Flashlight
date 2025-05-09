package ccetl.flashlight.dispatcher;

import ccetl.flashlight.annotation.EventListener;
import ccetl.flashlight.annotation.EventType;
import ccetl.flashlight.annotation.ListenerPriority;
import ccetl.flashlight.annotation.Nullable;
import ccetl.flashlight.event.Cancelable;
import ccetl.flashlight.event.DefaultPriorities;
import ccetl.flashlight.event.TypeEvent;
import ccetl.flashlight.listener.Listener;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An event system.
 *
 * @see Cancelable
 * @see java.util.EventListener
 * @see Listener
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class EventSystem implements IEventSystem {

    private final Map<Class<?>, List<Listener>> listeners = new ConcurrentHashMap<>();
    /**
     * The thread pool for asynchronous execution.
     */
    private final ThreadPoolExecutor executorService;

    public EventSystem() {
        this(new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>()));
    }

    public EventSystem(ThreadPoolExecutor executorService) {
        this.executorService = executorService;
    }

    @Override
    public boolean post(Object event) {
        return post(event, false, true);
    }

    @Override
    public boolean post(Object event, boolean asynchronous, boolean await) {
        final List<Listener> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null || eventListeners.isEmpty()) {
            return false;
        }

        boolean type = event instanceof TypeEvent;

        if (asynchronous) {
            if (postAsynchronous(event, await, eventListeners, type)) {
                return false;
            }
        } else {
            postSynchronous(event, eventListeners, type);
        }

        if (event instanceof Cancelable) {
            return ((Cancelable) event).isCanceled();
        } else {
            return false;
        }
    }

    private void postSynchronous(Object event, List<Listener> listeners, boolean type) {
        for (Listener listener : listeners) {
            if (notFiltered(event, listener, type)) {
                continue;
            }

            listener.invoke(event);
        }
    }

    private boolean postAsynchronous(Object event, boolean await, List<Listener> listeners, boolean type) {
        CountDownLatch countDownLatch = new CountDownLatch(listeners.size());
        for (Listener listener : listeners) {
            if (notFiltered(event, listener, type)) {
                countDownLatch.countDown();
                continue;
            }

            executorService.submit(() -> {
                listener.invoke(event);
                countDownLatch.countDown();
            });
        }

        if (await) {
            try {
                countDownLatch.await(); //the event system waits here until all listeners are executed so that the cancellation info is accurate
            } catch (InterruptedException e) {
                return true;
            }
        }
        return false;
    }

    /**
     * Will post the event but in reversed order.
     * That means events with a lower priority will receive the event earlier than those with high priority.
     *
     * @param event The event to post.
     * @deprecated This method should be avoided as it leads to a confusing order. It's included to create an easier
     *             transition from other event systems.
     */
    @Deprecated
    public void postReversed(Object event) {
        final List<Listener> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null || eventListeners.isEmpty()) {
            return;
        }

        boolean type = event instanceof TypeEvent;
        for (int i = eventListeners.size() - 1; i >= 0; i--) {
            Listener listener = eventListeners.get(i);
            if (notFiltered(event, listener, type)) {
                continue;
            }

            listener.invoke(event);
        }
    }

    @Override
    public void register(Object... objects) {
        for (Object object : objects) {
            register(object);
        }
    }

    @Override
    public void register(Object object) {
        for (Method method : object.getClass().getMethods()) {
            register(method, object);
        }
    }

    @Override
    public boolean register(Method method, Object provider) {
        if (isNotValid(method)) {
            return false;
        }

        method.setAccessible(true);

        Class<?> eventClass = method.getParameterTypes()[0];

        byte priority = DefaultPriorities.NORMAL;
        if (method.isAnnotationPresent(ListenerPriority.class)) {
            priority = method.getAnnotation(ListenerPriority.class).priority();
        }

        Class<?> type = null;
        if (method.isAnnotationPresent(EventType.class)) {
            type = method.getAnnotation(EventType.class).typeFilter();
        }

        register(new MethodListener<>(eventClass, method, provider, priority, type), eventClass);
        return true;
    }

    @Override
    public void register(Listener<?> listener) {
        register(listener, listener.getTarget());
    }

    private void register(Listener<?> listener, Class<?> target) {
        List<Listener> pairs = listeners.computeIfAbsent(target, e -> new CopyOnWriteArrayList<>());

        if (pairs.isEmpty()) {
            pairs.add(listener);
            return;
        }

        int insertionIndex = Collections.binarySearch(pairs, listener, Listener::compareTo);
        if (insertionIndex < 0) {
            insertionIndex = ~insertionIndex;
        } // we could loop here until we have the last position on duplicated listener priorities
        // but I consider it as a useless task since it changes nothing and could take some time if we have a lot of listeners
        // of one priority
        pairs.add(insertionIndex, listener);
    }

    @Override
    public void deregister(Object... objects) {
        for (Object object : objects) {
            deregister(object);
        }
    }

    @Override
    public void deregister(Object object) {
        for (Method method : object.getClass().getMethods()) {
            deregister(method, object);
        }
    }

    @Override
    public boolean deregister(Method method, Object provider) {
        if (isNotValid(method)) {
            return false;
        }

        Class<?> eventClass = method.getParameterTypes()[0];
        return deregister(eventClass, new MethodListener<>(eventClass, method, provider, DefaultPriorities.NORMAL, null));
    }

    @Override
    public boolean deregister(Listener<?> listener) {
        return deregister(listener.getTarget(), listener);
    }

    @Override
    public void deregisterAll(Class<?> clazz) {
        List<Listener> listenerList = listeners.get(clazz);
        if (listenerList == null) {
            return;
        }

        listenerList.clear();
        listeners.remove(clazz);
    }

    @Override
    public boolean hasListeners(Class<?> eventClass) {
        List<Listener> eventListeners = listeners.get(eventClass);
        return eventListeners != null && !eventListeners.isEmpty();
    }

    @Override
    public void scan(@Nullable Consumer<Class<?>> eventClassScanner, @Nullable BiConsumer<Class<?>, Listener> listenerScanner) {
        Scanner.scanListeners(listeners, eventClassScanner, listenerScanner);
    }

    private boolean deregister(Class<?> event, Listener<?> listener) {
        List<Listener> pairs = this.listeners.get(event);
        if (pairs != null) {
            return pairs.removeIf(p -> p.equals(listener));
        }
        return false;
    }

    private boolean notFiltered(Object event, Listener listener, boolean type) {
        return type && !listener.filterType(((TypeEvent) event).getType()) || !listener.filter(event);
    }

    private boolean isNotValid(Method method) {
        return !method.isAnnotationPresent(EventListener.class) || method.getParameterCount() != 1;
    }

    /**
     * Calls {@link ThreadPoolExecutor#prestartAllCoreThreads()} to pre start all always idling threads.
     * This can avoid lagging on the first {@link EventSystem#post}.
     *
     * @return the number of threads started
     */
    public int preStart() {
        return executorService.prestartAllCoreThreads();
    }

    /**
     * Calls {@link ExecutorService#shutdownNow()} to stop all executing tasks.
     *
     * @return a list of the tasks that have not commenced execution
     */
    public List<Runnable> shutDown() {
        return executorService.shutdownNow();
    }

}
