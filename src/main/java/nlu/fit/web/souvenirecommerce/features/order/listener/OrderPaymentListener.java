package nlu.fit.web.souvenirecommerce.features.order.listener;

import nlu.fit.web.souvenirecommerce.common.event.EventListener;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentSucceededEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentFailedEvent;
import nlu.fit.web.souvenirecommerce.features.order.service.OrderService;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Order;
import nlu.fit.web.souvenirecommerce.model.enums.OrderStatusCode;

public class OrderPaymentListener implements EventListener<PaymentEvent> {
    private final OrderService orderService = new OrderService();
    private final OrderRepository orderRepository = new OrderRepository();

    @Override
    public void onEvent(PaymentEvent event) {
        if (event instanceof PaymentSucceededEvent) {
            handlePaymentSucceeded((PaymentSucceededEvent) event);
        } else if (event instanceof PaymentFailedEvent) {
            handlePaymentFailed((PaymentFailedEvent) event);
        }
    }

    private void handlePaymentSucceeded(PaymentSucceededEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order != null) {
            orderService.updateStatus(order, OrderStatusCode.WAIT_CONFIRM, "Hệ thống", "Khách hàng thanh toán thành công. Mã giao dịch: " + event.getProviderTransactionRef());
        }
    }

    private void handlePaymentFailed(PaymentFailedEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order != null) {
            orderService.cancelOrder(order.getId(), "Hệ thống", "Thanh toán thất bại: " + event.getReason());
        }
    }
}
