package ccetl.flashlight.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class ListenersContainer {
    private final List<Listener<?>> listeners = new ArrayList<>();

    public <T> Listener<T> register(Listener<T> listener) {
        listeners.add(listener);
        return listener;
    }

    public void register(Listener<?>... listeners) {
        for (Listener<?> listener : listeners) {
            this.listeners.add(Objects.requireNonNull(listener));
        }
    }

    public <T> Listener<T> deregister(Listener<T> listener) {
        listeners.remove(listener);
        return listener;
    }

    public void deregister(Listener<?>... listeners) {
        for (Listener<?> listener : listeners) {
            this.listeners.remove(listener);
        }
    }

    public List<Listener<?>> getListeners() {
        return listeners;
    }

    /**
     * Should enable all listeners stored. For example:
     * <pre> {@code
     * Main.EVENT_SYSTEM.register(this);
     * for (Listener<?> listener : listeners) {
     *     Main.EVENT_SYSTEM.register(listener);
     * }}</pre>
     */
    public abstract void enableListeners();

    /**
     * Should disable all listeners stored. For example:
     * <pre> {@code
     * Main.EVENT_SYSTEM.deregister(this);
     * for (Listener<?> listener : listeners) {
     *     Main.EVENT_SYSTEM.deregister(listener);
     * }}</pre>
     */
    public abstract void disableListeners();
}
