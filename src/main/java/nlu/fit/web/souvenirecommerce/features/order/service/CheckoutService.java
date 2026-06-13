package nlu.fit.web.souvenirecommerce.features.order.service;

import nlu.fit.web.souvenirecommerce.model.enums.OrderStatusCode;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;
import nlu.fit.web.souvenirecommerce.features.cart.model.Cart;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItem;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutException;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutRequest;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutResult;
import nlu.fit.web.souvenirecommerce.features.order.dto.PaymentContext;
import nlu.fit.web.souvenirecommerce.features.order.dto.PaymentPreparation;
import nlu.fit.web.souvenirecommerce.features.order.payment.PaymentGateway;
import nlu.fit.web.souvenirecommerce.features.order.payment.PaymentGatewayRegistry;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderStatusRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.ProductRepository;
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
import java.time.LocalDateTime;
import java.util.List;

public class CheckoutService {
    private final OrderRepository orderRepository = new OrderRepository();
    private final OrderStatusRepository orderStatusRepository = new OrderStatusRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final AddressService addressService = new AddressService();
    private final PaymentGatewayRegistry paymentGatewayRegistry = new PaymentGatewayRegistry();
    public CheckoutResult checkout(User user, Cart cart, CheckoutRequest request) {
        return checkout(user, cart, request, null);
    }
    public CheckoutResult checkout(User user, Cart cart, CheckoutRequest request, PaymentContext paymentContext) {
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
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new CheckoutException("Sản phẩm không tồn tại"));
            validateStock(product, cartItem.getQuantity());

            BigDecimal price = BigDecimal.valueOf(cartItem.getPrice());
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
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order)
                .orElseThrow(() -> new CheckoutException("Không thể tạo đơn hàng"));

        PaymentGateway gateway = paymentGatewayRegistry.get(paymentMethod);
        PaymentPreparation paymentPreparation = gateway.prepare(savedOrder, paymentContext);
        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .order(savedOrder)
                .method(paymentMethod)
                .provider(paymentPreparation.getProvider())
                .status(paymentPreparation.getStatus())
                .amount(totalAmount)
                .providerTransactionRef(paymentPreparation.getProviderTransactionRef())
                .paymentUrl(paymentPreparation.getPaymentUrl())
                .qrPayload(paymentPreparation.getQrPayload())
                .build();
        savedOrder.setPaymentTransaction(paymentTransaction);
        orderRepository.update(savedOrder);

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

        return addressService.createAddress(
                        user,
                        request.getReceiverName(),
                        request.getReceiverPhone(),
                        request.getAddressDetail(),
                        request.getProvinceCode(),
                        request.getWardCode())
                .orElseThrow(() -> new CheckoutException("Vui lòng chọn đầy đủ tỉnh/thành phố, phường/xã và nhập địa chỉ chi tiết"));
    }

    private OrderStatus resolveInitialStatus(PaymentMethod method) {
        OrderStatusCode statusCode = method == PaymentMethod.COD
                ? OrderStatusCode.PENDING
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

    private void validateCart(Cart cart) {
        if (cart == null || cart.totalQuantity() <= 0) {
            throw new CheckoutException("Giỏ hàng đang trống");
        }
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (requestedQuantity <= 0) {
            throw new CheckoutException("Số lượng sản phẩm không hợp lệ");
        }
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
