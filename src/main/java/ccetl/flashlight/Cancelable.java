package ccetl.flashlight;

public abstract class Cancelable {
    /**
     * Stores the cancelation status.
     */
    protected boolean canceled = false;

    /**
     * @return the cancelation status
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Sets the cancelation status to true.
     */
    public void cancel() {
        this.canceled = true;
    }
}
