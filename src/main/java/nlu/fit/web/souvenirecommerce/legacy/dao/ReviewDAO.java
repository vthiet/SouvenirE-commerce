package nlu.fit.web.souvenirecommerce.legacy.dao;

import nlu.fit.web.souvenirecommerce.legacy.model.Review;
import nlu.fit.web.souvenirecommerce.legacy.model.ReviewSummary;
import nlu.fit.web.souvenirecommerce.legacy.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReviewDAO {

    private static final String REVIEW_JOIN_SQL = """
            FROM reviews r
            LEFT JOIN products p ON p.id = r.product_id
            LEFT JOIN users u ON u.id = r.user_id
            """;

    public List<Review> getReviewsByProductWithFilter(Long productId, Integer rating, String sort, int offset, int limit) {
        List<Review> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT r.id,
                       r.product_id,
                       COALESCE(p.name, CONCAT('Sản phẩm #', r.product_id)) AS product_name,
                       COALESCE(p.image_url, '') AS product_image,
                       r.user_id,
                       COALESCE(u.full_name, CONCAT('Khách hàng #', r.user_id)) AS user_name,
                       COALESCE(u.email, '') AS user_email,
                       r.rating,
                       r.comment,
                       r.created_at
                FROM reviews r
                LEFT JOIN products p ON p.id = r.product_id
                LEFT JOIN users u ON u.id = r.user_id
                WHERE r.product_id = ?
                """);

        if (rating != null) {
            sql.append(" AND r.rating = ? ");
        }

        if ("oldest".equalsIgnoreCase(sort)) {
            sql.append(" ORDER BY r.created_at ASC ");
        } else {
            sql.append(" ORDER BY r.created_at DESC ");
        }

        sql.append(" LIMIT ? OFFSET ? ");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setLong(idx++, productId);

            if (rating != null) {
                ps.setInt(idx++, rating);
            }

            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapReview(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Review> getReviewsForAdmin(String search, Integer rating, String sort, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT r.id,
                       r.product_id,
                       COALESCE(p.name, CONCAT('Sản phẩm #', r.product_id)) AS product_name,
                       COALESCE(p.image_url, '') AS product_image,
                       r.user_id,
                       COALESCE(u.full_name, CONCAT('Khách hàng #', r.user_id)) AS user_name,
                       COALESCE(u.email, '') AS user_email,
                       r.rating,
                       r.comment,
                       r.created_at
                """);
        sql.append(REVIEW_JOIN_SQL);
        sql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendAdminFilters(sql, params, search, rating);
        sql.append(resolveAdminSort(sort));
        sql.append(" LIMIT ? OFFSET ? ");
        params.add(limit);
        params.add(offset);

        return executeReviewListQuery(sql.toString(), params);
    }

    public int countReviewsForAdmin(String search, Integer rating) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) ");
        sql.append(REVIEW_JOIN_SQL);
        sql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendAdminFilters(sql, params, search, rating);
        return (int) executeLongQuery(sql.toString(), params);
    }

    public double averageRatingForAdmin(String search, Integer rating) {
        StringBuilder sql = new StringBuilder("SELECT AVG(r.rating) ");
        sql.append(REVIEW_JOIN_SQL);
        sql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendAdminFilters(sql, params, search, rating);
        return executeDoubleQuery(sql.toString(), params);
    }

    public int countDistinctReviewedProductsForAdmin(String search, Integer rating) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT r.product_id) ");
        sql.append(REVIEW_JOIN_SQL);
        sql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendAdminFilters(sql, params, search, rating);
        return (int) executeLongQuery(sql.toString(), params);
    }

    public int countRecentReviewsForAdmin(String search, Integer rating, LocalDateTime since) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) ");
        sql.append(REVIEW_JOIN_SQL);
        sql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendAdminFilters(sql, params, search, rating);
        if (since != null) {
            sql.append(" AND r.created_at >= ? ");
            params.add(Timestamp.valueOf(since));
        }
        return (int) executeLongQuery(sql.toString(), params);
    }

    public Map<String, Integer> countReviewsByRatingForAdmin(String search, Integer rating) {
        Map<String, Integer> map = initRatingMap();

        StringBuilder sql = new StringBuilder("""
                SELECT r.rating, COUNT(*) AS cnt
                """);
        sql.append(REVIEW_JOIN_SQL);
        sql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendAdminFilters(sql, params, search, rating);
        sql.append(" GROUP BY r.rating ");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            bindParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                map.put(String.valueOf(rs.getInt("rating")), rs.getInt("cnt"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public Review getReviewById(Long reviewId) {
        if (reviewId == null) {
            return null;
        }

        String sql = """
                SELECT r.id,
                       r.product_id,
                       COALESCE(p.name, CONCAT('Sản phẩm #', r.product_id)) AS product_name,
                       COALESCE(p.image_url, '') AS product_image,
                       r.user_id,
                       COALESCE(u.full_name, CONCAT('Khách hàng #', r.user_id)) AS user_name,
                       COALESCE(u.email, '') AS user_email,
                       r.rating,
                       r.comment,
                       r.created_at
                """ + REVIEW_JOIN_SQL + """
                 WHERE r.id = ?
                 LIMIT 1
                """;

        List<Object> params = List.of(reviewId);
        return executeSingleReviewQuery(sql, params);
    }

    public boolean deleteReview(Long reviewId) {
        if (reviewId == null || reviewId <= 0) {
            return false;
        }

        String selectSql = "SELECT product_id FROM reviews WHERE id = ?";
        String deleteSql = "DELETE FROM reviews WHERE id = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            Long productId = null;
            try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
                selectPs.setLong(1, reviewId);
                try (ResultSet rs = selectPs.executeQuery()) {
                    if (rs.next()) {
                        productId = rs.getLong("product_id");
                    }
                }
            }

            if (productId == null) {
                conn.rollback();
                return false;
            }

            int affected;
            try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                deletePs.setLong(1, reviewId);
                affected = deletePs.executeUpdate();
            }

            if (affected == 0) {
                conn.rollback();
                return false;
            }

            refreshProductReviewStats(conn, productId);
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ReviewSummary getReviewSummaryByProductId(Long productId) {
        String sql = """
                    SELECT COUNT(*) AS total_reviews,
                           AVG(rating) AS avg_rating
                    FROM reviews
                    WHERE product_id = ?
                """;

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, productId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total_reviews");
                double avg = rs.getDouble("avg_rating");

                if (rs.wasNull()) {
                    avg = 0.0;
                }

                return new ReviewSummary(total, Math.round(avg * 10.0) / 10.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ReviewSummary(0, 0.0);
    }

    public boolean hasPurchased(int userId, Long productId) {
        String sql = """
                    SELECT 1
                    FROM orders o
                    JOIN order_details od ON o.id = od.order_id
                    WHERE o.user_id = ?
                      AND od.product_id = ?
                    LIMIT 1
                """;

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setLong(2, productId);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Integer> countReviewsByRating(Long productId) {
        Map<String, Integer> map = initRatingMap();

        String sql = """
                    SELECT rating, COUNT(*) AS cnt
                    FROM reviews
                    WHERE product_id = ?
                    GROUP BY rating
                """;

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, productId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                map.put(String.valueOf(rs.getInt("rating")), rs.getInt("cnt"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public boolean addReview(Review r) {
        String sql = """
                    INSERT INTO reviews (product_id, user_id, rating, comment, created_at)
                    VALUES (?, ?, ?, ?, NOW())
                """;

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, r.getProductId());
            ps.setInt(2, r.getUserId());
            ps.setInt(3, r.getRating());
            ps.setString(4, r.getComment());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void refreshProductReviewStats(Long productId) {
        try (Connection conn = DBContext.getConnection()) {
            refreshProductReviewStats(conn, productId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Review> executeReviewListQuery(String sql, List<Object> params) {
        List<Review> list = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapReview(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private Review executeSingleReviewQuery(String sql, List<Object> params) {
        List<Review> reviews = executeReviewListQuery(sql, params);
        return reviews.isEmpty() ? null : reviews.get(0);
    }

    private long executeLongQuery(String sql, List<Object> params) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParameters(ps, params);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0L;
    }

    private double executeDoubleQuery(String sql, List<Object> params) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParameters(ps, params);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble(1);
                if (rs.wasNull()) {
                    return 0.0;
                }
                return Math.round(avg * 10.0) / 10.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    private void refreshProductReviewStats(Connection conn, Long productId) throws SQLException {
        String sql = """
                    UPDATE products p
                    LEFT JOIN (
                        SELECT
                            product_id,
                            ROUND(AVG(rating), 1) AS avg_rating,
                            COUNT(*) AS review_count
                        FROM reviews
                        WHERE product_id = ?
                        GROUP BY product_id
                    ) r ON r.product_id = p.id
                    SET
                        p.avg_rating = COALESCE(r.avg_rating, 0),
                        p.review_count = COALESCE(r.review_count, 0)
                    WHERE p.id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setLong(2, productId);
            ps.executeUpdate();
        }
    }

    private void appendAdminFilters(StringBuilder sql, List<Object> params, String search, Integer rating) {
        if (rating != null) {
            sql.append(" AND r.rating = ? ");
            params.add(rating);
        }

        String normalizedSearch = normalizeSearch(search);
        if (normalizedSearch != null) {
            sql.append("""
                    AND (
                        LOWER(COALESCE(p.name, '')) LIKE ?
                        OR LOWER(COALESCE(u.full_name, '')) LIKE ?
                        OR LOWER(COALESCE(u.email, '')) LIKE ?
                        OR LOWER(COALESCE(r.comment, '')) LIKE ?
                    )
                    """);
            String keyword = "%" + normalizedSearch.toLowerCase(Locale.ROOT) + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }
    }

    private String resolveAdminSort(String sort) {
        return switch (normalizeSort(sort)) {
            case "oldest" -> " ORDER BY r.created_at ASC, r.id ASC ";
            case "rating_asc" -> " ORDER BY r.rating ASC, r.created_at DESC, r.id DESC ";
            case "rating_desc" -> " ORDER BY r.rating DESC, r.created_at DESC, r.id DESC ";
            default -> " ORDER BY r.created_at DESC, r.id DESC ";
        };
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "newest";
        }

        String normalized = sort.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "oldest", "rating_asc", "rating_desc", "newest" -> normalized;
            default -> "newest";
        };
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Map<String, Integer> initRatingMap() {
        Map<String, Integer> map = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            map.put(String.valueOf(rating), 0);
        }
        return map;
    }

    private void bindParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private Review mapReview(ResultSet rs) throws SQLException {
        Review r = new Review();

        r.setId(rs.getInt("id"));
        r.setProductId(rs.getLong("product_id"));
        r.setProductName(rs.getString("product_name"));
        r.setProductImage(rs.getString("product_image"));
        r.setUserId(rs.getInt("user_id"));
        r.setUserName(rs.getString("user_name"));
        r.setUserEmail(rs.getString("user_email"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setCreatedAt(rs.getTimestamp("created_at"));

        return r;
    }
}
