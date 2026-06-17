package nlu.fit.web.souvenirecommerce.features.user.service;

import nlu.fit.web.souvenirecommerce.legacy.utils.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 * Service to initialize default roles and permissions on application startup.
 * Creates the default roles and keeps their permissions in sync.
 */
public class RoleInitializationService {
    private static final Logger logger = Logger.getLogger(RoleInitializationService.class.getName());

    public static void initializeDefaultRoles() {
        try {
            logger.info("Initializing default roles and permissions...");
            ensureBasePermissions();

            if (!rolesExist()) {
                createRoles();
                logger.info("Default roles created.");
            }

            assignPermissions();
            logger.info("Default roles and permissions initialized successfully!");
        } catch (Exception e) {
            logger.severe("Error initializing default roles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean rolesExist() {
        String sql = "SELECT COUNT(*) as count FROM roles WHERE is_system = TRUE";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count") >= 4;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void createRoles() throws Exception {
        String sql = """
                INSERT IGNORE INTO roles (name, description, is_system)
                VALUES
                    ('Super Admin', 'Full system access including role management', TRUE),
                    ('Admin', 'Administrative access to all features', TRUE),
                    ('Sales', 'Sales management access (products, orders, customers)', TRUE),
                    ('User', 'Basic user access', TRUE),
                    ('Customer', 'Default customer account', TRUE)
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            logger.info("Default roles created successfully.");
        }
    }

    private static void assignPermissions() throws Exception {
        // Super Admin - All permissions
        assignAllPermissionsToRole("Super Admin");

        // Admin - All except role management
        assignPermissionsByResourceToRole("Admin",
                "dashboard", "product", "category", "order", "customer", "review", "banner", "settings", "log");

        // Sales - Product, Order, Customer, Review, Dashboard (read/update only)
        assignLimitedPermissionsToRole("Sales",
                new String[]{"dashboard", "product", "order", "customer", "review"},
                new String[]{"read", "update"});

        // User - Dashboard read only
        assignSpecificPermissionToRole("User", "dashboard", "read");

        logger.info("Permissions assigned to roles successfully.");
    }

    private static void ensureBasePermissions() throws Exception {
        ensurePermission("dashboard", "read", "View admin dashboard");
        ensurePermission("log", "read", "View admin logs");
        ensurePermission("review", "read", "View product reviews");
        ensurePermission("review", "delete", "Delete product reviews");
    }

    private static void ensurePermission(String resource, String action, String description) throws Exception {
        String sql = """
                INSERT INTO permissions (resource, action, description)
                SELECT ?, ?, ?
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM permissions
                    WHERE resource = ? AND action = ?
                )
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resource);
            ps.setString(2, action);
            ps.setString(3, description);
            ps.setString(4, resource);
            ps.setString(5, action);
            ps.executeUpdate();
        }
    }

    private static void assignAllPermissionsToRole(String roleName) throws Exception {
        String sql = """
                INSERT IGNORE INTO role_permissions (role_id, permission_id)
                SELECT r.id, p.id
                FROM roles r
                CROSS JOIN permissions p
                WHERE r.name = ? AND p.deleted_at IS NULL
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName);
            ps.executeUpdate();
        }
    }

    private static void assignPermissionsByResourceToRole(String roleName, String... resources) throws Exception {
        if (resources.length == 0) {
            return;
        }

        StringBuilder sql = new StringBuilder("""
                INSERT IGNORE INTO role_permissions (role_id, permission_id)
                SELECT r.id, p.id
                FROM roles r
                JOIN permissions p ON 1=1
                WHERE r.name = ? AND p.deleted_at IS NULL AND p.resource IN (""");

        for (int i = 0; i < resources.length; i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, roleName);
            for (int i = 0; i < resources.length; i++) {
                ps.setString(i + 2, resources[i]);
            }
            ps.executeUpdate();
        }
    }

    private static void assignLimitedPermissionsToRole(String roleName, String[] resources, String[] actions) throws Exception {
        StringBuilder sql = new StringBuilder("""
                INSERT IGNORE INTO role_permissions (role_id, permission_id)
                SELECT r.id, p.id
                FROM roles r
                JOIN permissions p ON 1=1
                WHERE r.name = ? AND p.deleted_at IS NULL AND p.resource IN (""");

        for (int i = 0; i < resources.length; i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(") AND p.action IN (");

        for (int i = 0; i < actions.length; i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            ps.setString(paramIndex++, roleName);

            for (String resource : resources) {
                ps.setString(paramIndex++, resource);
            }
            for (String action : actions) {
                ps.setString(paramIndex++, action);
            }

            ps.executeUpdate();
        }
    }

    private static void assignSpecificPermissionToRole(String roleName, String resource, String action) throws Exception {
        String sql = """
                INSERT IGNORE INTO role_permissions (role_id, permission_id)
                SELECT r.id, p.id
                FROM roles r
                JOIN permissions p ON 1=1
                WHERE r.name = ? AND p.deleted_at IS NULL AND p.resource = ? AND p.action = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName);
            ps.setString(2, resource);
            ps.setString(3, action);
            ps.executeUpdate();
        }
    }
}
