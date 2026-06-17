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
            ensureProductLocalizationColumns(connection, statement);
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

    private static void ensureProductLocalizationColumns(Connection connection, Statement statement) throws SQLException {
        if (!tableExists(connection, "products")) {
            return;
        }

        java.util.List<String> alterations = new java.util.ArrayList<>();

        if (!columnExists(connection, "products", "name_en")) {
            alterations.add("ADD COLUMN name_en VARCHAR(255) NULL AFTER name");
        }
        if (!columnExists(connection, "products", "description_en")) {
            alterations.add("ADD COLUMN description_en TEXT NULL AFTER description");
        }
        if (!columnExists(connection, "products", "short_description_en")) {
            alterations.add("ADD COLUMN short_description_en TEXT NULL AFTER short_description");
        }

        if (!alterations.isEmpty()) {
            statement.executeUpdate("ALTER TABLE products " + String.join(", ", alterations));
            log.info("Added localized product columns: {}", String.join(", ", alterations));
        }

        statement.executeUpdate("ALTER TABLE products MODIFY COLUMN description_en TEXT NULL");
        statement.executeUpdate("ALTER TABLE products MODIFY COLUMN short_description_en TEXT NULL");
        log.info("Normalized localized product text columns to TEXT");
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
