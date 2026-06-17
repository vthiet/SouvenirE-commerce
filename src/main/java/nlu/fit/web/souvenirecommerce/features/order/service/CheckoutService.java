package nlu.fit.web.souvenirecommerce.features.order.service;

import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.model.enums.OrderStatusCode;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartEntity;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItemEntity;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutException;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutRequest;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutResult;
import nlu.fit.web.souvenirecommerce.features.payment.dto.PaymentContext;
import nlu.fit.web.souvenirecommerce.features.payment.dto.PaymentPreparation;
import nlu.fit.web.souvenirecommerce.features.payment.gateway.PaymentGateway;
import nlu.fit.web.souvenirecommerce.features.payment.gateway.PaymentGatewayRegistry;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderStatusRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.ProductRepository;
import nlu.fit.web.souvenirecommerce.features.shipping.service.ShippingService;
import nlu.fit.web.souvenirecommerce.features.user.address.AddressService;
import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.Order;
import nlu.fit.web.souvenirecommerce.model.entity.OrderItem;
import nlu.fit.web.souvenirecommerce.model.entity.OrderStatus;
import nlu.fit.web.souvenirecommerce.model.entity.PaymentTransaction;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.Province;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.math.BigDecimal;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class CheckoutService {
    private final OrderRepository orderRepository = new OrderRepository();
    private final OrderStatusRepository orderStatusRepository = new OrderStatusRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final AddressService addressService = new AddressService();
    private final PaymentGatewayRegistry paymentGatewayRegistry = new PaymentGatewayRegistry();
    private final ShippingService shippingService = new ShippingService();
    private final OrderService orderService = new OrderService();

    public CheckoutResult checkout(User user, CartEntity cart, CheckoutRequest request) {
        return checkout(user, cart, request, null);
    }

    public CheckoutResult checkout(User user, CartEntity cart, CheckoutRequest request, PaymentContext paymentContext) {
        validateUser(user);
        validateCart(cart);
        PaymentMethod paymentMethod = request.getPaymentMethod() == null ? PaymentMethod.COD : request.getPaymentMethod();
        if (!paymentGatewayRegistry.isAvailable(paymentMethod)) {
            throw new CheckoutException("Phương thức thanh toán đã chọn hiện không khả dụng.");
        }

        Address shippingAddress = resolveAddress(user, request);
        OrderStatus status = resolveInitialStatus(paymentMethod);

        Order order = Order.builder()
                .user(user)
                .address(shippingAddress)
                .orderDate(LocalDateTime.now())
                .status(status)
                .note(trimToNull(request.getNote()))
                .preferredCarrierCode(trimToNull(request.getPreferredCarrierCode()))
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemEntity cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new CheckoutException("Sản phẩm không tồn tại"));
            validateStock(product, cartItem.getQuantity());

            BigDecimal price = BigDecimal.valueOf(cartItem.getUnitPrice());
            order.addItem(OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(price)
                    .productName(product.getName())
                    .productImage(product.getImageUrl())
                    .build());
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            product.setTotalSold(product.getTotalSold() + cartItem.getQuantity());
        }
        BigDecimal shippingFee = resolveShippingFee(shippingAddress, request);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount.add(shippingFee));

        Order savedOrder = orderRepository.save(order)
                .orElseThrow(() -> new CheckoutException("Không thể tạo đơn hàng"));

        PaymentGateway gateway = paymentGatewayRegistry.get(paymentMethod);
        PaymentPreparation paymentPreparation = gateway.prepare(savedOrder, paymentContext);
        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .orderId(savedOrder.getId())
                .method(paymentMethod)
                .provider(paymentPreparation.getProvider())
                .status(paymentPreparation.getStatus())
                .amount(savedOrder.getTotalAmount())
                .providerTransactionRef(paymentPreparation.getProviderTransactionRef())
                .paymentUrl(paymentPreparation.getPaymentUrl())
                .qrPayload(paymentPreparation.getQrPayload())
                .build();
        new nlu.fit.web.souvenirecommerce.features.payment.repository.PaymentTransactionRepository().save(paymentTransaction);

        orderService.logHistory(
                savedOrder,
                savedOrder.getStatusDescription(),
                paymentMethod == PaymentMethod.COD ? "Đơn hàng được đặt thành công (COD)." : "Chờ khách hàng thanh toán qua VNPay.",
                "Khách hàng"
        );

        AuditLogService.success(
                CheckoutService.class,
                user,
                "ORDER",
                "ORDER_CREATED",
                "CHECKOUT",
                AuditLogService.describe(
                        "orderCode", savedOrder.getOrderCode(),
                        "orderId", savedOrder.getId(),
                        "paymentMethod", paymentMethod,
                        "status", savedOrder.getStatusDescription(),
                        "itemCount", cart.totalQuantity(),
                        "totalAmount", savedOrder.getTotalAmount(),
                        "paymentUrl", paymentPreparation.getPaymentUrl() == null ? "" : paymentPreparation.getPaymentUrl()
                )
        );

        OrderStatusCode initialStatusCode = paymentMethod == PaymentMethod.COD
                ? OrderStatusCode.WAIT_CONFIRM
                : OrderStatusCode.PENDING_PAYMENT;

        nlu.fit.web.souvenirecommerce.common.event.EventBus.publish(
                new nlu.fit.web.souvenirecommerce.features.notification.event.OrderStatusChangedEvent(
                        this,
                        savedOrder,
                        null,
                        initialStatusCode,
                        paymentMethod == PaymentMethod.COD ? "Đơn hàng được đặt thành công (COD)." : "Chờ khách hàng thanh toán qua VNPay."
                )
        );

        return CheckoutResult.builder()
                .order(savedOrder)
                .orderCode(savedOrder.getOrderCode())
                .paymentUrl(paymentPreparation.getPaymentUrl())
                .qrPayload(paymentPreparation.getQrPayload())
                .build();
    }

    public List<Address> getUserAddresses(Long userId) {
        return addressService.getUserAddresses(userId);
    }

    public List<Province> getProvinces() {
        return addressService.getProvinces();
    }

    public boolean isPaymentMethodAvailable(PaymentMethod method) {
        return paymentGatewayRegistry.isAvailable(method);
    }

    private Address resolveAddress(User user, CheckoutRequest request) {
        if (request.getSavedAddressId() != null) {
            return addressService.getUserAddress(user.getId(), request.getSavedAddressId())
                    .orElseThrow(() -> new CheckoutException("Địa chỉ giao hàng không hợp lệ"));
        }

        if (isBlank(request.getReceiverName()) || isBlank(request.getReceiverPhone()) || isBlank(request.getAddressDetail())) {
            throw new CheckoutException("Vui lòng nhập đầy đủ họ tên, số điện thoại và địa chỉ giao hàng");
        }

        if (request.getCarrierProvinceId() != null || request.getCarrierDistrictId() != null || !isBlank(request.getCarrierWardCode())) {
            return addressService.createCarrierAddress(
                            user,
                            request.getReceiverName(),
                            request.getReceiverPhone(),
                            request.getAddressDetail(),
                            request.getCarrierProvinceId(),
                            request.getCarrierDistrictId(),
                            request.getCarrierWardCode(),
                            request.getProvinceName(),
                            request.getDistrictName(),
                            request.getWardName())
                    .orElseThrow(() -> new CheckoutException("Vui lòng chọn đầy đủ tỉnh/thành phố, quận/huyện, phường/xã và nhập địa chỉ chi tiết"));
        }

        return addressService.createAddress(
                        user,
                        request.getReceiverName(),
                        request.getReceiverPhone(),
                        request.getAddressDetail(),
                        request.getProvinceCode(),
                        request.getWardCode())
                .orElseThrow(() -> new CheckoutException("Vui lòng chọn đầy đủ tỉnh/thành phố, phường/xã và nhập địa chỉ chi tiết"));
    }

    private BigDecimal resolveShippingFee(Address address, CheckoutRequest request) {
        try {
            if (address != null) {
                String carrierCode = request.getPreferredCarrierCode();
                nlu.fit.web.souvenirecommerce.features.shipping.ShippingProvider provider;
                if (carrierCode != null && !carrierCode.trim().isEmpty()) {
                    try {
                        provider = nlu.fit.web.souvenirecommerce.features.shipping.ShippingProviderRegistry.getByCode(carrierCode);
                    } catch (IllegalArgumentException e) {
                        provider = shippingService.getActiveProvider();
                    }
                } else {
                    provider = shippingService.getActiveProvider();
                }
                return provider.calculateFee(address);
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        }

        if (request.getShippingFee() != null && request.getShippingFee() >= 0) {
            return BigDecimal.valueOf(request.getShippingFee());
        }
        return BigDecimal.valueOf(30000);
    }

    private OrderStatus resolveInitialStatus(PaymentMethod method) {
        OrderStatusCode statusCode = method == PaymentMethod.COD
                ? OrderStatusCode.WAIT_CONFIRM
                : OrderStatusCode.PENDING_PAYMENT;
        return orderStatusRepository.findByDescription(statusCode.getDescription())
                .orElseGet(() -> orderStatusRepository.save(OrderStatus.builder()
                                .description(statusCode.getDescription())
                                .build())
                        .orElseThrow(() -> new CheckoutException("Không thể tạo trạng thái đơn hàng")));
    }

    private void validateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new CheckoutException("Vui lòng đăng nhập để đặt hàng");
        }
    }

    private void validateCart(CartEntity cart) {
        if (cart == null || cart.totalQuantity() <= 0) {
            throw new CheckoutException("Giỏ hàng đang trống");
        }
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (requestedQuantity <= 0) throw new CheckoutException("Số lượng sản phẩm không hợp lệ");
        if (product.getStockQuantity() < requestedQuantity) {
            throw new CheckoutException("Sản phẩm " + product.getName() + " không đủ tồn kho");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
