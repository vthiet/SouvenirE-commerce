package nlu.fit.web.souvenirecommerce.core.config;

import lombok.extern.slf4j.Slf4j;
import nlu.fit.web.souvenirecommerce.common.utils.ApplicationLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

@Slf4j
public final class SchemaMigrationRunner {

    private SchemaMigrationRunner() {
    }

    public static void runBeforeHibernate() {
        Properties props = ApplicationLoader.getProperties();
        String url = required(props, "db.url");
        String username = required(props, "db.username");
        String password = props.getProperty("db.password", "");

        try {
            Class.forName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Database driver not found", e);
        }

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            migrateUserPasswordColumn(connection, statement);
            ensureUniqueUserPhone(connection, statement);
            migrateShippingOrders(connection, statement);
            ensureInnoDBStorageEngine(connection, statement);
        } catch (SQLException e) {
            throw new IllegalStateException("Database schema migration failed", e);
        }
    }

    private static void migrateUserPasswordColumn(Connection connection, Statement statement) throws SQLException {
        if (!tableExists(connection, "users") || !columnExists(connection, "users", "password")) {
            return;
        }

        if (tableExists(connection, "user_credentials")) {
            int migrated = statement.executeUpdate("""
                    insert into user_credentials (user_id, password_hash, email_verified, created_at, updated_at)
                    select u.id, u.password, true, current_timestamp(6), current_timestamp(6)
                    from users u
                    left join user_credentials uc on uc.user_id = u.id
                    where uc.user_id is null
                      and u.password is not null
                      and u.password <> ''
                    """);
            log.info("Migrated {} legacy users.password values to user_credentials.password_hash", migrated);
        }

        statement.executeUpdate("alter table users drop column password");
        log.info("Dropped legacy users.password column");
    }

    /**
     * Migrates the GHN-specific columns out of the orders/addresses tables into
     * a dedicated shipping_orders table, making the model provider-agnostic.
     * Safe to run multiple times (idempotent per step).
     */
    private static void migrateShippingOrders(Connection connection, Statement statement) throws SQLException {
        // 1. Create shipping_orders table if not exists
        if (!tableExists(connection, "shipping_orders")) {
            statement.executeUpdate("""
                    CREATE TABLE shipping_orders (
                        id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
                        order_id           BIGINT NOT NULL,
                        carrier_code       VARCHAR(50) NOT NULL,
                        tracking_code      VARCHAR(100),
                        status             VARCHAR(50),
                        leadtime           DATETIME,
                        finish_date        DATETIME,
                        carrier_updated_at DATETIME,
                        created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_so_order FOREIGN KEY (order_id) REFERENCES orders(id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            log.info("Created table shipping_orders");
        }

        // 2. Migrate existing ghn_order_code data into shipping_orders (only if old columns still exist)
        boolean hasGhnOrderCode = columnExists(connection, "orders", "ghn_order_code");
        if (hasGhnOrderCode) {
            int migrated = statement.executeUpdate("""
                    INSERT INTO shipping_orders
                        (order_id, carrier_code, tracking_code, status, leadtime, finish_date, carrier_updated_at, created_at)
                    SELECT o.id, 'GHN', o.ghn_order_code, o.ghn_status,
                           o.ghn_leadtime, o.ghn_finish_date, o.ghn_updated_at, NOW()
                    FROM orders o
                    WHERE o.ghn_order_code IS NOT NULL
                      AND o.ghn_order_code <> ''
                      AND NOT EXISTS (
                          SELECT 1 FROM shipping_orders so WHERE so.order_id = o.id
                      )
                    """);
            log.info("Migrated {} GHN shipping records into shipping_orders", migrated);

            // 3. Drop ghn_* columns from orders
            statement.executeUpdate("""
                    ALTER TABLE orders
                        DROP COLUMN ghn_order_code,
                        DROP COLUMN ghn_status,
                        DROP COLUMN ghn_updated_at,
                        DROP COLUMN ghn_leadtime,
                        DROP COLUMN ghn_finish_date
                    """);
            log.info("Dropped ghn_* columns from orders table");
        }

        // 4. Rename ghn_* columns in addresses → carrier_*
        if (columnExists(connection, "addresses", "ghn_province_id")) {
            statement.executeUpdate("ALTER TABLE addresses RENAME COLUMN ghn_province_id TO carrier_province_id");
            log.info("Renamed addresses.ghn_province_id → carrier_province_id");
        }
        if (columnExists(connection, "addresses", "ghn_district_id")) {
            statement.executeUpdate("ALTER TABLE addresses RENAME COLUMN ghn_district_id TO carrier_district_id");
            log.info("Renamed addresses.ghn_district_id → carrier_district_id");
        }
        if (columnExists(connection, "addresses", "ghn_ward_code")) {
            statement.executeUpdate("ALTER TABLE addresses RENAME COLUMN ghn_ward_code TO carrier_ward_code");
            log.info("Renamed addresses.ghn_ward_code → carrier_ward_code");
        }

        // 5. Add preferred_carrier_code to orders if not exists
        if (!columnExists(connection, "orders", "preferred_carrier_code")) {
            statement.executeUpdate("ALTER TABLE orders ADD COLUMN preferred_carrier_code VARCHAR(50)");
            log.info("Added orders.preferred_carrier_code column");
        }
    }

    private static void ensureUniqueUserPhone(Connection connection, Statement statement) throws SQLException {
        if (!tableExists(connection, "users") || !columnExists(connection, "users", "phone")) {
            return;
        }
        if (indexExists(connection, "users", "uk_users_phone")) {
            return;
        }
        if (hasDuplicateUserPhones(connection)) {
            log.warn("Cannot create unique index users.phone because duplicate phone numbers exist. Clean duplicate users.phone values first.");
            return;
        }

        statement.executeUpdate("create unique index uk_users_phone on users (phone)");
        log.info("Created unique index uk_users_phone on users.phone");
    }

    private static boolean hasDuplicateUserPhones(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             var resultSet = statement.executeQuery("""
                     select phone
                     from users
                     where phone is not null and phone <> ''
                     group by phone
                     having count(*) > 1
                     limit 1
                     """)) {
            return resultSet.next();
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (var resultSet = connection.getMetaData().getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        try (var resultSet = connection.getMetaData().getColumns(connection.getCatalog(), null, tableName, columnName)) {
            return resultSet.next();
        }
    }

    private static boolean indexExists(Connection connection, String tableName, String indexName) throws SQLException {
        try (var resultSet = connection.getMetaData().getIndexInfo(connection.getCatalog(), null, tableName, false, false)) {
            while (resultSet.next()) {
                if (indexName.equalsIgnoreCase(resultSet.getString("INDEX_NAME"))) {
                    return true;
                }
            }
            return false;
        }
    }

    private static void ensureInnoDBStorageEngine(Connection connection, Statement statement) throws SQLException {
        String dbName = connection.getCatalog();
        String query = "SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA = '" + dbName + "'";
        java.util.List<String> tablesToAlter = new java.util.ArrayList<>();
        try (var rs = statement.executeQuery(query)) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                String engine = rs.getString("ENGINE");
                if (engine != null && !engine.equalsIgnoreCase("InnoDB")) {
                    tablesToAlter.add(tableName);
                }
            }
        }

        for (String tableName : tablesToAlter) {
            log.info("Altering table {} storage engine to InnoDB ROW_FORMAT=DEFAULT", tableName);
            statement.executeUpdate("ALTER TABLE `" + tableName + "` ENGINE=InnoDB ROW_FORMAT=DEFAULT");
        }
    }

    private static String required(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value;
    }
}
