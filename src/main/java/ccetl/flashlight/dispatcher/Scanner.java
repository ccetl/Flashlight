package ccetl.flashlight.dispatcher;

import ccetl.flashlight.annotations.Nullable;
import ccetl.flashlight.listeners.Listener;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
class Scanner {
    protected static void scanListeners(Map<Class<?>, List<Listener>> listeners, @Nullable Consumer<Class<?>> eventClass, @Nullable BiConsumer<Class<?>, Listener> scanner) {
        if (eventClass == null && scanner == null) {
            return;
        }

        for (Map.Entry<Class<?>, List<Listener>> entry : listeners.entrySet()) {
            if (eventClass != null) {
                eventClass.accept(entry.getKey());
            }

            iterateListeners(scanner, entry);
        }
    }

    private static void iterateListeners(BiConsumer<Class<?>, Listener> scanner, Map.Entry<Class<?>, List<Listener>> entry) {
        if (scanner == null) {
            return;
        }

        Class<?> eventClass = entry.getKey();
        for (Listener listener : entry.getValue()) {
            scanner.accept(eventClass, listener);
        }
    }
}
