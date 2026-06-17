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
import nlu.fit.web.souvenirecommerce.model.enums.Gender;
import nlu.fit.web.souvenirecommerce.common.utils.EmailUtil;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import nlu.fit.web.souvenirecommerce.common.utils.RecaptchaUtil;
import nlu.fit.web.souvenirecommerce.features.auth.service.AuthService;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/api/signup", "/signup"})
@Slf4j
public class SignupServlet extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RecaptchaUtil.expose(req, getServletContext());
        req.getRequestDispatcher("/WEB-INF/views/auth/signup.jsp").forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        JsonObject jsonResponse = new JsonObject();
        try {
            String email = EmailUtil.normalizeEmail(req.getParameter("email"));
            String firstName = normalize(req.getParameter("firstName"));
            String lastName = normalize(req.getParameter("lastName"));
            String phone = normalize(req.getParameter("phone"));
            String gender = normalize(req.getParameter("gender"));
            String password = req.getParameter("password");
            String confirmPassword = req.getParameter("confirmPassword");

            String validationKey = validate(email, firstName, lastName, phone, gender, password, confirmPassword);
            if (validationKey != null) {
                log.warn("Đăng ký thất bại do dữ liệu không hợp lệ: email={}, reason={}", email, validationKey);
                writeJson(resp, jsonResponse, "error", I18nUtil.message(req, validationKey));
                return;
            }

            HttpSession session = req.getSession(false);
            String verifiedEmail = session == null ? null : (String) session.getAttribute("signupVerifiedEmail");
            if (!email.equals(verifiedEmail)) {
                log.warn("Đăng ký thất bại do chưa xác thực email: email={}", email);
                writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.signup.email_not_verified"));
                return;
            }

            if (authService.hasEmailExist(email)) {
                log.warn("Đăng ký thất bại do email đã tồn tại: email={}", email);
                writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.signup.email_exists"));
                return;
            }

            if (authService.hasPhoneExist(phone)) {
                log.warn("Đăng ký thất bại do số điện thoại đã tồn tại: phone={}", phone);
                writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.signup.phone_exists"));
                return;
            }

            try {
                boolean registered = authService.createUser(email, password, firstName, lastName, phone, gender).isPresent();
                if (!registered) {
                    log.error("Đăng ký thất bại: createUser trả về rỗng cho email={}", email);
                    writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.signup.create_failed"));
                    return;
                }
            } catch (Exception createUserError) {
                rollbackCurrentTransaction();
                log.error("Lỗi khi tạo tài khoản: email={}", email, createUserError);

                String errorMsg = I18nUtil.message(req, "auth.server.signup.create_failed");
                if (createUserError.getMessage() != null) {
                    if (createUserError.getMessage().contains("Customer role")) {
                        errorMsg = I18nUtil.message(req, "auth.server.signup.role_missing");
                    } else if (createUserError.getMessage().contains("Database") || createUserError.getMessage().contains("Connection")) {
                        errorMsg = I18nUtil.message(req, "auth.server.signup.database_error");
                    }
                }
                writeJson(resp, jsonResponse, "error", errorMsg);
                return;
            }

            if (session != null) {
                session.removeAttribute("signupEmail");
                session.removeAttribute("signupVerifiedEmail");
            }

            log.info("Đăng ký thành công: email={}", email);
            AuditLogService.success(
                    SignupServlet.class,
                    email,
                    "AUTH",
                    "SIGNUP",
                    "ACCOUNT",
                    AuditLogService.describe("phone", phone, "gender", gender)
            );
            writeJson(resp, jsonResponse, "success", I18nUtil.message(req, "auth.js.signup_success"));
        } catch (Exception e) {
            rollbackCurrentTransaction();
            log.error("Lỗi không mong muốn trong luồng đăng ký", e);
            try {
                writeJson(resp, jsonResponse, "error", I18nUtil.message(req, "auth.server.signup.unexpected"));
            } catch (IOException ioError) {
                log.error("Không thể ghi response JSON của đăng ký", ioError);
            }
        }

    }

    private String validate(String email, String firstName, String lastName, String phone, String gender,
                            String password, String confirmPassword) {
        if (email == null || email.isEmpty()) {
            return "auth.server.check_email.empty";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "auth.server.check_email.invalid";
        }

        if (lastName == null || lastName.isBlank()) {
            return "auth.js.last_name_required";
        }

        if (firstName == null || firstName.isBlank()) {
            return "auth.js.first_name_required";
        }

        if (phone == null || !phone.matches("^[0-9]{10,20}$")) {
            return "auth.js.phone_invalid";
        }

        if (gender == null || gender.isBlank()) {
            return "auth.js.gender_required";
        }

        try {
            Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "auth.js.gender_required";
        }

        if (password == null || password.length() < 8) {
            return "auth.js.password_min";
        }

        if (!password.equals(confirmPassword)) {
            return "auth.js.password_mismatch";
        }

        return null;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
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
            log.warn("Không thể rollback transaction đăng ký", rollbackError);
        }
    }
}
