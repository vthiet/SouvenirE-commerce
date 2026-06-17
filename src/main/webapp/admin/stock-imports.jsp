<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="common/admin-access-guard.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activePage" value="stock-imports" scope="request" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Nhập hàng - Admin</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-dashboard.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-pages.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-products.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<div class="admin-shell">
    <div class="sidebar-backdrop" data-sidebar-close></div>
    <jsp:include page="common/admin-sidebar.jsp" />

    <div class="admin-main">
        <jsp:include page="common/admin-topbar.jsp" />

        <main class="dashboard-content">
            <div class="container-fluid px-3 px-lg-4 py-4">
                <div class="content-header d-flex flex-wrap justify-content-between align-items-start gap-3">
                    <div>
                        <h1>Nhập hàng</h1>
                        <p class="text-muted mb-0">Tạo phiếu nhập có đầy đủ chứng từ pháp lý và nhiều sản phẩm trong cùng một lần nhập.</p>
                    </div>
                    <a href="${ctx}/admin/products" class="btn btn-outline-secondary btn-sm">
                        <i class="fas fa-box-seam me-1"></i> Quay lại sản phẩm
                    </a>
                </div>

                <c:if test="${not empty message}">
                    <div class="alert alert-${messageType}" role="alert">
                        <c:out value="${message}" />
                    </div>
                </c:if>

                <div class="row g-3 mb-3">
                    <div class="col-12 col-md-4">
                        <div class="card h-100">
                            <div class="card-body">
                                <div class="text-muted small mb-1">Sản phẩm cần nhập</div>
                                <div class="h3 mb-0">${productCount}</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-12 col-md-4">
                        <div class="card h-100">
                            <div class="card-body">
                                <div class="text-muted small mb-1">Ngưỡng cảnh báo</div>
                                <div class="h3 mb-0">${threshold}</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-12 col-md-4">
                        <div class="card h-100">
                            <div class="card-body">
                                <div class="text-muted small mb-1">Phiếu nhập</div>
                                <div class="mb-0">Hóa đơn, hợp đồng, phiếu giao nhận, người nhận hàng và VAT.</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h3 class="mb-0">Danh sách cần nhập kho</h3>
                        <span class="text-muted">Tồn kho thấp hơn hoặc bằng ${threshold}</span>
                    </div>
                    <div class="table-container">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th class="product-col-id">ID</th>
                                <th>Sản phẩm</th>
                                <th class="product-col-stock">Tồn kho hiện tại</th>
                                <th class="product-col-actions">Thao tác</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${stockProducts}" var="p">
                                <c:set var="resolvedImageUrl" value="${p.imageUrl}" />
                                <c:choose>
                                    <c:when test="${empty p.imageUrl}">
                                        <c:set var="resolvedImageUrl" value="https://placehold.co/50x50?text=No+image" />
                                    </c:when>
                                    <c:when test="${fn:startsWith(p.imageUrl, 'http://') or fn:startsWith(p.imageUrl, 'https://') or fn:startsWith(p.imageUrl, 'data:')}">
                                        <c:set var="resolvedImageUrl" value="${p.imageUrl}" />
                                    </c:when>
                                    <c:when test="${fn:startsWith(p.imageUrl, ctx)}">
                                        <c:set var="resolvedImageUrl" value="${p.imageUrl}" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="resolvedImageUrl" value="${ctx}/${p.imageUrl}" />
                                    </c:otherwise>
                                </c:choose>
                                <tr>
                                    <td>${p.id}</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3">
                                            <img src="${resolvedImageUrl}"
                                                 alt="${p.name}"
                                                 class="product-thumb"
                                                 onerror="this.src='https://placehold.co/50x50?text=No+image'">
                                            <div>
                                                <div class="fw-semibold"><c:out value="${p.name}" /></div>
                                                <div class="text-muted small">
                                                    Giá bán hiện tại: <fmt:formatNumber value="${p.originalPrice}" pattern="#,###"/>đ
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="product-center">
                                        <span class="badge ${p.stockQuantity == 0 ? 'bg-danger' : 'bg-warning text-dark'}">${p.stockQuantity}</span>
                                    </td>
                                    <td>
                                        <button
                                                type="button"
                                                class="btn btn-primary btn-sm w-100 js-open-stock-modal"
                                                data-product-id="${p.id}"
                                                data-product-name="<c:out value='${p.name}' />"
                                                data-product-image="<c:out value='${resolvedImageUrl}' />"
                                                data-product-stock="${p.stockQuantity}"
                                                data-product-price="${p.originalPrice}">
                                            <i class="fas fa-box-open me-1"></i> Nhập kho
                                        </button>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty stockProducts}">
                                <tr>
                                    <td colspan="4" class="text-center text-muted py-5">
                                        Không có sản phẩm nào nằm trong ngưỡng cần nhập kho.
                                    </td>
                                </tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h3 class="mb-0">Phiếu nhập gần đây</h3>
                        <span class="text-muted">Theo dõi lịch sử nhập kho đã lưu vào DB</span>
                    </div>
                    <div class="table-container">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Mã phiếu</th>
                                <th>Trạng thái</th>
                                <th>Sản phẩm trong phiếu</th>
                                <th>Nhà cung cấp</th>
                                <th>Hóa đơn</th>
                                <th>SL</th>
                                <th>Tổng tiền</th>
                                <th>Người nhập</th>
                                <th>Thời gian</th>
                                <th>Thao tác</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${recentPurchaseOrders}" var="order">
                                <tr>
                                    <td><c:out value="${order.poCode}" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${order.status == 'DRAFT'}">
                                                <span class="badge bg-warning text-dark"><c:out value="${order.statusLabel}" /></span>
                                            </c:when>
                                            <c:when test="${order.status == 'FINALIZED'}">
                                                <span class="badge bg-success"><c:out value="${order.statusLabel}" /></span>
                                            </c:when>
                                            <c:when test="${order.status == 'CANCELLED'}">
                                                <span class="badge bg-danger"><c:out value="${order.statusLabel}" /></span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary"><c:out value="${order.statusLabel}" /></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="fw-semibold">
                                            ${order.itemCount} dòng, ${order.totalQuantity} sản phẩm
                                        </div>
                                        <details class="mt-1">
                                            <summary class="small text-primary" style="cursor:pointer;">Xem danh sách sản phẩm</summary>
                                            <ul class="list-unstyled small mt-2 mb-0">
                                                <c:forEach items="${order.items}" var="item">
                                                    <li class="d-flex justify-content-between gap-2 py-1 border-top">
                                                        <span><c:out value="${item.productName}" /></span>
                                                        <span>x${item.quantity}</span>
                                                    </li>
                                                </c:forEach>
                                            </ul>
                                        </details>
                                    </td>
                                    <td>
                                        <div><c:out value="${order.supplierName}" /></div>
                                        <div class="text-muted small">MST: <c:out value="${order.supplierTaxCode}" /></div>
                                    </td>
                                    <td>
                                        <div><c:out value="${order.invoiceNumber}" /></div>
                                        <div class="text-muted small">${order.invoiceDateDisplay}</div>
                                    </td>
                                    <td class="product-center">${order.totalQuantity}</td>
                                    <td class="product-center">
                                        <fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>đ
                                    </td>
                                    <td>
                                        <div><c:out value="${order.createdByLabel}" /></div>
                                        <div class="text-muted small"><c:out value="${order.receivedBy}" /></div>
                                    </td>
                                    <td>${order.createdAtDisplay}</td>
                                    <td>
                                        <div class="d-grid gap-2">
                                            <a href="${ctx}/admin/purchase-order-detail?id=${order.id}" class="btn btn-outline-primary btn-sm">
                                                <i class="fas fa-eye me-1"></i> Xem chi tiết
                                            </a>
                                            <c:if test="${order.status == 'DRAFT'}">
                                                <a href="${ctx}/admin/purchase-order-detail?id=${order.id}&mode=edit" class="btn btn-primary btn-sm">
                                                    <i class="fas fa-pen me-1"></i> Sửa nháp
                                                </a>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty recentPurchaseOrders}">
                                <tr>
                                    <td colspan="10" class="text-center text-muted py-5">
                                        Chưa có phiếu nhập nào được tạo.
                                    </td>
                                </tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<div class="modal fade" id="stockImportModal" tabindex="-1" aria-labelledby="stockImportModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable stock-import-modal-dialog">
        <div class="modal-content stock-import-modal-content">
            <div class="modal-header">
                <div>
                    <h5 class="modal-title" id="stockImportModalLabel">Tạo phiếu nhập kho</h5>
                    <p class="text-muted mb-0">Nhập đầy đủ chứng từ pháp lý, sau đó thêm một hoặc nhiều sản phẩm vào phiếu.</p>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
            </div>
            <form id="stockImportForm" class="stock-import-form" method="post" action="${ctx}/admin/stock-imports">
                <div class="modal-body stock-import-modal-body">
                    <input type="hidden" name="threshold" value="${threshold}">

                    <div class="stock-import-form-layout">
                        <aside class="stock-import-summary-column">
                            <div class="card h-100 border-0 bg-light stock-import-card stock-import-summary-card">
                                <div class="card-body">
                                    <div class="d-flex align-items-center gap-3 mb-3">
                                        <img id="stockImportProductImage"
                                             src="https://placehold.co/120x120?text=Product"
                                             alt="Xem trước sản phẩm"
                                             class="rounded border"
                                             style="width: 96px; height: 96px; object-fit: cover;">
                                        <div class="min-w-0">
                                            <div class="text-muted small">Sản phẩm mở nhanh</div>
                                            <h4 class="mb-1 text-truncate" id="stockImportProductName">Chưa chọn sản phẩm</h4>
                                            <div class="text-muted small">Tồn kho hiện tại: <strong id="stockImportCurrentStock">-</strong></div>
                                            <div class="text-muted small">Giá tham chiếu: <strong id="stockImportReferencePrice">-</strong></div>
                                        </div>
                                    </div>

                                    <div class="mb-3">
                                        <label class="form-label" for="vatRate">VAT (%)</label>
                                        <input type="number" class="form-control" id="vatRate" name="vatRate" min="0" max="100" step="0.01" value="0">
                                    </div>

                                    <div class="border rounded p-3 bg-white">
                                        <div class="d-flex justify-content-between mb-2">
                                            <span class="text-muted">Số dòng</span>
                                            <strong id="purchaseOrderItemCount">0</strong>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span class="text-muted">Tổng số lượng</span>
                                            <strong id="purchaseOrderTotalQuantity">0</strong>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span class="text-muted">Tạm tính</span>
                                            <strong id="purchaseOrderSubtotal">0đ</strong>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span class="text-muted">VAT</span>
                                            <strong id="purchaseOrderVatAmount">0đ</strong>
                                        </div>
                                        <div class="d-flex justify-content-between">
                                            <span class="text-muted">Tổng cộng</span>
                                            <strong id="purchaseOrderTotal">0đ</strong>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </aside>

                        <section class="stock-import-details-grid">
                            <div class="card border-0 bg-light stock-import-card">
                                <div class="card-body">
                                    <h5 class="card-title">Thông tin nhà cung cấp</h5>
                                    <div class="row g-3">
                                        <div class="col-12 col-md-6">
                                            <label class="form-label" for="supplierName">Tên nhà cung cấp *</label>
                                            <input type="text" class="form-control" id="supplierName" name="supplierName" required>
                                        </div>
                                        <div class="col-12 col-md-6">
                                            <label class="form-label" for="supplierTaxCode">Mã số thuế *</label>
                                            <input type="text" class="form-control" id="supplierTaxCode" name="supplierTaxCode" required>
                                        </div>
                                        <div class="col-12">
                                            <label class="form-label" for="supplierAddress">Địa chỉ nhà cung cấp *</label>
                                            <input type="text" class="form-control" id="supplierAddress" name="supplierAddress" required>
                                        </div>
                                        <div class="col-12 col-md-6">
                                            <label class="form-label" for="supplierPhone">Số điện thoại</label>
                                            <input type="text" class="form-control" id="supplierPhone" name="supplierPhone">
                                        </div>
                                        <div class="col-12 col-md-6">
                                            <label class="form-label" for="supplierEmail">Email</label>
                                            <input type="email" class="form-control" id="supplierEmail" name="supplierEmail">
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="card border-0 bg-light stock-import-card">
                                <div class="card-body">
                                    <h5 class="card-title">Chứng từ pháp lý</h5>
                                    <div class="row g-3">
                                        <div class="col-12 col-md-6">
                                            <label class="form-label" for="invoiceNumber">Số hóa đơn *</label>
                                            <input type="text" class="form-control" id="invoiceNumber" name="invoiceNumber" required>
                                        </div>
                                        <div class="col-12 col-md-6">
                                            <label class="form-label" for="invoiceDate">Ngày hóa đơn *</label>
                                            <input type="date" class="form-control" id="invoiceDate" name="invoiceDate" required>
                                        </div>
                                        <div class="col-12 col-md-6">
                                            <label class="form-label" for="contractNumber">Số hợp đồng</label>
                                            <input type="text" class="form-control" id="contractNumber" name="contractNumber">
                                        </div>
                                        <div class="col-12 col-md-6">
                                            <label class="form-label" for="deliveryNoteNumber">Số phiếu giao nhận</label>
                                            <input type="text" class="form-control" id="deliveryNoteNumber" name="deliveryNoteNumber">
                                        </div>
                                        <div class="col-12 col-md-6">
                                            <label class="form-label" for="receivedBy">Người nhận hàng *</label>
                                            <input type="text" class="form-control" id="receivedBy" name="receivedBy" required>
                                        </div>
                                        <div class="col-12 col-md-6">
                                            <label class="form-label" for="notes">Ghi chú</label>
                                            <input type="text" class="form-control" id="notes" name="notes">
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="card mt-1 border-0 bg-light stock-import-card stock-import-items-card">
                                <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
                                    <div>
                                        <h5 class="mb-0">Sản phẩm trong phiếu</h5>
                                        <div class="text-muted small">Mỗi sản phẩm chỉ nên xuất hiện một lần trong phiếu nhập.</div>
                                    </div>
                                    <button type="button" class="btn btn-outline-primary btn-sm" id="addPurchaseOrderItemBtn">
                                        <i class="fas fa-plus me-1"></i> Thêm dòng sản phẩm
                                    </button>
                                </div>
                                <div class="card-body">
                                    <div id="purchaseOrderItemList" class="d-grid gap-3"></div>
                                </div>
                            </div>
                        </section>
                    </div>
                </div>
                <div class="modal-footer stock-import-modal-footer">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-primary" name="action" value="finalize">
                        <i class="fas fa-save me-1"></i> Lưu phiếu nhập
                    </button>
                    <button type="submit" class="btn btn-outline-primary" name="action" value="saveDraft">
                        <i class="fas fa-file-pen me-1"></i> Lưu nháp
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<template id="purchaseOrderItemTemplate">
    <div class="purchase-order-item border rounded-3 p-3 bg-white shadow-sm" data-item-row>
        <div class="purchase-order-item-grid">
            <div class="purchase-order-item-product">
                <label class="form-label">Sản phẩm *</label>
                <select class="form-select js-item-product" name="productId" required>
                    <option value="">-- Chọn sản phẩm --</option>
                    <c:forEach items="${availableProducts}" var="optionProduct">
                        <option value="${optionProduct.id}"
                                data-stock="${optionProduct.stockQuantity}"
                                data-price="${optionProduct.originalPrice}">
                            <c:out value="${optionProduct.name}" />
                        </option>
                    </c:forEach>
                </select>
                <div class="form-text js-item-product-meta">Chọn sản phẩm từ danh sách.</div>
            </div>

            <div class="purchase-order-item-side">
                <div class="purchase-order-item-controls">
                    <div>
                        <label class="form-label">Số lượng *</label>
                        <input type="number" class="form-control js-item-quantity" name="quantity" min="1" step="1" value="1" required>
                    </div>
                    <div>
                        <label class="form-label">Giá nhập *</label>
                        <input type="number" class="form-control js-item-unit-cost" name="unitCost" min="0" step="0.01" required>
                    </div>
                </div>
                <div class="purchase-order-item-meta-row">
                    <div class="purchase-order-item-stock">
                        <div class="small text-muted">Dự kiến tồn kho</div>
                        <div class="fw-semibold js-item-stock-summary">-</div>
                        <div class="small text-muted">Thành tiền: <span class="fw-semibold js-item-line-total">0đ</span></div>
                    </div>
                    <div class="purchase-order-item-actions">
                        <button type="button" class="btn btn-outline-danger btn-sm js-remove-item">Xóa</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>
