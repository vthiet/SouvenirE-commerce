package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.legacy.dao.impl.UserDAOImpl;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/admin/customers")
public class AdminCustomerController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminCustomerController.class);
    private UserDAOImpl userDAOImpl;

    @Override
    public void init() {

            userDAOImpl = new UserDAOImpl();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = 1;
        int pageSize = 20;

        try {
            String pageParam = req.getParameter("page");
            if (pageParam != null) {
                page = Integer.parseInt(pageParam);
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        int offset = (page - 1) * pageSize;
        int totalCustomers = userDAOImpl.getTotalCustomers();
        int totalPages = (int) Math.ceil((double) totalCustomers / pageSize);
        log.info("Loaded admin customers page {} with {} total customers", page, totalCustomers);

        req.setAttribute("customers", userDAOImpl.getCustomersWithPagination(offset, pageSize));
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalCustomers", totalCustomers);

        HttpSession session = req.getSession();
        String message = (String) session.getAttribute("message");
        String messageType = (String) session.getAttribute("messageType");
        if (message != null) {
            req.setAttribute("message", message);
            req.setAttribute("messageType", messageType);
            session.removeAttribute("message");
            session.removeAttribute("messageType");
        }

        req.getRequestDispatcher("/admin/customers.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        log.debug("Admin customers action received: {}", action);

        try {
            User currentUser = getCurrentAdminUser(req);
            if ("add".equals(action)) {
                String fullName = req.getParameter("fullName");
                String email = req.getParameter("email");
                String password = req.getParameter("password");
                String phone = req.getParameter("phone");

                if (userDAOImpl.insertUser(fullName, email, password, phone)) {
                    log.info("Admin customer created: email={}", email);
                    AuditLogService.success(
                            AdminCustomerController.class,
                            currentUser,
                            "CUSTOMER",
                            "CUSTOMER_CREATED",
                            "CUSTOMER",
                            AuditLogService.describe("fullName", fullName, "email", email, "phone", phone)
                    );
                    req.getSession().setAttribute("message", "Thêm khách hàng thành công!");
                    req.getSession().setAttribute("messageType", "success");
                } else {
                    log.warn("Admin customer creation failed: email={}", email);
                    AuditLogService.failure(
                            AdminCustomerController.class,
                            currentUser,
                            "CUSTOMER",
                            "CUSTOMER_CREATED",
                            "CUSTOMER",
                            AuditLogService.describe("email", email, "reason", "insert_failed")
                    );
                    req.getSession().setAttribute("message", "Thêm khách hàng thất bại!");
                    req.getSession().setAttribute("messageType", "error");
                }

            } else if ("edit".equals(action)) {
                int userId = Integer.parseInt(req.getParameter("id"));
                String fullName = req.getParameter("fullName");
                String email = req.getParameter("email");
                String phone = req.getParameter("phone");

                if (userDAOImpl.updateUser(userId, fullName, email, phone)) {
                    log.info("Admin customer updated: userId={}", userId);
                    AuditLogService.success(
                            AdminCustomerController.class,
                            currentUser,
                            "CUSTOMER",
                            "CUSTOMER_UPDATED",
                            "CUSTOMER",
                            AuditLogService.describe("userId", userId, "fullName", fullName, "email", email, "phone", phone)
                    );
                    req.getSession().setAttribute("message", "Cập nhật khách hàng thành công!");
                    req.getSession().setAttribute("messageType", "success");
                } else {
                    log.warn("Admin customer update failed: userId={}", userId);
                    AuditLogService.failure(
                            AdminCustomerController.class,
                            currentUser,
                            "CUSTOMER",
                            "CUSTOMER_UPDATED",
                            "CUSTOMER",
                            AuditLogService.describe("userId", userId, "reason", "update_failed")
                    );
                    req.getSession().setAttribute("message", "Cập nhật khách hàng thất bại!");
                    req.getSession().setAttribute("messageType", "error");
                }

            } else if ("toggleStatus".equals(action)) {
                int userId = Integer.parseInt(req.getParameter("id"));
                String currentStatus = req.getParameter("currentStatus");
                String newStatus = "Active".equals(currentStatus) ? "Banned" : "Active";

                if (userDAOImpl.updateUserStatus(userId, newStatus)) {
                    log.info("Admin customer status changed: userId={}, newStatus={}", userId, newStatus);
                    AuditLogService.success(
                            AdminCustomerController.class,
                            currentUser,
                            "CUSTOMER",
                            "CUSTOMER_STATUS_CHANGED",
                            "CUSTOMER",
                            AuditLogService.describe("userId", userId, "newStatus", newStatus)
                    );
                    req.getSession().setAttribute("message", "Cập nhật trạng thái thành công!");
                    req.getSession().setAttribute("messageType", "success");
                } else {
                    log.warn("Admin customer status update failed: userId={}, newStatus={}", userId, newStatus);
                    AuditLogService.failure(
                            AdminCustomerController.class,
                            currentUser,
                            "CUSTOMER",
                            "CUSTOMER_STATUS_CHANGED",
                            "CUSTOMER",
                            AuditLogService.describe("userId", userId, "newStatus", newStatus, "reason", "update_failed")
                    );
                    req.getSession().setAttribute("message", "Cập nhật trạng thái thất bại!");
                    req.getSession().setAttribute("messageType", "error");
                }

            } else if ("delete".equals(action)) {
                int userId = Integer.parseInt(req.getParameter("id"));

                if (userDAOImpl.deleteUser(userId)) {
                    log.info("Admin customer deleted: userId={}", userId);
                    AuditLogService.success(
                            AdminCustomerController.class,
                            currentUser,
                            "CUSTOMER",
                            "CUSTOMER_DELETED",
                            "CUSTOMER",
                            AuditLogService.describe("userId", userId)
                    );
                    req.getSession().setAttribute("message", "Xóa khách hàng thành công!");
                    req.getSession().setAttribute("messageType", "success");
                } else {
                    log.warn("Admin customer deletion failed: userId={}", userId);
                    AuditLogService.failure(
                            AdminCustomerController.class,
                            currentUser,
                            "CUSTOMER",
                            "CUSTOMER_DELETED",
                            "CUSTOMER",
                            AuditLogService.describe("userId", userId, "reason", "delete_failed")
                    );
                    req.getSession().setAttribute("message", "Xóa khách hàng thất bại!");
                    req.getSession().setAttribute("messageType", "error");
                }
            }
        } catch (Exception e) {
            log.error("Admin customer action failed: {}", action, e);
            AuditLogService.failure(
                    AdminCustomerController.class,
                    getCurrentAdminUser(req),
                    "CUSTOMER",
                    "CUSTOMER_ACTION_FAILED",
                    "CUSTOMER",
                    AuditLogService.describe("action", action, "reason", e.getClass().getSimpleName(), "message", e.getMessage())
            );
            req.getSession().setAttribute("message", "Có lỗi xảy ra: " + e.getMessage());
            req.getSession().setAttribute("messageType", "error");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/customers");
    }

    private User getCurrentAdminUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("user");
        if (user instanceof User) {
            return (User) user;
        }
        user = session.getAttribute("userInSession");
        if (user instanceof User) {
            return (User) user;
        }
        user = session.getAttribute("currentUser");
        return user instanceof User ? (User) user : null;
    }
}
