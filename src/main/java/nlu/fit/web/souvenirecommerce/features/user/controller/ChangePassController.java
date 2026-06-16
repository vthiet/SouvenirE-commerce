package nlu.fit.web.souvenirecommerce.features.user.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.legacy.dao.impl.UserDAOImpl;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;

@WebServlet("/user/change-password")
public class ChangePassController extends HttpServlet {

    private final UserDAOImpl userDAOImpl = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null)
                ? (User) session.getAttribute("userInSession")
                : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.getRequestDispatcher("/user/userchange-pas.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null)
                ? (User) session.getAttribute("userInSession")
                : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (user.getId() == null || !userDAOImpl.checkPassword(user.getId().intValue(), currentPassword)) {
            AuditLogService.failure(
                    ChangePassController.class,
                    user,
                    "SECURITY",
                    "PASSWORD_CHANGED",
                    "CREDENTIAL",
                    AuditLogService.describe("reason", "current_password_invalid")
            );
            request.setAttribute("error", "Mật khẩu hiện tại không đúng");
        } else if (!newPassword.equals(confirmPassword)) {
            AuditLogService.failure(
                    ChangePassController.class,
                    user,
                    "SECURITY",
                    "PASSWORD_CHANGED",
                    "CREDENTIAL",
                    AuditLogService.describe("reason", "confirmation_mismatch")
            );
            request.setAttribute("error", "Mật khẩu xác nhận không khớp");
        } else if (newPassword.length() < 8) {
            AuditLogService.failure(
                    ChangePassController.class,
                    user,
                    "SECURITY",
                    "PASSWORD_CHANGED",
                    "CREDENTIAL",
                    AuditLogService.describe("reason", "password_too_short")
            );
            request.setAttribute("error", "Mật khẩu mới phải từ 8 ký tự trở lên");
        } else {

            boolean updated = userDAOImpl.updatePassword(user.getId().intValue(), newPassword);

            if (updated) {
                AuditLogService.success(
                        ChangePassController.class,
                        user,
                        "SECURITY",
                        "PASSWORD_CHANGED",
                        "CREDENTIAL",
                        AuditLogService.describe("result", "updated")
                );
                request.setAttribute("success", "Đổi mật khẩu thành công");
            } else {
                AuditLogService.failure(
                        ChangePassController.class,
                        user,
                        "SECURITY",
                        "PASSWORD_CHANGED",
                        "CREDENTIAL",
                        AuditLogService.describe("reason", "update_failed")
                );
                request.setAttribute("error", "Có lỗi xảy ra, vui lòng thử lại");
            }
        }

        request.getRequestDispatcher("/user/userchange-pas.jsp")
                .forward(request, response);
    }
}
