package ccetl.event.listeners;

import ccetl.event.Cancelable;
import ccetl.event.DefaultPriorities;
import ccetl.event.annotations.Nullable;

import java.util.function.Predicate;

public class Canceler<E extends Cancelable> extends LambdaListener<E> {
    public Canceler(Class<E> target, @Nullable Predicate<E> predicate, byte priority) {
        super(target, predicate, Cancelable::cancel, priority);
    }

    public Canceler(Class<E> target, Predicate<E> predicate) {
        this(target, predicate, DefaultPriorities.NORMAL);
    }

    public Canceler(Class<E> target, byte priority) {
        this(target, null, priority);
    }

    public Canceler(Class<E> target) {
        this(target, null, DefaultPriorities.NORMAL);
    }

    @Override
    public void invoke(E event) {
        if (event.isCanceled()) {
            return;
        }

        super.invoke(event);
    }
}