<script>
    const stockImportModalEl = document.getElementById('stockImportModal');
    const stockImportModal = new bootstrap.Modal(stockImportModalEl);
    const stockImportForm = document.getElementById('stockImportForm');
    const stockImportProductImage = document.getElementById('stockImportProductImage');
    const stockImportProductName = document.getElementById('stockImportProductName');
    const stockImportCurrentStock = document.getElementById('stockImportCurrentStock');
    const stockImportReferencePrice = document.getElementById('stockImportReferencePrice');
    const vatRateInput = document.getElementById('vatRate');
    const purchaseOrderItemCount = document.getElementById('purchaseOrderItemCount');
    const purchaseOrderTotalQuantity = document.getElementById('purchaseOrderTotalQuantity');
    const purchaseOrderSubtotal = document.getElementById('purchaseOrderSubtotal');
    const purchaseOrderVatAmount = document.getElementById('purchaseOrderVatAmount');
    const purchaseOrderTotal = document.getElementById('purchaseOrderTotal');
    const purchaseOrderItemList = document.getElementById('purchaseOrderItemList');
    const addPurchaseOrderItemBtn = document.getElementById('addPurchaseOrderItemBtn');
    const purchaseOrderItemTemplate = document.getElementById('purchaseOrderItemTemplate');
    const invoiceDateInput = document.getElementById('invoiceDate');
    const supplierNameInput = document.getElementById('supplierName');
    const supplierTaxCodeInput = document.getElementById('supplierTaxCode');
    const supplierAddressInput = document.getElementById('supplierAddress');
    const supplierPhoneInput = document.getElementById('supplierPhone');
    const supplierEmailInput = document.getElementById('supplierEmail');
    const invoiceNumberInput = document.getElementById('invoiceNumber');
    const contractNumberInput = document.getElementById('contractNumber');
    const deliveryNoteNumberInput = document.getElementById('deliveryNoteNumber');
    const receivedByInput = document.getElementById('receivedBy');
    const notesInput = document.getElementById('notes');

    function formatCurrency(value) {
        return Number(value || 0).toLocaleString('vi-VN') + 'đ';
    }

    function toLocalDateInputValue(date) {
        const current = date || new Date();
        const tzOffset = current.getTimezoneOffset() * 60000;
        return new Date(current.getTime() - tzOffset).toISOString().slice(0, 10);
    }

    function getItemRows() {
        return Array.from(purchaseOrderItemList.querySelectorAll('[data-item-row]'));
    }

    function setQuickProduct(button) {
        if (!button) {
            stockImportProductImage.src = 'https://placehold.co/120x120?text=Product';
            stockImportProductName.innerText = 'Chưa chọn sản phẩm';
            stockImportCurrentStock.innerText = '-';
            stockImportReferencePrice.innerText = '-';
            return;
        }

        stockImportProductImage.src = button.dataset.productImage || 'https://placehold.co/120x120?text=Product';
        stockImportProductName.innerText = button.dataset.productName || 'Sản phẩm';
        stockImportCurrentStock.innerText = Number(button.dataset.productStock || 0).toLocaleString('vi-VN');
        stockImportReferencePrice.innerText = formatCurrency(button.dataset.productPrice || 0);
    }

    function refreshDuplicateSelections() {
        const selectedValues = getItemRows()
            .map(function (row) {
                const select = row.querySelector('.js-item-product');
                return select ? select.value : '';
            })
            .filter(Boolean);

        getItemRows().forEach(function (row) {
            const select = row.querySelector('.js-item-product');
            const currentValue = select.value;
            Array.from(select.options).forEach(function (option) {
                if (!option.value) {
                    return;
                }
                option.disabled = selectedValues.includes(option.value) && option.value !== currentValue;
            });
        });
    }

    function updateRowPreview(row) {
        const select = row.querySelector('.js-item-product');
        const quantityInput = row.querySelector('.js-item-quantity');
        const unitCostInput = row.querySelector('.js-item-unit-cost');
        const meta = row.querySelector('.js-item-product-meta');
        const stockSummary = row.querySelector('.js-item-stock-summary');
        const lineTotal = row.querySelector('.js-item-line-total');

        const selectedOption = select.options[select.selectedIndex];
        if (!selectedOption || !selectedOption.value) {
            meta.innerText = 'Chọn sản phẩm từ danh sách.';
            stockSummary.innerText = '-';
            lineTotal.innerText = '0đ';
            return;
        }

        const stockBefore = Number(selectedOption.dataset.stock || 0);
        const suggestedPrice = Number(selectedOption.dataset.price || 0);
        if (!unitCostInput.value || Number(unitCostInput.value) <= 0) {
            unitCostInput.value = suggestedPrice > 0 ? suggestedPrice.toFixed(2) : '';
        }

        const quantity = Math.max(0, Number(quantityInput.value || 0));
        const unitCost = Number(unitCostInput.value || suggestedPrice || 0);
        const stockAfter = quantity > 0 ? stockBefore + quantity : stockBefore;

        meta.innerText = 'Tồn kho hiện tại: ' + stockBefore.toLocaleString('vi-VN') + ' | Giá tham chiếu: ' + formatCurrency(suggestedPrice);
        stockSummary.innerText = stockBefore.toLocaleString('vi-VN') + ' -> ' + stockAfter.toLocaleString('vi-VN');
        lineTotal.innerText = quantity > 0 ? formatCurrency(quantity * unitCost) : '0đ';
    }

    function updateTotals() {
        let subtotal = 0;
        let totalQuantity = 0;
        let itemCount = 0;

        getItemRows().forEach(function (row) {
            const select = row.querySelector('.js-item-product');
            const quantityInput = row.querySelector('.js-item-quantity');
            const unitCostInput = row.querySelector('.js-item-unit-cost');
            if (!select || !select.value) {
                return;
            }

            const quantity = Math.max(0, Number(quantityInput.value || 0));
            const unitCost = Number(unitCostInput.value || 0);
            if (quantity > 0 && unitCost > 0) {
                subtotal += quantity * unitCost;
                totalQuantity += quantity;
                itemCount += 1;
            }
        });

        const vatRate = Number(vatRateInput.value || 0);
        const vatAmount = subtotal > 0 && vatRate > 0 ? subtotal * vatRate / 100 : 0;
        const total = subtotal + vatAmount;

        purchaseOrderItemCount.innerText = itemCount.toLocaleString('vi-VN');
        purchaseOrderTotalQuantity.innerText = totalQuantity.toLocaleString('vi-VN');
        purchaseOrderSubtotal.innerText = formatCurrency(subtotal);
        purchaseOrderVatAmount.innerText = formatCurrency(vatAmount);
        purchaseOrderTotal.innerText = formatCurrency(total);
    }

    function syncRemoveButtons() {
        const rows = getItemRows();
        rows.forEach(function (row) {
            const removeBtn = row.querySelector('.js-remove-item');
            if (!removeBtn) {
                return;
            }
            removeBtn.disabled = rows.length <= 1;
        });
    }

    function refreshRows() {
        refreshDuplicateSelections();
        getItemRows().forEach(function (row) {
            updateRowPreview(row);
        });
        syncRemoveButtons();
        updateTotals();
    }

    function attachRowHandlers(row) {
        const select = row.querySelector('.js-item-product');
        const quantityInput = row.querySelector('.js-item-quantity');
        const unitCostInput = row.querySelector('.js-item-unit-cost');
        const removeBtn = row.querySelector('.js-remove-item');

        select.addEventListener('change', function () {
            const selectedOption = select.options[select.selectedIndex];
            if (selectedOption && selectedOption.value) {
                const suggestedPrice = Number(selectedOption.dataset.price || 0);
                unitCostInput.value = suggestedPrice > 0 ? suggestedPrice.toFixed(2) : '';
            }
            refreshRows();
        });

        quantityInput.addEventListener('input', refreshRows);
        unitCostInput.addEventListener('input', refreshRows);
        removeBtn.addEventListener('click', function () {
            row.remove();
            ensureAtLeastOneRow();
            refreshRows();
        });
    }

    function addItemRow(initial) {
        const fragment = purchaseOrderItemTemplate.content.cloneNode(true);
        const row = fragment.querySelector('[data-item-row]');
        const select = row.querySelector('.js-item-product');
        const quantityInput = row.querySelector('.js-item-quantity');
        const unitCostInput = row.querySelector('.js-item-unit-cost');

        if (initial && initial.productId) {
            select.value = String(initial.productId);
        }
        if (initial && initial.quantity) {
            quantityInput.value = String(initial.quantity);
        } else {
            quantityInput.value = '1';
        }
        if (initial && typeof initial.unitCost !== 'undefined' && initial.unitCost !== null) {
            unitCostInput.value = Number(initial.unitCost).toFixed(2);
        }

        purchaseOrderItemList.appendChild(fragment);
        const appendedRow = purchaseOrderItemList.lastElementChild;
        attachRowHandlers(appendedRow);
        refreshRows();
        return appendedRow;
    }

    function ensureAtLeastOneRow() {
        if (getItemRows().length === 0) {
            addItemRow({});
        }
    }

    function resetModalInputs() {
        stockImportForm.reset();
        purchaseOrderItemList.innerHTML = '';
        vatRateInput.value = '0';
        invoiceDateInput.value = toLocalDateInputValue(new Date());
        supplierNameInput.value = '';
        supplierTaxCodeInput.value = '';
        supplierAddressInput.value = '';
        supplierPhoneInput.value = '';
        supplierEmailInput.value = '';
        invoiceNumberInput.value = '';
        contractNumberInput.value = '';
        deliveryNoteNumberInput.value = '';
        receivedByInput.value = '';
        notesInput.value = '';
        setQuickProduct(null);
        purchaseOrderItemCount.innerText = '0';
        purchaseOrderTotalQuantity.innerText = '0';
        purchaseOrderSubtotal.innerText = '0đ';
        purchaseOrderVatAmount.innerText = '0đ';
        purchaseOrderTotal.innerText = '0đ';
    }

    function openImportModal(button) {
        resetModalInputs();
        setQuickProduct(button);

        if (button) {
            addItemRow({
                productId: button.dataset.productId,
                quantity: 1,
                unitCost: button.dataset.productPrice
            });
        } else {
            addItemRow({});
        }

        stockImportModal.show();
        setTimeout(function () {
            const firstQuantityInput = purchaseOrderItemList.querySelector('.js-item-quantity');
            if (firstQuantityInput) {
                firstQuantityInput.focus();
            }
        }, 150);
    }

    document.querySelectorAll('.js-open-stock-modal').forEach(function (button) {
        button.addEventListener('click', function () {
            openImportModal(button);
        });
    });

    addPurchaseOrderItemBtn.addEventListener('click', function () {
        addItemRow({});
        const lastRow = getItemRows()[getItemRows().length - 1];
        if (lastRow) {
            const productSelect = lastRow.querySelector('.js-item-product');
            if (productSelect) {
                productSelect.focus();
            }
        }
    });

    vatRateInput.addEventListener('input', updateTotals);

    stockImportModalEl.addEventListener('hidden.bs.modal', function () {
        resetModalInputs();
    });

    invoiceDateInput.value = toLocalDateInputValue(new Date());
</script>
<script src="${ctx}/assets/js/admin-main.js"></script>
</body>
</html>
