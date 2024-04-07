package ccetl.flashlight;

public class PersistentCancelable extends Cancelable {
    /**
     * Sets the cancelation status to false.
     */
    public void rescind() {
        this.canceled = false;
    }
}