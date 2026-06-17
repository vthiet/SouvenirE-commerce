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
            ensurePurchaseOrdersSchema(connection, statement);
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

    private static void ensurePurchaseOrdersSchema(Connection connection, Statement statement) throws SQLException {
        ensurePurchaseOrdersTable(statement);
        ensurePurchaseOrderItemsTable(statement);
        ensurePurchaseOrdersColumns(connection, statement);
        migrateLegacyPurchaseOrderItems(connection, statement);
    }

    private static void ensurePurchaseOrdersTable(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `purchase_orders` (
                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                  `po_code` VARCHAR(40) NOT NULL,
                  `supplier_name` VARCHAR(255) NOT NULL,
                  `supplier_tax_code` VARCHAR(50) NOT NULL,
                  `supplier_address` VARCHAR(255) NOT NULL,
                  `supplier_phone` VARCHAR(32) DEFAULT NULL,
                  `supplier_email` VARCHAR(255) DEFAULT NULL,
                  `invoice_number` VARCHAR(100) NOT NULL,
                  `invoice_date` DATE NOT NULL,
                  `contract_number` VARCHAR(100) DEFAULT NULL,
                  `delivery_note_number` VARCHAR(100) DEFAULT NULL,
                  `received_by` VARCHAR(255) NOT NULL,
                  `status` VARCHAR(20) NOT NULL DEFAULT 'FINALIZED',
                  `item_count` INT NOT NULL DEFAULT 0,
                  `quantity` INT NOT NULL,
                  `unit_cost` DECIMAL(15,2) NOT NULL,
                  `subtotal_amount` DECIMAL(15,2) NOT NULL,
                  `vat_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
                  `vat_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                  `total_amount` DECIMAL(15,2) NOT NULL,
                  `product_id` BIGINT DEFAULT NULL,
                  `product_name` VARCHAR(255) DEFAULT NULL,
                  `stock_before` INT DEFAULT NULL,
                  `stock_after` INT DEFAULT NULL,
                  `notes` TEXT NULL,
                  `created_by` BIGINT DEFAULT NULL,
                  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  PRIMARY KEY (`id`),
                  UNIQUE KEY `uk_purchase_orders_po_code` (`po_code`),
                  KEY `idx_purchase_orders_product_id` (`product_id`),
                  KEY `idx_purchase_orders_invoice_number` (`invoice_number`),
                  KEY `idx_purchase_orders_supplier_tax_code` (`supplier_tax_code`),
                  KEY `idx_purchase_orders_created_by` (`created_by`),
                  KEY `idx_purchase_orders_created_at` (`created_at`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """);
    }

    private static void ensurePurchaseOrderItemsTable(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `purchase_order_items` (
                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                  `purchase_order_id` BIGINT NOT NULL,
                  `product_id` BIGINT NOT NULL,
                  `product_name` VARCHAR(255) NOT NULL,
                  `quantity` INT NOT NULL,
                  `unit_cost` DECIMAL(15,2) NOT NULL,
                  `line_amount` DECIMAL(15,2) NOT NULL,
                  `stock_before` INT NOT NULL,
                  `stock_after` INT NOT NULL,
                  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  PRIMARY KEY (`id`),
                  UNIQUE KEY `uk_purchase_order_items_order_product` (`purchase_order_id`, `product_id`),
                  KEY `idx_purchase_order_items_product_id` (`product_id`),
                  CONSTRAINT `fk_purchase_order_items_purchase_order`
                      FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_orders` (`id`)
                      ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """);
    }

    private static void ensurePurchaseOrdersColumns(Connection connection, Statement statement) throws SQLException {
        ensureColumn(statement, connection, "purchase_orders", "status", "VARCHAR(20) NOT NULL DEFAULT 'FINALIZED'");
        ensureColumn(statement, connection, "purchase_orders", "item_count", "INT NOT NULL DEFAULT 0");
        ensureNullableColumn(statement, connection, "purchase_orders", "product_id", "BIGINT");
        ensureNullableColumn(statement, connection, "purchase_orders", "product_name", "VARCHAR(255)");
        ensureNullableColumn(statement, connection, "purchase_orders", "stock_before", "INT");
        ensureNullableColumn(statement, connection, "purchase_orders", "stock_after", "INT");
        statement.executeUpdate("""
                UPDATE purchase_orders
                SET status = 'FINALIZED'
                WHERE status IS NULL OR TRIM(status) = ''
                """);
    }

    private static void migrateLegacyPurchaseOrderItems(Connection connection, Statement statement) throws SQLException {
        if (!tableExists(connection, "purchase_order_items")) {
            return;
        }
        if (!tableExists(connection, "purchase_orders") || !columnExists(connection, "purchase_orders", "product_id")) {
            return;
        }

        int migrated = statement.executeUpdate("""
                INSERT IGNORE INTO purchase_order_items (
                    purchase_order_id,
                    product_id,
                    product_name,
                    quantity,
                    unit_cost,
                    line_amount,
                    stock_before,
                    stock_after
                )
                SELECT
                    po.id,
                    po.product_id,
                    COALESCE(NULLIF(po.product_name, ''), COALESCE(NULLIF(p.name, ''), 'Sản phẩm')),
                    po.quantity,
                    po.unit_cost,
                    ROUND(COALESCE(po.quantity, 0) * COALESCE(po.unit_cost, 0), 2),
                    po.stock_before,
                    po.stock_after
                FROM purchase_orders po
                LEFT JOIN products p ON p.id = po.product_id
                WHERE po.product_id IS NOT NULL
                  AND po.quantity IS NOT NULL
                  AND po.unit_cost IS NOT NULL
                """);
        if (migrated > 0) {
            log.info("Migrated {} legacy purchase order items into purchase_order_items", migrated);
        }

        int normalized = statement.executeUpdate("""
                UPDATE purchase_orders
                SET item_count = CASE WHEN item_count IS NULL OR item_count = 0 THEN 1 ELSE item_count END,
                    product_id = NULL,
                    product_name = NULL,
                    stock_before = NULL,
                    stock_after = NULL
                WHERE item_count IS NULL
                   OR item_count = 0
                   OR product_id IS NOT NULL
                   OR product_name IS NOT NULL
                   OR stock_before IS NOT NULL
                   OR stock_after IS NOT NULL
                """);
        if (normalized > 0) {
            log.info("Normalized {} legacy purchase order header rows", normalized);
        }
    }

    private static void ensureColumn(Statement statement,
                                     Connection connection,
                                     String tableName,
                                     String columnName,
                                     String columnDefinition) throws SQLException {
        if (columnExists(connection, tableName, columnName)) {
            statement.executeUpdate("ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + columnName + "` " + columnDefinition);
        } else {
            statement.executeUpdate("ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + columnDefinition);
        }
    }

    private static void ensureNullableColumn(Statement statement,
                                             Connection connection,
                                             String tableName,
                                             String columnName,
                                             String columnType) throws SQLException {
        if (columnExists(connection, tableName, columnName)) {
            statement.executeUpdate("ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + columnName + "` " + columnType + " NULL");
        } else {
            statement.executeUpdate("ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + columnType + " NULL");
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
