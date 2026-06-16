package nlu.fit.web.souvenirecommerce.features.user.controller;

import com.google.gson.JsonObject;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import nlu.fit.web.souvenirecommerce.features.auth.service.AuthService;
import nlu.fit.web.souvenirecommerce.legacy.dao.impl.UserDAOImpl;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@WebServlet(urlPatterns = {
        "/user/change-password",
        "/api/change-password/send-code",
        "/api/change-password/verify-code"
})
@Slf4j
public class ChangePassController extends HttpServlet {

    private final UserDAOImpl userDAOImpl = new UserDAOImpl();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = getLoggedInUser(session);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setAttribute("pageTitle", "Đổi mật khẩu");
        request.setAttribute("pageCss", "account/account-layout.css");
        request.setAttribute("contentCss", "account/profile-form.css");
        request.setAttribute("pageJs", "account/change-password.js");
        request.setAttribute("contentPage", "/WEB-INF/views/account/account_layout.jsp");
        request.setAttribute("pageContent", "/WEB-INF/views/account/change-password.jsp");

        request.getRequestDispatcher("/WEB-INF/layout/base.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/api/change-password/send-code".equals(path)) {
            handleSendCode(request, response);
        } else if ("/api/change-password/verify-code".equals(path)) {
            handleVerifyCode(request, response);
        } else if ("/user/change-password".equals(path)) {
            handleChangePasswordForm(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleSendCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        User user = getLoggedInUser(session);

        if (user == null) {
            writeJson(response, jsonResponse, "error", "Vui lòng đăng nhập để thực hiện chức năng này");
            return;
        }

        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            writeJson(response, jsonResponse, "error", "Không tìm thấy địa chỉ email của tài khoản");
            return;
        }

        try {
            LocalDateTime expiresAt = authService.sendChangePasswordCode(email);
            session.setAttribute("changePasswordEmail", email);
            session.removeAttribute("changePasswordVerifiedEmail");

            log.info("Gửi mã xác thực đổi mật khẩu thành công: email={}, expiresAt={}", email, expiresAt);
            writeJson(response, jsonResponse, "success", "Mã xác thực đổi mật khẩu đã được gửi tới email của bạn");
        } catch (MessagingException | RuntimeException e) {
            log.error("Gửi mã xác thực đổi mật khẩu thất bại: email={}", email, e);
            writeJson(response, jsonResponse, "error", "Không gửi được mã xác thực. Vui lòng kiểm tra cấu hình email");
        }
    }

    private void handleVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        User user = getLoggedInUser(session);

        if (user == null) {
            writeJson(response, jsonResponse, "error", "Vui lòng đăng nhập để thực hiện chức năng này");
            return;
        }

        String code = request.getParameter("code") == null ? "" : request.getParameter("code").trim();
        if (!code.matches("^[0-9]{6}$")) {
            writeJson(response, jsonResponse, "error", "Mã xác thực gồm 6 chữ số");
            return;
        }

        String sessionEmail = (session != null) ? (String) session.getAttribute("changePasswordEmail") : null;
        if (sessionEmail == null || !sessionEmail.equals(user.getEmail())) {
            writeJson(response, jsonResponse, "error", "Vui lòng gửi mã xác thực trước");
            return;
        }

        boolean verified = authService.verifyChangePasswordCode(user.getEmail(), code);
        if (!verified) {
            writeJson(response, jsonResponse, "error", "Mã xác thực không đúng hoặc đã hết hạn");
            return;
        }

        session.setAttribute("changePasswordVerifiedEmail", user.getEmail());
        log.info("Xác thực mã đổi mật khẩu thành công: email={}", user.getEmail());
        writeJson(response, jsonResponse, "success", "Xác thực email thành công! Hãy nhập mật khẩu mới");
    }

    private void handleChangePasswordForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = getLoggedInUser(session);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String verifiedEmail = (session != null) ? (String) session.getAttribute("changePasswordVerifiedEmail") : null;
        if (verifiedEmail == null || !verifiedEmail.equalsIgnoreCase(user.getEmail())) {
            request.setAttribute("error", "Vui lòng xác thực email trước khi đổi mật khẩu");
            renderPage(request, response);
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
        } else if (newPassword == null || newPassword.length() < 8) {
            request.setAttribute("error", "Mật khẩu mới phải từ 8 ký tự trở lên");
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
                if (session != null) {
                    session.removeAttribute("changePasswordEmail");
                    session.removeAttribute("changePasswordVerifiedEmail");
                }
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

        renderPage(request, response);
    }

    private void renderPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Đổi mật khẩu");
        request.setAttribute("pageCss", "account/account-layout.css");
        request.setAttribute("contentCss", "account/profile-form.css");
        request.setAttribute("pageJs", "account/change-password.js");
        request.setAttribute("contentPage", "/WEB-INF/views/account/account_layout.jsp");
        request.setAttribute("pageContent", "/WEB-INF/views/account/change-password.jsp");

        request.getRequestDispatcher("/WEB-INF/layout/base.jsp")
                .forward(request, response);
    }

    private User getLoggedInUser(HttpSession session) {
        if (session == null) return null;
        for (String key : new String[]{"user", "userInSession", "currentUser", "authUser"}) {
            Object attr = session.getAttribute(key);
            if (attr instanceof User u) return u;
        }
        return null;
    }

    private void writeJson(HttpServletResponse response, JsonObject jsonResponse, String status, String message)
            throws IOException {
        jsonResponse.addProperty("status", status);
        jsonResponse.addProperty("message", message);
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
    }
}
