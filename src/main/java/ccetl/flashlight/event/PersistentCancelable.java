package ccetl.flashlight.event;

public class PersistentCancelable extends Cancelable {
    /**
     * Sets the cancelation status to false.
     */
    public void rescind() {
        this.canceled = false;
    }
}
