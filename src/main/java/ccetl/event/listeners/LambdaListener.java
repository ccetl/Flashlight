package ccetl.event.listeners;

import ccetl.event.DefaultPriorities;
import ccetl.event.EventSystem;
import ccetl.event.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Adds simple lambda functions to the event system.
 * @param <E> target event
 * @see Listener
 * @see EventSystem
 */
public class LambdaListener<E> implements Listener<E> {
    /**
     * The underlying listener to be executed when the event conditions are met.
     */
    private final Consumer<E> listener;
    /**
     * The priority of this listener.
     */
    private final byte priority;
    /**
     * The target event, what triggers this listener.
     */
    private final Class<? super E> target;
    /**
     * A predicate that determines whether the listener should be invoked based on a condition.
     * If null, the listener is invoked unconditionally.
     */
    private final Predicate<E> filter;

    /**
     * Constructs a new LambdaListener with the specified parameters.
     *
     * @param target   The class type of events this listener is interested in.
     * @param filter   The predicate to filter events. If null, no filtering is applied.
     * @param listener The listener to be executed when the event conditions are met.
     * @param priority The priority of this listener.
     */
    public LambdaListener(Class<? super E> target, @Nullable Predicate<E> filter, Consumer<E> listener, byte priority) {
        this.listener = listener;
        this.priority = priority;
        this.target = target;
        this.filter = filter;
    }

    /**
     * Constructs a new LambdaListener with default priority and no event filtering.
     *
     * @param target   The class type of events this listener is interested in.
     * @param listener The listener to be executed when the event conditions are met.
     * @param priority The priority of this listener.
     */
    public LambdaListener(Class<? super E> target, Consumer<E> listener, byte priority) {
        this(target, null, listener, priority);
    }
    
    // Additional constructors for convenience...

    public LambdaListener(Class<? super E> target, byte priority, Consumer<E> listener) {
        this(target, null, listener, priority);
    }
    
    public LambdaListener(Class<? super E> target, Consumer<E> listener) {
        this(target, null, listener, DefaultPriorities.NORMAL);
    }

    public LambdaListener(Class<? super E> target, @Nullable Predicate<E> filter, Consumer<E> listener) {
        this(target, filter, listener, DefaultPriorities.NORMAL);
    }

    /**
     * Invokes the underlying listener if the event passes the filter condition.
     *
     * @param event The event to be processed by the listener.
     */
    @Override
    public void invoke(E event) {
        listener.accept(event);
    }

    @Override
    public boolean filter(E event) {
        return filter == null || filter.test(event);
    }

    @Override
    public Class<? super E> getTarget() {
        return target;
    }

    @Override
    public byte getPriority() {
        return priority;
    }
}
