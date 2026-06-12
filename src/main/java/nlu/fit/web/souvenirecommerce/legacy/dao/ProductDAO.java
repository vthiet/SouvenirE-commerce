package nlu.fit.web.souvenirecommerce.legacy.dao;

import nlu.fit.web.souvenirecommerce.model.enums.ProductSort;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.CategorySalesDTO;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.Category;
import nlu.fit.web.souvenirecommerce.legacy.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private static final String SEARCH_SELECT = """
    SELECT
        p.id,
        p.category_id,
        p.name,
        p.description,
        p.original_price,
        p.image_url,
        p.stock_quantity,
        p.total_sold,

        COALESCE(p.avg_rating, 0) AS avg_rating,
        COALESCE(p.review_count, 0) AS review_count,

        0 AS discount_percent,
        NULL AS sale_price,

        c.category_name

    FROM products p
    LEFT JOIN categories c ON p.category_id = c.id
"""
            ;
    private static final String BASE_SELECT = """
        SELECT
            p.id,
            p.category_id,
            p.name,
            p.description,
            p.original_price,
            p.image_url,
            p.stock_quantity,
            p.total_sold,
            COALESCE(p.avg_rating, 0) AS avg_rating,
            COALESCE(p.review_count, 0) AS review_count,
        
            0 AS discount_percent,
            NULL AS sale_price
        
        FROM products p
        
        GROUP BY
            p.id,
            p.category_id,
            p.name,
            p.description,
            p.original_price,
            p.image_url,
            p.stock_quantity,
            p.total_sold,
            p.avg_rating,
            p.review_count
        
    """;

    public List<Product> getBestSellingProducts(int limit) {
        String sql = BASE_SELECT + " ORDER BY p.total_sold DESC LIMIT ?";
        return getProductsByLimit(sql, limit);
    }

    public List<Product> getNewestProducts(int limit) {
        String sql = BASE_SELECT + " ORDER BY p.id DESC LIMIT ?";
        return getProductsByLimit(sql, limit);
    }

    public List<Product> getTopRatedProducts(int limit) {
        String sql = BASE_SELECT + " ORDER BY avg_rating DESC, review_count DESC LIMIT ?";
        return getProductsByLimit(sql, limit);
    }

    public List<Product> getTopSellingByCategory(Long categoryId, int limit) {
        String sql = """
            SELECT * FROM (
                """ + BASE_SELECT + """
            ) t
            WHERE t.category_id = ?
            ORDER BY t.total_sold DESC
            LIMIT ?
        """;

        List<Product> list = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, categoryId);
            ps.setInt(2, limit);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Product> getProductsByCategoryWithFilter(
            Long categoryId,
            Integer minPrice,
            Integer maxPrice,
            Integer rating,
            ProductSort sort,
            int offset,
            int limit
    ) {

        List<Product> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT * FROM (
                """ + BASE_SELECT + """
            ) t
            WHERE t.category_id = ?
        """);

        if (minPrice != null) sql.append(" AND t.original_price >= ?");
        if (maxPrice != null) sql.append(" AND t.original_price <= ?");
        if (rating != null)   sql.append(" AND t.avg_rating >= ?");

        if (sort != null) {
            switch (sort) {
                case PRICE_ASC  -> sql.append(" ORDER BY t.original_price ASC");
                case PRICE_DESC -> sql.append(" ORDER BY t.original_price DESC");
                case NEWEST     -> sql.append(" ORDER BY t.id DESC");
                default         -> sql.append(" ORDER BY t.total_sold DESC");
            }
        } else {
            sql.append(" ORDER BY t.total_sold DESC");
        }

        sql.append(" LIMIT ? OFFSET ?");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setLong(idx++, categoryId);
            if (minPrice != null) ps.setInt(idx++, minPrice);
            if (maxPrice != null) ps.setInt(idx++, maxPrice);
            if (rating != null)   ps.setInt(idx++, rating);
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapProduct(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public int countProductsByCategoryWithFilter(
            Long categoryId,
            Integer minPrice,
            Integer maxPrice,
            Integer rating
    ) {

        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) FROM (
                SELECT p.id
                FROM products p
                WHERE p.category_id = ?
        """);

        if (minPrice != null) sql.append(" AND p.original_price >= ?");
        if (maxPrice != null) sql.append(" AND p.original_price <= ?");

        sql.append(" GROUP BY p.id ");

        if (rating != null) sql.append(" HAVING COALESCE(MAX(p.avg_rating), 0) >= ? ");

        sql.append(") t");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setLong(idx++, categoryId);
            if (minPrice != null) ps.setInt(idx++, minPrice);
            if (maxPrice != null) ps.setInt(idx++, maxPrice);
            if (rating != null)   ps.setInt(idx, rating);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<Product> searchProductsWithFilter(
            String keyword,
            Integer minPrice,
            Integer maxPrice,
            Integer rating,
            ProductSort sort,
            int offset,
            int limit
    ) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT * FROM (
                """ + BASE_SELECT + """
            ) t
            WHERE (
                LOWER(t.name) LIKE LOWER(?)
                OR LOWER(t.description) LIKE LOWER(?)
            )
        """);

        if (minPrice != null) sql.append(" AND t.original_price >= ?");
        if (maxPrice != null) sql.append(" AND t.original_price <= ?");
        if (rating != null) sql.append(" AND t.avg_rating >= ?");

        if (sort != null) {
            switch (sort) {
                case PRICE_ASC -> sql.append(" ORDER BY t.original_price ASC");
                case PRICE_DESC -> sql.append(" ORDER BY t.original_price DESC");
                case NEWEST -> sql.append(" ORDER BY t.id DESC");
                default -> sql.append(" ORDER BY t.total_sold DESC, t.avg_rating DESC");
            }
        } else {
            sql.append(" ORDER BY t.total_sold DESC, t.avg_rating DESC");
        }

        sql.append(" LIMIT ? OFFSET ?");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            String searchPattern = "%" + keyword + "%";
            ps.setString(idx++, searchPattern);
            ps.setString(idx++, searchPattern);
            if (minPrice != null) ps.setInt(idx++, minPrice);
            if (maxPrice != null) ps.setInt(idx++, maxPrice);
            if (rating != null) ps.setInt(idx++, rating);
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapProduct(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public int countSearchProductsWithFilter(
            String keyword,
            Integer minPrice,
            Integer maxPrice,
            Integer rating
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) FROM (
                SELECT p.id
                FROM products p
                WHERE (
                    LOWER(p.name) LIKE LOWER(?)
                    OR LOWER(p.description) LIKE LOWER(?)
                )
        """);

        if (minPrice != null) sql.append(" AND p.original_price >= ?");
        if (maxPrice != null) sql.append(" AND p.original_price <= ?");

        sql.append(" GROUP BY p.id, p.avg_rating ");

        if (rating != null) sql.append(" HAVING COALESCE(MAX(p.avg_rating), 0) >= ? ");

        sql.append(") t");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            String searchPattern = "%" + keyword + "%";
            ps.setString(idx++, searchPattern);
            ps.setString(idx++, searchPattern);
            if (minPrice != null) ps.setInt(idx++, minPrice);
            if (maxPrice != null) ps.setInt(idx++, maxPrice);
            if (rating != null) ps.setInt(idx, rating);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public Product getProductById(Long id) {
        String sql = """
            SELECT * FROM (
                """ + BASE_SELECT + """
            ) t
            WHERE t.id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapProduct(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Product> getRelatedProducts(Long categoryId, Long excludeId, int limit) {

        String sql = """
        SELECT * FROM (
            """ + BASE_SELECT + """
        ) t
        WHERE t.category_id = ?
          AND t.id <> ?
        ORDER BY t.total_sold DESC
        LIMIT ?
    """;

        List<Product> list = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, categoryId);
            ps.setLong(2, excludeId);
            ps.setInt(3, limit);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private List<Product> getProductsByLimit(String sql, int limit) {
        List<Product> list = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Product> searchProductsByName(String keyword, int limit) {
        List<Product> list = new ArrayList<>();
        String sql = """
            SELECT * FROM (
                """ + BASE_SELECT + """
            ) t
            WHERE LOWER(t.name) LIKE LOWER(?)
            ORDER BY t.total_sold DESC, t.avg_rating DESC
            LIMIT ?
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setInt(2, limit);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                Category category = new Category();
                category.setId(rs.getLong("category_id"));
                p.setCategory(category);
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setOriginalPrice(rs.getDouble("original_price"));
                p.setDiscountPercent(rs.getInt("discount_percent"));
                p.setSalePrice(rs.getDouble("sale_price"));
                p.setImage(rs.getString("image_url"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                p.setTotalSold(rs.getInt("total_sold"));
                p.setAvgRating(rs.getDouble("avg_rating"));
                p.setReviewCount(rs.getInt("review_count"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Product mapProduct(ResultSet rs) throws Exception {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        Long categoryId =  rs.getLong("category_id");
        if (categoryId != null) {
            Category category = new Category();
            category.setId(categoryId);
            p.setCategory(category);
        }
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setOriginalPrice(rs.getDouble("original_price"));
        p.setImage(rs.getString("image_url"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setTotalSold(rs.getInt("total_sold"));
        p.setAvgRating(rs.getDouble("avg_rating"));
        p.setReviewCount(rs.getInt("review_count"));

        // discount
        int discount = rs.getInt("discount_percent");
        if (discount > 0) {
            p.setDiscountPercent(discount);
            p.setSalePrice(rs.getDouble("sale_price"));
        }

        return p;
    }

    public int getTotalProducts() {
        String sql = "SELECT COUNT(*) as total FROM products";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Product> getTopSellingProducts(int limit) {
        String sql = BASE_SELECT + " ORDER BY total_sold DESC LIMIT ?";
        return getProductsByLimit(sql, limit);
    }

    public List<Product> getLowStockProducts(int threshold) {
        String sql = """
            SELECT
                p.id,
                p.category_id,
                p.name,
                p.description,
                p.original_price,
                p.image_url,
                p.stock_quantity,
                p.total_sold,
                COALESCE(p.avg_rating, 0) AS avg_rating,
                COALESCE(p.review_count, 0) AS review_count,
                0 AS discount_percent,
                NULL AS sale_price
            FROM products p
            WHERE p.stock_quantity <= ?
            GROUP BY
                p.id,
                p.category_id,
                p.name,
                p.description,
                p.original_price,
                p.image_url,
                p.stock_quantity,
                p.total_sold,
                p.avg_rating,
                p.review_count
            ORDER BY p.stock_quantity ASC
            LIMIT 20
        """;
        List<Product> list = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<CategorySalesDTO> getTopCategoriesBySales(int limit) {
        List<CategorySalesDTO> categories = new ArrayList<>();
        String sql = """
            SELECT c.category_name AS category_name,
                   COALESCE(SUM(oi.quantity), 0) AS total_sold,
                   COALESCE(SUM(oi.quantity * oi.price_at_purchase), 0) AS revenue
            FROM products p
            JOIN categories c ON p.category_id = c.id
            JOIN order_items oi ON p.id = oi.product_id
            JOIN orders o ON oi.order_id = o.id
            JOIN order_status os ON o.status_id = os.id
            WHERE os.description = 'Hoàn thành'
            GROUP BY c.category_name
            ORDER BY revenue DESC
            LIMIT ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CategorySalesDTO dto = new CategorySalesDTO();
                    dto.setCategoryName(rs.getString("category_name"));
                    dto.setTotalSold(rs.getInt("total_sold"));
                    dto.setRevenue(rs.getDouble("revenue"));
                    categories.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }

    public List<Product> getAllProducts() {
        String sql = BASE_SELECT + " ORDER BY id DESC";
        List<Product> list = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Product> getProductsWithPagination(int offset, int limit) {
        String sql = BASE_SELECT + " ORDER BY id DESC LIMIT ? OFFSET ?";
        List<Product> list = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertProduct(Product product) {
        return insertProduct(product, product.getCategory());
    }

    public boolean insertProduct(Product product, Category category) {
        String sql = """
        INSERT INTO products (
            category_id, name, description, original_price,
            image_url, stock_quantity, total_sold, avg_rating, review_count
        )
        VALUES (?, ?, ?, ?, ?, ?, 0, 0, 0)
    """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, category != null ? category.getId() : null);
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getOriginalPrice());
            ps.setString(5, product.getImage());
            ps.setInt(6, product.getStockQuantity());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateProduct(Product product) {
        String sql = """
        UPDATE products
        SET category_id = ?, name = ?, description = ?,
            original_price = ?, image_url = ?, stock_quantity = ?
        WHERE id = ?
    """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, product.getCategory() != null ? product.getCategory().getId() : null);
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getOriginalPrice());
            ps.setString(5, product.getImage());
            ps.setInt(6, product.getStockQuantity());
            ps.setLong(7, product.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteProduct(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Product> searchProducts(String keyword) {

        List<Product> list = new ArrayList<>();

        String sql = SEARCH_SELECT + """
        WHERE LOWER(p.name) LIKE LOWER(?)
           OR LOWER(p.description) LIKE LOWER(?)
           OR LOWER(c.category_name) LIKE LOWER(?)
        GROUP BY
            p.id,
            p.category_id,
            p.name,
            p.description,
            p.original_price,
            p.image_url,
            p.stock_quantity,
            p.total_sold,
            c.category_name
        ORDER BY p.total_sold DESC, avg_rating DESC
    """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapProduct(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

}
