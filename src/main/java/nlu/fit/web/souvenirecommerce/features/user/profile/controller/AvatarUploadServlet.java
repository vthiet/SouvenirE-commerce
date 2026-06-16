package nlu.fit.web.souvenirecommerce.features.user.profile.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.features.product.service.CloudinaryService;
import nlu.fit.web.souvenirecommerce.features.user.profile.service.ProfileService;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@WebServlet("/user/upload-avatar")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 10
)
public class AvatarUploadServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AvatarUploadServlet.class);
    private ProfileService profileService;

    @Override
    public void init() throws ServletException {
        profileService = new ProfileService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            if (expectsJson(req)) {
                writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "{\"error\":\"Unauthorized\"}");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (!CloudinaryService.isConfigured()) {
            handleError(req, resp, session, "Hệ thống chưa được cấu hình để tải hình ảnh");
            return;
        }

        try {
            Part filePart = getAvatarPart(req);

            if (filePart == null || filePart.getSize() == 0) {
                handleError(req, resp, session, "Vui lòng chọn một file ảnh");
                return;
            }

            if (filePart.getSize() > 5 * 1024 * 1024) {
                handleError(req, resp, session, "Hình ảnh không được vượt quá 5MB");
                return;
            }

            String oldPublicId = currentUser.getAvatarPublicId();
            Map<String, String> uploadResult = CloudinaryService.uploadAvatar(filePart, currentUser.getId());
            String avatarUrl = uploadResult.get("url");
            String publicId = uploadResult.get("public_id");

            currentUser.setAvatarUrl(avatarUrl);
            currentUser.setAvatarPublicId(publicId);

            Optional<User> updatedUser = profileService.update(currentUser);
            User savedUser = updatedUser.orElse(currentUser);
            updateUserSession(session, savedUser);
            AuditLogService.success(
                    AvatarUploadServlet.class,
                    savedUser,
                    "ACCOUNT",
                    "AVATAR_CHANGED",
                    "PROFILE",
                    AuditLogService.describe("publicId", publicId, "source", "avatar-upload")
            );

            if (oldPublicId != null && !oldPublicId.isBlank() && !oldPublicId.equals(publicId)) {
                try {
                    CloudinaryService.deleteImage(oldPublicId);
                } catch (Exception e) {
                    log.warn("Xóa avatar cũ thất bại. publicId={}", oldPublicId, e);
                }
            }

            setProfileMessage(session, "Cập nhật ảnh đại diện thành công!", "success");
            if (expectsJson(req)) {
                writeJson(resp, HttpServletResponse.SC_OK, "{\"success\":true,\"avatarUrl\":\"" + escapeJson(avatarUrl) + "\"}");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/user/profile");

        } catch (IllegalArgumentException e) {
            AuditLogService.failure(
                    AvatarUploadServlet.class,
                    currentUser,
                    "ACCOUNT",
                    "AVATAR_CHANGE_FAILED",
                    "PROFILE",
                    AuditLogService.describe("reason", e.getMessage())
            );
            handleError(req, resp, session, e.getMessage());

        } catch (Exception e) {
            log.error("Cập nhật avatar thất bại cho người dùng có ID: {}", currentUser.getId(), e);
            AuditLogService.failure(
                    AvatarUploadServlet.class,
                    currentUser,
                    "ACCOUNT",
                    "AVATAR_CHANGE_FAILED",
                    "PROFILE",
                    AuditLogService.describe("reason", e.getClass().getSimpleName(), "message", e.getMessage())
            );
            handleError(req, resp, session, "Có lỗi xảy ra trong quá trình tải ảnh đại diện");
        }
    }

    private Part getAvatarPart(HttpServletRequest req) throws IOException, ServletException {
        Part filePart = req.getPart("avatarFile");
        if (filePart == null) {
            filePart = req.getPart("avatar");
        }
        return filePart;
    }

    private User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("user");
        if (user instanceof User) {
            return (User) user;
        }

        user = session.getAttribute("currentUser");
        if (user instanceof User) {
            return (User) user;
        }

        user = session.getAttribute("userInSession");
        if (user instanceof User) {
            return (User) user;
        }

        user = session.getAttribute("authUser");
        if (user instanceof User) {
            return (User) user;
        }

        user = session.getAttribute("userDto");
        return user instanceof User ? (User) user : null;
    }

    private void updateUserSession(HttpSession session, User user) {
        session.setAttribute("currentUser", user);
        session.setAttribute("userInSession", user);
        session.setAttribute("user", user);
        session.setAttribute("authUser", user);
        session.setAttribute("userDto", user);
    }

    private void setProfileMessage(HttpSession session, String message, String type) {
        session.setAttribute("profileMessage", message);
        session.setAttribute("profileMessageType", type);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp, HttpSession session, String message) throws IOException {
        setProfileMessage(session, message, "error");
        if (expectsJson(req)) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"" + escapeJson(message) + "\"}");
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/user/profile");
    }

    private boolean expectsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String requestedWith = req.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith) || (accept != null && accept.contains("application/json"));
    }

    private void writeJson(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(json);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
