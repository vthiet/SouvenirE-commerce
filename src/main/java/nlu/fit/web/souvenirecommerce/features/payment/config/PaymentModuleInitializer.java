package nlu.fit.web.souvenirecommerce.features.payment.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import nlu.fit.web.souvenirecommerce.common.event.EventBus;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentSucceededEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentFailedEvent;
import nlu.fit.web.souvenirecommerce.features.order.listener.OrderPaymentListener;
import nlu.fit.web.souvenirecommerce.features.order.listener.InventoryPaymentListener;
import nlu.fit.web.souvenirecommerce.features.order.listener.NotificationPaymentListener;

@WebListener
public class PaymentModuleInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing Payment Module Event Listeners...");
        
        OrderPaymentListener orderListener = new OrderPaymentListener();
        InventoryPaymentListener inventoryListener = new InventoryPaymentListener();
        NotificationPaymentListener notificationListener = new NotificationPaymentListener();

        EventBus.subscribe(PaymentSucceededEvent.class, orderListener);
        EventBus.subscribe(PaymentFailedEvent.class, orderListener);

        EventBus.subscribe(PaymentSucceededEvent.class, inventoryListener);
        EventBus.subscribe(PaymentFailedEvent.class, inventoryListener);

        EventBus.subscribe(PaymentSucceededEvent.class, notificationListener);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if necessary
    }
}
