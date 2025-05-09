package ccetl.flashlight.dispatcher;

import ccetl.flashlight.listener.Listener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventBusTest {

    private EventBus eventBus;

    private static class TestEvent {
    }

    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
    }

    @Test
    void threadSafety() throws InterruptedException {
        int threads = 100;
        CountDownLatch latch = new CountDownLatch(threads);
        ConcurrentHashMap<Integer, Boolean> results = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            final int id = i;
            Listener<TestEvent> listener = new Listener<TestEvent>() {
                @Override
                public void invoke(TestEvent event) {
                    results.put(id, true);
                }

                @Override
                public Class<? super TestEvent> getTarget() {
                    return TestEvent.class;
                }
            };
            eventBus.register(listener);
            executor.submit(() -> {
                eventBus.post(new TestEvent());
                latch.countDown();
            });
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        executor.shutdown();
        assertEquals(threads, results.size());
    }

    @Test
    void deregisterDuringPropagation() {
        AtomicInteger count = new AtomicInteger();
        Listener<TestEvent> selfRemovingListener = new Listener<TestEvent>() {
            @Override
            public void invoke(TestEvent event) {
                count.incrementAndGet();
                eventBus.deregister(this);
            }

            @Override
            public Class<? super TestEvent> getTarget() {
                return TestEvent.class;
            }
        };

        eventBus.register(selfRemovingListener);
        eventBus.post(new TestEvent());
        eventBus.post(new TestEvent());
        assertEquals(1, count.get());
    }

}