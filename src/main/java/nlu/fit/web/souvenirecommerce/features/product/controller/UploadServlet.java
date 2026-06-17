package nlu.fit.web.souvenirecommerce.features.product.controller;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.features.product.service.CloudinaryService;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;

@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User currentUser = resolveCurrentUser(session);

        try {
            Part filePart = request.getPart("file");
            byte[] fileBytes = filePart.getInputStream().readAllBytes();

            CloudinaryService.uploadImage(fileBytes, "avatar");
            AuditLogService.success(
                    UploadServlet.class,
                    currentUser,
                    "MEDIA",
                    "AVATAR_UPLOADED",
                    "FILE",
                    AuditLogService.describe("folder", "avatar", "sizeBytes", fileBytes.length)
            );

        } catch (Exception e) {
            AuditLogService.failure(
                    UploadServlet.class,
                    currentUser,
                    "MEDIA",
                    "AVATAR_UPLOADED",
                    "FILE",
                    AuditLogService.describe("folder", "avatar", "reason", e.getClass().getSimpleName(), "message", e.getMessage())
            );
        }

        response.sendRedirect("dashboard");
    }

    private User resolveCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            currentUser = (User) session.getAttribute("userInSession");
        }
        if (currentUser == null) {
            currentUser = (User) session.getAttribute("currentUser");
        }
        return currentUser;
    }
}
