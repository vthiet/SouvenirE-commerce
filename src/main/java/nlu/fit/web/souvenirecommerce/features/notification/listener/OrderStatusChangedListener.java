package nlu.fit.web.souvenirecommerce.features.notification.listener;

import nlu.fit.web.souvenirecommerce.common.event.EventListener;
import nlu.fit.web.souvenirecommerce.features.notification.event.OrderStatusChangedEvent;
import nlu.fit.web.souvenirecommerce.features.notification.service.NotificationService;

public class OrderStatusChangedListener implements EventListener<OrderStatusChangedEvent> {
    private final NotificationService notificationService = new NotificationService();

    @Override
    public void onEvent(OrderStatusChangedEvent event) {
        notificationService.sendOrderStatusEmail(
                event.getOrder(),
                event.getPreviousStatus(),
                event.getNewStatus(),
                event.getDescription()
        );
    }
}
