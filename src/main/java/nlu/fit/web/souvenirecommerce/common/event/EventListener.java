package nlu.fit.web.souvenirecommerce.common.event;

public interface EventListener<T> {
    void onEvent(T event);
}
