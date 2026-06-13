package nlu.fit.web.souvenirecommerce.features.cart.service;

import nlu.fit.web.souvenirecommerce.legacy.utils.DBContext;
import nlu.fit.web.souvenirecommerce.model.entity.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CartPriceService {

    public double getCurrentPrice(Product product) {
        if (product == null || product.getId() == null) {
            return 0;
        }

        Integer discountPercent = getBestActiveDiscountPercent(product.getId());

        if (discountPercent == null || discountPercent <= 0) {
            return product.getOriginalPrice();
        }

        return product.getOriginalPrice() * (100 - discountPercent) / 100.0;
    }

    private Integer getBestActiveDiscountPercent(Long productId) {
        String sql = """
                SELECT discount_percent
                FROM promotions
                WHERE product_id = ?
                  AND (start_date IS NULL OR start_date <= CURRENT_TIMESTAMP)
                  AND (end_date IS NULL OR end_date >= CURRENT_TIMESTAMP)
                ORDER BY discount_percent DESC
                LIMIT 1
                """;

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("discount_percent");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
