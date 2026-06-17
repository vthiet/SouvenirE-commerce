package nlu.fit.web.souvenirecommerce.core.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import nlu.fit.web.souvenirecommerce.common.event.EventBus;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentSucceededEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentFailedEvent;
import nlu.fit.web.souvenirecommerce.features.order.listener.OrderPaymentListener;
import nlu.fit.web.souvenirecommerce.features.order.listener.InventoryPaymentListener;
import nlu.fit.web.souvenirecommerce.features.notification.listener.NotificationPaymentListener;
import nlu.fit.web.souvenirecommerce.features.notification.listener.OrderStatusChangedListener;
import nlu.fit.web.souvenirecommerce.features.notification.event.OrderStatusChangedEvent;

@WebListener
public class PaymentModuleInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing Payment and Notification Module Event Listeners...");
        
        OrderPaymentListener orderListener = new OrderPaymentListener();
        InventoryPaymentListener inventoryListener = new InventoryPaymentListener();
        NotificationPaymentListener notificationListener = new NotificationPaymentListener();
        OrderStatusChangedListener orderStatusChangedListener = new OrderStatusChangedListener();

        // Order payments updates
        EventBus.subscribe(PaymentSucceededEvent.class, orderListener);
        EventBus.subscribe(PaymentFailedEvent.class, orderListener);

        // Inventory update on payment events
        EventBus.subscribe(PaymentSucceededEvent.class, inventoryListener);
        EventBus.subscribe(PaymentFailedEvent.class, inventoryListener);

        // Notifications on payment events
        EventBus.subscribe(PaymentSucceededEvent.class, notificationListener);
        EventBus.subscribe(PaymentFailedEvent.class, notificationListener);

        // General notifications on order status changes
        EventBus.subscribe(OrderStatusChangedEvent.class, orderStatusChangedListener);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if necessary
    }
}
