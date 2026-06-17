package nlu.fit.web.souvenirecommerce.common.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus {
    private static final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    public static <T> void subscribe(Class<T> eventType, EventListener<? super T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T> void publish(T event) {
        List<EventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener<?> listener : eventListeners) {
                ((EventListener<T>) listener).onEvent(event);
            }
        }
    }
}
