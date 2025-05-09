package ccetl.flashlight.listener;

import ccetl.flashlight.event.Cancelable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CancelerTest {

    @Test
    void invoke_CancelsEvent() {
        Cancelable event = new Cancelable() {};
        Canceler<Cancelable> canceler = new Canceler<>(Cancelable.class, e -> true);

        canceler.invoke(event);
        assertTrue(event.isCanceled());
    }

}