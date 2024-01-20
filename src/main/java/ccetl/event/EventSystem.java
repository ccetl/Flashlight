package ccetl.event;

import ccetl.event.annotations.ListenerPriority;
import ccetl.event.listeners.Listener;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An event system.
 *
 * @see Cancelable
 * @see java.util.EventListener
 * @see Listener
 */
@SuppressWarnings({"unused", "UnusedReturnValue", "rawtypes", "unchecked"})
public class EventSystem implements IEventSystem {
    private final Map<Class<?>, List<Listener>> listeners = new HashMap<>();
    /**
     * The thread pool for asynchronous execution.
     */
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    /**
     * A locking mechanism to ensure thread safety, so you can't post and register at
     * the same time, for example. It also ensures that only one event / listeners list
     * modification gets posted at a time.
     */
    private final Lock reentrantLock = new ReentrantLock();

    @Override
    public boolean post(Object event) {
        return post(event, false, true);
    }

    @Override
    public boolean post(Object event, boolean asynchronous, boolean await) {
        reentrantLock.lock();
        try {
            final List<Listener> listeners = this.listeners.get(event.getClass());
            if (listeners == null || listeners.isEmpty()) {
                return false;
            }

            if (asynchronous) {
                CountDownLatch countDownLatch = new CountDownLatch(listeners.size());
                for (Listener listener : listeners) {
                    if (!listener.filter(event)) {
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
                        return false;
                    }
                }
            } else {
                for (Listener listener : listeners) {
                    if (!listener.filter(event)) {
                        continue;
                    }

                    listener.invoke(event);
                }
            }

            if (event instanceof Cancelable) {
                return ((Cancelable) event).isCanceled();
            } else {
                return false;
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    @Deprecated
    public void postReversed(Object event) {
        reentrantLock.lock();
        try {
            final List<Listener> listeners = this.listeners.get(event.getClass());
            for (int i = listeners.size() - 1; i >= 0; i--) {
                Listener listener = listeners.get(i);
                if (!listener.filter(event)) {
                    continue;
                }

                listener.invoke(event);
            }
        } finally {
            reentrantLock.unlock();
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

        register(new MethodListener<>(eventClass, method, provider, priority), eventClass, priority);
        return true;
    }

    @Override
    public void register(Listener<?> listener) {
        register(listener, listener.getTarget(), listener.getPriority());
    }

    private void register(Listener<?> listener, Class<?> target, byte priority) {
        reentrantLock.lock();
        try {
            List<Listener> pairs = listeners.computeIfAbsent(target, e -> new LinkedList<>());

            if (pairs.isEmpty()) {
                pairs.add(listener);
                return;
            }

            int insertionIndex = Collections.binarySearch(pairs, listener, Listener::compareTo);
            if (insertionIndex < 0) {
                insertionIndex = Math.abs(insertionIndex) - 1;
            } //we could loop here until we have the last position on duplicated listener priorities
            //but I consider it as useless task since it changes nothing and could take some time if we have a lot of listeners
            //of one priority
            pairs.add(insertionIndex, listener);
        } finally {
            reentrantLock.unlock();
        }
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
        return deregister(eventClass, new MethodListener<>(eventClass, method, provider, DefaultPriorities.NORMAL));
    }

    @Override
    public boolean deregister(Listener<?> listener) {
        return deregister(listener.getTarget(), listener);
    }

    @Override
    public boolean hasListeners(Class<?> eventClass) {
        reentrantLock.lock();
        try {
            List<Listener> listeners = this.listeners.get(eventClass);
            return listeners != null && !listeners.isEmpty();
        } finally {
            reentrantLock.unlock();
        }
    }

    private boolean deregister(Class<?> event, Listener<?> listener) {
        reentrantLock.lock();
        try {
            List<Listener> pairs = this.listeners.get(event);
            if (pairs != null) {
                return pairs.removeIf(p -> p.equals(listener));
            }
            return false;
        } finally {
            reentrantLock.unlock();
        }
    }

    private boolean isNotValid(Method method) {
        return !method.isAnnotationPresent(ccetl.event.annotations.EventListener.class) || method.getParameterCount() != 1;
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
