package nlu.fit.web.souvenirecommerce.common.utils;

import jakarta.persistence.EntityManager;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCart;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCartItem;
import nlu.fit.web.souvenirecommerce.model.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Collection;
import java.util.Properties;

public class HibernateUtil {
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();

            applyHibernateProperties(configuration);
            addAnnotatedClasses(configuration);

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed. " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static void addAnnotatedClasses(Configuration configuration) {
        configuration.addAnnotatedClass(Category.class);
        configuration.addAnnotatedClass(OAuthAccount.class);
        configuration.addAnnotatedClass(Permission.class);
        configuration.addAnnotatedClass(Product.class);
        configuration.addAnnotatedClass(Role.class);
        configuration.addAnnotatedClass(UserSession.class);
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(UserCredential.class);
        configuration.addAnnotatedClass(VerificationCode.class);
        configuration.addAnnotatedClass(Address.class);
        configuration.addAnnotatedClass(Province.class);
        configuration.addAnnotatedClass(Ward.class);
        configuration.addAnnotatedClass(Order.class);
        configuration.addAnnotatedClass(OrderItem.class);
        configuration.addAnnotatedClass(OrderStatus.class);
        configuration.addAnnotatedClass(PaymentTransaction.class);
        configuration.addAnnotatedClass(Promotion.class);
        configuration.addAnnotatedClass(ProductSpecification.class);
        configuration.addAnnotatedClass(Review.class);
        configuration.addAnnotatedClass(NewCart.class);
        configuration.addAnnotatedClass(NewCartItem.class);
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }

    public static Collection<?> getManagedEntities(Session session) {
        return session.getManagedEntities();
    }

    private static void applyHibernateProperties(Configuration configuration) {
        Properties props = ApplicationLoader.getProperties();

        configuration.setProperty(
                "hibernate.connection.provider_class",
                "org.hibernate.hikaricp.internal.HikariCPConnectionProvider"
        );

        configuration.setProperty("hibernate.hikari.jdbcUrl", required(props, "db.url"));
        configuration.setProperty("hibernate.hikari.username", required(props, "db.username"));
        configuration.setProperty("hibernate.hikari.password", props.getProperty("db.password", ""));
        configuration.setProperty("hibernate.hikari.driverClassName", props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));

        configuration.setProperty("hibernate.hbm2ddl.auto", props.getProperty("hibernate.hbm2ddl.auto", "update"));
        configuration.setProperty("hibernate.show_sql", props.getProperty("hibernate.show_sql", "true"));
        configuration.setProperty("hibernate.format_sql", props.getProperty("hibernate.format_sql", "true"));
        configuration.setProperty("hibernate.highlight_sql", props.getProperty("hibernate.highlight_sql", "true"));
        configuration.setProperty("hibernate.current_session_context_class",
                props.getProperty("hibernate.current_session_context_class", "thread"));

        configuration.setProperty("hibernate.hikari.poolName", props.getProperty("hibernate.hikari.poolName", "SouvenirHibernatePool"));
        configuration.setProperty("hibernate.hikari.minimumIdle", props.getProperty("hibernate.hikari.minimumIdle", "2"));
        configuration.setProperty("hibernate.hikari.maximumPoolSize", props.getProperty("hibernate.hikari.maximumPoolSize", "10"));
        configuration.setProperty("hibernate.hikari.idleTimeout", props.getProperty("hibernate.hikari.idleTimeout", "30000"));
        configuration.setProperty("hibernate.hikari.connectionTimeout", props.getProperty("hibernate.hikari.connectionTimeout", "20000"));
        configuration.setProperty("hibernate.hikari.maxLifetime", props.getProperty("hibernate.hikari.maxLifetime", "1800000"));
    }

    private static String required(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value;
    }

    public static EntityManager getEntityManager() {
        return getSessionFactory().createEntityManager();
    }
}
