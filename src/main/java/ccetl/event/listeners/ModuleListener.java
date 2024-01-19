package ccetl.event.listeners;

import ccetl.event.DefaultPriorities;

public abstract class ModuleListener<M, E> implements Listener<E> {
    public final M module;
    private final Class<E> target;
    private final byte priority;

    public ModuleListener(M module, Class<E> target) {
        this.module = module;
        this.target = target;
        this.priority = DefaultPriorities.NORMAL;
    }

    public ModuleListener(M module, Class<E> target, byte priority) {
        this.module = module;
        this.target = target;
        this.priority = priority;
    }

    @Override
    public byte getPriority() {
        return priority;
    }

    @Override
    public Class<E> getTarget() {
        return target;
    }
}
