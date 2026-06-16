package nlu.fit.web.souvenirecommerce.features.product.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.features.product.service.CloudinaryService;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(DashboardServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String cursor = request.getParameter("cursor");

        try {
            Map result = CloudinaryService.getImages(cursor);
            request.setAttribute("data", result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.getRequestDispatcher("WEB-INF/views/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User currentUser = resolveCurrentUser(session);
        String action = request.getParameter("action");

        try {
            if ("delete".equals(action)) {
                String publicId = request.getParameter("publicId");
                if (publicId == null || publicId.isBlank()) {
                    throw new IllegalArgumentException("publicId is required");
                }
                CloudinaryService.deleteImage(publicId);
                log.info("Deleted dashboard image publicId={}", publicId);
                AuditLogService.success(
                        DashboardServlet.class,
                        currentUser,
                        "MEDIA",
                        "IMAGE_DELETED",
                        "FILE",
                        AuditLogService.describe("publicId", publicId, "source", "dashboard")
                );
            } else {
                log.warn("Unsupported dashboard action: {}", action);
                AuditLogService.failure(
                        DashboardServlet.class,
                        currentUser,
                        "MEDIA",
                        "DASHBOARD_ACTION_FAILED",
                        "FILE",
                        AuditLogService.describe("action", action, "reason", "unsupported_action")
                );
            }

        } catch (Exception e) {
            log.error("Dashboard action failed: {}", action, e);
            AuditLogService.failure(
                    DashboardServlet.class,
                    currentUser,
                    "MEDIA",
                    "IMAGE_DELETED",
                    "FILE",
                    AuditLogService.describe("action", action, "reason", e.getClass().getSimpleName(), "message", e.getMessage())
            );
        }

        response.sendRedirect("dashboard");
    }

    private User resolveCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object currentUser = session.getAttribute("user");
        if (currentUser instanceof User user) {
            return user;
        }
        currentUser = session.getAttribute("userInSession");
        if (currentUser instanceof User user) {
            return user;
        }
        currentUser = session.getAttribute("currentUser");
        return currentUser instanceof User user ? user : null;
    }
}

