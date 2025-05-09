package ccetl.flashlight.dispatcher;

import ccetl.flashlight.annotation.Nullable;
import ccetl.flashlight.event.Cancelable;
import ccetl.flashlight.listener.Listener;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Synchronous-only implementation; Doesn't support filters, types and annotations.
 * Therefore, it's 3.5 times faster than {@link EventSystem}.
 * Although for an event system with more functionality use {@link EventSystem}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class EventBus implements IEventSystem {

    private final Map<Class<?>, List<Listener>> listeners = new ConcurrentHashMap<>();

    @Override
    public boolean post(Object event) {
        final List<Listener> listeners = this.listeners.get(event.getClass());
        if (listeners == null || listeners.isEmpty()) {
            return false;
        }

        for (Listener listener : listeners) {
            listener.invoke(event);
        }

        if (event instanceof Cancelable) {
            return ((Cancelable) event).isCanceled();
        } else {
            return false;
        }
    }

    @Override
    public boolean post(Object event, boolean asynchronous, boolean await) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void register(Object... objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void register(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean register(Method method, Object provider) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void register(Listener<?> listener) {
        List<Listener> pairs = listeners.computeIfAbsent(listener.getTarget(), e -> new CopyOnWriteArrayList<>());
        int insertionIndex = Collections.binarySearch(pairs, listener, Listener::compareTo);
        if (insertionIndex < 0) {
            insertionIndex = ~insertionIndex;
        }
        pairs.add(insertionIndex, listener);
    }

    @Override
    public void deregister(Object... objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deregister(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deregister(Method method, Object provider) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deregister(Listener<?> listener) {
        List<Listener> pairs = this.listeners.get(listener.getTarget());
        if (pairs != null) {
            return pairs.removeIf(p -> p.equals(listener));
        }
        return false;
    }

    @Override
    public void deregisterAll(Class<?> clazz) {
        listeners.remove(clazz);
    }

    @Override
    public boolean hasListeners(Class<?> eventClass) {
        List<Listener> listeners = this.listeners.get(eventClass);
        return listeners != null && !listeners.isEmpty();
    }

    @Override
    public void scan(@Nullable Consumer<Class<?>> eventClassScanner, @Nullable BiConsumer<Class<?>, Listener> listenerScanner) {
        Scanner.scanListeners(listeners, eventClassScanner, listenerScanner);
    }

}
