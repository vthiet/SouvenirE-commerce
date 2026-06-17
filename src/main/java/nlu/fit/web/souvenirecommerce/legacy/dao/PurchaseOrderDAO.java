package nlu.fit.web.souvenirecommerce.legacy.dao;

import nlu.fit.web.souvenirecommerce.features.dashboard.dto.PurchaseOrderImportForm;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.PurchaseOrderItemForm;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.PurchaseOrderItemSummaryDTO;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.PurchaseOrderSummaryDTO;
import nlu.fit.web.souvenirecommerce.legacy.utils.DBContext;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import nlu.fit.web.souvenirecommerce.model.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PurchaseOrderDAO {

    private static final DateTimeFormatter INVOICE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HISTORY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PurchaseOrderSummaryDTO createPurchaseOrder(PurchaseOrderImportForm form, User actor) {
        return createPurchaseOrder(form, actor, PurchaseOrderStatus.FINALIZED);
    }

    public PurchaseOrderSummaryDTO createPurchaseOrder(PurchaseOrderImportForm form, User actor, PurchaseOrderStatus targetStatus) {
        validateHeader(form);
        PurchaseOrderStatus effectiveStatus = normalizeStatus(targetStatus);
        if (effectiveStatus.isCancelled()) {
            throw new IllegalArgumentException("Không thể tạo phiếu nhập với trạng thái đã hủy.");
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<RequestedItem> requestedItems = normalizeRequestedItems(form.getItems());
                if (requestedItems.isEmpty()) {
                    throw new IllegalArgumentException("Vui lòng thêm ít nhất 1 sản phẩm vào phiếu nhập.");
                }

                Set<Long> productIds = extractProductIds(requestedItems);
                Map<Long, ProductSnapshot> snapshots = loadProductSnapshotsForUpdate(conn, productIds);
                List<ResolvedItem> resolvedItems = resolveRequestedItems(requestedItems, snapshots, Map.of(), false);
                Map<Long, Integer> targetStockByProduct = new LinkedHashMap<>();
                if (effectiveStatus.isFinalized()) {
                    for (ResolvedItem resolvedItem : resolvedItems) {
                        targetStockByProduct.put(resolvedItem.productId(), resolvedItem.stockAfter());
                    }
                }

                BigDecimal subtotalAmount = sumAmounts(resolvedItems);
                int totalQuantity = sumQuantities(resolvedItems);
                BigDecimal vatRate = normalizeRate(form.getVatRate());
                BigDecimal vatAmount = subtotalAmount.multiply(vatRate)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal totalAmount = subtotalAmount.add(vatAmount).setScale(2, RoundingMode.HALF_UP);
                BigDecimal averageUnitCost = totalQuantity > 0
                        ? subtotalAmount.divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                String poCode = generatePoCode();

                long purchaseOrderId = insertPurchaseOrderHeader(
                        conn,
                        poCode,
                        effectiveStatus,
                        form,
                        requestedItems.size(),
                        totalQuantity,
                        averageUnitCost,
                        subtotalAmount,
                        vatRate,
                        vatAmount,
                        totalAmount,
                        actor
                );

                insertPurchaseOrderItems(conn, purchaseOrderId, resolvedItems);
                if (effectiveStatus.isFinalized()) {
                    updateProductStocks(conn, targetStockByProduct);
                }

                conn.commit();
                return loadPurchaseOrderById(conn, purchaseOrderId);
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (Exception rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Không thể tạo phiếu nhập kho.", e);
        }
    }

    public PurchaseOrderSummaryDTO updatePurchaseOrder(long purchaseOrderId,
                                                       PurchaseOrderImportForm form,
                                                       User actor,
                                                       PurchaseOrderStatus targetStatus) {
        validateHeader(form);
        PurchaseOrderStatus effectiveTargetStatus = normalizeStatus(targetStatus);
        if (effectiveTargetStatus.isCancelled()) {
            throw new IllegalArgumentException("Không thể cập nhật phiếu nhập sang trạng thái đã hủy.");
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ExistingPurchaseOrder existing = loadExistingPurchaseOrderForUpdate(conn, purchaseOrderId);
                if (existing == null) {
                    throw new IllegalArgumentException("Phiếu nhập không tồn tại hoặc đã bị xóa.");
                }
                if (existing.status().isCancelled()) {
                    throw new IllegalArgumentException("Phiếu nhập đã bị hủy nên không thể chỉnh sửa.");
                }
                if (existing.status().isFinalized() && effectiveTargetStatus.isDraft()) {
                    throw new IllegalArgumentException("Không thể chuyển phiếu nhập đã hoàn tất về nháp.");
                }

                List<RequestedItem> requestedItems = normalizeRequestedItems(form.getItems());
                if (requestedItems.isEmpty()) {
                    throw new IllegalArgumentException("Vui lòng thêm ít nhất 1 sản phẩm vào phiếu nhập.");
                }

                Map<Long, Integer> oldQuantities = existing.oldQuantitiesByProductId();
                Map<Long, Integer> newQuantities = toQuantityMap(requestedItems);
                Set<Long> affectedProductIds = new HashSet<>();
                affectedProductIds.addAll(oldQuantities.keySet());
                affectedProductIds.addAll(newQuantities.keySet());

                Map<Long, ProductSnapshot> snapshots = loadProductSnapshotsForUpdate(conn, affectedProductIds);
                Map<Long, Integer> targetStockByProduct = new LinkedHashMap<>();
                List<ResolvedItem> resolvedItems;

                if (effectiveTargetStatus.isFinalized()) {
                    if (existing.status().isFinalized()) {
                        resolvedItems = resolveRequestedItems(requestedItems, snapshots, oldQuantities, true);
                        for (Long productId : affectedProductIds) {
                            ProductSnapshot snapshot = snapshots.get(productId);
                            if (snapshot == null) {
                                throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xóa.");
                            }
                            int oldQuantity = oldQuantities.getOrDefault(productId, 0);
                            int newQuantity = newQuantities.getOrDefault(productId, 0);
                            targetStockByProduct.put(productId, snapshot.stockQuantity() - oldQuantity + newQuantity);
                        }
                    } else {
                        resolvedItems = resolveRequestedItems(requestedItems, snapshots, Map.of(), false);
                        for (Map.Entry<Long, ProductSnapshot> entry : snapshots.entrySet()) {
                            int quantity = newQuantities.getOrDefault(entry.getKey(), 0);
                            if (quantity > 0) {
                                targetStockByProduct.put(entry.getKey(), entry.getValue().stockQuantity() + quantity);
                            }
                        }
                    }
                } else {
                    resolvedItems = resolveRequestedItems(requestedItems, snapshots, Map.of(), false);
                }

                BigDecimal subtotalAmount = sumAmounts(resolvedItems);
                int totalQuantity = sumQuantities(resolvedItems);
                BigDecimal vatRate = normalizeRate(form.getVatRate());
                BigDecimal vatAmount = subtotalAmount.multiply(vatRate)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal totalAmount = subtotalAmount.add(vatAmount).setScale(2, RoundingMode.HALF_UP);
                BigDecimal averageUnitCost = totalQuantity > 0
                        ? subtotalAmount.divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                if (effectiveTargetStatus.isFinalized() && !targetStockByProduct.isEmpty()) {
                    updateProductStocks(conn, targetStockByProduct);
                }

                deletePurchaseOrderItems(conn, purchaseOrderId);
                insertPurchaseOrderItems(conn, purchaseOrderId, resolvedItems);
                updatePurchaseOrderHeader(
                        conn,
                        purchaseOrderId,
                        effectiveTargetStatus,
                        form,
                        requestedItems.size(),
                        totalQuantity,
                        averageUnitCost,
                        subtotalAmount,
                        vatRate,
                        vatAmount,
                        totalAmount,
                        actor
                );

                conn.commit();
                return loadPurchaseOrderById(conn, purchaseOrderId);
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (Exception rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Không thể cập nhật phiếu nhập kho.", e);
        }
    }

    public PurchaseOrderSummaryDTO cancelPurchaseOrder(long purchaseOrderId, User actor) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ExistingPurchaseOrder existing = loadExistingPurchaseOrderForUpdate(conn, purchaseOrderId);
                if (existing == null) {
                    throw new IllegalArgumentException("Phiếu nhập không tồn tại hoặc đã bị xóa.");
                }
                if (existing.status().isCancelled()) {
                    throw new IllegalArgumentException("Phiếu nhập đã bị hủy.");
                }

                if (existing.status().isFinalized()) {
                    Map<Long, Integer> targetStockByProduct = new LinkedHashMap<>();
                    Set<Long> productIds = existing.oldQuantitiesByProductId().keySet();
                    Map<Long, ProductSnapshot> snapshots = loadProductSnapshotsForUpdate(conn, productIds);
                    for (Map.Entry<Long, Integer> entry : existing.oldQuantitiesByProductId().entrySet()) {
                        ProductSnapshot snapshot = snapshots.get(entry.getKey());
                        if (snapshot == null) {
                            throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xóa.");
                        }
                        targetStockByProduct.put(entry.getKey(), snapshot.stockQuantity() - entry.getValue());
                    }
                    if (!targetStockByProduct.isEmpty()) {
                        updateProductStocks(conn, targetStockByProduct);
                    }
                }

                updatePurchaseOrderStatusOnly(conn, purchaseOrderId, PurchaseOrderStatus.CANCELLED, actor);
                conn.commit();
                return loadPurchaseOrderById(conn, purchaseOrderId);
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (Exception rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Không thể hủy phiếu nhập kho.", e);
        }
    }

    public PurchaseOrderSummaryDTO getPurchaseOrderById(long purchaseOrderId) {
        try (Connection conn = DBContext.getConnection()) {
            return loadPurchaseOrderById(conn, purchaseOrderId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<PurchaseOrderSummaryDTO> getRecentPurchaseOrders(int limit) {
        int effectiveLimit = Math.max(1, limit);
        List<PurchaseOrderSummaryDTO> list = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT
                     po.id,
                     po.po_code,
                     po.status,
                     po.supplier_name,
                     po.supplier_tax_code,
                     po.supplier_address,
                     po.supplier_phone,
                     po.supplier_email,
                     po.invoice_number,
                     po.invoice_date,
                     po.contract_number,
                     po.delivery_note_number,
                     po.received_by,
                     po.item_count,
                     po.quantity,
                     po.subtotal_amount,
                     po.vat_rate,
                     po.vat_amount,
                     po.total_amount,
                     po.notes,
                     po.created_at,
                     COALESCE(NULLIF(u.full_name, ''), NULLIF(u.email, ''), 'System') AS created_by_label
                 FROM purchase_orders po
                 LEFT JOIN users u ON po.created_by = u.id
                 ORDER BY po.created_at DESC, po.id DESC
                 LIMIT ?
             """)) {

            ps.setInt(1, effectiveLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapHeader(rs));
                }
            }

            if (!list.isEmpty()) {
                Map<Long, List<PurchaseOrderItemSummaryDTO>> itemsByOrder = loadItemsByOrderIds(
                        conn,
                        list.stream().map(PurchaseOrderSummaryDTO::getId).toList()
                );
                for (PurchaseOrderSummaryDTO dto : list) {
                    dto.setItems(new ArrayList<>(itemsByOrder.getOrDefault(dto.getId(), List.of())));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private PurchaseOrderSummaryDTO loadPurchaseOrderById(Connection conn, long purchaseOrderId) throws Exception {
        PurchaseOrderSummaryDTO dto = null;
        String headerSql = """
            SELECT
                po.id,
                po.po_code,
                po.status,
                po.supplier_name,
                po.supplier_tax_code,
                po.supplier_address,
                po.supplier_phone,
                po.supplier_email,
                po.invoice_number,
                po.invoice_date,
                po.contract_number,
                po.delivery_note_number,
                po.received_by,
                po.item_count,
                po.quantity,
                po.subtotal_amount,
                po.vat_rate,
                po.vat_amount,
                po.total_amount,
                po.notes,
                po.created_at,
                COALESCE(NULLIF(u.full_name, ''), NULLIF(u.email, ''), 'System') AS created_by_label
            FROM purchase_orders po
            LEFT JOIN users u ON po.created_by = u.id
            WHERE po.id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
            ps.setLong(1, purchaseOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto = mapHeader(rs);
                }
            }
        }

        if (dto == null) {
            return null;
        }

        Map<Long, List<PurchaseOrderItemSummaryDTO>> itemsByOrder = loadItemsByOrderIds(conn, List.of(purchaseOrderId));
        dto.setItems(new ArrayList<>(itemsByOrder.getOrDefault(purchaseOrderId, List.of())));
        return dto;
    }

    private Map<Long, List<PurchaseOrderItemSummaryDTO>> loadItemsByOrderIds(Connection conn, List<Long> purchaseOrderIds) throws Exception {
        Map<Long, List<PurchaseOrderItemSummaryDTO>> result = new HashMap<>();
        if (purchaseOrderIds == null || purchaseOrderIds.isEmpty()) {
            return result;
        }

        StringBuilder sql = new StringBuilder("""
            SELECT
                poi.id,
                poi.purchase_order_id,
                poi.product_id,
                poi.product_name,
                poi.quantity,
                poi.unit_cost,
                poi.line_amount,
                poi.stock_before,
                poi.stock_after
            FROM purchase_order_items poi
            WHERE poi.purchase_order_id IN (
        """);
        appendPlaceholders(sql, purchaseOrderIds.size());
        sql.append(") ORDER BY poi.purchase_order_id DESC, poi.id ASC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (Long purchaseOrderId : purchaseOrderIds) {
                ps.setLong(index++, purchaseOrderId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseOrderItemSummaryDTO item = mapItem(rs);
                    result.computeIfAbsent(item.getPurchaseOrderId(), key -> new ArrayList<>()).add(item);
                }
            }
        }

        return result;
    }

    private ExistingPurchaseOrder loadExistingPurchaseOrderForUpdate(Connection conn, long purchaseOrderId) throws Exception {
        String headerSql = """
            SELECT id, status
            FROM purchase_orders
            WHERE id = ?
            FOR UPDATE
        """;

        PurchaseOrderStatus status;
        try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
            ps.setLong(1, purchaseOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                status = normalizeStatus(PurchaseOrderStatus.fromCode(rs.getString("status")));
            }
        }

        Map<Long, Integer> quantitiesByProductId = new LinkedHashMap<>();
        List<ExistingItem> items = new ArrayList<>();

        String itemSql = """
            SELECT
                id,
                purchase_order_id,
                product_id,
                product_name,
                quantity,
                unit_cost,
                line_amount,
                stock_before,
                stock_after
            FROM purchase_order_items
            WHERE purchase_order_id = ?
            ORDER BY id ASC
            FOR UPDATE
        """;

        try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
            ps.setLong(1, purchaseOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExistingItem item = new ExistingItem(
                            rs.getLong("id"),
                            rs.getLong("purchase_order_id"),
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_cost"),
                            rs.getBigDecimal("line_amount"),
                            rs.getInt("stock_before"),
                            rs.getInt("stock_after")
                    );
                    items.add(item);
                    quantitiesByProductId.put(item.productId(), item.quantity());
                }
            }
        }

        return new ExistingPurchaseOrder(purchaseOrderId, status, quantitiesByProductId, items);
    }

    private Map<Long, ProductSnapshot> loadProductSnapshotsForUpdate(Connection conn, Set<Long> productIds) throws Exception {
        Map<Long, ProductSnapshot> snapshots = new LinkedHashMap<>();
        if (productIds == null || productIds.isEmpty()) {
            return snapshots;
        }

        List<Long> sortedIds = new ArrayList<>(productIds);
        sortedIds.sort(Long::compareTo);

        StringBuilder sql = new StringBuilder("""
            SELECT id, name, stock_quantity
            FROM products
            WHERE id IN (
        """);
        appendPlaceholders(sql, sortedIds.size());
        sql.append(") ORDER BY id FOR UPDATE");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (Long productId : sortedIds) {
                ps.setLong(index++, productId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductSnapshot snapshot = new ProductSnapshot(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getInt("stock_quantity")
                    );
                    snapshots.put(snapshot.id(), snapshot);
                }
            }
        }

        if (snapshots.size() != sortedIds.size()) {
            for (Long productId : sortedIds) {
                if (!snapshots.containsKey(productId)) {
                    throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xóa.");
                }
            }
        }

        return snapshots;
    }

    private List<RequestedItem> normalizeRequestedItems(List<PurchaseOrderItemForm> requestedItems) {
        List<RequestedItem> normalized = new ArrayList<>();
        Set<Long> seenProductIds = new HashSet<>();

        if (requestedItems == null) {
            return normalized;
        }

        for (PurchaseOrderItemForm item : requestedItems) {
            if (item == null || isBlankRow(item)) {
                continue;
            }

            validateItem(item);
            if (!seenProductIds.add(item.getProductId())) {
                throw new IllegalArgumentException("Mỗi sản phẩm chỉ được xuất hiện một lần trong cùng phiếu nhập.");
            }

            normalized.add(new RequestedItem(
                    item.getProductId(),
                    item.getQuantity(),
                    normalizeMoney(item.getUnitCost())
            ));
        }

        return normalized;
    }

    private List<ResolvedItem> resolveRequestedItems(List<RequestedItem> requestedItems,
                                                     Map<Long, ProductSnapshot> snapshots,
                                                     Map<Long, Integer> oldQuantities,
                                                     boolean applyOldQuantities) {
        List<ResolvedItem> resolvedItems = new ArrayList<>();
        for (RequestedItem requestedItem : requestedItems) {
            ProductSnapshot snapshot = snapshots.get(requestedItem.productId());
            if (snapshot == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xóa.");
            }

            int oldQuantity = applyOldQuantities ? oldQuantities.getOrDefault(requestedItem.productId(), 0) : 0;
            int stockBefore = snapshot.stockQuantity() - oldQuantity;
            int stockAfter = stockBefore + requestedItem.quantity();
            BigDecimal lineAmount = requestedItem.unitCost()
                    .multiply(BigDecimal.valueOf(requestedItem.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            resolvedItems.add(new ResolvedItem(
                    requestedItem.productId(),
                    snapshot.name(),
                    requestedItem.quantity(),
                    requestedItem.unitCost(),
                    lineAmount,
                    stockBefore,
                    stockAfter
            ));
        }
        return resolvedItems;
    }

    private long insertPurchaseOrderHeader(Connection conn,
                                           String poCode,
                                           PurchaseOrderStatus status,
                                           PurchaseOrderImportForm form,
                                           int itemCount,
                                           int totalQuantity,
                                           BigDecimal averageUnitCost,
                                           BigDecimal subtotalAmount,
                                           BigDecimal vatRate,
                                           BigDecimal vatAmount,
                                           BigDecimal totalAmount,
                                           User actor) throws Exception {
        String sql = """
            INSERT INTO purchase_orders (
                po_code, status,
                supplier_name, supplier_tax_code, supplier_address,
                supplier_phone, supplier_email,
                invoice_number, invoice_date,
                contract_number, delivery_note_number,
                received_by, item_count,
                quantity, unit_cost,
                subtotal_amount, vat_rate, vat_amount, total_amount,
                product_id, product_name, stock_before, stock_after,
                notes, created_by
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            ps.setString(index++, poCode);
            ps.setString(index++, status.name());
            ps.setString(index++, form.getSupplierName());
            ps.setString(index++, form.getSupplierTaxCode());
            ps.setString(index++, form.getSupplierAddress());
            ps.setString(index++, normalizeBlank(form.getSupplierPhone()));
            ps.setString(index++, normalizeBlank(form.getSupplierEmail()));
            ps.setString(index++, form.getInvoiceNumber());
            ps.setDate(index++, Date.valueOf(form.getInvoiceDate()));
            ps.setString(index++, normalizeBlank(form.getContractNumber()));
            ps.setString(index++, normalizeBlank(form.getDeliveryNoteNumber()));
            ps.setString(index++, form.getReceivedBy());
            ps.setInt(index++, itemCount);
            ps.setInt(index++, totalQuantity);
            ps.setBigDecimal(index++, averageUnitCost);
            ps.setBigDecimal(index++, subtotalAmount);
            ps.setBigDecimal(index++, vatRate);
            ps.setBigDecimal(index++, vatAmount);
            ps.setBigDecimal(index++, totalAmount);
            ps.setNull(index++, Types.BIGINT);
            ps.setNull(index++, Types.VARCHAR);
            ps.setNull(index++, Types.INTEGER);
            ps.setNull(index++, Types.INTEGER);
            ps.setString(index++, normalizeBlank(form.getNotes()));
            if (actor != null && actor.getId() != null) {
                ps.setLong(index, actor.getId());
            } else {
                ps.setNull(index, Types.BIGINT);
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows <= 0) {
                throw new IllegalStateException("Không thể lưu phiếu nhập kho.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new IllegalStateException("Không thể lấy mã phiếu nhập kho vừa tạo.");
    }

    private void updatePurchaseOrderHeader(Connection conn,
                                           long purchaseOrderId,
                                           PurchaseOrderStatus status,
                                           PurchaseOrderImportForm form,
                                           int itemCount,
                                           int totalQuantity,
                                           BigDecimal averageUnitCost,
                                           BigDecimal subtotalAmount,
                                           BigDecimal vatRate,
                                           BigDecimal vatAmount,
                                           BigDecimal totalAmount,
                                           User actor) throws Exception {
        String sql = """
            UPDATE purchase_orders
            SET status = ?,
                supplier_name = ?,
                supplier_tax_code = ?,
                supplier_address = ?,
                supplier_phone = ?,
                supplier_email = ?,
                invoice_number = ?,
                invoice_date = ?,
                contract_number = ?,
                delivery_note_number = ?,
                received_by = ?,
                item_count = ?,
                quantity = ?,
                unit_cost = ?,
                subtotal_amount = ?,
                vat_rate = ?,
                vat_amount = ?,
                total_amount = ?,
                product_id = NULL,
                product_name = NULL,
                stock_before = NULL,
                stock_after = NULL,
                notes = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            ps.setString(index++, status.name());
            ps.setString(index++, form.getSupplierName());
            ps.setString(index++, form.getSupplierTaxCode());
            ps.setString(index++, form.getSupplierAddress());
            ps.setString(index++, normalizeBlank(form.getSupplierPhone()));
            ps.setString(index++, normalizeBlank(form.getSupplierEmail()));
            ps.setString(index++, form.getInvoiceNumber());
            ps.setDate(index++, Date.valueOf(form.getInvoiceDate()));
            ps.setString(index++, normalizeBlank(form.getContractNumber()));
            ps.setString(index++, normalizeBlank(form.getDeliveryNoteNumber()));
            ps.setString(index++, form.getReceivedBy());
            ps.setInt(index++, itemCount);
            ps.setInt(index++, totalQuantity);
            ps.setBigDecimal(index++, averageUnitCost);
            ps.setBigDecimal(index++, subtotalAmount);
            ps.setBigDecimal(index++, vatRate);
            ps.setBigDecimal(index++, vatAmount);
            ps.setBigDecimal(index++, totalAmount);
            ps.setString(index++, normalizeBlank(form.getNotes()));
            ps.setLong(index, purchaseOrderId);
            int updated = ps.executeUpdate();
            if (updated <= 0) {
                throw new IllegalStateException("Không thể cập nhật phiếu nhập kho.");
            }
        }
    }

    private void updatePurchaseOrderStatusOnly(Connection conn,
                                               long purchaseOrderId,
                                               PurchaseOrderStatus status,
                                               User actor) throws Exception {
        String sql = """
            UPDATE purchase_orders
            SET status = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, purchaseOrderId);
            int updated = ps.executeUpdate();
            if (updated <= 0) {
                throw new IllegalStateException("Không thể cập nhật trạng thái phiếu nhập.");
            }
        }
    }

    private void insertPurchaseOrderItems(Connection conn,
                                          long purchaseOrderId,
                                          List<ResolvedItem> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO purchase_order_items (
                purchase_order_id,
                product_id,
                product_name,
                quantity,
                unit_cost,
                line_amount,
                stock_before,
                stock_after
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ResolvedItem item : items) {
                int index = 1;
                ps.setLong(index++, purchaseOrderId);
                ps.setLong(index++, item.productId());
                ps.setString(index++, item.productName());
                ps.setInt(index++, item.quantity());
                ps.setBigDecimal(index++, item.unitCost());
                ps.setBigDecimal(index++, item.lineAmount());
                ps.setInt(index++, item.stockBefore());
                ps.setInt(index++, item.stockAfter());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            if (results.length != items.size()) {
                throw new IllegalStateException("Không thể lưu toàn bộ sản phẩm của phiếu nhập.");
            }
        }
    }

    private void deletePurchaseOrderItems(Connection conn, long purchaseOrderId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("""
            DELETE FROM purchase_order_items
            WHERE purchase_order_id = ?
        """)) {
            ps.setLong(1, purchaseOrderId);
            ps.executeUpdate();
        }
    }

    private void updateProductStocks(Connection conn, Map<Long, Integer> targetStockByProduct) throws Exception {
        if (targetStockByProduct == null || targetStockByProduct.isEmpty()) {
            return;
        }

        String sql = """
            UPDATE products
            SET stock_quantity = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map.Entry<Long, Integer> entry : targetStockByProduct.entrySet()) {
                ps.setInt(1, entry.getValue());
                ps.setLong(2, entry.getKey());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            if (results.length != targetStockByProduct.size()) {
                throw new IllegalStateException("Không thể cập nhật tồn kho sản phẩm.");
            }
        }
    }

    private BigDecimal sumAmounts(List<ResolvedItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (ResolvedItem item : items) {
            total = total.add(item.lineAmount());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private int sumQuantities(List<ResolvedItem> items) {
        int total = 0;
        for (ResolvedItem item : items) {
            total += item.quantity();
        }
        return total;
    }

    private Set<Long> extractProductIds(List<RequestedItem> requestedItems) {
        Set<Long> productIds = new LinkedHashSet<>();
        for (RequestedItem item : requestedItems) {
            productIds.add(item.productId());
        }
        return productIds;
    }

    private Map<Long, Integer> toQuantityMap(List<RequestedItem> requestedItems) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (RequestedItem item : requestedItems) {
            quantities.put(item.productId(), item.quantity());
        }
        return quantities;
    }

    private void validateHeader(PurchaseOrderImportForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu phiếu nhập kho không hợp lệ.");
        }
        if (form.getVatRate() != null && form.getVatRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Thuế VAT không được nhỏ hơn 0.");
        }
        requireText(form.getSupplierName(), "Vui lòng nhập tên nhà cung cấp.");
        requireText(form.getSupplierTaxCode(), "Vui lòng nhập mã số thuế nhà cung cấp.");
        requireText(form.getSupplierAddress(), "Vui lòng nhập địa chỉ nhà cung cấp.");
        requireText(form.getInvoiceNumber(), "Vui lòng nhập số hóa đơn.");
        if (form.getInvoiceDate() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày hóa đơn.");
        }
        requireText(form.getReceivedBy(), "Vui lòng nhập người nhận hàng.");
    }

    private void validateItem(PurchaseOrderItemForm item) {
        if (item.getProductId() == null || item.getProductId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm cho từng dòng nhập kho.");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng nhập của từng sản phẩm phải lớn hơn 0.");
        }
        if (item.getUnitCost() == null || item.getUnitCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá nhập của từng sản phẩm phải lớn hơn 0.");
        }
    }

    private boolean isBlankRow(PurchaseOrderItemForm item) {
        return item.getProductId() == null && item.getQuantity() == null && item.getUnitCost() == null;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeRate(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private PurchaseOrderStatus normalizeStatus(PurchaseOrderStatus status) {
        return status == null ? PurchaseOrderStatus.FINALIZED : status;
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private String resolveActorLabel(User actor) {
        if (actor == null) {
            return "System";
        }
        if (actor.getFullName() != null && !actor.getFullName().isBlank()) {
            return actor.getFullName();
        }
        if (actor.getEmail() != null && !actor.getEmail().isBlank()) {
            return actor.getEmail();
        }
        if (actor.getId() != null) {
            return "User#" + actor.getId();
        }
        return "System";
    }

    private String generatePoCode() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        return "PO-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + suffix;
    }

    private void appendPlaceholders(StringBuilder sql, int count) {
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
    }

    private PurchaseOrderSummaryDTO mapHeader(ResultSet rs) throws Exception {
        PurchaseOrderSummaryDTO dto = new PurchaseOrderSummaryDTO();
        dto.setId(rs.getLong("id"));
        dto.setPoCode(rs.getString("po_code"));
        dto.setStatus(normalizeBlank(rs.getString("status")));
        dto.setStatusLabel(resolveStatusLabel(dto.getStatus()));
        dto.setSupplierName(rs.getString("supplier_name"));
        dto.setSupplierTaxCode(rs.getString("supplier_tax_code"));
        dto.setSupplierAddress(rs.getString("supplier_address"));
        dto.setSupplierPhone(normalizeBlank(rs.getString("supplier_phone")));
        dto.setSupplierEmail(normalizeBlank(rs.getString("supplier_email")));
        dto.setInvoiceNumber(rs.getString("invoice_number"));
        Date invoiceDate = rs.getDate("invoice_date");
        dto.setInvoiceDate(invoiceDate == null ? null : invoiceDate.toLocalDate());
        dto.setInvoiceDateDisplay(invoiceDate == null ? "" : invoiceDate.toLocalDate().format(INVOICE_DATE_FORMAT));
        dto.setContractNumber(normalizeBlank(rs.getString("contract_number")));
        dto.setDeliveryNoteNumber(normalizeBlank(rs.getString("delivery_note_number")));
        dto.setReceivedBy(rs.getString("received_by"));
        dto.setItemCount(rs.getInt("item_count"));
        dto.setTotalQuantity(rs.getInt("quantity"));
        dto.setSubtotalAmount(rs.getBigDecimal("subtotal_amount"));
        dto.setVatRate(rs.getBigDecimal("vat_rate"));
        dto.setVatAmount(rs.getBigDecimal("vat_amount"));
        dto.setTotalAmount(rs.getBigDecimal("total_amount"));
        dto.setNotes(normalizeBlank(rs.getString("notes")));
        dto.setCreatedByLabel(normalizeBlank(rs.getString("created_by_label")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        dto.setCreatedAtDisplay(createdAt == null ? "" : createdAt.toLocalDateTime().format(HISTORY_DATE_FORMAT));
        return dto;
    }

    private PurchaseOrderItemSummaryDTO mapItem(ResultSet rs) throws Exception {
        PurchaseOrderItemSummaryDTO dto = new PurchaseOrderItemSummaryDTO();
        dto.setId(rs.getLong("id"));
        dto.setPurchaseOrderId(rs.getLong("purchase_order_id"));
        dto.setProductId(rs.getLong("product_id"));
        dto.setProductName(rs.getString("product_name"));
        dto.setQuantity(rs.getInt("quantity"));
        dto.setUnitCost(rs.getBigDecimal("unit_cost"));
        dto.setLineAmount(rs.getBigDecimal("line_amount"));
        dto.setStockBefore(rs.getInt("stock_before"));
        dto.setStockAfter(rs.getInt("stock_after"));
        return dto;
    }

    private String resolveStatusLabel(String statusCode) {
        PurchaseOrderStatus status = PurchaseOrderStatus.fromCode(statusCode);
        return status.getDescription();
    }

    private record ProductSnapshot(Long id, String name, int stockQuantity) {
    }

    private record RequestedItem(Long productId, int quantity, BigDecimal unitCost) {
    }

    private record ResolvedItem(Long productId,
                                String productName,
                                int quantity,
                                BigDecimal unitCost,
                                BigDecimal lineAmount,
                                int stockBefore,
                                int stockAfter) {
    }

    private record ExistingItem(Long id,
                                long purchaseOrderId,
                                Long productId,
                                String productName,
                                int quantity,
                                BigDecimal unitCost,
                                BigDecimal lineAmount,
                                int stockBefore,
                                int stockAfter) {
    }

    private record ExistingPurchaseOrder(long id,
                                         PurchaseOrderStatus status,
                                         Map<Long, Integer> oldQuantitiesByProductId,
                                         List<ExistingItem> items) {
    }
}
