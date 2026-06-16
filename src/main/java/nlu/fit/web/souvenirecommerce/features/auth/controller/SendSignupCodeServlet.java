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
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import nlu.fit.web.souvenirecommerce.common.utils.RecaptchaUtil;
import nlu.fit.web.souvenirecommerce.features.auth.service.AuthService;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@WebServlet("/api/signup/send-code")
@Slf4j
public class SendSignupCodeServlet extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        String email = EmailUtil.normalizeEmail(req.getParameter("email"));
        JsonObject jsonResponse = new JsonObject();
        String actorLabel = email == null || email.isBlank() ? "Guest" : email;

        if (!RecaptchaUtil.verify(req, getServletContext())) {
            log.warn("Gửi mã đăng ký thất bại do captcha không hợp lệ: email={}", email);
            AuditLogService.failure(
                    SendSignupCodeServlet.class,
                    actorLabel,
                    "AUTH",
                    "SIGNUP_CODE_SENT",
                    "EMAIL",
                    AuditLogService.describe("reason", "captcha_failed", "email", email)
            );
            writeJson(resp, jsonResponse, "error", "Vui lòng xác nhận bạn không phải robot.");
            return;
        }

        if (email == null || email.isEmpty()) {
            log.warn("Gửi mã đăng ký thất bại: email rỗng");
            AuditLogService.failure(
                    SendSignupCodeServlet.class,
                    actorLabel,
                    "AUTH",
                    "SIGNUP_CODE_SENT",
                    "EMAIL",
                    AuditLogService.describe("reason", "email_empty")
            );
            writeJson(resp, jsonResponse, "error", "Email không được để trống");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            log.warn("Gửi mã đăng ký thất bại: email không hợp lệ {}", email);
            AuditLogService.failure(
                    SendSignupCodeServlet.class,
                    actorLabel,
                    "AUTH",
                    "SIGNUP_CODE_SENT",
                    "EMAIL",
                    AuditLogService.describe("reason", "email_invalid", "email", email)
            );
            writeJson(resp, jsonResponse, "error", "Email không hợp lệ");
            return;
        }

        if (authService.hasEmailExist(email)) {
            log.warn("Gửi mã đăng ký thất bại: email đã tồn tại {}", email);
            AuditLogService.failure(
                    SendSignupCodeServlet.class,
                    actorLabel,
                    "AUTH",
                    "SIGNUP_CODE_SENT",
                    "EMAIL",
                    AuditLogService.describe("reason", "email_exists", "email", email)
            );
            writeJson(resp, jsonResponse, "error", "Email này đã được đăng ký");
            return;
        }

        LocalDateTime expiresAt;
        try {
            expiresAt = authService.sendSignupVerificationCode(email);
        } catch (MessagingException | RuntimeException e) {
            rollbackCurrentTransaction();
            log.error("Gửi mã đăng ký thất bại: email={}", email, e);
            AuditLogService.failure(
                    SendSignupCodeServlet.class,
                    actorLabel,
                    "AUTH",
                    "SIGNUP_CODE_SENT",
                    "EMAIL",
                    AuditLogService.describe("reason", "send_failed", "email", email, "message", e.getMessage())
            );
            writeJson(resp, jsonResponse, "error", "Không gửi được mã xác thực. Vui lòng kiểm tra cấu hình email");
            return;
        }

        HttpSession session = req.getSession();
        session.setAttribute("signupEmail", email);
        session.removeAttribute("signupVerifiedEmail");

        log.info("Gửi mã đăng ký thành công: email={}, expiresAt={}", email, expiresAt);
        AuditLogService.success(
                SendSignupCodeServlet.class,
                email,
                "AUTH",
                "SIGNUP_CODE_SENT",
                "EMAIL",
                AuditLogService.describe("email", email, "expiresAt", String.valueOf(expiresAt))
        );
        writeJson(resp, jsonResponse, "success", "Mã xác thực đã được gửi tới email của bạn");
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
            log.warn("Không thể rollback transaction gửi mã đăng ký", rollbackError);
        }
    }
}
