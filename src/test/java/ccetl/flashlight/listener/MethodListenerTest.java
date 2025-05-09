package ccetl.flashlight.listener;

import ccetl.flashlight.annotation.EventListener;
import ccetl.flashlight.annotation.EventType;
import ccetl.flashlight.annotation.ListenerPriority;
import ccetl.flashlight.dispatcher.EventSystem;
import ccetl.flashlight.event.DefaultPriorities;
import ccetl.flashlight.event.TypeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class MethodListenerTest {

    private EventSystem eventSystem;

    private static class TestEvent implements TypeEvent {
        private final Class<?> clazz;

        private TestEvent(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Class<?> getType() {
            return clazz;
        }
    }

    @BeforeEach
    void setUp() {
        eventSystem = new EventSystem();
    }

    @Test
    void methodPriority() throws InterruptedException {
        PriorityListenerContainingClassA object = new PriorityListenerContainingClassA();
        eventSystem.register(object);
        eventSystem.post(new TestEvent(String.class));
        assertTrue(object.latch.await(1, TimeUnit.SECONDS));
        assertEquals(3, object.counter.get());
    }

    @Test
    void methodTypeFilter() throws InterruptedException {
        PriorityListenerContainingClassB object = new PriorityListenerContainingClassB();
        eventSystem.register(object);
        eventSystem.post(new TestEvent(String.class));
        assertTrue(object.latch.await(1, TimeUnit.SECONDS));
    }

    private static class PriorityListenerContainingClassA {
        public AtomicInteger counter = new AtomicInteger();
        public CountDownLatch latch = new CountDownLatch(3);

        @EventListener
        @ListenerPriority(priority = DefaultPriorities.LOW)
        public void listenerA(TestEvent event) {
            assertEquals(3, counter.incrementAndGet());
            latch.countDown();
        }

        @EventListener
        @ListenerPriority(priority = DefaultPriorities.HIGH)
        public void listenerB(TestEvent event) {
            assertEquals(1, counter.incrementAndGet());
            latch.countDown();
        }

        @EventListener
        @ListenerPriority(priority = DefaultPriorities.NORMAL)
        public void listenerC(TestEvent event) {
            assertEquals(2, counter.incrementAndGet());
            latch.countDown();
        }

        @ListenerPriority(priority = DefaultPriorities.HIGH)
        private void notAListener(TestEvent event) {
            fail();
        }
    }

    private static class PriorityListenerContainingClassB {
        public CountDownLatch latch = new CountDownLatch(2);

        @EventListener
        @EventType(typeFilter = String.class)
        public void listenerA(TestEvent event) {
            latch.countDown();
        }

        @EventListener
        public void listenerB(TestEvent event) {
            latch.countDown();
        }

        @EventListener
        @EventType(typeFilter = Integer.class)
        public void listenerC(TestEvent event) {
            fail();
        }

    }

}
