package ccetl.flashlight.dispatcher;

import ccetl.flashlight.event.Cancelable;
import ccetl.flashlight.listener.Listener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventSystemTest {

    private EventSystem eventSystem;

    private static class TestEvent extends Cancelable {
    }

    private static final int CONCURRENT_THREADS = 100;

    @BeforeEach
    void setUp() {
        eventSystem = new EventSystem();
    }

    @Test
    void priorityOrder() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(3);

        eventSystem.register(new Listener<TestEvent>() {
            @Override
            public void invoke(TestEvent e) {
                assertEquals(2, counter.incrementAndGet());
                latch.countDown();
            }

            @Override
            public byte getPriority() {
                return 10;
            }

            @Override
            public Class<? super TestEvent> getTarget() {
                return TestEvent.class;
            }
        });

        eventSystem.register(new Listener<TestEvent>() {
            @Override
            public void invoke(TestEvent e) {
                assertEquals(3, counter.incrementAndGet());
                latch.countDown();
            }

            @Override
            public byte getPriority() {
                return -10;
            }

            @Override
            public Class<? super TestEvent> getTarget() {
                return TestEvent.class;
            }
        });

        eventSystem.register(new Listener<TestEvent>() {
            @Override
            public void invoke(TestEvent e) {
                assertEquals(1, counter.incrementAndGet());
                latch.countDown();
            }

            @Override
            public byte getPriority() {
                return 20;
            }

            @Override
            public Class<? super TestEvent> getTarget() {
                return TestEvent.class;
            }
        });

        eventSystem.post(new TestEvent());
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(3, counter.get());
    }

    @Test
    void concurrentPosting() throws InterruptedException {
        AtomicInteger successfulInvocations = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            Listener<TestEvent> listener = new Listener<TestEvent>() {
                @Override
                public void invoke(TestEvent event) {
                    successfulInvocations.incrementAndGet();
                }

                @Override
                public Class<? super TestEvent> getTarget() {
                    return TestEvent.class;
                }
            };
            eventSystem.register(listener);

            executor.submit(() -> {
                eventSystem.post(new TestEvent());
                latch.countDown();
            });
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(successfulInvocations.get() >= CONCURRENT_THREADS);
    }

    @Test
    void deregisterDuringPropagation() {
        AtomicInteger count = new AtomicInteger();
        Listener<TestEvent> selfRemovingListener = new Listener<TestEvent>() {
            @Override
            public void invoke(TestEvent event) {
                count.incrementAndGet();
                eventSystem.deregister(this);
            }

            @Override
            public Class<? super TestEvent> getTarget() {
                return TestEvent.class;
            }
        };

        eventSystem.register(selfRemovingListener);
        eventSystem.post(new TestEvent());
        eventSystem.post(new TestEvent());
        assertEquals(1, count.get());
    }

}