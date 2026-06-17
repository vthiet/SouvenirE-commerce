package nlu.fit.web.souvenirecommerce.features.notification.listener;

import nlu.fit.web.souvenirecommerce.common.event.EventListener;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentSucceededEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentFailedEvent;
import nlu.fit.web.souvenirecommerce.features.notification.service.NotificationService;

public class NotificationPaymentListener implements EventListener<PaymentEvent> {
    private final NotificationService notificationService = new NotificationService();

    @Override
    public void onEvent(PaymentEvent event) {
        if (event instanceof PaymentSucceededEvent) {
            System.out.println("NotificationPaymentListener: Sending Payment Success Email for order " + event.getOrderId());
            notificationService.sendPaymentSuccessEmail(event.getOrderId());
        } else if (event instanceof PaymentFailedEvent) {
            System.out.println("NotificationPaymentListener: Sending Payment Failed Email for order " + event.getOrderId());
            notificationService.sendPaymentFailedEmail(event.getOrderId(), ((PaymentFailedEvent) event).getReason());
        }
    }
}
