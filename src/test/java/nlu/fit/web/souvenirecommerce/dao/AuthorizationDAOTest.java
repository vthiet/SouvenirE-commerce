// package nlu.fit.web.souvenirecommerce.dao;

// import nlu.fit.web.souvenirecommerce.legacy.dao.AuthorizationDAO;
// import nlu.fit.web.souvenirecommerce.legacy.utils.DBContext;
// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Test;

// import java.sql.Connection;
// import java.sql.Statement;
// import java.util.Arrays;
// import java.util.Collections;

// public class AuthorizationDAOTest {
//     private static AuthorizationDAO authorizationDAO;

//     @BeforeAll
//     public static void setup() throws Exception {
//         // initialize in-memory schema
//         try (Connection conn = DBContext.getConnection(); Statement st = conn.createStatement()) {
//             st.execute("CREATE TABLE roles (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL UNIQUE, description VARCHAR(255), is_system BOOLEAN DEFAULT FALSE);");
//             st.execute("CREATE TABLE permissions (id INT AUTO_INCREMENT PRIMARY KEY, resource VARCHAR(50) NOT NULL, action VARCHAR(30) NOT NULL, description VARCHAR(255));");
//             st.execute("CREATE TABLE role_permissions (role_id INT NOT NULL, permission_id INT NOT NULL, PRIMARY KEY (role_id, permission_id));");

//             // insert a permission
//             st.execute("INSERT INTO permissions (resource, action, description) VALUES ('product','create','Create products');");
//             st.execute("INSERT INTO permissions (resource, action, description) VALUES ('product','read','Read products');");
//         }
//         authorizationDAO = new AuthorizationDAO();
//     }

//     @AfterAll
//     public static void teardown() {
//         DBContext.shutdown();
//     }
// }
