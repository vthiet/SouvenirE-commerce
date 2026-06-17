package nlu.fit.web.souvenirecommerce.features.auth.controller;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.common.utils.I18nUtil;
import nlu.fit.web.souvenirecommerce.features.auth.service.AuthService;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/signup/verify-code")
@Slf4j
public class VerifySignupCodeServlet extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        String email = normalizeEmail(req.getParameter("email"));
        String code = req.getParameter("code") == null ? "" : req.getParameter("code").trim();
        JsonObject jsonResponse = new JsonObject();

        if (email == null || email.isEmpty()) {
            log.warn("Xác thực mã đăng ký thất bại: email rỗng");
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.signup.verify_code.empty"));
            return;
        }

        if (!code.matches("^[0-9]{6}$")) {
            log.warn("Xác thực mã đăng ký thất bại: format code sai, email={}", email);
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.signup.verify_code.invalid_format"));
            return;
        }

        if (authService.hasEmailExist(email)) {
            log.warn("Xác thực mã đăng ký thất bại: email đã tồn tại {}", email);
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.signup.verify_code.exists"));
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            log.warn("Xác thực mã đăng ký thất bại: không có session, email={}", email);
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.signup.verify_code.send_first"));
            return;
        }

        String sessionEmail = (String) session.getAttribute("signupEmail");
        if (!email.equals(sessionEmail)) {
            log.warn("Xác thực mã đăng ký thất bại: email không khớp session, email={}, sessionEmail={}", email, sessionEmail);
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.signup.verify_code.send_first"));
            return;
        }

        boolean verified = authService.verifySignupCode(email, code);
        if (!verified) {
            log.warn("Xác thực mã đăng ký thất bại: code không đúng/hết hạn, email={}", email);
            writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.signup.verify_code.invalid"));
            return;
        }

        session.setAttribute("signupVerifiedEmail", email);
        log.info("Xác thực mã đăng ký thành công: email={}", email);
        AuditLogService.success(
                VerifySignupCodeServlet.class,
                email,
                "AUTH",
                "SIGNUP_VERIFIED",
                "ACCOUNT",
                AuditLogService.describe("result", "verified")
        );
        writeJson(resp, jsonResponse, "success", I18nUtil.message(req, "auth.server.signup.verify_code.success"));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private void writeJson(HttpServletResponse resp, JsonObject jsonResponse, String status, String message) throws IOException {
        jsonResponse.addProperty("status", status);
        jsonResponse.addProperty("message", message);
        PrintWriter out = resp.getWriter();
        out.print(jsonResponse.toString());
    }
}
