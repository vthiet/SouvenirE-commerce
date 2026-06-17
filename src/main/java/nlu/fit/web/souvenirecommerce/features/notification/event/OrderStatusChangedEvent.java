package nlu.fit.web.souvenirecommerce.features.notification.event;

import lombok.Getter;
import nlu.fit.web.souvenirecommerce.model.entity.Order;
import nlu.fit.web.souvenirecommerce.model.enums.OrderStatusCode;
import java.util.EventObject;

@Getter
public class OrderStatusChangedEvent extends EventObject {
    private final Order order;
    private final OrderStatusCode previousStatus;
    private final OrderStatusCode newStatus;
    private final String description;

    public OrderStatusChangedEvent(Object source, Order order, OrderStatusCode previousStatus, OrderStatusCode newStatus, String description) {
        super(source);
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.description = description;
    }
}
