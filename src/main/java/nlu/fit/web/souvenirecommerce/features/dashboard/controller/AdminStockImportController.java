package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.utils.PermissionHelper;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.StockImportSummaryDTO;
import nlu.fit.web.souvenirecommerce.features.dashboard.repository.StockImportRecordRepository;
import nlu.fit.web.souvenirecommerce.features.product.repository.ProductRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.StockImportRecord;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;

@WebServlet("/admin/stock-imports")
public class AdminStockImportController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminStockImportController.class);
    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int LOW_STOCK_LIMIT = 6;
    private static final int RECENT_IMPORT_LIMIT = 15;

    private final ProductRepository productRepository = new ProductRepository();
    private final StockImportRecordRepository stockImportRepository = new StockImportRecordRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!canManageInventory(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng nhập hàng.");
            return;
        }

        String message = request.getParameter("message");
        String messageType = request.getParameter("messageType");
        if (message != null && !message.isBlank()) {
            request.setAttribute("message", message);
            request.setAttribute("messageType", messageType == null || messageType.isBlank() ? "success" : messageType);
        }

        request.setAttribute("formProductId", parseLongParameter(request.getParameter("productId")));
        request.setAttribute("formQuantity", safeString(request.getParameter("quantity")));
        request.setAttribute("formUnitCost", safeString(request.getParameter("unitCost")));
        request.setAttribute("formNote", safeString(request.getParameter("note")));

        List<Product> products = productRepository.findAllForStockImport();
        List<Product> lowStockProducts = productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD, LOW_STOCK_LIMIT);
        List<StockImportRecord> recentImports = stockImportRepository.findRecentImports(RECENT_IMPORT_LIMIT);
        StockImportSummaryDTO summary = stockImportRepository.loadMonthlySummary(
                YearMonth.now().atDay(1).atStartOfDay(),
                LOW_STOCK_THRESHOLD
        );

        request.setAttribute("stockImportSummary", summary);
        request.setAttribute("products", products);
        request.setAttribute("lowStockProducts", lowStockProducts);
        request.setAttribute("recentImports", recentImports);
        request.setAttribute("lowStockThreshold", LOW_STOCK_THRESHOLD);
        request.setAttribute("selectedProductId", request.getAttribute("formProductId"));

        request.getRequestDispatcher("/admin/stock-imports.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!canManageInventory(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện nhập hàng.");
            return;
        }

        String action = request.getParameter("action");
        if (action != null && !action.isBlank() && !"import".equalsIgnoreCase(action)) {
            sendRedirectWithMessage(request, response, "Hành động không được hỗ trợ.", "error");
            return;
        }

        try {
            User currentUser = resolveCurrentUser(request);
            Long productId = parseLongParameter(request.getParameter("productId"));
            int quantity = parseIntParameter(request.getParameter("quantity"));
            BigDecimal unitCost = parseBigDecimalParameter(request.getParameter("unitCost"));
            String note = request.getParameter("note");

            if (currentUser == null) {
                throw new IllegalStateException("Không xác định được người thao tác");
            }

            StockImportRecord record = stockImportRepository.importStock(
                    productId,
                    quantity,
                    unitCost,
                    note,
                    currentUser.getId(),
                    currentUser.getFullName()
            );

            log.info("Stock import created: productId={}, quantity={}, stockAfter={}, importedBy={}",
                    record.getProductId(), record.getQuantity(), record.getStockAfter(), currentUser.getId());

            String successMessage = "Đã nhập " + quantity + " sản phẩm cho " + record.getProductNameSnapshot()
                    + ". Tồn kho hiện tại: " + record.getStockAfter() + ".";
            sendRedirectWithMessage(request, response, successMessage, "success");
        } catch (Exception ex) {
            log.warn("Stock import failed", ex);
            sendRedirectWithMessage(
                    request,
                    response,
                    ex.getMessage() == null || ex.getMessage().isBlank()
                            ? "Không thể nhập hàng. Vui lòng kiểm tra lại dữ liệu."
                            : ex.getMessage(),
                    "error",
                    request.getParameter("productId"),
                    request.getParameter("quantity"),
                    request.getParameter("unitCost"),
                    request.getParameter("note")
            );
        }
    }

    private void sendRedirectWithMessage(HttpServletRequest request,
                                         HttpServletResponse response,
                                         String message,
                                         String messageType,
                                         String... formValues) throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath()).append("/admin/stock-imports")
                .append("?message=").append(encode(message))
                .append("&messageType=").append(encode(messageType));

        if (formValues != null && formValues.length >= 4) {
            url.append("&productId=").append(encode(formValues[0]));
            url.append("&quantity=").append(encode(formValues[1]));
            url.append("&unitCost=").append(encode(formValues[2]));
            url.append("&note=").append(encode(formValues[3]));
        }

        response.sendRedirect(url.toString());
    }

    private User resolveCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("userInSession");
        if (user instanceof User current) {
            return current;
        }

        user = session.getAttribute("currentUser");
        if (user instanceof User current) {
            return current;
        }

        user = session.getAttribute("user");
        return user instanceof User ? (User) user : null;
    }

    private Long parseLongParameter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int parseIntParameter(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập số lượng");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Số lượng nhập không hợp lệ");
        }
    }

    private BigDecimal parseBigDecimalParameter(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập đơn giá nhập");
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Đơn giá nhập không hợp lệ");
        }
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private boolean canManageInventory(HttpServletRequest request) {
        return PermissionHelper.hasPermission(request, "product", "update");
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
