package nlu.fit.web.souvenirecommerce.features.banner.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.features.banner.service.BannerService;
import nlu.fit.web.souvenirecommerce.model.entity.Banner;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@WebServlet("/admin/banner")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class BannerController extends HttpServlet {
    private static final String UPLOAD_DIR = "assets/images/home_banner";
    private static final String DEFAULT_IMAGE_URL = "assets/images/home_banner/default.jpg";

    private BannerService bannerService;

    @Override
    public void init() throws ServletException {
        bannerService = new BannerService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Banner> banners = bannerService.findAll();
        req.setAttribute("banners", banners);
        req.getRequestDispatcher("/admin/banners.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        User currentUser = resolveCurrentUser(req);
        String action = req.getParameter("action");

        try {
            if ("add".equalsIgnoreCase(action)) {
                handleAdd(req, currentUser);
            } else if ("update".equalsIgnoreCase(action)) {
                handleUpdate(req, currentUser);
            } else if ("delete".equalsIgnoreCase(action)) {
                handleDelete(req, currentUser);
            } else if ("toggle".equalsIgnoreCase(action)) {
                handleToggle(req, currentUser);
            } else {
                AuditLogService.failure(
                        BannerController.class,
                        currentUser,
                        "ADMIN",
                        "BANNER_MANAGED",
                        "BANNER",
                        AuditLogService.describe("action", action, "reason", "unsupported_action")
                );
            }
        } catch (NumberFormatException e) {
            AuditLogService.failure(
                    BannerController.class,
                    currentUser,
                    "ADMIN",
                    "BANNER_MANAGED",
                    "BANNER",
                    AuditLogService.describe("action", action, "reason", "invalid_id", "message", e.getMessage())
            );
        } catch (Exception e) {
            AuditLogService.failure(
                    BannerController.class,
                    currentUser,
                    "ADMIN",
                    "BANNER_MANAGED",
                    "BANNER",
                    AuditLogService.describe("action", action, "reason", e.getClass().getSimpleName(), "message", e.getMessage())
            );
        }

        resp.sendRedirect(req.getContextPath() + "/admin/banner");
    }

    private void handleAdd(HttpServletRequest req, User currentUser) throws IOException, ServletException {
        String title = normalize(req.getParameter("title"));
        int position = parseInt(req.getParameter("position"), 1);
        boolean status = Boolean.parseBoolean(req.getParameter("status"));
        String imageUrl = saveFileUpload(req, "imageFile");
        if (imageUrl == null) {
            imageUrl = DEFAULT_IMAGE_URL;
        }

        Banner newBanner = Banner.builder()
                .imageUrl(imageUrl)
                .title(title)
                .position(position)
                .status(status)
                .build();

        Optional<Banner> saved = bannerService.save(newBanner);
        if (saved.isPresent()) {
            auditSuccess(
                    currentUser,
                    "BANNER_CREATED",
                    AuditLogService.describe(
                            "title", title,
                            "position", position,
                            "status", status,
                            "imageUrl", imageUrl
                    )
            );
        } else {
            auditFailure(
                    currentUser,
                    "BANNER_CREATED",
                    AuditLogService.describe("title", title, "reason", "save_failed")
            );
        }
    }

    private void handleUpdate(HttpServletRequest req, User currentUser) throws IOException, ServletException {
        long id = Long.parseLong(req.getParameter("id"));
        Banner banner = bannerService.findById(id).orElse(null);
        if (banner == null) {
            auditFailure(
                    currentUser,
                    "BANNER_UPDATED",
                    AuditLogService.describe("bannerId", id, "reason", "not_found")
            );
            return;
        }

        String title = normalize(req.getParameter("title"));
        banner.setTitle(title);
        banner.setPosition(parseInt(req.getParameter("position"), banner.getPosition()));
        banner.setStatus(Boolean.parseBoolean(req.getParameter("status")));

        String newImage = saveFileUpload(req, "imageFile");
        boolean imageChanged = false;
        if (newImage != null) {
            banner.setImageUrl(newImage);
            imageChanged = true;
        }

        Optional<Banner> updated = bannerService.update(banner);
        if (updated.isPresent()) {
            auditSuccess(
                    currentUser,
                    "BANNER_UPDATED",
                    AuditLogService.describe(
                            "bannerId", id,
                            "title", title,
                            "position", banner.getPosition(),
                            "status", banner.isStatus(),
                            "imageChanged", imageChanged,
                            "imageUrl", banner.getImageUrl()
                    )
            );
        } else {
            auditFailure(
                    currentUser,
                    "BANNER_UPDATED",
                    AuditLogService.describe("bannerId", id, "reason", "update_failed")
            );
        }
    }

    private void handleDelete(HttpServletRequest req, User currentUser) {
        long id = Long.parseLong(req.getParameter("id"));
        Banner banner = bannerService.findById(id).orElse(null);
        if (banner == null) {
            auditFailure(
                    currentUser,
                    "BANNER_DELETED",
                    AuditLogService.describe("bannerId", id, "reason", "not_found")
            );
            return;
        }

        bannerService.delete(id);
        auditSuccess(
                currentUser,
                "BANNER_DELETED",
                AuditLogService.describe("bannerId", id, "title", banner.getTitle())
        );
    }

    private void handleToggle(HttpServletRequest req, User currentUser) {
        long id = Long.parseLong(req.getParameter("id"));
        Banner banner = bannerService.findById(id).orElse(null);
        if (banner == null) {
            auditFailure(
                    currentUser,
                    "BANNER_TOGGLED",
                    AuditLogService.describe("bannerId", id, "reason", "not_found")
            );
            return;
        }

        banner.setStatus(!banner.isStatus());
        bannerService.update(banner);
        auditSuccess(
                currentUser,
                "BANNER_TOGGLED",
                AuditLogService.describe("bannerId", id, "status", banner.isStatus())
        );
    }

    private String saveFileUpload(HttpServletRequest req, String partName) {
        try {
            Part filePart = req.getPart(partName);
            if (filePart != null
                    && filePart.getSize() > 0
                    && filePart.getSubmittedFileName() != null
                    && !filePart.getSubmittedFileName().isBlank()) {
                String fileName = System.currentTimeMillis() + "_" + Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

                String applicationPath = req.getServletContext().getRealPath("");
                if (applicationPath == null || applicationPath.isBlank()) {
                    return null;
                }

                File uploadDir = new File(applicationPath, UPLOAD_DIR);
                if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                    return null;
                }

                File targetFile = new File(uploadDir, fileName);
                filePart.write(targetFile.getAbsolutePath());
                return UPLOAD_DIR + "/" + fileName;
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private User resolveCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
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

    private void auditSuccess(User currentUser, String action, String details) {
        AuditLogService.success(BannerController.class, currentUser, "ADMIN", action, "BANNER", details);
    }

    private void auditFailure(User currentUser, String action, String details) {
        AuditLogService.failure(BannerController.class, currentUser, "ADMIN", action, "BANNER", details);
    }
}
