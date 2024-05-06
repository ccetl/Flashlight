package ccetl.flashlight.dispatcher;

import ccetl.flashlight.annotations.Nullable;
import ccetl.flashlight.listeners.Listener;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IEventSystem {
    /**
     * This will execute the listeners synchronously.
     *
     * @param event the event to post
     * @return true if the event got canceled
     */
    boolean post(Object event);

    /**
     * @param event        the event to post
     * @param asynchronous Determines whether the listeners get executed synchronous (false) or asynchronous (true);
     *                     asynchronously ignores the priority and executes the invoker on multiple threads, so
     *                     your event and listener for asynchronous execution have to be thread save.
     * @param await        Waits until all listeners have been executed before continuing with the thread calling this
     *                     method. This doesn't matter if you run the execution synchronous since you already run the
     *                     listeners on the calling thread this way. It should always be true when you need to know the
     *                     cancellation status.
     * @return the cancellation status
     */
    boolean post(Object event, boolean asynchronous, boolean await);

    /**
     * This method will register all annotated listeners from the given objects.
     *
     * @param objects target objects
     */
    void register(Object... objects);

    /**
     * This method will register all annotated listeners from the given object.
     *
     * @param object target object
     */
    void register(Object object);

    /**
     * @param method   target method
     * @param provider target object
     * @return true when the method was successfully registered
     */
    boolean register(Method method, Object provider);

    /**
     * @param listener target listener
     */
    void register(Listener<?> listener);

    /**
     * This method will deregister all annotated listeners from the given objects.
     *
     * @param objects target object
     */
    void deregister(Object... objects);

    /**
     * This method will deregister all annotated listeners from the given object.
     *
     * @param object target object
     */
    void deregister(Object object);

    /**
     * @param method   target method
     * @param provider target object
     * @return true if the method was present and got removed
     */
    boolean deregister(Method method, Object provider);

    /**
     * @param listener target listener
     * @return true if the method was present and got removed
     */
    boolean deregister(Listener<?> listener);

    /**
     * This method will deregister all listeners for the specified event.
     *
     * @param clazz the class of the event
     */
    void deregisterAll(Class<?> clazz);

    /**
     * @param eventClass what event the listeners have to listen to
     * @return true when the event system has registered listeners for the event
     */
    boolean hasListeners(Class<?> eventClass);

    /**
     * Scans through all listeners.
     * <p>
     * The order is as following:
     * First, the event classes known to the event system are delivered to the {@code eventClassScanner}.
     * Then, all listeners are delivered to the {@code listenerScanner}.
     * This repeats for all events.
     *
     * @param eventClassScanner A consumer to be invoked with each event class known to the event system. Can be {@code null}.
     * @param listenerScanner   A consumer to be invoked with each event class-listener pair encountered during scanning. Can be {@code null}.
     */
    @SuppressWarnings("rawtypes")
    void scan(@Nullable Consumer<Class<?>> eventClassScanner, @Nullable BiConsumer<Class<?>, Listener> listenerScanner);
}
