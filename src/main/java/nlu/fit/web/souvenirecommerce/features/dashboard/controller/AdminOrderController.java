package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderRepository;
import nlu.fit.web.souvenirecommerce.features.order.service.OrderService;
import nlu.fit.web.souvenirecommerce.model.entity.Order;
import nlu.fit.web.souvenirecommerce.model.enums.OrderStatusCode;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/orders")
public class AdminOrderController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminOrderController.class);
    private final OrderRepository orderRepository = new OrderRepository();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        log.debug("Admin order GET request received. action={}, page={}, status={}",
                action, request.getParameter("page"), request.getParameter("status"));

        if ("view".equals(action)) {
            viewOrderDetail(request, response);
            return;
        }

        // Get filter parameter
        String statusFilter = request.getParameter("status");

        // Get pagination parameters
        int page = 1;
        int pageSize = 20;

        String pageParam = request.getParameter("page");
        if (pageParam != null) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        // Get orders with pagination and filter
        List<Order> orders;
        int totalOrders;

        if (statusFilter != null && !statusFilter.isEmpty() && !"all".equals(statusFilter)) {
            orders = orderRepository.getOrdersByStatus(statusFilter, page, pageSize);
            totalOrders = orderRepository.getOrderCountByStatus(statusFilter);
        } else {
            orders = orderRepository.getOrdersPaginated(page, pageSize);
            totalOrders = orderRepository.getTotalOrders();
        }

        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        log.info("Loaded admin orders page {} with {} records (statusFilter={})",
                page, orders.size(), statusFilter == null || statusFilter.isBlank() ? "all" : statusFilter);

        // Get status counts for stats cards
        int pendingCount = orderRepository.getOrderCountByStatus("Chờ xác nhận");
        int processingCount = orderRepository.getOrderCountByStatus("Đang xử lý");
        int shippingCount = orderRepository.getOrderCountByStatus("Đang giao");
        int completedCount = orderRepository.getOrderCountByStatus("Hoàn thành");

        // Set attributes
        request.setAttribute("orders", orders);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("statusFilter", statusFilter);
        request.setAttribute("pendingCount", pendingCount);
        request.setAttribute("processingCount", processingCount);
        request.setAttribute("shippingCount", shippingCount);
        request.setAttribute("completedCount", completedCount);

        // Forward to JSP
        request.getRequestDispatcher("/admin/orders.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        User currentUser = resolveCurrentUser(request);
        log.debug("Admin order POST request received. action={}", action);

        if ("updateStatus".equals(action)) {
            updateOrderStatus(request, response, currentUser);
        } else {
            log.warn("Unsupported admin order POST action: {}", action);
            AuditLogService.failure(
                    AdminOrderController.class,
                    currentUser,
                    "ORDER",
                    "ORDER_STATUS_UPDATED",
                    "ORDER",
                    AuditLogService.describe("action", action, "reason", "unsupported_action")
            );
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported action");
        }
    }

    private void viewOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int orderId;
        try {
            orderId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException ex) {
            log.warn("Invalid order id supplied for admin order detail view: {}", request.getParameter("id"));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order id");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/order-detail?id=" + orderId);
    }

    private void updateOrderStatus(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {

        int orderId;
        try {
            orderId = Integer.parseInt(request.getParameter("orderId"));
        } catch (NumberFormatException ex) {
            log.warn("Invalid order id supplied for admin order status update: {}", request.getParameter("orderId"));
            AuditLogService.failure(
                    AdminOrderController.class,
                    currentUser,
                    "ORDER",
                    "ORDER_STATUS_UPDATED",
                    "ORDER",
                    AuditLogService.describe("orderId", request.getParameter("orderId"), "reason", "invalid_order_id")
            );
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order id");
            return;
        }

        String newStatus = request.getParameter("status");
        String performedBy = currentUser != null ? currentUser.getEmail() : "Admin";

        log.info("Updating order status. orderId={}, newStatus={}", orderId, newStatus);

        try {
            OrderStatusCode code = null;
            for (OrderStatusCode c : OrderStatusCode.values()) {
                if (c.getDescription().equalsIgnoreCase(newStatus)) {
                    code = c;
                    break;
                }
            }
            if (code == null) {
                throw new IllegalArgumentException("Trạng thái không hợp lệ: " + newStatus);
            }

            Order order = orderService.getOrderById((long) orderId);
            if (code == OrderStatusCode.PENDING) {
                orderService.confirmOrder((long) orderId, performedBy);
            } else if (code == OrderStatusCode.SHIPPING) {
                orderService.startShipping((long) orderId, performedBy);
            } else if (code == OrderStatusCode.COMPLETED) {
                orderService.completeOrder((long) orderId, performedBy);
            } else if (code == OrderStatusCode.CANCELLED) {
                orderService.cancelOrder((long) orderId, performedBy, "Hủy bởi quản trị viên");
            } else {
                orderService.updateStatus(order, code, performedBy, "Cập nhật trạng thái bởi quản trị viên");
            }

            log.info("Order status updated successfully. orderId={}, newStatus={}", orderId, newStatus);
            AuditLogService.success(
                    AdminOrderController.class,
                    currentUser,
                    "ORDER",
                    "ORDER_STATUS_UPDATED",
                    "ORDER",
                    AuditLogService.describe("orderId", orderId, "newStatus", newStatus)
            );
            response.sendRedirect(request.getContextPath() + "/admin/orders?success=true");
        } catch (Exception e) {
            log.error("Order status update failed. orderId={}, newStatus={}", orderId, newStatus, e);
            AuditLogService.failure(
                    AdminOrderController.class,
                    currentUser,
                    "ORDER",
                    "ORDER_STATUS_UPDATED",
                    "ORDER",
                    AuditLogService.describe("orderId", orderId, "newStatus", newStatus, "reason", e.getMessage())
            );
            response.sendRedirect(request.getContextPath() + "/admin/orders?error=true");
        }
    }

    private User resolveCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        User currentUser = (User) session.getAttribute("userInSession");
        if (currentUser == null) {
            currentUser = (User) session.getAttribute("currentUser");
        }
        if (currentUser == null) {
            currentUser = (User) session.getAttribute("user");
        }
        return currentUser;
    }
}
