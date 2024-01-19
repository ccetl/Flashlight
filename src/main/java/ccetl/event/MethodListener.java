package ccetl.event;

import ccetl.event.annotations.Nullable;
import ccetl.event.listeners.LambdaListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

class MethodListener<E> extends LambdaListener<E> {
    private final Object provider;
    private final Method method;

    public MethodListener(Class<E> target, Method method, @Nullable Object provider, byte priority) {
        super(target, event -> {
            try {
                method.invoke(provider, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, priority);
        this.provider = provider;
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodListener<?> that = (MethodListener<?>) o;

        if (!Objects.equals(provider, that.provider)) return false;
        return method.equals(that.method);
    }

    @Override
    public int hashCode() {
        int result = provider != null ? provider.hashCode() : 0;
        result = 31 * result + method.hashCode();
        return result;
    }
}
