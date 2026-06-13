package nlu.fit.web.souvenirecommerce.features.auth.controller;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import nlu.fit.web.souvenirecommerce.model.enums.Gender;
import nlu.fit.web.souvenirecommerce.common.utils.EmailUtil;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
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

            String validationError = validate(email, firstName, lastName, phone, gender, password, confirmPassword);
            if (validationError != null) {
                log.warn("Đăng ký thất bại do dữ liệu không hợp lệ: email={}, reason={}", email, validationError);
                writeJson(resp, jsonResponse, "error", validationError);
                return;
            }

            HttpSession session = req.getSession(false);
            String verifiedEmail = session == null ? null : (String) session.getAttribute("signupVerifiedEmail");
            if (!email.equals(verifiedEmail)) {
                log.warn("Đăng ký thất bại do chưa xác thực email: email={}", email);
                writeJson(resp, jsonResponse, "error", "Vui lòng xác thực email trước khi đăng ký");
                return;
            }

            if (authService.hasEmailExist(email)) {
                log.warn("Đăng ký thất bại do email đã tồn tại: email={}", email);
                writeJson(resp, jsonResponse, "error", "Email này đã được đăng ký");
                return;
            }

            if (authService.hasPhoneExist(phone)) {
                log.warn("Đăng ký thất bại do số điện thoại đã tồn tại: phone={}", phone);
                writeJson(resp, jsonResponse, "error", "Số điện thoại này đã được đăng ký");
                return;
            }

            try {
                boolean registered = authService.createUser(email, password, firstName, lastName, phone, gender).isPresent();
                if (!registered) {
                    log.error("Đăng ký thất bại: createUser trả về rỗng cho email={}", email);
                    writeJson(resp, jsonResponse, "error", "Không thể tạo tài khoản. Vui lòng thử lại");
                    return;
                }
            } catch (Exception createUserError) {
                rollbackCurrentTransaction();
                log.error("Lỗi khi tạo tài khoản: email={}", email, createUserError);

                String errorMsg = "Không thể tạo tài khoản. Vui lòng thử lại";
                if (createUserError.getMessage() != null) {
                    if (createUserError.getMessage().contains("Customer role")) {
                        errorMsg = "Lỗi hệ thống: Role chưa được khởi tạo. Liên hệ admin";
                    } else if (createUserError.getMessage().contains("Database") || createUserError.getMessage().contains("Connection")) {
                        errorMsg = "Lỗi kết nối cơ sở dữ liệu. Vui lòng thử lại sau";
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
            writeJson(resp, jsonResponse, "success", "Tạo tài khoản thành công");
        } catch (Exception e) {
            rollbackCurrentTransaction();
            log.error("Lỗi không mong muốn trong luồng đăng ký", e);
            try {
                writeJson(resp, jsonResponse, "error", "Có lỗi xảy ra. Vui lòng thử lại");
            } catch (IOException ioError) {
                log.error("Không thể ghi response JSON của đăng ký", ioError);
            }
        }

        // Create UserSession and setAttributes
    }

    private String validate(String email, String firstName, String lastName, String phone, String gender,
                            String password, String confirmPassword) {
        if (email == null || email.isEmpty()) {
            return "Email không được để trống";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Email không hợp lệ";
        }

        if (lastName == null || lastName.isBlank()) {
            return "Họ không được để trống";
        }

        if (firstName == null || firstName.isBlank()) {
            return "Tên không được để trống";
        }

        if (phone == null || !phone.matches("^[0-9]{10,20}$")) {
            return "Số điện thoại phải từ 10-20 chữ số";
        }

        if (gender == null || gender.isBlank()) {
            return "Vui lòng chọn giới tính";
        }

        try {
            Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Giới tính không hợp lệ";
        }

        if (password == null || password.length() < 8) {
            return "Mật khẩu phải ít nhất 8 ký tự";
        }

        if (!password.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không trùng khớp";
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
