package ccetl.flashlight.listener;

import ccetl.flashlight.event.DefaultPriorities;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LambdaListenerTest {

    @Test
    void listenerChain_WithTargetType() {
        AtomicBoolean invoked = new AtomicBoolean(false);
        LambdaListener<String> listener = new LambdaListener<>(String.class, DefaultPriorities.NORMAL, s -> invoked.set(true));

        listener.invoke("test");
        assertTrue(invoked.get());
        assertEquals(String.class, listener.getTarget());
    }

}