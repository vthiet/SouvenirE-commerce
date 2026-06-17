package nlu.fit.web.souvenirecommerce.features.order.service;

import nlu.fit.web.souvenirecommerce.features.order.repository.OrderHistoryRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderStatusRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.ProductRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.ShippingOrderRepository;
import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProvider;
import nlu.fit.web.souvenirecommerce.features.shipping.service.ShippingService;
import nlu.fit.web.souvenirecommerce.model.entity.Order;
import nlu.fit.web.souvenirecommerce.model.entity.OrderHistory;
import nlu.fit.web.souvenirecommerce.model.entity.OrderItem;
import nlu.fit.web.souvenirecommerce.model.entity.OrderStatus;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.ShippingOrder;
import nlu.fit.web.souvenirecommerce.model.enums.OrderStatusCode;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutException;
import nlu.fit.web.souvenirecommerce.features.order.dto.OrderListDTO;
import nlu.fit.web.souvenirecommerce.features.order.dto.OrderItemDTO;
import nlu.fit.web.souvenirecommerce.features.order.dto.OrderStatusTabDTO;

import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
    private final OrderRepository orderRepository = new OrderRepository();
    private final OrderStatusRepository orderStatusRepository = new OrderStatusRepository();
    private final OrderHistoryRepository orderHistoryRepository = new OrderHistoryRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final ShippingOrderRepository shippingOrderRepository = new ShippingOrderRepository();
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

        // Lazy-create a carrier shipment if none exists yet
        ShippingOrder activeShipment = order.getActiveShippingOrder();
        if (activeShipment == null || activeShipment.getTrackingCode() == null
                || activeShipment.getTrackingCode().isBlank()) {
            try {
                ShippingProvider.ShipmentResult result = provider.createShipment(order);
                ShippingOrder newShipment = buildShippingOrder(order, provider.getCode(), result);
                shippingOrderRepository.save(newShipment);
            } catch (Exception e) {
                // Record the failure in a stub shipment so we can see it in order detail
                ShippingOrder failedShipment = ShippingOrder.builder()
                        .order(order)
                        .carrierCode(provider.getCode())
                        .status("create_failed")
                        .carrierUpdatedAt(LocalDateTime.now())
                        .build();
                shippingOrderRepository.save(failedShipment);
                logHistory(order, currentStatusDesc,
                        "Tạo đơn vận chuyển thất bại: " + e.getMessage(), "Hệ thống");
                throw new CheckoutException("Không thể tạo đơn vận chuyển: " + e.getMessage());
            }
        }

        return updateStatus(order, OrderStatusCode.SHIPPING, performedBy,
                "Đã giao hàng cho đơn vị vận chuyển (" + provider.getName() + ").");
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

        // Restore stock
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

    /**
     * Syncs the carrier shipment status from the provider API.
     * Updates the {@link ShippingOrder} record and, when the carrier confirms
     * delivery or return, also updates the shop order status accordingly.
     */
    public Order syncCarrierStatus(Long orderId, String performedBy) {
        Order order = getOrderById(orderId);
        ShippingOrder shipment = shippingOrderRepository.findLatestByOrderId(orderId)
                .orElseThrow(() -> new CheckoutException("Đơn hàng này chưa có mã vận đơn vận chuyển."));

        if (shipment.getTrackingCode() == null || shipment.getTrackingCode().isBlank()) {
            throw new CheckoutException("Đơn hàng này chưa có mã vận đơn vận chuyển.");
        }

        ShippingProvider provider;
        try {
            provider = nlu.fit.web.souvenirecommerce.features.shipping.ShippingProviderRegistry
                    .getByCode(shipment.getCarrierCode());
        } catch (IllegalArgumentException e) {
            throw new CheckoutException("Đơn vị vận chuyển không được hỗ trợ: " + shipment.getCarrierCode());
        }

        try {
            ShippingProvider.ShipmentResult result =
                    provider.getShipmentStatus(shipment.getTrackingCode(), shipment.getStatus());
            applyShipmentSnapshot(shipment, result);
            shippingOrderRepository.update(shipment);

            String status = result.status();
            if ("delivered".equalsIgnoreCase(status)) {
                updateStatus(order, OrderStatusCode.COMPLETED,
                        "Hệ thống", "Đồng bộ từ đơn vị vận chuyển: Giao hàng thành công.");
            } else if ("returned".equalsIgnoreCase(status)) {
                cancelOrder(orderId, "Hệ thống", "Đơn hàng bị hoàn trả từ đơn vị vận chuyển.");
            } else {
                logHistory(order, order.getStatusDescription(),
                        "Cập nhật trạng thái vận chuyển: " + status, performedBy);
            }
        } catch (Exception e) {
            logHistory(order, order.getStatusDescription(),
                    "Đồng bộ trạng thái vận chuyển thất bại: " + e.getMessage(), performedBy);
            throw new CheckoutException("Không thể đồng bộ trạng thái vận chuyển: " + e.getMessage());
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

    public List<OrderListDTO> getUserOrderList(Long userId, String statusFilter, String keyword) {
        List<Order> orders = orderRepository.findUserOrders(userId, statusFilter, keyword);
        List<OrderListDTO> dtoList = new java.util.ArrayList<>();
        for (Order o : orders) {
            OrderListDTO dto = new OrderListDTO();
            dto.setOrderId(o.getId().intValue());
            dto.setOrderDate(o.getCreatedAt());
            dto.setStatusText(o.getStatusDescription());

            String payStatus = "PENDING";
            String repayUrl = null;
            if (o.getPaymentTransaction() != null) {
                payStatus = o.getPaymentTransaction().getStatus().name();
                repayUrl = o.getPaymentTransaction().getPaymentUrl();
            }
            dto.setPaymentStatus(payStatus);
            dto.setRepayUrl(repayUrl);
            dto.setTotalAmount(o.getTotalAmount().doubleValue());

            if (o.getItems() != null) {
                for (OrderItem item : o.getItems()) {
                    OrderItemDTO itemDto = new OrderItemDTO();
                    itemDto.setProductId(item.getProduct() != null ? item.getProduct().getId() : 0L);
                    itemDto.setProductName(item.getProductName());
                    itemDto.setProductImage(item.getProductImage());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setPriceAtPurchase(item.getPriceAtPurchase() != null ? item.getPriceAtPurchase().doubleValue() : 0.0);
                    dto.getItems().add(itemDto);
                }
            }
            dtoList.add(dto);
        }
        return dtoList;
    }

    public List<OrderStatusTabDTO> getUserOrderStatusTabs(Long userId) {
        List<OrderStatusTabDTO> tabs = new java.util.ArrayList<>();
        List<Object[]> rows = orderRepository.getOrderStatusCountsByUserId(userId);
        int totalOrders = 0;
        for (Object[] row : rows) {
            String status = (String) row[0];
            int count = ((Number) row[1]).intValue();
            totalOrders += count;
            tabs.add(new OrderStatusTabDTO(status, toOrderStatusLabel(status), count));
        }
        tabs.add(0, new OrderStatusTabDTO("all", "Tất cả", totalOrders));
        return tabs;
    }

    private String toOrderStatusLabel(String status) {
        if (status == null || status.isBlank()) {
            return "Đang xử lý";
        }
        return switch (status) {
            case "PENDING_PAYMENT" -> "Chờ thanh toán";
            case "WAIT_CONFIRM", "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "SHIPPED" -> "Đang giao";
            case "DELIVERED" -> "Đã giao";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            case "PAYMENT_FAILED" -> "Thanh toán thất bại";
            default -> status;
        };
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private OrderStatus resolveStatus(OrderStatusCode code) {
        return orderStatusRepository.findByDescription(code.getDescription())
                .orElseGet(() -> orderStatusRepository.save(OrderStatus.builder()
                                .description(code.getDescription())
                                .build())
                        .orElseThrow(() -> new CheckoutException("Không thể cập nhật trạng thái đơn hàng.")));
    }

    private ShippingOrder buildShippingOrder(Order order, String carrierCode,
                                             ShippingProvider.ShipmentResult result) {
        return ShippingOrder.builder()
                .order(order)
                .carrierCode(carrierCode)
                .trackingCode(result.trackingCode())
                .status(result.status())
                .leadtime(result.leadtime())
                .finishDate(result.finishDate())
                .carrierUpdatedAt(result.carrierUpdatedAt())
                .build();
    }

    private void applyShipmentSnapshot(ShippingOrder shipment, ShippingProvider.ShipmentResult result) {
        if (shipment == null || result == null) {
            return;
        }
        shipment.setTrackingCode(result.trackingCode());
        shipment.setStatus(result.status());
        shipment.setLeadtime(result.leadtime());
        shipment.setFinishDate(result.finishDate());
        shipment.setCarrierUpdatedAt(result.carrierUpdatedAt());
    }
}
