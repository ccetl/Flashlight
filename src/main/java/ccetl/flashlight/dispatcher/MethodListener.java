package ccetl.flashlight.dispatcher;

import ccetl.flashlight.annotation.Nullable;
import ccetl.flashlight.listener.LambdaListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

class MethodListener<E> extends LambdaListener<E> {
    private final Object provider;
    private final Method method;

    public MethodListener(Class<E> target, Method method, @Nullable Object provider, byte priority) {
        super(target, priority, event -> {
            try {
                method.invoke(provider, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
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
