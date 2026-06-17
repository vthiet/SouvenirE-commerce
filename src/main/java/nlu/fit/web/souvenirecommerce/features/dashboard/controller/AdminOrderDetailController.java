package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.features.order.service.OrderService;
import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.Order;
import nlu.fit.web.souvenirecommerce.model.entity.OrderItem;
import nlu.fit.web.souvenirecommerce.model.entity.OrderHistory;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin/order-detail")
public class AdminOrderDetailController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminOrderDetailController.class);
    private static final String PLACEHOLDER_IMAGE = "https://placehold.co/120x120?text=No+Image";

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long orderId;
        try {
            orderId = Long.parseLong(request.getParameter("id"));
        } catch (NumberFormatException ex) {
            log.warn("Invalid order id supplied for dedicated admin order detail route: {}", request.getParameter("id"));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order id");
            return;
        }

        Order order = null;
        try {
            order = orderService.getOrderById(orderId);
        } catch (Exception e) {
            log.warn("Admin order detail requested for missing orderId={}", orderId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
            return;
        }

        // Action check for GET (sync GHN)
        String getAction = request.getParameter("action");
        if ("syncGhn".equals(getAction)) {
            HttpSession session = request.getSession(false);
            User adminUser = (session != null) ? (User) session.getAttribute("userInSession") : null;
            String performedBy = (adminUser != null) ? adminUser.getEmail() : "Admin";
            try {
                orderService.syncGhnStatus(orderId, performedBy);
                AuditLogService.success(
                        AdminOrderDetailController.class,
                        adminUser,
                        "ORDER",
                        "ORDER_GHN_SYNCED",
                        "ORDER",
                        AuditLogService.describe("orderId", orderId, "performedBy", performedBy)
                );
                response.sendRedirect(request.getContextPath() + "/admin/order-detail?id=" + orderId + "&success=true");
                return;
            } catch (Exception e) {
                AuditLogService.failure(
                        AdminOrderDetailController.class,
                        adminUser,
                        "ORDER",
                        "ORDER_GHN_SYNCED",
                        "ORDER",
                        AuditLogService.describe("orderId", orderId, "performedBy", performedBy, "reason", e.getClass().getSimpleName(), "message", e.getMessage())
                );
                String errorMessage = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                response.sendRedirect(request.getContextPath() + "/admin/order-detail?id=" + orderId + "&error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
                return;
            }
        }

        OrderDetailView orderView = buildOrderView(order);
        List<OrderItemView> orderItemViews = buildOrderItemViews(order.getItems());
        List<OrderHistory> historyList = orderService.getOrderHistory(orderId);

        log.info("Opened dedicated admin order detail for orderId={}", orderId);

        request.setAttribute("orderView", orderView);
        request.setAttribute("orderItemViews", orderItemViews);
        request.setAttribute("historyList", historyList);
        request.getRequestDispatcher("/admin/order-detail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        User adminUser = null;
        if (session != null) {
            adminUser = (User) session.getAttribute("userInSession");
            if (adminUser == null) {
                adminUser = (User) session.getAttribute("currentUser");
            }
        }
        String performedBy = adminUser != null ? adminUser.getEmail() : "Admin";

        long orderId;
        try {
            orderId = Long.parseLong(request.getParameter("orderId"));
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order id");
            return;
        }

        String action = request.getParameter("action");
        try {
            String auditAction = null;
            if ("confirm".equals(action)) {
                orderService.confirmOrder(orderId, performedBy);
                auditAction = "ORDER_CONFIRMED";
            } else if ("ship".equals(action)) {
                orderService.startShipping(orderId, performedBy);
                auditAction = "ORDER_SHIPPED";
            } else if ("complete".equals(action)) {
                orderService.completeOrder(orderId, performedBy);
                auditAction = "ORDER_COMPLETED";
            } else if ("cancel".equals(action)) {
                String reason = request.getParameter("reason");
                if (reason == null || reason.isBlank()) {
                    reason = "Bị hủy bởi Admin";
                }
                orderService.cancelOrder(orderId, performedBy, reason);
                auditAction = "ORDER_CANCELLED";
            } else {
                AuditLogService.failure(
                        AdminOrderDetailController.class,
                        adminUser,
                        "ORDER",
                        "ORDER_STATUS_CHANGED",
                        "ORDER",
                        AuditLogService.describe("orderId", orderId, "action", action, "reason", "unsupported_action")
                );
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported action");
                return;
            }
            AuditLogService.success(
                    AdminOrderDetailController.class,
                    adminUser,
                    "ORDER",
                    auditAction,
                    "ORDER",
                    AuditLogService.describe("orderId", orderId, "action", action, "performedBy", performedBy)
            );
            response.sendRedirect(request.getContextPath() + "/admin/order-detail?id=" + orderId + "&success=true");
        } catch (Exception e) {
            AuditLogService.failure(
                    AdminOrderDetailController.class,
                    adminUser,
                    "ORDER",
                    "ORDER_STATUS_CHANGED",
                    "ORDER",
                    AuditLogService.describe("orderId", orderId, "action", action, "performedBy", performedBy, "reason", e.getClass().getSimpleName(), "message", e.getMessage())
            );
            String errorMessage = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            response.sendRedirect(request.getContextPath() + "/admin/order-detail?id=" + orderId + "&error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
        }
    }

    private OrderDetailView buildOrderView(Order order) {
        String shippingAddr = "";
        if (order.getAddress() != null) {
            Address addr = order.getAddress();
            shippingAddr = addr.getAddressDetail() + ", " + addr.getWard() + ", " + addr.getDistrict() + ", " + addr.getProvince();
        }

        String customerPhone = order.getUser() != null ? order.getUser().getPhone() : "";
        if (order.getAddress() != null && order.getAddress().getReceiverPhone() != null) {
            customerPhone = order.getAddress().getReceiverPhone();
        }

        return new OrderDetailView(
                order.getId().intValue(),
                java.sql.Timestamp.valueOf(order.getOrderDate()),
                order.getAddress() != null ? order.getAddress().getReceiverName() : (order.getUser() != null ? order.getUser().getFullName() : "Khách hàng"),
                order.getUser() != null ? order.getUser().getEmail() : "",
                customerPhone,
                shippingAddr,
                order.getNote(),
                order.getPaymentTransaction() != null ? order.getPaymentTransaction().getMethod().name() : "COD",
                order.getStatusDescription(),
                order.getTotalAmount(),
                safeMoney(order.getShippingFee()),
                order.getGhnOrderCode(),
                order.getGhnStatus(),
                toDate(order.getGhnUpdatedAt()),
                toDate(order.getGhnLeadtime()),
                toDate(order.getGhnFinishDate())
        );
    }

    private List<OrderItemView> buildOrderItemViews(List<OrderItem> orderItems) {
        List<OrderItemView> views = new ArrayList<>();
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                views.add(new OrderItemView(
                        item.getProductName(),
                        item.getProductImage(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getSubTotal()
                ));
            }
        }
        return views;
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
            BigDecimal totalAmount,
            BigDecimal shippingFee,
            String ghnOrderCode,
            String ghnStatus,
            Date ghnUpdatedAt,
            Date ghnLeadtime,
            Date ghnFinishDate
    ) {
        public int getId() { return id; }
        public java.util.Date getOrderDate() { return orderDate; }
        public String getCustomerName() { return customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public String getCustomerPhone() { return customerPhone; }
        public String getShippingAddress() { return shippingAddress; }
        public String getNote() { return note; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getStatus() { return status; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getShippingFee() { return shippingFee; }
        public String getGhnOrderCode() { return ghnOrderCode; }
        public String getGhnStatus() { return ghnStatus; }
        public Date getGhnUpdatedAt() { return ghnUpdatedAt; }
        public Date getGhnLeadtime() { return ghnLeadtime; }
        public Date getGhnFinishDate() { return ghnFinishDate; }
    }

    public record OrderItemView(
            String productName,
            String productImageUrl,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        public String getProductName() { return productName; }
        public String getProductImageUrl() { return productImageUrl; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getSubtotal() { return subtotal; }
    }

    private Date toDate(java.time.LocalDateTime value) {
        return value == null ? null : Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
