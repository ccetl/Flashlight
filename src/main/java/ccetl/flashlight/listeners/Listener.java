package ccetl.flashlight.listeners;

import ccetl.flashlight.annotations.NotNull;
import ccetl.flashlight.dispatcher.EventSystem;
import ccetl.flashlight.event.DefaultPriorities;

/**
 * Listener interface.
 * @param <E> target event
 * @see LambdaListener
 * @see EventSystem
 */
public interface Listener<E> {
    /**
     * Executes the listener's code.
     *
     * @param event the event what triggered the listener's execution
     */
    void invoke(E event);

    /**
     * Gets the class type of events that this listener is interested in.
     *
     * @return The target class type.
     */
    Class<? super E> getTarget();

    /**
     * Gets the priority of this listener.
     *
     * @return The listener priority.
     */
    default byte getPriority() {
        return DefaultPriorities.NORMAL;
    }

    /**
     * Filter the execution.
     *
     * @return true if the listener should be executed
     */
    default boolean filter(E event) {
        return true;
    }

    /**
     * Filter the execution.
     *
     * @return true if the listener should be executed
     */
    default boolean filterType(Class<?> eventClass) {
        return true;
    }

    /**
     * Compares the priorities of two listeners to
     * use binary search in listener lists.
     *
     * @param o the other listener
     * @return the result (0 if the priorities are equal)
     */
    default int compareTo(@NotNull Listener<?> o) {
        return Byte.compare(o.getPriority(), getPriority());
    }
}
