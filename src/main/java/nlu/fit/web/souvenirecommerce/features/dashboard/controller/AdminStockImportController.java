package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.utils.PermissionHelper;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.PurchaseOrderSummaryDTO;
import nlu.fit.web.souvenirecommerce.legacy.dao.ProductDAO;
import nlu.fit.web.souvenirecommerce.legacy.dao.PurchaseOrderDAO;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import nlu.fit.web.souvenirecommerce.model.enums.PurchaseOrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/admin/stock-imports", "/admin/stock-imports/"})
public class AdminStockImportController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminStockImportController.class);
    private static final int DEFAULT_THRESHOLD = 20;
    private static final int MAX_RESULTS = 100;
    private static final int RECENT_HISTORY_LIMIT = 10;

    private ProductDAO productDAO;
    private PurchaseOrderDAO purchaseOrderDAO;

    @Override
    public void init() {
        productDAO = new ProductDAO();
        purchaseOrderDAO = new PurchaseOrderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!PermissionHelper.hasPermission(request, "product", "update")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem trang nhập hàng.");
            return;
        }

        request.setAttribute("activePage", "stock-imports");

        int threshold = parseNonNegativeInt(request.getParameter("threshold"), DEFAULT_THRESHOLD);
        List<Product> stockProducts = productDAO.getLowStockProducts(threshold, MAX_RESULTS);
        List<Product> availableProducts = productDAO.getAllProducts();
        List<PurchaseOrderSummaryDTO> recentPurchaseOrders = purchaseOrderDAO.getRecentPurchaseOrders(RECENT_HISTORY_LIMIT);

        request.setAttribute("threshold", threshold);
        request.setAttribute("stockProducts", stockProducts);
        request.setAttribute("availableProducts", availableProducts);
        request.setAttribute("recentPurchaseOrders", recentPurchaseOrders);
        request.setAttribute("productCount", stockProducts.size());
        request.setAttribute("canUpdateProduct", true);

        HttpSession session = request.getSession(false);
        if (session != null) {
            String message = (String) session.getAttribute("message");
            String messageType = (String) session.getAttribute("messageType");
            if (message != null) {
                request.setAttribute("message", message);
                request.setAttribute("messageType", messageType);
                session.removeAttribute("message");
                session.removeAttribute("messageType");
            }
        }

        log.info("Loaded admin stock imports page threshold={} count={}", threshold, stockProducts.size());
        request.getRequestDispatcher("/admin/stock-imports.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");

        if (!PermissionHelper.hasPermission(request, "product", "update")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền nhập kho.");
            return;
        }

        HttpSession session = request.getSession();
        User currentUser = resolveCurrentAdminUser(request);
        String action = PurchaseOrderRequestMapper.normalizeAction(request.getParameter("action"));
        int threshold = parseNonNegativeInt(request.getParameter("threshold"), DEFAULT_THRESHOLD);

        try {
            PurchaseOrderStatus targetStatus = PurchaseOrderRequestMapper.resolveCreateTargetStatus(action);
            PurchaseOrderSummaryDTO created = purchaseOrderDAO.createPurchaseOrder(
                    PurchaseOrderRequestMapper.parse(request),
                    currentUser,
                    targetStatus
            );

            log.info(
                    "Purchase order created: id={}, code={}, status={}, itemCount={}, totalQuantity={}, totalAmount={}",
                    created.getId(),
                    created.getPoCode(),
                    created.getStatus(),
                    created.getItemCount(),
                    created.getTotalQuantity(),
                    created.getTotalAmount()
            );

            String auditAction = targetStatus.isDraft() ? "STOCK_IMPORT_DRAFT_SAVED" : "STOCK_IMPORTED";
            AuditLogService.success(
                    AdminStockImportController.class,
                    currentUser,
                    "PRODUCT",
                    auditAction,
                    "PURCHASE_ORDER",
                    AuditLogService.describe(
                            "purchaseOrderId", created.getId(),
                            "poCode", created.getPoCode(),
                            "status", created.getStatus(),
                            "itemCount", created.getItemCount(),
                            "totalQuantity", created.getTotalQuantity(),
                            "supplierName", created.getSupplierName(),
                            "invoiceNumber", created.getInvoiceNumber(),
                            "totalAmount", created.getTotalAmount()
                    )
            );

            flash(
                    session,
                    targetStatus.isDraft()
                            ? "Đã lưu nháp phiếu nhập " + created.getPoCode() + "."
                            : "Đã lưu phiếu nhập " + created.getPoCode() + " với " + created.getItemCount() + " sản phẩm.",
                    "success"
            );
            response.sendRedirect(buildDetailRedirectUrl(request, created.getId()));
        } catch (IllegalArgumentException e) {
            log.warn("Stock import validation failed: {}", e.getMessage());
            AuditLogService.failure(
                    AdminStockImportController.class,
                    currentUser,
                    "PRODUCT",
                    "STOCK_IMPORT_FAILED",
                    "PURCHASE_ORDER",
                    AuditLogService.describe("action", action, "reason", e.getMessage())
            );
            flash(session, e.getMessage(), "error");
            response.sendRedirect(buildRedirectUrl(request, threshold));
        } catch (Exception e) {
            log.error("Stock import action failed: {}", action, e);
            AuditLogService.failure(
                    AdminStockImportController.class,
                    currentUser,
                    "PRODUCT",
                    "STOCK_IMPORT_FAILED",
                    "PURCHASE_ORDER",
                    AuditLogService.describe("action", action, "reason", e.getClass().getSimpleName(), "message", e.getMessage())
            );
            flash(session, "Có lỗi xảy ra: " + e.getMessage(), "error");
            response.sendRedirect(buildRedirectUrl(request, threshold));
        }
    }

    private String buildRedirectUrl(HttpServletRequest request, int threshold) {
        return request.getContextPath() + "/admin/stock-imports?threshold=" + threshold;
    }

    private String buildDetailRedirectUrl(HttpServletRequest request, long purchaseOrderId) {
        return request.getContextPath() + "/admin/purchase-order-detail?id=" + purchaseOrderId;
    }

    private void flash(HttpSession session, String message, String messageType) {
        session.setAttribute("message", message);
        session.setAttribute("messageType", messageType);
    }

    private int parseNonNegativeInt(String value, int defaultValue) {
        try {
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private User resolveCurrentAdminUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("user");
        if (user instanceof User) {
            return (User) user;
        }

        user = session.getAttribute("userInSession");
        if (user instanceof User) {
            return (User) user;
        }

        user = session.getAttribute("currentUser");
        return user instanceof User ? (User) user : null;
    }
}
