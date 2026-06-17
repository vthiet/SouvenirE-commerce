package nlu.fit.web.souvenirecommerce.features.order.service;

import nlu.fit.web.souvenirecommerce.features.order.repository.OrderHistoryRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderStatusRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.ProductRepository;
import nlu.fit.web.souvenirecommerce.features.shipping.service.ShippingService;
import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProvider;
import nlu.fit.web.souvenirecommerce.model.entity.*;
import nlu.fit.web.souvenirecommerce.model.enums.OrderStatusCode;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutException;

import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
    private final OrderRepository orderRepository = new OrderRepository();
    private final OrderStatusRepository orderStatusRepository = new OrderStatusRepository();
    private final OrderHistoryRepository orderHistoryRepository = new OrderHistoryRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final ShippingService shippingService = new ShippingService();

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CheckoutException("Không tìm thấy đơn hàng với ID: " + orderId));
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<OrderHistory> getOrderHistory(Long orderId) {
        return orderHistoryRepository.findByOrderId(orderId);
    }

    public Order updateStatus(Order order, OrderStatusCode statusCode, String performedBy, String description) {
        OrderStatus status = resolveStatus(statusCode);
        order.setStatus(status);
        orderRepository.update(order);

        logHistory(order, statusCode.getDescription(), description, performedBy);
        return order;
    }

    public Order confirmOrder(Long orderId, String performedBy) {
        Order order = getOrderById(orderId);
        String currentStatusDesc = order.getStatusDescription();
        if (!"Chờ xác nhận".equals(currentStatusDesc)) {
            throw new CheckoutException("Chỉ có thể xác nhận đơn hàng đang ở trạng thái 'Chờ xác nhận'.");
        }
        return updateStatus(order, OrderStatusCode.PENDING, performedBy, "Nhận đơn hàng thành công, chuẩn bị đóng gói hàng hóa.");
    }

    public Order startShipping(Long orderId, String performedBy) {
        Order order = getOrderById(orderId);
        String currentStatusDesc = order.getStatusDescription();
        if (!"Đang xử lý".equals(currentStatusDesc) && !"Chờ xác nhận".equals(currentStatusDesc)) {
            throw new CheckoutException("Chỉ có thể giao đơn hàng đang ở trạng thái 'Đang xử lý' hoặc 'Chờ xác nhận'.");
        }

        ShippingProvider provider = shippingService.getActiveProvider();

        // Lazy create shipping order if not exists
        if (order.getGhnOrderCode() == null || order.getGhnOrderCode().isBlank()) {
            try {
                ShippingProvider.ShippingOrderResult result = provider.createOrder(order);
                applyShippingSnapshot(order, result);
                orderRepository.update(order);
            } catch (Exception e) {
                order.setGhnStatus("create_failed");
                order.setGhnUpdatedAt(LocalDateTime.now());
                orderRepository.update(order);
                logHistory(order, currentStatusDesc, "Tạo đơn vận chuyển thất bại: " + e.getMessage(), "Hệ thống");
                throw new CheckoutException("Không thể tạo đơn vận chuyển GHN: " + e.getMessage());
            }
        }

        return updateStatus(order, OrderStatusCode.SHIPPING, performedBy, "Đã giao hàng cho đơn vị vận chuyển (" + provider.getName() + ").");
    }

    public Order completeOrder(Long orderId, String performedBy) {
        Order order = getOrderById(orderId);
        String currentStatusDesc = order.getStatusDescription();
        if (!"Đang giao".equals(currentStatusDesc)) {
            throw new CheckoutException("Chỉ có thể hoàn thành đơn hàng đang ở trạng thái 'Đang giao'.");
        }
        return updateStatus(order, OrderStatusCode.COMPLETED, performedBy, "Khách hàng đã nhận được hàng. Giao dịch hoàn tất.");
    }

    public Order cancelOrder(Long orderId, String performedBy, String reason) {
        Order order = getOrderById(orderId);
        String currentStatusDesc = order.getStatusDescription();

        if ("Hoàn thành".equals(currentStatusDesc) || "Đã hủy".equals(currentStatusDesc)) {
            throw new CheckoutException("Không thể hủy đơn hàng đã hoàn thành hoặc đã bị hủy.");
        }

        // Restoring stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                product.setTotalSold(Math.max(0, product.getTotalSold() - item.getQuantity()));
                productRepository.update(product);
            }
        }

        // Update payment transaction status if exists
        if (order.getPaymentTransaction() != null) {
            order.getPaymentTransaction().setStatus(nlu.fit.web.souvenirecommerce.model.enums.PaymentStatus.CANCELLED);
        }

        return updateStatus(order, OrderStatusCode.CANCELLED, performedBy, "Hủy đơn hàng. Lý do: " + reason);
    }

    public Order syncGhnStatus(Long orderId, String performedBy) {
        Order order = getOrderById(orderId);
        if (order.getGhnOrderCode() == null || order.getGhnOrderCode().isBlank()) {
            throw new CheckoutException("Đơn hàng này chưa được tạo mã vận đơn vận chuyển.");
        }

        ShippingProvider provider = shippingService.getActiveProvider();
        try {
            ShippingProvider.ShippingOrderResult result = provider.getOrderDetail(order.getGhnOrderCode(), order.getGhnStatus());
            applyShippingSnapshot(order, result);
            orderRepository.update(order);

            String status = result.status();
            if ("delivered".equalsIgnoreCase(status)) {
                updateStatus(order, OrderStatusCode.COMPLETED, "Hệ thống", "Đồng bộ từ GHN: Giao hàng thành công.");
            } else if ("returned".equalsIgnoreCase(status)) {
                cancelOrder(orderId, "Hệ thống", "Đơn hàng bị trả lại từ GHN.");
            } else {
                String desc = "Cập nhật trạng thái vận chuyển: " + status;
                logHistory(order, order.getStatusDescription(), desc, performedBy);
            }
        } catch (Exception e) {
            logHistory(order, order.getStatusDescription(), "Đồng bộ trạng thái vận chuyển thất bại: " + e.getMessage(), performedBy);
            throw new CheckoutException("Không thể đồng bộ trạng thái GHN: " + e.getMessage());
        }

        return order;
    }

    public void logHistory(Order order, String status, String description, String performedBy) {
        OrderHistory log = OrderHistory.builder()
                .order(order)
                .status(status)
                .description(description)
                .performedBy(performedBy)
                .build();
        orderHistoryRepository.save(log);
    }

    private OrderStatus resolveStatus(OrderStatusCode code) {
        return orderStatusRepository.findByDescription(code.getDescription())
                .orElseGet(() -> orderStatusRepository.save(OrderStatus.builder()
                                .description(code.getDescription())
                                .build())
                        .orElseThrow(() -> new CheckoutException("Không thể cập nhật trạng thái đơn hàng.")));
    }

    private void applyShippingSnapshot(Order order, ShippingProvider.ShippingOrderResult result) {
        if (order == null || result == null) {
            return;
        }

        order.setGhnOrderCode(result.orderCode());
        order.setGhnStatus(result.status());
        order.setGhnLeadtime(result.leadtime());
        order.setGhnFinishDate(result.finishDate());
        order.setGhnUpdatedAt(result.updatedAt());
    }
}
