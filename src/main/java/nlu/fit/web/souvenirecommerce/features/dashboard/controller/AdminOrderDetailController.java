package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.legacy.dao.OrderDAO;
import nlu.fit.web.souvenirecommerce.legacy.model.Order;
import nlu.fit.web.souvenirecommerce.legacy.model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin/order-detail")
public class AdminOrderDetailController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminOrderDetailController.class);
    private static final String PLACEHOLDER_IMAGE = "https://placehold.co/120x120?text=No+Image";

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int orderId;
        try {
            orderId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException ex) {
            log.warn("Invalid order id supplied for dedicated admin order detail route: {}", request.getParameter("id"));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order id");
            return;
        }

        Order order = orderDAO.getOrderById(orderId);
        if (order == null) {
            log.warn("Admin order detail requested for missing orderId={}", orderId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
            return;
        }

        OrderDetailView orderView = buildOrderView(order, orderId);
        List<OrderItemView> orderItemViews = buildOrderItemViews(orderDAO.getOrderItems(orderId));

        log.info("Opened dedicated admin order detail for orderId={}", orderId);

        request.setAttribute("orderView", orderView);
        request.setAttribute("orderItemViews", orderItemViews);
        request.getRequestDispatcher("/admin/order-detail.jsp").forward(request, response);
    }

    private OrderDetailView buildOrderView(Order order, int orderId) {
        return new OrderDetailView(
                orderId,
                firstDate(
                        invoke(order, "getOrderDate"),
                        invoke(order, "getCreatedAt"),
                        invoke(order, "getCreatedDate")
                ),
                firstNonBlank(
                        asString(invoke(order, "getCustomerName")),
                        asString(invoke(order, "getFullName")),
                        asString(invoke(order, "getName")),
                        "Khách hàng"
                ),
                firstNonBlank(
                        asString(invoke(order, "getCustomerEmail")),
                        asString(invoke(order, "getEmail")),
                        ""
                ),
                firstNonBlank(
                        asString(invoke(order, "getCustomerPhone")),
                        asString(invoke(order, "getPhone")),
                        asString(invoke(order, "getPhoneNumber")),
                        ""
                ),
                firstNonBlank(
                        asString(invoke(order, "getShippingAddress")),
                        asString(invoke(order, "getAddress")),
                        asString(invoke(order, "getDeliveryAddress")),
                        ""
                ),
                firstNonBlank(
                        asString(invoke(order, "getNote")),
                        asString(invoke(order, "getCustomerNote")),
                        asString(invoke(order, "getDescription")),
                        ""
                ),
                firstNonBlank(
                        asString(invoke(order, "getPaymentMethod")),
                        asString(invoke(order, "getPaymentType")),
                        "COD"
                ),
                firstNonBlank(
                        asString(invoke(order, "getStatus")),
                        asString(invoke(order, "getOrderStatus")),
                        "Chờ xác nhận"
                ),
                asBigDecimal(firstNonNull(
                        asBigDecimal(invoke(order, "getTotalAmount")),
                        asBigDecimal(invoke(order, "getTotal")),
                        BigDecimal.ZERO
                ))
        );
    }

    private List<OrderItemView> buildOrderItemViews(List<OrderItem> orderItems) {
        List<OrderItemView> views = new ArrayList<>();
        for (OrderItem item : orderItems) {
            views.add(buildOrderItemView(item));
        }
        return views;
    }

    private OrderItemView buildOrderItemView(OrderItem item) {
        Object product = invoke(item, "getProduct");
        String productName = firstNonBlank(
                asString(invoke(item, "getProductName")),
                asString(invoke(item, "getName")),
                asString(invoke(product, "getName")),
                asString(invoke(product, "getProductName"))
        );
        if (productName == null) {
            Object productId = firstNonNull(
                    invoke(item, "getProductId"),
                    invoke(item, "getProductID"),
                    invoke(item, "getId")
            );
            productName = productId != null ? "Sản phẩm #" + productId : "Sản phẩm";
        }

        String imageUrl = firstNonBlank(
                asString(invoke(item, "getImageUrl")),
                asString(invoke(item, "getProductImageUrl")),
                asString(invoke(product, "getImageUrl")),
                asString(invoke(product, "getProductImageUrl")),
                PLACEHOLDER_IMAGE
        );

        Integer quantity = asInteger(firstNonNull(
                invoke(item, "getQuantity"),
                invoke(item, "getQty")
        ));
        if (quantity == null) {
            quantity = 0;
        }

        BigDecimal unitPrice = asBigDecimal(firstNonNull(
                invoke(item, "getPrice"),
                invoke(item, "getUnitPrice"),
                invoke(item, "getSalePrice")
        ));
        if (unitPrice == null) {
            unitPrice = BigDecimal.ZERO;
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal resolvedSubtotal = asBigDecimal(firstNonNull(
                invoke(item, "getTotal"),
                invoke(item, "getSubtotal")
        ));
        if (resolvedSubtotal != null && resolvedSubtotal.compareTo(BigDecimal.ZERO) > 0) {
            subtotal = resolvedSubtotal;
        }

        return new OrderItemView(productName, imageUrl, quantity, unitPrice, subtotal);
    }

    private Object invoke(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private java.util.Date firstDate(Object... values) {
        for (Object value : values) {
            if (value instanceof java.util.Date date) {
                return date;
            }
        }
        return null;
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public record OrderDetailView(
            int id,
            java.util.Date orderDate,
            String customerName,
            String customerEmail,
            String customerPhone,
            String shippingAddress,
            String note,
            String paymentMethod,
            String status,
            BigDecimal totalAmount
    ) {
        public int getId() {
            return id;
        }

        public java.util.Date getOrderDate() {
            return orderDate;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public String getNote() {
            return note;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public String getStatus() {
            return status;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }

    public record OrderItemView(
            String productName,
            String productImageUrl,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        public String getProductName() {
            return productName;
        }

        public String getProductImageUrl() {
            return productImageUrl;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }
    }
}
