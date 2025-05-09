package ccetl.flashlight.event;

public interface TypeEvent {

    /**
     * What subtype the event is.
     *
     * @return the class of the subtype
     */
    Class<?> getType();

}
