package ccetl.flashlight.listeners;

import ccetl.flashlight.DefaultPriorities;

public abstract class ModuleListener<M, E> implements Listener<E> {
    public final M module;
    private final Class<? super E> target;
    private final byte priority;

    public ModuleListener(M module, Class<? super E> target) {
        this.module = module;
        this.target = target;
        this.priority = DefaultPriorities.NORMAL;
    }

    public ModuleListener(M module, Class<? super E> target, byte priority) {
        this.module = module;
        this.target = target;
        this.priority = priority;
    }

    @Override
    public byte getPriority() {
        return priority;
    }

    @Override
    public Class<? super E> getTarget() {
        return target;
    }
}
