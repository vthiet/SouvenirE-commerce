package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.features.product.dto.CategoryAdminDTO;
import nlu.fit.web.souvenirecommerce.features.product.service.ICategoryService;
import nlu.fit.web.souvenirecommerce.features.product.service.impl.CategoryServiceImpl;
import nlu.fit.web.souvenirecommerce.common.utils.GsonUtil;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/admin/categories")
public class AdminCategoryController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AdminCategoryController.class);
    private static final Gson gson = GsonUtil.getGson();

    private ICategoryService categoryService;

    @Override
    public void init() {
        categoryService = new CategoryServiceImpl();
    }

    /**
     * GET: Display category management page with all categories
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            log.info("GET /admin/categories - Loading category management page");
            
            // Fetch all categories with product counts
            var categories = categoryService.getAllCategoriesForAdmin();
            
            req.setAttribute("categories", categories);
            req.setAttribute("canCreateCategory", true);
            req.setAttribute("canUpdateCategory", true);
            req.setAttribute("canDeleteCategory", true);

            log.info("Successfully loaded {} categories", categories.size());
            req.getRequestDispatcher("/admin/categories.jsp").forward(req, resp);
        } catch (Exception e) {
            log.error("GET /admin/categories failed", e);
            req.setAttribute("message", "Lỗi tải trang: " + e.getMessage());
            req.setAttribute("messageType", "error");
            throw new RuntimeException("Failed to load category management page", e);
        }
    }

    /**
     * POST: Handle category CRUD operations (add, edit, delete)
     * Supports both form submission and AJAX JSON requests
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        
        String action = req.getParameter("action");
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("POST /admin/categories - Action: {}", action);

            if ("add".equals(action)) {
                handleAddCategory(req, resp, response);
            } else if ("edit".equals(action)) {
                handleEditCategory(req, resp, response);
            } else if ("delete".equals(action)) {
                handleDeleteCategory(req, resp, response);
            } else {
                response.put("success", false);
                response.put("message", "Unknown action");
                resp.getWriter().write(gson.toJson(response));
            }
        } catch (IllegalArgumentException e) {
            log.warn("POST /admin/categories validation error: {}", e.getMessage());
            AuditLogService.failure(
                    AdminCategoryController.class,
                    getCurrentAdminUser(req),
                    "CATEGORY",
                    "CATEGORY_ACTION_FAILED",
                    "CATEGORY",
                    AuditLogService.describe("action", action, "reason", e.getMessage())
            );
            response.put("success", false);
            response.put("message", e.getMessage());
            resp.getWriter().write(gson.toJson(response));
            throw new RuntimeException("Category operation validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("POST /admin/categories failed for action: {}", action, e);
            AuditLogService.failure(
                    AdminCategoryController.class,
                    getCurrentAdminUser(req),
                    "CATEGORY",
                    "CATEGORY_ACTION_FAILED",
                    "CATEGORY",
                    AuditLogService.describe("action", action, "reason", e.getClass().getSimpleName(), "message", e.getMessage())
            );
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            resp.getWriter().write(gson.toJson(response));
            throw new RuntimeException("Category operation failed", e);
        }
    }

    /**
     * Handle add category operation
     */
    private void handleAddCategory(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> response) throws IOException {
        String name = req.getParameter("name");
        String image = req.getParameter("imageUrl");
        User currentUser = getCurrentAdminUser(req);

        log.info("Adding new category: name={}", name);

        CategoryAdminDTO dto = new CategoryAdminDTO();
        dto.setCategoryName(name);
        dto.setImage(image);

        CategoryAdminDTO created = categoryService.createCategory(dto);
        
        response.put("success", true);
        response.put("message", "Thêm danh mục thành công!");
        response.put("data", created);
        
        resp.getWriter().write(gson.toJson(response));
        log.info("Category created successfully: id={}", created.getId());
        AuditLogService.success(
                AdminCategoryController.class,
                currentUser,
                "CATEGORY",
                "CATEGORY_CREATED",
                "CATEGORY",
                AuditLogService.describe("name", name, "image", image, "categoryId", created.getId())
        );
    }

    /**
     * Handle edit category operation
     */
    private void handleEditCategory(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> response) throws IOException {
        long id = Long.parseLong(req.getParameter("id"));
        String name = req.getParameter("name");
        String image = req.getParameter("imageUrl");
        User currentUser = getCurrentAdminUser(req);

        log.info("Updating category: id={}, name={}", id, name);

        CategoryAdminDTO dto = new CategoryAdminDTO();
        dto.setCategoryName(name);
        dto.setImage(image);

        CategoryAdminDTO updated = categoryService.updateCategory(id, dto);
        
        response.put("success", true);
        response.put("message", "Cập nhật danh mục thành công!");
        response.put("data", updated);
        
        resp.getWriter().write(gson.toJson(response));
        log.info("Category updated successfully: id={}", id);
        AuditLogService.success(
                AdminCategoryController.class,
                currentUser,
                "CATEGORY",
                "CATEGORY_UPDATED",
                "CATEGORY",
                AuditLogService.describe("categoryId", id, "name", name, "image", image)
        );
    }

    /**
     * Handle delete category operation
     */
    private void handleDeleteCategory(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> response) throws IOException {
        long id = Long.parseLong(req.getParameter("id"));
        User currentUser = getCurrentAdminUser(req);

        log.info("Deleting category: id={}", id);

        // Check if can delete
        if (!categoryService.canDeleteCategory(id)) {
            throw new IllegalArgumentException("Không thể xóa danh mục đang có sản phẩm");
        }

        categoryService.deleteCategory(id);
        
        response.put("success", true);
        response.put("message", "Xóa danh mục thành công!");
        
        resp.getWriter().write(gson.toJson(response));
        log.info("Category deleted successfully: id={}", id);
        AuditLogService.success(
                AdminCategoryController.class,
                currentUser,
                "CATEGORY",
                "CATEGORY_DELETED",
                "CATEGORY",
                AuditLogService.describe("categoryId", id)
        );
    }

    private User getCurrentAdminUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("user");
        if (user instanceof User) {
            return (User) user;
        }
        user = session.getAttribute("userInSession");
        if (user instanceof User) {
            return (User) user;
        }
        user = session.getAttribute("currentUser");
        return user instanceof User ? (User) user : null;
    }
}
