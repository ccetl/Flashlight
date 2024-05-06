package ccetl.flashlight.annotations;

import ccetl.flashlight.dispatcher.EventSystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the priority of the method (optional, 0 by default).
 * @see EventListener
 * @see EventSystem
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ListenerPriority {
    byte priority();
}
