package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.utils.PermissionHelper;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.PurchaseOrderImportForm;
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

@WebServlet(urlPatterns = {"/admin/purchase-order-detail", "/admin/purchase-order-detail/"})
public class AdminPurchaseOrderDetailController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminPurchaseOrderDetailController.class);

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

        long purchaseOrderId = parseRequiredId(request, response);
        if (purchaseOrderId <= 0) {
            return;
        }

        if (!PermissionHelper.hasPermission(request, "product", "read")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem phiếu nhập.");
            return;
        }

        PurchaseOrderSummaryDTO order = purchaseOrderDAO.getPurchaseOrderById(purchaseOrderId);
        if (order == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Phiếu nhập không tồn tại.");
            return;
        }

        boolean editMode = "edit".equalsIgnoreCase(request.getParameter("mode"));
        boolean canUpdatePurchaseOrder = PermissionHelper.hasPermission(request, "product", "update");
        if (editMode && !canUpdatePurchaseOrder) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền sửa phiếu nhập.");
            return;
        }

        PurchaseOrderStatus status = PurchaseOrderStatus.fromCode(order.getStatus());
        if (editMode && status.isCancelled()) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "Phiếu nhập đã bị hủy nên không thể chỉnh sửa.");
            return;
        }

        request.setAttribute("activePage", "stock-imports");
        request.setAttribute("purchaseOrder", order);
        request.setAttribute("purchaseOrderId", order.getId());
        request.setAttribute("editMode", editMode);
        request.setAttribute("canUpdatePurchaseOrder", canUpdatePurchaseOrder && !status.isCancelled());
        request.setAttribute("canCancelPurchaseOrder", canUpdatePurchaseOrder && !status.isCancelled());
        request.setAttribute("availableProducts", editMode ? productDAO.getAllProducts() : List.of());

        if (editMode) {
            request.setAttribute("purchaseOrderForm", PurchaseOrderRequestMapper.fromOrder(order));
        }

        loadFlash(request);
        log.info("Loaded purchase order detail id={} editMode={}", purchaseOrderId, editMode);
        request.getRequestDispatcher("/admin/purchase-order-detail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        request.setCharacterEncoding("UTF-8");

        long purchaseOrderId = parseRequiredId(request, response);
        if (purchaseOrderId <= 0) {
            return;
        }

        if (!PermissionHelper.hasPermission(request, "product", "update")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền cập nhật phiếu nhập.");
            return;
        }

        User currentUser = resolveCurrentAdminUser(request);
        String action = PurchaseOrderRequestMapper.normalizeAction(request.getParameter("action"));
        PurchaseOrderImportForm submittedSnapshot = PurchaseOrderRequestMapper.snapshot(request);

        PurchaseOrderSummaryDTO existingOrder = purchaseOrderDAO.getPurchaseOrderById(purchaseOrderId);
        if (existingOrder == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Phiếu nhập không tồn tại.");
            return;
        }

        PurchaseOrderStatus currentStatus = PurchaseOrderStatus.fromCode(existingOrder.getStatus());
        if (currentStatus.isCancelled()) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "Phiếu nhập đã bị hủy nên không thể thao tác.");
            return;
        }

        if (PurchaseOrderRequestMapper.isCancelAction(action)) {
            try {
                PurchaseOrderSummaryDTO cancelled = purchaseOrderDAO.cancelPurchaseOrder(purchaseOrderId, currentUser);
                AuditLogService.success(
                        AdminPurchaseOrderDetailController.class,
                        currentUser,
                        "PRODUCT",
                        "PURCHASE_ORDER_CANCELLED",
                        "PURCHASE_ORDER",
                        AuditLogService.describe(
                                "purchaseOrderId", purchaseOrderId,
                                "poCode", cancelled != null ? cancelled.getPoCode() : existingOrder.getPoCode(),
                                "status", "CANCELLED"
                        )
                );
                flash(request.getSession(), "Đã hủy phiếu nhập " + existingOrder.getPoCode() + ".", "success");
                response.sendRedirect(buildDetailRedirectUrl(request, purchaseOrderId));
            } catch (Exception e) {
                AuditLogService.failure(
                        AdminPurchaseOrderDetailController.class,
                        currentUser,
                        "PRODUCT",
                        "PURCHASE_ORDER_CANCEL_FAILED",
                        "PURCHASE_ORDER",
                        AuditLogService.describe(
                                "purchaseOrderId", purchaseOrderId,
                                "poCode", existingOrder.getPoCode(),
                                "action", action,
                                "reason", e.getClass().getSimpleName(),
                                "message", e.getMessage()
                        )
                );
                flash(request.getSession(), "Không thể hủy phiếu nhập: " + e.getMessage(), "error");
                response.sendRedirect(buildDetailRedirectUrl(request, purchaseOrderId));
            }
            return;
        }

        try {
            PurchaseOrderImportForm submittedForm = PurchaseOrderRequestMapper.parse(request);
            PurchaseOrderStatus targetStatus = PurchaseOrderRequestMapper.resolveUpdateTargetStatus(action, currentStatus);
            PurchaseOrderSummaryDTO updated = purchaseOrderDAO.updatePurchaseOrder(
                    purchaseOrderId,
                    submittedForm,
                    currentUser,
                    targetStatus
            );

            AuditLogService.success(
                    AdminPurchaseOrderDetailController.class,
                    currentUser,
                    "PRODUCT",
                    targetStatus.isDraft() ? "PURCHASE_ORDER_SAVED_AS_DRAFT" : "PURCHASE_ORDER_UPDATED",
                    "PURCHASE_ORDER",
                    AuditLogService.describe(
                            "purchaseOrderId", updated.getId(),
                            "poCode", updated.getPoCode(),
                            "status", updated.getStatus(),
                            "itemCount", updated.getItemCount(),
                            "totalQuantity", updated.getTotalQuantity(),
                            "totalAmount", updated.getTotalAmount()
                    )
            );

            flash(
                    request.getSession(),
                    targetStatus.isDraft()
                            ? "Đã lưu nháp phiếu nhập " + updated.getPoCode() + "."
                            : "Đã cập nhật phiếu nhập " + updated.getPoCode() + ".",
                    "success"
            );
            response.sendRedirect(buildDetailRedirectUrl(request, purchaseOrderId));
        } catch (IllegalArgumentException e) {
            AuditLogService.failure(
                    AdminPurchaseOrderDetailController.class,
                    currentUser,
                    "PRODUCT",
                    "PURCHASE_ORDER_UPDATE_FAILED",
                    "PURCHASE_ORDER",
                    AuditLogService.describe(
                            "purchaseOrderId", purchaseOrderId,
                            "poCode", existingOrder.getPoCode(),
                            "action", action,
                            "reason", e.getMessage()
                    )
            );
            renderEditMode(request, response, existingOrder, submittedSnapshot, e.getMessage(), "error");
        } catch (Exception e) {
            AuditLogService.failure(
                    AdminPurchaseOrderDetailController.class,
                    currentUser,
                    "PRODUCT",
                    "PURCHASE_ORDER_UPDATE_FAILED",
                    "PURCHASE_ORDER",
                    AuditLogService.describe(
                            "purchaseOrderId", purchaseOrderId,
                            "poCode", existingOrder.getPoCode(),
                            "action", action,
                            "reason", e.getClass().getSimpleName(),
                            "message", e.getMessage()
                    )
            );
            renderEditMode(request, response, existingOrder, submittedSnapshot, "Có lỗi xảy ra: " + e.getMessage(), "error");
        }
    }

    private void renderEditMode(HttpServletRequest request,
                                HttpServletResponse response,
                                PurchaseOrderSummaryDTO existingOrder,
                                PurchaseOrderImportForm submittedForm,
                                String message,
                                String messageType) throws ServletException, IOException {
        PurchaseOrderStatus status = PurchaseOrderStatus.fromCode(existingOrder.getStatus());
        if (status.isCancelled()) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "Phiếu nhập đã bị hủy nên không thể chỉnh sửa.");
            return;
        }

        request.setAttribute("activePage", "stock-imports");
        request.setAttribute("purchaseOrder", existingOrder);
        request.setAttribute("purchaseOrderId", existingOrder.getId());
        request.setAttribute("editMode", true);
        request.setAttribute("canUpdatePurchaseOrder", true);
        request.setAttribute("canCancelPurchaseOrder", true);
        request.setAttribute("availableProducts", productDAO.getAllProducts());
        request.setAttribute("purchaseOrderForm", submittedForm != null ? submittedForm : PurchaseOrderRequestMapper.fromOrder(existingOrder));
        request.setAttribute("message", message);
        request.setAttribute("messageType", messageType);
        request.setAttribute("submittedAction", request.getParameter("action"));
        request.getRequestDispatcher("/admin/purchase-order-detail.jsp").forward(request, response);
    }

    private String buildDetailRedirectUrl(HttpServletRequest request, long purchaseOrderId) {
        return request.getContextPath() + "/admin/purchase-order-detail?id=" + purchaseOrderId;
    }

    private void loadFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        String message = (String) session.getAttribute("message");
        String messageType = (String) session.getAttribute("messageType");
        if (message != null) {
            request.setAttribute("message", message);
            request.setAttribute("messageType", messageType);
            session.removeAttribute("message");
            session.removeAttribute("messageType");
        }
    }

    private long parseRequiredId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String value = request.getParameter("id");
        if (value == null || value.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã phiếu nhập.");
            return -1;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã phiếu nhập không hợp lệ.");
            return -1;
        }
    }

    private void flash(HttpSession session, String message, String messageType) {
        session.setAttribute("message", message);
        session.setAttribute("messageType", messageType);
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
