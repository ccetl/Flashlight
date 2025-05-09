package ccetl.flashlight.annotation;

import ccetl.flashlight.dispatcher.EventSystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ccetl.flashlight.event.TypeEvent;

/**
 * A filter to filter {@link TypeEvent}.
 *
 * @see ListenerPriority
 * @see EventSystem
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventType {

     Class<?> typeFilter();

}
