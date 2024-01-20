import ccetl.event.Cancelable;
import ccetl.event.DefaultPriorities;
import ccetl.event.EventSystem;
import ccetl.event.TypeEvent;
import ccetl.event.listeners.LambdaListener;
import ccetl.event.listeners.Listener;

import java.util.List;

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

        System.out.println(eventSystem.hasListeners(TestEvent.class));
        System.out.println(eventSystem.hasListeners(TestListener.class));

        eventSystem = new EventSystem();
        Human<Organ> humanA = new Human<>(new Brain());
        Human<Organ> humanB = new Human<>(new Liver());

        eventSystem.register(new Listener<Human<Liver>>() {
            @Override
            public void invoke(Human<Liver> event) {
                System.out.println("Liver");
            }

            @Override
            public Class<? super Human<Liver>> getTarget() {
                return Human.class;
            }

            @Override
            public boolean filterType(Class<?> eventClass) {
                return eventClass == Liver.class;
            }
        });

        eventSystem.post(humanA, true, false);
        eventSystem.post(humanB, true, false);

        List<Runnable> runnable = eventSystem.shutDown();
        System.out.println(runnable.size());
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

    static class Human<T> implements TypeEvent {
        private final T t;

        Human(T t) {
            this.t = t;
        }

        @Override
        public Class<?> getType() {
            return t.getClass();
        }
    }

    static class Organ {

    }

    static class Brain extends Organ {

    }

    static class Liver extends Organ {

    }
}