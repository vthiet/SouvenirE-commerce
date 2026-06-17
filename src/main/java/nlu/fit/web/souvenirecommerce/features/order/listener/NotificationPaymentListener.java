package nlu.fit.web.souvenirecommerce.features.order.listener;

import nlu.fit.web.souvenirecommerce.common.event.EventListener;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentSucceededEvent;

public class NotificationPaymentListener implements EventListener<PaymentEvent> {
    
    @Override
    public void onEvent(PaymentEvent event) {
        if (event instanceof PaymentSucceededEvent) {
            System.out.println("NotificationPaymentListener: Sending Payment Success Email for order " + event.getOrderId());
            // NotificationService.sendPaymentSuccessEmail(event.getOrderId());
        }
    }
}
