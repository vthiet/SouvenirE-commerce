package nlu.fit.web.souvenirecommerce.legacy.dao;

import nlu.fit.web.souvenirecommerce.legacy.model.Order;
import nlu.fit.web.souvenirecommerce.legacy.model.OrderItem;
import nlu.fit.web.souvenirecommerce.legacy.utils.DBContext;
import nlu.fit.web.souvenirecommerce.features.order.dto.OrderItemDTO;
import nlu.fit.web.souvenirecommerce.features.order.dto.OrderListDTO;
import nlu.fit.web.souvenirecommerce.features.order.dto.OrderStatusTabDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    public int getTotalOrders() {
        String sql = "SELECT COUNT(*) as total FROM orders";
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

    public double getTotalRevenue() {
        String sql = """
            SELECT COALESCE(SUM(o.total_amount), 0) as total 
            FROM orders o
            JOIN order_status os ON o.status_id = os.id
            WHERE os.description = 'Hoàn thành'
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getMonthlyOrders() {
        String sql = """
            SELECT COUNT(*) as total 
            FROM orders 
            WHERE MONTH(order_date) = MONTH(CURRENT_DATE()) 
            AND YEAR(order_date) = YEAR(CURRENT_DATE())
        """;
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

    public double getMonthlyRevenue() {
        String sql = """
            SELECT COALESCE(SUM(o.total_amount), 0) as total 
            FROM orders o
            JOIN order_status os ON o.status_id = os.id
            WHERE os.description = 'Hoàn thành'
            AND MONTH(o.order_date) = MONTH(CURRENT_DATE()) 
            AND YEAR(o.order_date) = YEAR(CURRENT_DATE())
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Integer> getMonthlyOrdersData(int months) {
        List<Integer> counts = new ArrayList<>();
        Map<String, Integer> countByMonth = new HashMap<>();
        String sql = """
            SELECT YEAR(o.order_date) AS y,
                   MONTH(o.order_date) AS m,
                   COUNT(*) AS total_orders
            FROM orders o
            JOIN order_status os ON o.status_id = os.id
            WHERE os.description = 'Hoàn thành'
              AND o.order_date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL ? MONTH), '%Y-%m-01')
              AND o.order_date < DATE_ADD(DATE_FORMAT(CURDATE(), '%Y-%m-01'), INTERVAL 1 MONTH)
            GROUP BY y, m
            ORDER BY y, m
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, months - 1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getInt("y") + "-" + rs.getInt("m");
                    countByMonth.put(key, rs.getInt("total_orders"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        LocalDate current = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate month = current.minusMonths(i);
            String key = month.getYear() + "-" + month.getMonthValue();
            counts.add(countByMonth.getOrDefault(key, 0));
        }
        return counts;
    }

    public Map<String, Integer> getOrderStatusCounts() {
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        String sql = """
            SELECT os.description AS status, COUNT(*) AS total
            FROM orders o
            JOIN order_status os ON o.status_id = os.id
            GROUP BY os.description
            ORDER BY total DESC
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                statusCounts.put(rs.getString("status"), rs.getInt("total"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusCounts;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, os.description as status_name, u.full_name, u.email 
            FROM orders o 
            LEFT JOIN order_status os ON o.status_id = os.id
            LEFT JOIN users u ON o.user_id = u.id 
            ORDER BY o.id DESC
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setCustomerName(rs.getString("full_name"));
                order.setCustomerEmail(rs.getString("email"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status_name"));
                orders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersPaginated(int page, int pageSize) {
        List<Order> orders = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        String sql = """
            SELECT o.*, os.description as status_name, u.full_name, u.email 
            FROM orders o 
            LEFT JOIN order_status os ON o.status_id = os.id
            LEFT JOIN users u ON o.user_id = u.id 
            ORDER BY o.id DESC
            LIMIT ? OFFSET ?
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setCustomerName(rs.getString("full_name"));
                    order.setCustomerEmail(rs.getString("email"));
                    order.setOrderDate(rs.getTimestamp("order_date"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setStatus(rs.getString("status_name"));
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public int createOrder(int userId, int addressId, double totalAmount, int statusId) {
        String sql = "INSERT INTO orders (user_id, address_id, total_amount, status_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, addressId);
            ps.setDouble(3, totalAmount);
            ps.setInt(4, statusId);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean createOrderDetail(int orderId, Long productId, int quantity, double price) {
        String sql = "INSERT INTO order_details (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            ps.setDouble(4, price);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int createAddress(int userId, String addressDetail, String city, String district, String ward) {
        String sql = "INSERT INTO addresses (user_id, address_detail, city, district, ward) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, addressDetail);
            ps.setString(3, city);
            ps.setString(4, district);
            ps.setString(5, ward);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getOrCreateOrderStatus(String description) {
        String selectSql = "SELECT id FROM order_status WHERE description = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, description);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String insertSql = "INSERT INTO order_status (description) VALUES (?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, description);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public int getOrderCountByStatus(String status) {
        String sql = "SELECT COUNT(*) as total FROM orders o JOIN order_status os ON o.status_id = os.id WHERE os.description = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Order getOrderById(int orderId) {
        String sql = """
            SELECT o.*, os.description as status_name, u.full_name, u.email, u.phone,
                   a.address_detail, a.city, a.district, a.ward
            FROM orders o 
            LEFT JOIN order_status os ON o.status_id = os.id
            LEFT JOIN users u ON o.user_id = u.id
            LEFT JOIN addresses a ON o.address_id = a.id
            WHERE o.id = ?
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setCustomerName(rs.getString("full_name"));
                    order.setCustomerEmail(rs.getString("email"));
                    order.setOrderDate(rs.getTimestamp("order_date"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setStatus(rs.getString("status_name"));

                    // Build shipping address
                    String address = rs.getString("address_detail") + ", " +
                            rs.getString("district") + ", " +
                            rs.getString("city");
                    order.setShippingAddress(address);

                    return order;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        int statusId = getOrCreateOrderStatus(newStatus);
        String sql = "UPDATE orders SET status_id = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, statusId);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Order> getOrdersByStatus(String status, int page, int pageSize) {
        List<Order> orders = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        String sql = """
            SELECT o.*, os.description as status_name, u.full_name, u.email 
            FROM orders o 
            LEFT JOIN order_status os ON o.status_id = os.id
            LEFT JOIN users u ON o.user_id = u.id 
            WHERE os.description = ?
            ORDER BY o.id DESC
            LIMIT ? OFFSET ?
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, pageSize);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setCustomerName(rs.getString("full_name"));
                    order.setCustomerEmail(rs.getString("email"));
                    order.setOrderDate(rs.getTimestamp("order_date"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setStatus(rs.getString("status_name"));
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = """
            SELECT od.*, p.name as product_name, p.image_url 
            FROM order_details od
            JOIN products p ON od.product_id = p.id
            WHERE od.order_id = ?
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderId(orderId);
                    item.setProductId(rs.getLong("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setProductImage(rs.getString("image_url"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPriceAtPurchase(rs.getDouble("price_at_purchase"));
                    item.setSubTotal(rs.getInt("quantity") * rs.getDouble("price_at_purchase"));
                    items.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<Order> getRecentOrders(int limit) {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, os.description as status_name, u.full_name, u.email 
            FROM orders o 
            LEFT JOIN order_status os ON o.status_id = os.id
            LEFT JOIN users u ON o.user_id = u.id 
            ORDER BY o.id DESC
            LIMIT ?
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setCustomerName(rs.getString("full_name"));
                    order.setCustomerEmail(rs.getString("email"));
                    order.setOrderDate(rs.getTimestamp("order_date"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setStatus(rs.getString("status_name"));
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Double> getMonthlyRevenueData(int months) {
        List<Double> revenues = new ArrayList<>();
        Map<String, Double> revenueMap = new HashMap<>();
        String sql = """
            SELECT YEAR(o.order_date) AS y,
                   MONTH(o.order_date) AS m,
                   COALESCE(SUM(o.total_amount), 0) AS revenue
            FROM orders o
            JOIN order_status os ON o.status_id = os.id
            WHERE os.description = 'Hoàn thành'
              AND o.order_date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL ? MONTH), '%Y-%m-01')
              AND o.order_date < DATE_ADD(DATE_FORMAT(CURDATE(), '%Y-%m-01'), INTERVAL 1 MONTH)
            GROUP BY y, m
            ORDER BY y, m
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, months - 1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getInt("y") + "-" + rs.getInt("m");
                    revenueMap.put(key, rs.getDouble("revenue"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        LocalDate today = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            String key = month.getYear() + "-" + month.getMonthValue();
            revenues.add(revenueMap.getOrDefault(key, 0.0));
        }

        return revenues;
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, os.description as status_name
            FROM orders o 
            LEFT JOIN order_status os ON o.status_id = os.id
            WHERE o.user_id = ?
            ORDER BY o.id DESC
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setOrderDate(rs.getTimestamp("order_date"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setStatus(rs.getString("status_name"));
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<OrderListDTO> getUserOrderList(int userId, String statusFilter, String keyword) {
        Map<Integer, OrderListDTO> orderMap = new LinkedHashMap<>();
        List<Object> params = new ArrayList<>();
        params.add(userId);

        StringBuilder sql = new StringBuilder("""
            SELECT o.id,
                   o.order_date,
                   o.total_amount,
                   os.description AS status_name,
                   pt.status AS payment_status,
                   pt.payment_url,
                   od.product_id,
                   od.quantity,
                   od.price_at_purchase,
                   COALESCE(NULLIF(od.product_name, ''), p.name) AS product_name,
                   COALESCE(NULLIF(od.product_image, ''), p.image_url) AS product_image
            FROM orders o
            LEFT JOIN order_status os ON o.status_id = os.id
            LEFT JOIN payment_transactions pt ON pt.order_id = o.id
            LEFT JOIN order_details od ON od.order_id = o.id
            LEFT JOIN products p ON od.product_id = p.id
            WHERE o.user_id = ?
        """);

        if (statusFilter != null && !statusFilter.isBlank() && !"all".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND os.description = ? ");
            params.add(statusFilter);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append("""
                AND (
                    CAST(o.id AS CHAR) LIKE ?
                    OR EXISTS (
                        SELECT 1
                        FROM order_details od2
                        LEFT JOIN products p2 ON od2.product_id = p2.id
                        WHERE od2.order_id = o.id
                          AND COALESCE(NULLIF(od2.product_name, ''), p2.name) LIKE ?
                    )
                )
            """);
            String searchPattern = "%" + keyword.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        sql.append(" ORDER BY o.id DESC, od.product_id ASC ");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("id");
                    OrderListDTO order = orderMap.computeIfAbsent(orderId, id -> {
                        OrderListDTO dto = new OrderListDTO();
                        try {
                            dto.setOrderId(id);
                            dto.setOrderDate(rs.getTimestamp("order_date"));
                            dto.setTotalAmount(rs.getDouble("total_amount"));
                            dto.setStatusText(rs.getString("status_name"));
                            dto.setPaymentStatus(rs.getString("payment_status"));
                            dto.setRepayUrl(rs.getString("payment_url"));
                        } catch (Exception e) {
                            throw new IllegalStateException("Cannot map order list row", e);
                        }
                        return dto;
                    });

                    long productId = rs.getLong("product_id");
                    if (!rs.wasNull()) {
                        OrderItemDTO item = new OrderItemDTO();
                        item.setProductId(productId);
                        item.setProductName(rs.getString("product_name"));
                        item.setProductImage(rs.getString("product_image"));
                        item.setQuantity(rs.getInt("quantity"));
                        item.setPriceAtPurchase(rs.getDouble("price_at_purchase"));
                        order.getItems().add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(orderMap.values());
    }

    public List<OrderStatusTabDTO> getUserOrderStatusTabs(int userId) {
        List<OrderStatusTabDTO> tabs = new ArrayList<>();
        String sql = """
            SELECT os.description AS status_name, COUNT(*) AS total
            FROM orders o
            LEFT JOIN order_status os ON o.status_id = os.id
            WHERE o.user_id = ?
              AND os.description IS NOT NULL
            GROUP BY os.description
            ORDER BY MIN(os.id)
        """;

        int totalOrders = 0;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status_name");
                    int count = rs.getInt("total");
                    totalOrders += count;
                    tabs.add(new OrderStatusTabDTO(status, toOrderStatusLabel(status), count));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tabs.add(0, new OrderStatusTabDTO("all", "Tất cả", totalOrders));
        return tabs;
    }

    private String toOrderStatusLabel(String status) {
        if (status == null || status.isBlank()) {
            return "Đang xử lý";
        }
        return switch (status) {
            case "PENDING_PAYMENT" -> "Chờ thanh toán";
            case "WAIT_CONFIRM", "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "SHIPPED" -> "Đang giao";
            case "DELIVERED" -> "Đã giao";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            case "PAYMENT_FAILED" -> "Thanh toán thất bại";
            default -> status;
        };
    }
}
