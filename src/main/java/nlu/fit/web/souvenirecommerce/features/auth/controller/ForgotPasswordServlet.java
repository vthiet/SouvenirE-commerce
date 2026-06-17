package nlu.fit.web.souvenirecommerce.features.auth.controller;

import com.google.gson.JsonObject;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.common.utils.EmailUtil;
import nlu.fit.web.souvenirecommerce.common.utils.I18nUtil;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import nlu.fit.web.souvenirecommerce.common.utils.RecaptchaUtil;
import nlu.fit.web.souvenirecommerce.features.auth.service.AuthService;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@WebServlet(urlPatterns = {
        "/forgot-password",
        "/api/forgot-password/send-code",
        "/api/forgot-password/verify-code",
        "/api/forgot-password/reset"
})
@Slf4j
public class ForgotPasswordServlet extends HttpServlet {
    private static final String AUDIT_CATEGORY = "AUTH";
    private static final String AUDIT_ENTITY = "EMAIL";
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RecaptchaUtil.expose(req, getServletContext());
        req.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        String path = req.getServletPath();
        JsonObject jsonResponse = new JsonObject();

        if ("/api/forgot-password/send-code".equals(path)) {
            handleSendCode(req, resp, jsonResponse);
        } else if ("/api/forgot-password/verify-code".equals(path)) {
            handleVerifyCode(req, resp, jsonResponse);
        } else if ("/api/forgot-password/reset".equals(path)) {
            handleResetPassword(req, resp, jsonResponse);
        } else {
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.reset.failed"));
        }
    }

    private void handleSendCode(HttpServletRequest req, HttpServletResponse resp, JsonObject jsonResponse) throws IOException {
        String email = EmailUtil.normalizeEmail(req.getParameter("email"));
        String actorLabel = resolveActorLabel(email);

        if (!RecaptchaUtil.verify(req, getServletContext())) {
            log.warn("Gửi mã reset mật khẩu thất bại do captcha: email={}", email);
            auditFailure(actorLabel, "FORGOT_PASSWORD_SEND_CODE",
                    AuditLogService.describe("email", email, "reason", "captcha_failed"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.send_code.captcha"));
            return;
        }

        if (email == null || email.isEmpty()) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_SEND_CODE",
                    AuditLogService.describe("reason", "email_empty"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.send_code.empty"));
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_SEND_CODE",
                    AuditLogService.describe("email", email, "reason", "email_invalid"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.send_code.invalid"));
            return;
        }

        if (!authService.hasEmailExist(email)) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_SEND_CODE",
                    AuditLogService.describe("email", email, "reason", "email_not_found"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.send_code.not_found"));
            return;
        }

        LocalDateTime expiresAt;
        try {
            expiresAt = authService.sendResetPasswordCode(email);
        } catch (MessagingException | RuntimeException e) {
            rollbackCurrentTransaction();
            log.error("Gửi mã reset mật khẩu thất bại: email={}", email, e);
            auditFailure(actorLabel, "FORGOT_PASSWORD_SEND_CODE",
                    AuditLogService.describe("email", email, "reason", "send_failed", "message", e.getMessage()));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.send_code.failed"));
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("forgotPasswordEmail", email);
        session.removeAttribute("forgotPasswordVerifiedEmail");

        log.info("Gửi mã reset mật khẩu thành công: email={}, expiresAt={}", email, expiresAt);
        auditSuccess(actorLabel, "FORGOT_PASSWORD_SEND_CODE",
                AuditLogService.describe("email", email, "expiresAt", String.valueOf(expiresAt)));
        writeJson(resp, jsonResponse, "success", I18nUtil.message(req, "auth.server.forgot.send_code.success"));
    }

    private void handleVerifyCode(HttpServletRequest req, HttpServletResponse resp, JsonObject jsonResponse) throws IOException {
        String email = EmailUtil.normalizeEmail(req.getParameter("email"));
        String code = req.getParameter("code") == null ? "" : req.getParameter("code").trim();
        String actorLabel = resolveActorLabel(email);

        if (email == null || email.isEmpty()) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_VERIFY_CODE",
                    AuditLogService.describe("reason", "email_empty"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.verify_code.empty"));
            return;
        }

        if (!code.matches("^[0-9]{6}$")) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_VERIFY_CODE",
                    AuditLogService.describe("email", email, "reason", "code_invalid"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.verify_code.invalid_format"));
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_VERIFY_CODE",
                    AuditLogService.describe("email", email, "reason", "session_missing"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.verify_code.send_first"));
            return;
        }

        String sessionEmail = (String) session.getAttribute("forgotPasswordEmail");
        if (!email.equals(sessionEmail)) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_VERIFY_CODE",
                    AuditLogService.describe("email", email, "reason", "session_email_mismatch"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.verify_code.send_first"));
            return;
        }

        boolean verified = authService.verifyResetPasswordCode(email, code);
        if (!verified) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_VERIFY_CODE",
                    AuditLogService.describe("email", email, "reason", "code_invalid_or_expired"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.verify_code.invalid"));
            return;
        }

        session.setAttribute("forgotPasswordVerifiedEmail", email);
        log.info("Xác thực mã reset mật khẩu thành công: email={}", email);
        auditSuccess(actorLabel, "FORGOT_PASSWORD_VERIFY_CODE",
                AuditLogService.describe("email", email, "result", "verified"));
        writeJson(resp, jsonResponse, "success", I18nUtil.message(req, "auth.server.forgot.verify_code.success"));
    }

    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp, JsonObject jsonResponse) throws IOException {
        String email = EmailUtil.normalizeEmail(req.getParameter("email"));
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String actorLabel = resolveActorLabel(email);

        if (email == null || email.isEmpty()) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_RESET",
                    AuditLogService.describe("reason", "email_empty"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.reset.empty"));
            return;
        }

        HttpSession session = req.getSession(false);
        String verifiedEmail = session == null ? null : (String) session.getAttribute("forgotPasswordVerifiedEmail");

        if (!email.equals(verifiedEmail)) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_RESET",
                    AuditLogService.describe("email", email, "reason", "email_not_verified"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.reset.not_verified"));
            return;
        }

        if (password == null || password.length() < 8) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_RESET",
                    AuditLogService.describe("email", email, "reason", "password_too_short"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.reset.password_min"));
            return;
        }

        if (!password.equals(confirmPassword)) {
            auditFailure(actorLabel, "FORGOT_PASSWORD_RESET",
                    AuditLogService.describe("email", email, "reason", "confirmation_mismatch"));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.reset.password_mismatch"));
            return;
        }

        try {
            boolean reset = authService.resetPassword(email, password);
            if (!reset) {
                auditFailure(actorLabel, "FORGOT_PASSWORD_RESET",
                        AuditLogService.describe("email", email, "reason", "reset_failed"));
                writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.reset.failed"));
                return;
            }
        } catch (Exception e) {
            rollbackCurrentTransaction();
            log.error("Đặt lại mật khẩu thất bại: email={}", email, e);
            auditFailure(actorLabel, "FORGOT_PASSWORD_RESET",
                    AuditLogService.describe("email", email, "reason", "reset_exception", "message", e.getMessage()));
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.forgot.reset.exception"));
            return;
        }

        if (session != null) {
            session.removeAttribute("forgotPasswordEmail");
            session.removeAttribute("forgotPasswordVerifiedEmail");
        }

        log.info("Đặt lại mật khẩu thành công: email={}", email);
        auditSuccess(actorLabel, "FORGOT_PASSWORD_RESET",
                AuditLogService.describe("email", email, "result", "updated"));
        writeJson(resp, jsonResponse, "success", I18nUtil.message(req, "auth.server.forgot.reset.success"));
    }

    private void writeJson(HttpServletResponse resp, JsonObject jsonResponse, String status, String message) throws IOException {
        jsonResponse.addProperty("status", status);
        jsonResponse.addProperty("message", message);
        PrintWriter out = resp.getWriter();
        out.print(jsonResponse.toString());
    }

    private void rollbackCurrentTransaction() {
        try {
            Transaction transaction = HibernateUtil.getSessionFactory().getCurrentSession().getTransaction();
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
        } catch (RuntimeException rollbackError) {
            log.warn("Không thể rollback transaction trong forgot-password", rollbackError);
        }
    }

    private void auditSuccess(String actorLabel, String action, String details) {
        AuditLogService.success(ForgotPasswordServlet.class, actorLabel, AUDIT_CATEGORY, action, AUDIT_ENTITY, details);
    }

    private void auditFailure(String actorLabel, String action, String details) {
        AuditLogService.failure(ForgotPasswordServlet.class, actorLabel, AUDIT_CATEGORY, action, AUDIT_ENTITY, details);
    }

    private String resolveActorLabel(String email) {
        return email == null || email.isBlank() ? "Guest" : email;
    }
}
