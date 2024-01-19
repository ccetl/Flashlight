import ccetl.event.Cancelable;
import ccetl.event.DefaultPriorities;
import ccetl.event.EventSystem;
import ccetl.event.listeners.LambdaListener;
import ccetl.event.listeners.Listener;

public class Main {
    public static void main(String[] args) {
        EventSystem eventSystem = new EventSystem();

        TestListener listener1 = new TestListener("Listener 1");
        TestListener listener2 = new TestListener("Listener 2");
        eventSystem.register(listener1);
        eventSystem.register(listener2);

        TestEvent event = new TestEvent("Hello world!");
        eventSystem.post(event, true, false);
        eventSystem.deregister(listener1);
        eventSystem.post(event);
        eventSystem.deregister(listener2);
        eventSystem.shutDown();

        eventSystem = new EventSystem();
        eventSystem.register(new LambdaListener<>(TestEvent.class, event1 -> System.out.println("Hello from listener B!"), DefaultPriorities.NORMAL));
        eventSystem.register(new LambdaListener<>(TestEvent.class, event1 -> System.out.println("Hello from listener C!"), DefaultPriorities.LOW));
        eventSystem.register(new LambdaListener<>(TestEvent.class, event1 -> System.out.println("Hello from listener A!"), DefaultPriorities.HIGH));
        eventSystem.post(new TestEvent("Hello world!"));
    }

    static class TestEvent extends Cancelable {
        private final String message;

        public TestEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    static class TestListener implements Listener<TestEvent> {
        private final String name;

        public TestListener(String name) {
            this.name = name;
        }

        @Override
        public void invoke(TestEvent event) {
            System.out.println(name + " received event: " + event.getMessage());
        }

        @Override
        public Class<TestEvent> getTarget() {
            return TestEvent.class;
        }
    }
}