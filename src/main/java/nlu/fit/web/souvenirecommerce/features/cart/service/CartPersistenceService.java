package nlu.fit.web.souvenirecommerce.features.cart.service;

import nlu.fit.web.souvenirecommerce.features.cart.model.Cart;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItem;
import nlu.fit.web.souvenirecommerce.legacy.dao.ProductDAO;
import nlu.fit.web.souvenirecommerce.legacy.utils.DBContext;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CartPersistenceService {

    private final ProductDAO productDAO = new ProductDAO();
    private final CartPriceService cartPriceService = new CartPriceService();

    public Cart loadCart(User user) {
        Cart cart = new Cart();

        if (user == null || user.getId() == null) {
            return cart;
        }

        ensureTable();

        String sql = """
                SELECT product_id, quantity
                FROM user_carts
                WHERE user_id = ?
                ORDER BY updated_at DESC
                """;

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, user.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Product product = productDAO.getProductById(resultSet.getLong("product_id"));
                    int quantity = resultSet.getInt("quantity");

                    if (product != null && quantity > 0) {
                        cart.addItem(product, quantity, cartPriceService.getCurrentPrice(product));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cart;
    }

    public void saveCart(User user, Cart cart) {
        if (user == null || user.getId() == null || cart == null) {
            return;
        }

        ensureTable();

        try (Connection connection = DBContext.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteStatement = connection.prepareStatement(
                    "DELETE FROM user_carts WHERE user_id = ?")) {
                deleteStatement.setLong(1, user.getId());
                deleteStatement.executeUpdate();
            }

            try (PreparedStatement insertStatement = connection.prepareStatement("""
                    INSERT INTO user_carts (user_id, product_id, quantity)
                    VALUES (?, ?, ?)
                    """)) {

                for (CartItem item : cart.getItems()) {
                    if (item.getProduct() == null || item.getProduct().getId() == null || item.getQuantity() <= 0) {
                        continue;
                    }

                    insertStatement.setLong(1, user.getId());
                    insertStatement.setLong(2, item.getProduct().getId());
                    insertStatement.setInt(3, item.getQuantity());
                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            }

            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS user_carts (
                    user_id BIGINT NOT NULL,
                    product_id BIGINT NOT NULL,
                    quantity INT NOT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, product_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
