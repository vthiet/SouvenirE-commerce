package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nlu.fit.web.souvenirecommerce.legacy.dao.OrderDAO;
import nlu.fit.web.souvenirecommerce.legacy.model.Order;
import nlu.fit.web.souvenirecommerce.legacy.model.OrderItem;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/orders")
public class AdminOrderController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminOrderController.class);
    private final OrderDAO orderDAO = new OrderDAO();

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
            orders = orderDAO.getOrdersByStatus(statusFilter, page, pageSize);
            totalOrders = orderDAO.getOrderCountByStatus(statusFilter);
        } else {
            orders = orderDAO.getOrdersPaginated(page, pageSize);
            totalOrders = orderDAO.getTotalOrders();
        }

        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        log.info("Loaded admin orders page {} with {} records (statusFilter={})",
                page, orders.size(), statusFilter == null || statusFilter.isBlank() ? "all" : statusFilter);

        // Get status counts for stats cards
        int pendingCount = orderDAO.getOrderCountByStatus("Chờ xác nhận");
        int processingCount = orderDAO.getOrderCountByStatus("Đang xử lý");
        int shippingCount = orderDAO.getOrderCountByStatus("Đang giao");
        int completedCount = orderDAO.getOrderCountByStatus("Hoàn thành");

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
        log.debug("Admin order POST request received. action={}", action);

        if ("updateStatus".equals(action)) {
            updateOrderStatus(request, response);
        } else {
            log.warn("Unsupported admin order POST action: {}", action);
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

        Order order = orderDAO.getOrderById(orderId);
        List<OrderItem> orderItems = orderDAO.getOrderItems(orderId);

        log.info("Opened admin order detail for orderId={}", orderId);

        request.setAttribute("order", order);
        request.setAttribute("orderItems", orderItems);
        request.getRequestDispatcher("/admin/order-detail.jsp").forward(request, response);
    }

    private void updateOrderStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int orderId;
        try {
            orderId = Integer.parseInt(request.getParameter("orderId"));
        } catch (NumberFormatException ex) {
            log.warn("Invalid order id supplied for admin order status update: {}", request.getParameter("orderId"));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order id");
            return;
        }

        String newStatus = request.getParameter("status");

        log.info("Updating order status. orderId={}, newStatus={}", orderId, newStatus);
        boolean success = orderDAO.updateOrderStatus(orderId, newStatus);

        if (success) {
            log.info("Order status updated successfully. orderId={}, newStatus={}", orderId, newStatus);
            response.sendRedirect(request.getContextPath() + "/admin/orders?success=true");
        } else {
            log.warn("Order status update failed. orderId={}, newStatus={}", orderId, newStatus);
            response.sendRedirect(request.getContextPath() + "/admin/orders?error=true");
        }
    }
}
