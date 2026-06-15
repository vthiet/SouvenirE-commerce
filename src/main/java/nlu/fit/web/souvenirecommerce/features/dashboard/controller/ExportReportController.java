package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.legacy.dao.OrderDAO;
import nlu.fit.web.souvenirecommerce.legacy.dao.ProductDAO;
import nlu.fit.web.souvenirecommerce.legacy.dao.impl.UserDAOImpl;
import nlu.fit.web.souvenirecommerce.legacy.model.Order;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/admin/export-report")
public class ExportReportController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ExportReportController.class);
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final UserDAOImpl userDAOImpl = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String type = request.getParameter("type");

        if (type == null || type.isEmpty()) {
            type = "summary";
        }

        log.info("Export report requested: type={}", type);

        switch (type) {
            case "summary":
                exportSummaryReport(response);
                break;
            case "products":
                exportProductsReport(response);
                break;
            case "orders":
                exportOrdersReport(response);
                break;
            case "customers":
                exportCustomersReport(response);
                break;
            default:
                exportSummaryReport(response);
        }
    }

    private void exportSummaryReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String filename = "bao-cao-tong-quan-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        PrintWriter writer = response.getWriter();

        // BOM for UTF-8
        writer.write('\ufeff');

        writer.println("BÁO CÁO TỔNG QUAN HỆ THỐNG");
        writer.println("Ngày xuất: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        writer.println();

        // Statistics
        double totalRevenue = orderDAO.getTotalRevenue();
        int totalOrders = orderDAO.getTotalOrders();
        int totalProducts = productDAO.getTotalProducts();
        int totalCustomers = userDAOImpl.getTotalCustomers();

        writer.println("CHỈ SỐ TỔNG QUAN");
        writer.println("Chỉ số,Giá trị");
        writer.println("Tổng doanh thu," + String.format("%.0f", totalRevenue) + " VNĐ");
        writer.println("Tổng đơn hàng," + totalOrders);
        writer.println("Tổng sản phẩm," + totalProducts);
        writer.println("Tổng khách hàng," + totalCustomers);
        writer.println();

        // Top products
        writer.println("TOP 10 SẢN PHẨM BÁN CHẠY");
        writer.println("ID,Tên sản phẩm,Giá,Đã bán,Tồn kho,Đánh giá");

        List<Product> topProducts = productDAO.getTopSellingProducts(10);
        for (Product p : topProducts) {
            writer.println(String.format("%d,\"%s\",%s,%d,%d,%.1f",
                    p.getId(),
                    p.getName().replace("\"", "\"\""),
                    String.format("%.0f", p.getOriginalPrice()),
                    p.getTotalSold(),
                    p.getStockQuantity(),
                    p.getAvgRating()
            ));
        }

        writer.flush();
        log.info("Exported summary report");
    }

    private void exportProductsReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String filename = "bao-cao-san-pham-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');

        writer.println("BÁO CÁO SẢN PHẨM");
        writer.println("Ngày xuất: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        writer.println();
        writer.println("ID,Tên sản phẩm,Danh mục,Giá,Tồn kho,Đã bán,Đánh giá,Trạng thái");

        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            String status = p.getStockQuantity() > 0 ? "Còn hàng" : "Hết hàng";
            writer.println(String.format("%d,\"%s\",\"%s\",%s,%d,%d,%.1f,%s",
                    p.getId(),
                    p.getName().replace("\"", "\"\""),
                    p.getCategory() != null ? p.getCategory().getCategoryName().replace("\"", "\"\"") : "",
                    String.format("%.0f", p.getOriginalPrice()),
                    p.getStockQuantity(),
                    p.getTotalSold(),
                    p.getAvgRating(),
                    status
            ));
        }

        writer.flush();
        log.info("Exported products report");
    }

    private void exportOrdersReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String filename = "bao-cao-don-hang-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');

        writer.println("BÁO CÁO ĐỐN HÀNG");
        writer.println("Ngày xuất: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        writer.println();
        writer.println("ID,Khách hàng,Email,Ngày đặt,Tổng tiền,Trạng thái");

        List<Order> orders = orderDAO.getAllOrders();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (Order o : orders) {
            writer.println(String.format("%d,\"%s\",\"%s\",%s,%s,%s",
                    o.getId(),
                    o.getCustomerName() != null ? o.getCustomerName().replace("\"", "\"\"") : "",
                    o.getCustomerEmail() != null ? o.getCustomerEmail().replace("\"", "\"\"") : "",
                    o.getOrderDate() != null ? sdf.format(o.getOrderDate()) : "",
                    String.format("%.0f", o.getTotalAmount()),
                    o.getStatus() != null ? o.getStatus() : "Pending"
            ));
        }

        writer.flush();
        log.info("Exported orders report");
    }

    private void exportCustomersReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String filename = "bao-cao-khach-hang-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');

        writer.println("BÁO CÁO KHÁCH HÀNG");
        writer.println("Ngày xuất: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        writer.println();
        writer.println("ID,Họ tên,Email,Số điện thoại,Vai trò,Trạng thái,Ngày tạo");

        List<User> customers = userDAOImpl.getAllUsers();

        for (User u : customers) {
            writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",%s,%s,%s",
                    u.getId(),
                    u.getFullName() != null ? u.getFullName().replace("\"", "\"\"") : "",
                    u.getEmail() != null ? u.getEmail().replace("\"", "\"\"") : "",
                    u.getPhone() != null ? u.getPhone() : "",
                    u.getRoles() != null && !u.getRoles().isEmpty()
                            ? u.getRoles().stream().map(r -> r.getName()).collect(Collectors.joining("|"))
                            : "Customer",
                    u.isActive() ? "Active" : "Inactive",
                    u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
            ));
        }

        writer.flush();
        log.info("Exported customers report");
    }
}
