package nlu.fit.web.souvenirecommerce.features.order.listener;

import nlu.fit.web.souvenirecommerce.common.event.EventListener;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentSucceededEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentFailedEvent;

public class InventoryPaymentListener implements EventListener<PaymentEvent> {
    
    @Override
    public void onEvent(PaymentEvent event) {
        if (event instanceof PaymentSucceededEvent) {
            // Confirm Inventory. Because inventory was reserved at Order Creation.
            // confirmInventory(event.getOrderId());
            System.out.println("InventoryPaymentListener: Confirmed inventory for order " + event.getOrderId());
        } else if (event instanceof PaymentFailedEvent) {
            // Release Inventory. Payment failed, so we should release to prevent overselling.
            // releaseInventory(event.getOrderId());
            System.out.println("InventoryPaymentListener: Released inventory for order " + event.getOrderId());
        }
    }
}
