package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.legacy.dao.SettingsDAO;
import nlu.fit.web.souvenirecommerce.legacy.dao.impl.UserDAOImpl;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/admin/settings")
public class AdminSettingsController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminSettingsController.class);
    private UserDAOImpl userDAOImpl;
    private SettingsDAO settingsDAO;

    @Override
    public void init() {
        userDAOImpl = new UserDAOImpl();
        settingsDAO = new SettingsDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Get settings from database
        log.info("Loaded admin settings page");
        Map<String, String> settings = settingsDAO.getAllSettings();
        req.setAttribute("settings", settings);

        // Get message from session
        HttpSession session = req.getSession();
        String message = (String) session.getAttribute("message");
        String messageType = (String) session.getAttribute("messageType");
        if (message != null) {
            req.setAttribute("message", message);
            req.setAttribute("messageType", messageType);
            session.removeAttribute("message");
            session.removeAttribute("messageType");
        }

        req.getRequestDispatcher("/admin/settings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        log.debug("Admin settings action received: {}", action);
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            currentUser = (User) session.getAttribute("userInSession");
        }

        if (currentUser == null) {
            log.warn("Rejected admin settings action because no current user was found");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            if ("updateProfile".equals(action)) {
                String fullName = req.getParameter("fullName"); 
                String email = req.getParameter("email");
                String phone = req.getParameter("phone");

                if (currentUser.getId() != null && userDAOImpl.updateUser(currentUser.getId().intValue(), fullName, email, phone)) {
                    log.info("Admin profile updated: userId={}", currentUser.getId());
                    // Update session user
                    String normalized = fullName == null ? "" : fullName.trim();
                    int split = normalized.lastIndexOf(' ');
                    if (split > 0) {
                        currentUser.setFirstName(normalized.substring(0, split));
                        currentUser.setLastName(normalized.substring(split + 1));
                    } else {
                        currentUser.setFirstName(normalized);
                        currentUser.setLastName(normalized);
                    }
                    currentUser.setEmail(email);
                    currentUser.setPhone(phone);
                    session.setAttribute("user", currentUser);
                    session.setAttribute("userInSession", currentUser);

                    session.setAttribute("message", "Cập nhật thông tin thành công!");
                    session.setAttribute("messageType", "success");
                } else {
                    log.warn("Admin profile update failed: userId={}", currentUser.getId());
                    session.setAttribute("message", "Cập nhật thông tin thất bại!");
                    session.setAttribute("messageType", "error");
                }

            } else if ("changePassword".equals(action)) {
                String currentPassword = req.getParameter("currentPassword");
                String newPassword = req.getParameter("newPassword");
                String confirmPassword = req.getParameter("confirmPassword");

                if (!newPassword.equals(confirmPassword)) {
                    log.warn("Admin password change rejected because confirmation did not match: userId={}", currentUser.getId());
                    session.setAttribute("message", "Mật khẩu mới không khớp!");
                    session.setAttribute("messageType", "error");
                } else if (currentUser.getId() == null || !userDAOImpl.checkPassword(currentUser.getId().intValue(), currentPassword)) {
                    log.warn("Admin password change rejected because current password was invalid: userId={}", currentUser.getId());
                    session.setAttribute("message", "Mật khẩu hiện tại không đúng!");
                    session.setAttribute("messageType", "error");
                } else if (userDAOImpl.updatePasswordByUserId(currentUser.getId().intValue(), newPassword)) {
                    log.info("Admin password changed: userId={}", currentUser.getId());
                    session.setAttribute("message", "Đổi mật khẩu thành công!");
                    session.setAttribute("messageType", "success");
                } else {
                    log.warn("Admin password change failed: userId={}", currentUser.getId());
                    session.setAttribute("message", "Đổi mật khẩu thất bại!");
                    session.setAttribute("messageType", "error");
                }

            } else if ("updateSystem".equals(action)) {
                Map<String, String> settings = new HashMap<>();
                settings.put("site_name", req.getParameter("siteName"));
                settings.put("site_email", req.getParameter("siteEmail"));
                settings.put("site_phone", req.getParameter("sitePhone"));
                settings.put("site_address", req.getParameter("siteAddress"));
                settings.put("payment_cod", req.getParameter("paymentCod") != null ? "true" : "false");
                settings.put("payment_vnpay", req.getParameter("paymentVnpay") != null ? "true" : "false");
                settings.put("payment_momo", req.getParameter("paymentMomo") != null ? "true" : "false");
                settings.put("payment_card", req.getParameter("paymentCard") != null ? "true" : "false");
                settings.put("shipping_ghn", req.getParameter("shippingGhn") != null ? "true" : "false");
                settings.put("shipping_ghtk", req.getParameter("shippingGhtk") != null ? "true" : "false");
                settings.put("shipping_jnt", req.getParameter("shippingJnt") != null ? "true" : "false");
                // Additional main website settings
                settings.put("default_language", req.getParameter("defaultLanguage") != null ? req.getParameter("defaultLanguage") : "");
                settings.put("default_currency", req.getParameter("defaultCurrency") != null ? req.getParameter("defaultCurrency") : "");
                settings.put("items_per_page", req.getParameter("itemsPerPage") != null ? req.getParameter("itemsPerPage") : "");
                settings.put("tax_rate", req.getParameter("taxRate") != null ? req.getParameter("taxRate") : "");
                settings.put("maintenance_mode", req.getParameter("maintenanceMode") != null ? "true" : "false");
                settings.put("site_logo_url", req.getParameter("siteLogoUrl") != null ? req.getParameter("siteLogoUrl") : "");
                settings.put("meta_description", req.getParameter("metaDescription") != null ? req.getParameter("metaDescription") : "");
                settings.put("social_facebook", req.getParameter("socialFacebook") != null ? req.getParameter("socialFacebook") : "");
                settings.put("social_instagram", req.getParameter("socialInstagram") != null ? req.getParameter("socialInstagram") : "");

                if (settingsDAO.updateMultipleSettings(settings)) {
                    log.info("Admin system settings updated: userId={}", currentUser.getId());
                    session.setAttribute("message", "Cập nhật cài đặt hệ thống thành công!");
                    session.setAttribute("messageType", "success");
                } else {
                    log.warn("Admin system settings update failed: userId={}", currentUser.getId());
                    session.setAttribute("message", "Cập nhật cài đặt hệ thống thất bại!");
                    session.setAttribute("messageType", "error");
                }
            }
        } catch (Exception e) {
            log.error("Admin settings action failed: {}", action, e);
            session.setAttribute("message", "Có lỗi xảy ra: " + e.getMessage());
            session.setAttribute("messageType", "error");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/settings");
    }
}
