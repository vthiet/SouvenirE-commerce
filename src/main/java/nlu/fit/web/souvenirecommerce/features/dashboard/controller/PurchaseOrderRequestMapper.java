package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.http.HttpServletRequest;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.PurchaseOrderImportForm;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.PurchaseOrderItemForm;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.PurchaseOrderItemSummaryDTO;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.PurchaseOrderSummaryDTO;
import nlu.fit.web.souvenirecommerce.model.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class PurchaseOrderRequestMapper {

    private PurchaseOrderRequestMapper() {
    }

    public static PurchaseOrderImportForm parse(HttpServletRequest request) {
        PurchaseOrderImportForm form = new PurchaseOrderImportForm();
        form.setSupplierName(parseRequiredText(request.getParameter("supplierName"), "Vui lòng nhập tên nhà cung cấp."));
        form.setSupplierTaxCode(parseRequiredText(request.getParameter("supplierTaxCode"), "Vui lòng nhập mã số thuế nhà cung cấp."));
        form.setSupplierAddress(parseRequiredText(request.getParameter("supplierAddress"), "Vui lòng nhập địa chỉ nhà cung cấp."));
        form.setSupplierPhone(parseOptionalText(request.getParameter("supplierPhone")));
        form.setSupplierEmail(parseOptionalText(request.getParameter("supplierEmail")));
        form.setInvoiceNumber(parseRequiredText(request.getParameter("invoiceNumber"), "Vui lòng nhập số hóa đơn."));
        form.setInvoiceDate(parseRequiredDate(request.getParameter("invoiceDate"), "Vui lòng chọn ngày hóa đơn."));
        form.setContractNumber(parseOptionalText(request.getParameter("contractNumber")));
        form.setDeliveryNoteNumber(parseOptionalText(request.getParameter("deliveryNoteNumber")));
        form.setReceivedBy(parseRequiredText(request.getParameter("receivedBy"), "Vui lòng nhập người nhận hàng."));
        form.setVatRate(parseOptionalMoney(request.getParameter("vatRate"), BigDecimal.ZERO));
        form.setNotes(parseOptionalText(request.getParameter("notes")));

        String[] productIds = request.getParameterValues("productId");
        String[] quantities = request.getParameterValues("quantity");
        String[] unitCosts = request.getParameterValues("unitCost");
        int itemCount = Math.max(length(productIds), Math.max(length(quantities), length(unitCosts)));
        for (int i = 0; i < itemCount; i++) {
            PurchaseOrderItemForm item = new PurchaseOrderItemForm();
            item.setProductId(parseOptionalLong(valueAt(productIds, i)));
            item.setQuantity(parseOptionalPositiveInt(valueAt(quantities, i)));
            item.setUnitCost(parseOptionalMoney(valueAt(unitCosts, i), null));
            form.getItems().add(item);
        }

        return form;
    }

    public static PurchaseOrderImportForm snapshot(HttpServletRequest request) {
        PurchaseOrderImportForm form = new PurchaseOrderImportForm();
        form.setSupplierName(parseOptionalText(request.getParameter("supplierName")));
        form.setSupplierTaxCode(parseOptionalText(request.getParameter("supplierTaxCode")));
        form.setSupplierAddress(parseOptionalText(request.getParameter("supplierAddress")));
        form.setSupplierPhone(parseOptionalText(request.getParameter("supplierPhone")));
        form.setSupplierEmail(parseOptionalText(request.getParameter("supplierEmail")));
        form.setInvoiceNumber(parseOptionalText(request.getParameter("invoiceNumber")));
        form.setInvoiceDate(parseOptionalDate(request.getParameter("invoiceDate")));
        form.setContractNumber(parseOptionalText(request.getParameter("contractNumber")));
        form.setDeliveryNoteNumber(parseOptionalText(request.getParameter("deliveryNoteNumber")));
        form.setReceivedBy(parseOptionalText(request.getParameter("receivedBy")));
        form.setVatRate(parseOptionalMoneyLenient(request.getParameter("vatRate")));
        form.setNotes(parseOptionalText(request.getParameter("notes")));

        String[] productIds = request.getParameterValues("productId");
        String[] quantities = request.getParameterValues("quantity");
        String[] unitCosts = request.getParameterValues("unitCost");
        int itemCount = Math.max(length(productIds), Math.max(length(quantities), length(unitCosts)));
        for (int i = 0; i < itemCount; i++) {
            PurchaseOrderItemForm item = new PurchaseOrderItemForm();
            item.setProductId(parseOptionalLongLenient(valueAt(productIds, i)));
            item.setQuantity(parseOptionalPositiveIntLenient(valueAt(quantities, i)));
            item.setUnitCost(parseOptionalMoneyLenient(valueAt(unitCosts, i)));
            form.getItems().add(item);
        }

        return form;
    }

    public static PurchaseOrderImportForm fromOrder(PurchaseOrderSummaryDTO order) {
        PurchaseOrderImportForm form = new PurchaseOrderImportForm();
        if (order == null) {
            return form;
        }

        form.setSupplierName(order.getSupplierName());
        form.setSupplierTaxCode(order.getSupplierTaxCode());
        form.setSupplierAddress(order.getSupplierAddress());
        form.setSupplierPhone(order.getSupplierPhone());
        form.setSupplierEmail(order.getSupplierEmail());
        form.setInvoiceNumber(order.getInvoiceNumber());
        form.setInvoiceDate(order.getInvoiceDate());
        form.setContractNumber(order.getContractNumber());
        form.setDeliveryNoteNumber(order.getDeliveryNoteNumber());
        form.setReceivedBy(order.getReceivedBy());
        form.setVatRate(order.getVatRate());
        form.setNotes(order.getNotes());

        if (order.getItems() != null) {
            for (PurchaseOrderItemSummaryDTO item : order.getItems()) {
                PurchaseOrderItemForm mappedItem = new PurchaseOrderItemForm();
                mappedItem.setProductId(item.getProductId());
                mappedItem.setQuantity(item.getQuantity());
                mappedItem.setUnitCost(item.getUnitCost());
                form.getItems().add(mappedItem);
            }
        }

        return form;
    }

    public static PurchaseOrderStatus resolveCreateTargetStatus(String action) {
        String normalized = normalizeAction(action);
        return switch (normalized) {
            case "", "import", "finalize", "finalise", "save", "savefinalize" -> PurchaseOrderStatus.FINALIZED;
            case "savedraft", "draft" -> PurchaseOrderStatus.DRAFT;
            default -> throw new IllegalArgumentException("Hành động nhập kho không hợp lệ.");
        };
    }

    public static PurchaseOrderStatus resolveUpdateTargetStatus(String action, PurchaseOrderStatus currentStatus) {
        String normalized = normalizeAction(action);
        return switch (normalized) {
            case "save", "update", "savedraft", "draft" -> currentStatus == null ? PurchaseOrderStatus.FINALIZED : currentStatus;
            case "finalize", "finalise", "import" -> PurchaseOrderStatus.FINALIZED;
            default -> throw new IllegalArgumentException("Hành động cập nhật phiếu nhập không hợp lệ.");
        };
    }

    public static boolean isCancelAction(String action) {
        String normalized = normalizeAction(action);
        return "delete".equals(normalized) || "remove".equals(normalized) || "cancel".equals(normalized);
    }

    public static String normalizeAction(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String parseRequiredText(String value, String message) {
        String parsed = parseOptionalText(value);
        if (parsed == null) {
            throw new IllegalArgumentException(message);
        }
        return parsed;
    }

    private static String parseOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String parsed = value.trim();
        return parsed.isEmpty() ? null : parsed;
    }

    private static Long parseOptionalLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }
    }

    private static Long parseOptionalLongLenient(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseOptionalPositiveInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
        }
    }

    private static Integer parseOptionalPositiveIntLenient(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal parseOptionalMoney(String value, BigDecimal defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            BigDecimal parsed = new BigDecimal(value.trim());
            if (parsed.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Giá trị tiền không được nhỏ hơn 0.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá trị tiền không hợp lệ.");
        }
    }

    private static BigDecimal parseOptionalMoneyLenient(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            BigDecimal parsed = new BigDecimal(value.trim());
            return parsed.compareTo(BigDecimal.ZERO) >= 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate parseRequiredDate(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private static LocalDate parseOptionalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String valueAt(String[] values, int index) {
        if (values == null || index < 0 || index >= values.length) {
            return null;
        }
        return values[index];
    }

    private static int length(String[] values) {
        return values == null ? 0 : values.length;
    }
}
