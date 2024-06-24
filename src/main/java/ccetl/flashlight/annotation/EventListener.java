package ccetl.flashlight.annotation;

import ccetl.flashlight.dispatcher.EventSystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks event listener functions.
 * @see ListenerPriority
 * @see EventSystem
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener {
}
