package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.legacy.dao.AuthorizationDAO;
import nlu.fit.web.souvenirecommerce.legacy.dao.impl.UserDAOImpl;
import nlu.fit.web.souvenirecommerce.legacy.model.PermissionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nlu.fit.web.souvenirecommerce.core.exception.RoleExistsException;
import nlu.fit.web.souvenirecommerce.core.exception.PermissionNotFoundException;

@WebServlet("/admin/roles")
public class AdminRoleController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AdminRoleController.class);
    private AuthorizationDAO authorizationDAO;
    private UserDAOImpl userDAOImpl;

    @Override
    public void init() {
        authorizationDAO = new AuthorizationDAO();
        userDAOImpl = new UserDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("Loaded admin roles page");
        req.setAttribute("roles", authorizationDAO.getAllRoleGroups());
        req.setAttribute("permissions", authorizationDAO.getAllPermissions());
        req.setAttribute("users", userDAOImpl.getAllUsers());

        String editId = req.getParameter("editId");
        if (editId != null && !editId.isBlank()) {
            try {
                Long roleId = Long.parseLong(editId);
                PermissionGroup role = authorizationDAO.getRoleById(roleId);
                if (role != null) {
                    req.setAttribute("editRole", role);
                    req.setAttribute("editPermissionIds", authorizationDAO.getPermissionIdsByRoleId(roleId));
                    List<String> editUserIds = new ArrayList<>();
                    for (Long userId : authorizationDAO.getUserIdsByRoleId(roleId)) {
                        editUserIds.add(String.valueOf(userId));
                    }
                    req.setAttribute("editUserIds", editUserIds);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String message = (String) req.getSession().getAttribute("message");
        String messageType = (String) req.getSession().getAttribute("messageType");
        if (message != null) {
            req.setAttribute("message", message);
            req.setAttribute("messageType", messageType);
            req.getSession().removeAttribute("message");
            req.getSession().removeAttribute("messageType");
        }

        req.getRequestDispatcher("/admin/roles.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        log.debug("Admin roles action received: {}", action);

        try {
            if ("delete".equalsIgnoreCase(action)) {
                Long roleId = Long.parseLong(req.getParameter("id"));
                if (authorizationDAO.deleteRole(roleId)) {
                    log.info("Admin role deleted: roleId={}", roleId);
                    req.getSession().setAttribute("message", "Xóa nhóm quyền thành công!");
                    req.getSession().setAttribute("messageType", "success");
                } else {
                    log.warn("Admin role deletion rejected: roleId={}", roleId);
                    req.getSession().setAttribute("message", "Không thể xóa nhóm quyền hệ thống!");
                    req.getSession().setAttribute("messageType", "error");
                }
            } else if ("assignUsers".equalsIgnoreCase(action)) {
                Long roleId = Long.parseLong(req.getParameter("roleId"));
                List<Long> userIds = parseLongList(req.getParameterValues("userIds"));
                if (authorizationDAO.assignUsersToRole(roleId, userIds)) {
                    log.info("Admin role assignments updated: roleId={}, userCount={}", roleId, userIds.size());
                    req.getSession().setAttribute("message", "Cập nhật người dùng cho nhóm quyền thành công!");
                    req.getSession().setAttribute("messageType", "success");
                } else {
                    log.warn("Admin role assignments failed: roleId={}", roleId);
                    req.getSession().setAttribute("message", "Cập nhật người dùng cho nhóm quyền thất bại!");
                    req.getSession().setAttribute("messageType", "error");
                }
            } else {
                Long roleId = null;
                String idParam = req.getParameter("id");
                if (idParam != null && !idParam.isBlank()) {
                    roleId = Long.parseLong(idParam);
                }

                String name = req.getParameter("name");
                String description = req.getParameter("description");
                List<Long> permissionIds = parseLongList(req.getParameterValues("permissionIds"));

                if (authorizationDAO.saveRole(roleId, name, description, permissionIds)) {
                    log.info("Admin role saved: roleId={}, name={}", roleId, name);
                    req.getSession().setAttribute("message", roleId == null
                            ? "Tạo nhóm quyền thành công!"
                            : "Cập nhật nhóm quyền thành công!");
                    req.getSession().setAttribute("messageType", "success");
                } else {
                    log.warn("Admin role save failed: roleId={}, name={}", roleId, name);
                    req.getSession().setAttribute("message", "Không thể lưu nhóm quyền!");
                    req.getSession().setAttribute("messageType", "error");
                }
            }
        } catch (Exception e) {
            if (e instanceof RoleExistsException) {
                log.warn("Admin role validation failed: {}", e.getMessage());
                req.getSession().setAttribute("message", "Tên nhóm quyền đã tồn tại!");
                req.getSession().setAttribute("messageType", "error");
            } else if (e instanceof PermissionNotFoundException) {
                log.warn("Admin role validation failed: {}", e.getMessage());
                req.getSession().setAttribute("message", "Có quyền không hợp lệ (một hoặc nhiều permissions không tồn tại).");
                req.getSession().setAttribute("messageType", "error");
            } else {
                log.error("Admin role action failed: {}", action, e);
                req.getSession().setAttribute("message", "Có lỗi xảy ra: " + e.getMessage());
                req.getSession().setAttribute("messageType", "error");
            }
        }

        resp.sendRedirect(req.getContextPath() + "/admin/roles");
    }

    private List<Long> parseLongList(String[] values) {
        List<Long> list = new ArrayList<>();
        if (values == null) {
            return list;
        }
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            list.add(Long.parseLong(value));
        }
        return list;
    }
}
