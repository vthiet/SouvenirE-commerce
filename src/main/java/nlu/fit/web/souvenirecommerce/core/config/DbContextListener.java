package nlu.fit.web.souvenirecommerce.core.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lombok.extern.slf4j.Slf4j;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import nlu.fit.web.souvenirecommerce.core.logging.ProjectLogPaths;
import nlu.fit.web.souvenirecommerce.features.shipping.service.GhnAutoSyncScheduler;
import nlu.fit.web.souvenirecommerce.features.user.service.RoleInitializationService;
import nlu.fit.web.souvenirecommerce.legacy.utils.DBContext;
import org.hibernate.Session;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

@Slf4j
public class DbContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ProjectLogPaths.ensureLogDirExists();

        String contextPath = sce.getServletContext().getContextPath();
        log.info("Application starting: contextPath={}", contextPath);
        log.info("Log directory ready: {}", ProjectLogPaths.resolveLogDir());

        SchemaMigrationRunner.runBeforeHibernate();
        HibernateUtil.getSessionFactory();
        log.info("Hibernate SessionFactory initialized");
        RoleInitializationService.initializeDefaultRoles();
        log.info("Default roles and permissions verified");

        // Verify required roles exist
        verifyRequiredRoles();
        try {
            GhnAutoSyncScheduler.start();
        } catch (Exception e) {
            log.error("Failed to start GHN auto sync scheduler", e);
        }
        log.info("Application started: contextPath={}", contextPath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        String contextPath = sce.getServletContext().getContextPath();
        log.info("Application stopping: contextPath={}", contextPath);

        GhnAutoSyncScheduler.shutdown();
        shutdownHibernate();
        shutdownLegacyJdbc();
        cleanupJdbcDrivers();
        log.info("Application stopped: contextPath={}", contextPath);
    }

    private void verifyRequiredRoles() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long customerRoleCount = session.createQuery(
                    "select count(r.id) from Role r where lower(r.name) = lower(:name)",
                    Long.class)
                    .setParameter("name", "Customer")
                    .uniqueResult();

            if (customerRoleCount == null || customerRoleCount == 0) {
                log.warn("'Customer' role not found. Please run init_auth_roles.sql");
            } else {
                log.info("'Customer' role verified");
            }
        } catch (Exception e) {
            log.error("Error verifying roles", e);
        }
    }

    private void shutdownHibernate() {
        try {
            if (HibernateUtil.getSessionFactory() != null) {
                HibernateUtil.shutdown();
                log.info("Hibernate SessionFactory destroyed");
            }
        } catch (Throwable e) {
            log.warn("Hibernate SessionFactory was not available during shutdown", e);
        }
    }

    private void shutdownLegacyJdbc() {
        try {
            DBContext.shutdown();
            log.info("JDBC HikariDataSource destroyed");
        } catch (Throwable e) {
            log.warn("JDBC HikariDataSource was not available during shutdown", e);
        }
    }

    private void cleanupJdbcDrivers() {
        try {
            AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Throwable e) {
            log.warn("Could not stop MySQL abandoned connection cleanup thread", e);
        }

        ClassLoader webappClassLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() != webappClassLoader) {
                continue;
            }
            try {
                DriverManager.deregisterDriver(driver);
                log.info("Deregistered JDBC driver {}", driver);
            } catch (SQLException e) {
                log.warn("Could not deregister JDBC driver {}", driver, e);
            }
        }
    }
}
