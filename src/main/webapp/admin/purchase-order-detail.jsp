<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="common/admin-access-guard.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isDraft" value="${purchaseOrder.status == 'DRAFT'}" />
<c:set var="isFinalized" value="${purchaseOrder.status == 'FINALIZED'}" />
<c:set var="isCancelled" value="${purchaseOrder.status == 'CANCELLED'}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết phiếu nhập - Admin</title>
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
            <div class="container-fluid px-3 px-lg-4 py-4 admin-page purchase-order-detail-page">
                <c:if test="${not empty message}">
                    <div class="alert alert-${messageType}" role="alert">
                        <c:out value="${message}" />
                    </div>
                </c:if>

                <section class="admin-page-hero purchase-order-hero">
                    <div class="hero-copy">
                        <a href="${ctx}/admin/stock-imports" class="detail-back-link">
                            <i class="fas fa-arrow-left"></i>
                            Trở về danh sách phiếu nhập
                        </a>
                        <span class="page-eyebrow">Phiếu nhập kho</span>
                        <h1><c:out value="${purchaseOrder.poCode}" /></h1>
                        <p class="page-lead">
                            <c:choose>
                                <c:when test="${editMode}">
                                    Đang chỉnh sửa phiếu nhập này. Hãy cập nhật thông tin pháp lý, từng dòng sản phẩm và chọn lưu nháp hoặc hoàn tất.
                                </c:when>
                                <c:otherwise>
                                    Xem đầy đủ từng dòng sản phẩm, thông tin pháp lý và lịch sử ghi nhận của phiếu nhập.
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <div class="hero-meta">
                            <div class="meta-pill">
                                <span>Ngày hóa đơn</span>
                                <strong><c:out value="${purchaseOrder.invoiceDateDisplay}" /></strong>
                            </div>
                            <div class="meta-pill">
                                <span>Người nhập</span>
                                <strong><c:out value="${purchaseOrder.createdByLabel}" /></strong>
                            </div>
                        <div class="meta-pill">
                            <span>Tổng tiền</span>
                            <strong><fmt:formatNumber value="${purchaseOrder.totalAmount}" pattern="#,###"/>₫</strong>
                        </div>
                        <div class="meta-pill">
                            <span>Ngày tạo</span>
                            <strong><c:out value="${purchaseOrder.createdAtDisplay}" /></strong>
                        </div>
                    </div>
                </div>

                    <div class="hero-surface">
                        <span class="hero-surface-label">Trạng thái hiện tại</span>
                        <c:choose>
                            <c:when test="${isDraft}">
                                <div class="detail-status-pill is-warning">${purchaseOrder.statusLabel}</div>
                            </c:when>
                            <c:when test="${isFinalized}">
                                <div class="detail-status-pill is-success">${purchaseOrder.statusLabel}</div>
                            </c:when>
                            <c:when test="${isCancelled}">
                                <div class="detail-status-pill is-danger">${purchaseOrder.statusLabel}</div>
                            </c:when>
                            <c:otherwise>
                                <div class="detail-status-pill is-neutral">${purchaseOrder.statusLabel}</div>
                            </c:otherwise>
                        </c:choose>
                        <div class="hero-surface-note">
                            Phiếu nhập có ${purchaseOrder.itemCount} dòng sản phẩm và ${purchaseOrder.totalQuantity} sản phẩm.
                        </div>

                        <div class="order-detail-actions">
                            <button type="button" class="btn btn-outline-light order-action-btn" onclick="window.print()">
                                <i class="fas fa-print"></i>
                                In phiếu
                            </button>
                            <c:choose>
                                <c:when test="${editMode}">
                                    <a href="${ctx}/admin/purchase-order-detail?id=${purchaseOrder.id}" class="btn btn-light order-action-btn">
                                        <i class="fas fa-eye"></i>
                                        Xem phiếu
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${canUpdatePurchaseOrder}">
                                        <a href="${ctx}/admin/purchase-order-detail?id=${purchaseOrder.id}&mode=edit" class="btn btn-light order-action-btn">
                                            <i class="fas fa-pen"></i>
                                            Chỉnh sửa
                                        </a>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${canCancelPurchaseOrder}">
                                <button type="button" class="btn btn-danger order-action-btn" onclick="showCancelModal(${purchaseOrder.id})">
                                    <i class="fas fa-trash"></i>
                                    Hủy phiếu
                                </button>
                            </c:if>
                        </div>
                    </div>
                </section>

                <section class="order-summary-grid">
                    <article class="summary-card">
                        <span class="summary-label">Số dòng</span>
                        <div class="summary-value" id="purchaseOrderItemCount"><c:out value="${purchaseOrder.itemCount}" /></div>
                        <span class="summary-note">Số dòng hàng trong phiếu</span>
                    </article>
                    <article class="summary-card">
                        <span class="summary-label">Tổng số lượng</span>
                        <div class="summary-value" id="purchaseOrderTotalQuantity"><c:out value="${purchaseOrder.totalQuantity}" /></div>
                        <span class="summary-note">Tổng sản phẩm đã ghi nhận</span>
                    </article>
                    <article class="summary-card">
                        <span class="summary-label">Tạm tính</span>
                        <div class="summary-value" id="purchaseOrderSubtotal"><fmt:formatNumber value="${purchaseOrder.subtotalAmount}" pattern="#,###"/>₫</div>
                        <span class="summary-note">Trước VAT</span>
                    </article>
                    <article class="summary-card">
                        <span class="summary-label">Tổng tiền</span>
                        <div class="summary-value" id="purchaseOrderTotal"><fmt:formatNumber value="${purchaseOrder.totalAmount}" pattern="#,###"/>₫</div>
                        <span class="summary-note">Sau VAT</span>
                    </article>
                </section>

                <c:choose>
                    <c:when test="${editMode}">
                        <form id="purchaseOrderForm" method="post" action="${ctx}/admin/purchase-order-detail">
                            <input type="hidden" name="id" value="${purchaseOrder.id}">

                            <section class="order-detail-layout">
                                <div class="order-detail-main">
                                    <article class="card admin-panel-card order-section-card">
                                        <div class="card-header order-section-header">
                                            <div>
                                                <h3>Thông tin nhà cung cấp</h3>
                                                <p class="orders-card-subtitle">Cập nhật dữ liệu pháp lý của nhà cung cấp.</p>
                                            </div>
                                        </div>
                                        <div class="card-body">
                                            <div class="row g-3">
                                                <div class="col-12 col-md-6">
                                                    <label class="form-label" for="supplierName">Tên nhà cung cấp *</label>
                                                    <input type="text" class="form-control" id="supplierName" name="supplierName" value="${purchaseOrderForm.supplierName}" required>
                                                </div>
                                                <div class="col-12 col-md-6">
                                                    <label class="form-label" for="supplierTaxCode">Mã số thuế *</label>
                                                    <input type="text" class="form-control" id="supplierTaxCode" name="supplierTaxCode" value="${purchaseOrderForm.supplierTaxCode}" required>
                                                </div>
                                                <div class="col-12">
                                                    <label class="form-label" for="supplierAddress">Địa chỉ nhà cung cấp *</label>
                                                    <input type="text" class="form-control" id="supplierAddress" name="supplierAddress" value="${purchaseOrderForm.supplierAddress}" required>
                                                </div>
                                                <div class="col-12 col-md-6">
                                                    <label class="form-label" for="supplierPhone">Số điện thoại</label>
                                                    <input type="text" class="form-control" id="supplierPhone" name="supplierPhone" value="${purchaseOrderForm.supplierPhone}">
                                                </div>
                                                <div class="col-12 col-md-6">
                                                    <label class="form-label" for="supplierEmail">Email</label>
                                                    <input type="email" class="form-control" id="supplierEmail" name="supplierEmail" value="${purchaseOrderForm.supplierEmail}">
                                                </div>
                                            </div>
                                        </div>
                                    </article>

                                    <article class="card admin-panel-card order-section-card" style="margin-top: 24px;">
                                        <div class="card-header order-section-header">
                                            <div>
                                                <h3>Chứng từ pháp lý</h3>
                                                <p class="orders-card-subtitle">Hóa đơn, hợp đồng, phiếu giao nhận và người nhận hàng.</p>
                                            </div>
                                        </div>
                                        <div class="card-body">
                                            <div class="row g-3">
                                                <div class="col-12 col-md-6">
                                                    <label class="form-label" for="invoiceNumber">Số hóa đơn *</label>
                                                    <input type="text" class="form-control" id="invoiceNumber" name="invoiceNumber" value="${purchaseOrderForm.invoiceNumber}" required>
                                                </div>
                                                <div class="col-12 col-md-6">
                                                    <label class="form-label" for="invoiceDate">Ngày hóa đơn *</label>
                                                    <input type="date" class="form-control" id="invoiceDate" name="invoiceDate" value="${purchaseOrderForm.invoiceDate}" required>
                                                </div>
                                                <div class="col-12 col-md-6">
                                                    <label class="form-label" for="contractNumber">Số hợp đồng</label>
                                                    <input type="text" class="form-control" id="contractNumber" name="contractNumber" value="${purchaseOrderForm.contractNumber}">
                                                </div>
                                                <div class="col-12 col-md-6">
                                                    <label class="form-label" for="deliveryNoteNumber">Số phiếu giao nhận</label>
                                                    <input type="text" class="form-control" id="deliveryNoteNumber" name="deliveryNoteNumber" value="${purchaseOrderForm.deliveryNoteNumber}">
                                                </div>
                                                <div class="col-12 col-md-6">
                                                    <label class="form-label" for="receivedBy">Người nhận hàng *</label>
                                                    <input type="text" class="form-control" id="receivedBy" name="receivedBy" value="${purchaseOrderForm.receivedBy}" required>
                                                </div>
                                                <div class="col-12 col-md-6">
                                                    <label class="form-label" for="vatRate">VAT (%)</label>
                                                    <input type="number" class="form-control" id="vatRate" name="vatRate" min="0" max="100" step="0.01" value="${empty purchaseOrderForm.vatRate ? 0 : purchaseOrderForm.vatRate}">
                                                </div>
                                                <div class="col-12">
                                                    <label class="form-label" for="notes">Ghi chú</label>
                                                    <textarea class="form-control" id="notes" name="notes" rows="3"><c:out value="${purchaseOrderForm.notes}" /></textarea>
                                                </div>
                                            </div>
                                        </div>
                                    </article>

                                    <article class="card admin-panel-card order-section-card" style="margin-top: 24px;">
                                        <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2 order-section-header">
                                            <div>
                                                <h3>Sản phẩm trong phiếu</h3>
                                                <p class="orders-card-subtitle">Thêm, bớt và điều chỉnh từng dòng nhập kho.</p>
                                            </div>
                                            <button type="button" class="btn btn-outline-primary btn-sm" id="addPurchaseOrderItemBtn">
                                                <i class="fas fa-plus me-1"></i> Thêm dòng sản phẩm
                                            </button>
                                        </div>
                                        <div class="card-body">
                                            <div id="purchaseOrderItemList" class="d-grid gap-3">
                                                <c:choose>
                                                    <c:when test="${not empty purchaseOrderForm.items}">
                                                        <c:forEach items="${purchaseOrderForm.items}" var="item">
                                                            <div class="purchase-order-item border rounded-3 p-3 bg-white shadow-sm" data-item-row>
                                                                <div class="row g-3 align-items-end">
                                                                    <div class="col-12 col-lg-5">
                                                                        <label class="form-label">Sản phẩm *</label>
                                                                        <select class="form-select js-item-product" name="productId" required>
                                                                            <option value="">-- Chọn sản phẩm --</option>
                                                                            <c:forEach items="${availableProducts}" var="optionProduct">
                                                                                <c:choose>
                                                                                    <c:when test="${optionProduct.id == item.productId}">
                                                                                        <option value="${optionProduct.id}" data-stock="${optionProduct.stockQuantity}" data-price="${optionProduct.originalPrice}" selected="selected">
                                                                                            <c:out value="${optionProduct.name}" />
                                                                                        </option>
                                                                                    </c:when>
                                                                                    <c:otherwise>
                                                                                        <option value="${optionProduct.id}" data-stock="${optionProduct.stockQuantity}" data-price="${optionProduct.originalPrice}">
                                                                                            <c:out value="${optionProduct.name}" />
                                                                                        </option>
                                                                                    </c:otherwise>
                                                                                </c:choose>
                                                                            </c:forEach>
                                                                        </select>
                                                                        <div class="form-text js-item-product-meta">Chọn sản phẩm từ danh sách.</div>
                                                                    </div>
                                                                    <div class="col-6 col-lg-2">
                                                                        <label class="form-label">Số lượng *</label>
                                                                        <input type="number" class="form-control js-item-quantity" name="quantity" min="1" step="1" value="${item.quantity}" required>
                                                                    </div>
                                                                    <div class="col-6 col-lg-2">
                                                                        <label class="form-label">Giá nhập *</label>
                                                                        <input type="number" class="form-control js-item-unit-cost" name="unitCost" min="0" step="0.01" value="${item.unitCost}" required>
                                                                    </div>
                                                                    <div class="col-12 col-lg-2">
                                                                        <div class="small text-muted">Dự kiến tồn kho</div>
                                                                        <div class="fw-semibold js-item-stock-summary">-</div>
                                                                        <div class="small text-muted">Thành tiền: <span class="fw-semibold js-item-line-total">0đ</span></div>
                                                                    </div>
                                                                    <div class="col-12 col-lg-1 text-end">
                                                                        <button type="button" class="btn btn-outline-danger btn-sm w-100 js-remove-item">Xóa</button>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </c:forEach>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="purchase-order-item border rounded-3 p-3 bg-white shadow-sm" data-item-row>
                                                            <div class="row g-3 align-items-end">
                                                                <div class="col-12 col-lg-5">
                                                                    <label class="form-label">Sản phẩm *</label>
                                                                    <select class="form-select js-item-product" name="productId" required>
                                                                        <option value="">-- Chọn sản phẩm --</option>
                                                                        <c:forEach items="${availableProducts}" var="optionProduct">
                                                                            <option value="${optionProduct.id}" data-stock="${optionProduct.stockQuantity}" data-price="${optionProduct.originalPrice}">
                                                                                <c:out value="${optionProduct.name}" />
                                                                            </option>
                                                                        </c:forEach>
                                                                    </select>
                                                                    <div class="form-text js-item-product-meta">Chọn sản phẩm từ danh sách.</div>
                                                                </div>
                                                                <div class="col-6 col-lg-2">
                                                                    <label class="form-label">Số lượng *</label>
                                                                    <input type="number" class="form-control js-item-quantity" name="quantity" min="1" step="1" value="1" required>
                                                                </div>
                                                                <div class="col-6 col-lg-2">
                                                                    <label class="form-label">Giá nhập *</label>
                                                                    <input type="number" class="form-control js-item-unit-cost" name="unitCost" min="0" step="0.01" required>
                                                                </div>
                                                                <div class="col-12 col-lg-2">
                                                                    <div class="small text-muted">Dự kiến tồn kho</div>
                                                                    <div class="fw-semibold js-item-stock-summary">-</div>
                                                                    <div class="small text-muted">Thành tiền: <span class="fw-semibold js-item-line-total">0đ</span></div>
                                                                </div>
                                                                <div class="col-12 col-lg-1 text-end">
                                                                    <button type="button" class="btn btn-outline-danger btn-sm w-100 js-remove-item">Xóa</button>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </article>
                                </div>

                                <aside class="order-detail-aside">
                                    <article class="card admin-panel-card order-side-card">
                                        <div class="card-header order-section-header">
                                            <div>
                                                <h3>Tổng kết nhanh</h3>
                                                <p class="orders-card-subtitle">Tổng tiền sẽ tự tính lại theo từng dòng.</p>
                                            </div>
                                        </div>
                                        <div class="order-totals">
                                            <div class="order-info-row">
                                                <span>Số dòng</span>
                                                <strong id="purchaseOrderItemCount"><c:out value="${purchaseOrder.itemCount}" /></strong>
                                            </div>
                                            <div class="order-info-row">
                                                <span>Tổng số lượng</span>
                                                <strong id="purchaseOrderTotalQuantity"><c:out value="${purchaseOrder.totalQuantity}" /></strong>
                                            </div>
                                            <div class="order-info-row">
                                                <span>Tạm tính</span>
                                                <strong id="purchaseOrderSubtotal"><fmt:formatNumber value="${purchaseOrder.subtotalAmount}" pattern="#,###"/>₫</strong>
                                            </div>
                                            <div class="order-info-row">
                                                <span>VAT</span>
                                                <strong id="purchaseOrderVatAmount"><fmt:formatNumber value="${purchaseOrder.vatAmount}" pattern="#,###"/>₫</strong>
                                            </div>
                                            <div class="order-info-row order-total-highlight">
                                                <span>Tổng tiền</span>
                                                <strong id="purchaseOrderTotal"><fmt:formatNumber value="${purchaseOrder.totalAmount}" pattern="#,###"/>₫</strong>
                                            </div>
                                        </div>
                                    </article>

                                    <article class="card admin-panel-card order-side-card" style="margin-top: 24px;">
                                        <div class="card-header order-section-header">
                                            <div>
                                                <h3>Thông tin hiện tại</h3>
                                                <p class="orders-card-subtitle">Bối cảnh của phiếu nhập trước khi lưu.</p>
                                            </div>
                                        </div>
                                        <div class="order-info-list">
                                            <div class="order-info-row">
                                                <span>Mã phiếu</span>
                                                <strong><c:out value="${purchaseOrder.poCode}" /></strong>
                                            </div>
                                            <div class="order-info-row">
                                                <span>Trạng thái</span>
                                                <strong>${purchaseOrder.statusLabel}</strong>
                                            </div>
                                            <div class="order-info-row">
                                                <span>Ngày tạo</span>
                                                <strong>${purchaseOrder.createdAtDisplay}</strong>
                                            </div>
                                            <div class="order-info-row">
                                                <span>Người nhập</span>
                                                <strong><c:out value="${purchaseOrder.createdByLabel}" /></strong>
                                            </div>
                                        </div>
                                    </article>
                                </aside>
                            </section>

                            <div class="d-flex flex-wrap justify-content-end gap-2 mt-4">
                                <a href="${ctx}/admin/purchase-order-detail?id=${purchaseOrder.id}" class="btn btn-outline-secondary">
                                    Quay lại
                                </a>
                                <c:choose>
                                    <c:when test="${isDraft}">
                                        <button type="submit" class="btn btn-primary" name="action" value="save">
                                            <i class="fas fa-save me-1"></i> Lưu nháp
                                        </button>
                                        <button type="submit" class="btn btn-outline-success" name="action" value="finalize">
                                            <i class="fas fa-check me-1"></i> Lưu phiếu nhập
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="submit" class="btn btn-primary" name="action" value="save">
                                            <i class="fas fa-save me-1"></i> Lưu thay đổi
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <section class="order-detail-layout">
                            <div class="order-detail-main">
                                <article class="card admin-panel-card order-section-card">
                                    <div class="card-header order-section-header">
                                        <div>
                                            <h3>Danh sách từng dòng sản phẩm</h3>
                                            <p class="orders-card-subtitle">Mỗi dòng thể hiện sản phẩm, số lượng, giá nhập và tồn kho trước/sau khi nhập.</p>
                                        </div>
                                    </div>

                                    <div class="table-container orders-table-wrap">
                                        <table class="data-table order-items-table">
                                            <thead>
                                            <tr>
                                                <th>Sản phẩm</th>
                                                <th>Đơn giá</th>
                                                <th>Số lượng</th>
                                                <th>Tạm tính</th>
                                                <th>Tồn trước</th>
                                                <th>Tồn sau</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:choose>
                                                <c:when test="${not empty purchaseOrder.items}">
                                                    <c:forEach items="${purchaseOrder.items}" var="item">
                                                        <tr>
                                                            <td>
                                                                <div class="fw-semibold"><c:out value="${item.productName}" /></div>
                                                                <div class="text-muted small">Mã SP: ${item.productId}</div>
                                                            </td>
                                                            <td><fmt:formatNumber value="${item.unitCost}" pattern="#,###"/>₫</td>
                                                            <td>${item.quantity}</td>
                                                            <td><fmt:formatNumber value="${item.lineAmount}" pattern="#,###"/>₫</td>
                                                            <td>${item.stockBefore}</td>
                                                            <td>${item.stockAfter}</td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise>
                                                    <tr>
                                                        <td colspan="6" class="orders-empty-state">
                                                            <div class="orders-empty-card">
                                                                <i class="fas fa-box-open orders-empty-icon"></i>
                                                                <p>Chưa có dữ liệu sản phẩm cho phiếu nhập này.</p>
                                                                <span class="orders-empty-note">Nếu phiếu chưa được đồng bộ đủ dữ liệu, phần này sẽ hiển thị trống.</span>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </c:otherwise>
                                            </c:choose>
                                            </tbody>
                                        </table>
                                    </div>
                                </article>
                            </div>

                            <aside class="order-detail-aside">
                                <article class="card admin-panel-card order-side-card">
                                    <div class="card-header order-section-header">
                                        <div>
                                            <h3>Nhà cung cấp</h3>
                                            <p class="orders-card-subtitle">Thông tin đầu mối nhập hàng.</p>
                                        </div>
                                    </div>
                                    <div class="order-info-list">
                                        <div class="order-info-row">
                                            <span>Tên</span>
                                            <strong><c:out value="${purchaseOrder.supplierName}" /></strong>
                                        </div>
                                        <div class="order-info-row">
                                            <span>Mã số thuế</span>
                                            <strong><c:out value="${purchaseOrder.supplierTaxCode}" /></strong>
                                        </div>
                                        <div class="order-info-row">
                                            <span>Địa chỉ</span>
                                            <strong><c:out value="${purchaseOrder.supplierAddress}" /></strong>
                                        </div>
                                        <div class="order-info-row">
                                            <span>Điện thoại</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty purchaseOrder.supplierPhone}">
                                                        <c:out value="${purchaseOrder.supplierPhone}" />
                                                    </c:when>
                                                    <c:otherwise>Chưa cập nhật</c:otherwise>
                                                </c:choose>
                                            </strong>
                                        </div>
                                        <div class="order-info-row">
                                            <span>Email</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty purchaseOrder.supplierEmail}">
                                                        <c:out value="${purchaseOrder.supplierEmail}" />
                                                    </c:when>
                                                    <c:otherwise>Chưa cập nhật</c:otherwise>
                                                </c:choose>
                                            </strong>
                                        </div>
                                    </div>
                                </article>

                                <article class="card admin-panel-card order-side-card" style="margin-top: 24px;">
                                    <div class="card-header order-section-header">
                                        <div>
                                            <h3>Chứng từ pháp lý</h3>
                                            <p class="orders-card-subtitle">Số hóa đơn, hợp đồng và phiếu giao nhận.</p>
                                        </div>
                                    </div>
                                    <div class="order-info-list">
                                        <div class="order-info-row">
                                            <span>Số hóa đơn</span>
                                            <strong><c:out value="${purchaseOrder.invoiceNumber}" /></strong>
                                        </div>
                                        <div class="order-info-row">
                                            <span>Ngày hóa đơn</span>
                                            <strong><c:out value="${purchaseOrder.invoiceDateDisplay}" /></strong>
                                        </div>
                                        <div class="order-info-row">
                                            <span>Số hợp đồng</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty purchaseOrder.contractNumber}">
                                                        <c:out value="${purchaseOrder.contractNumber}" />
                                                    </c:when>
                                                    <c:otherwise>Không có</c:otherwise>
                                                </c:choose>
                                            </strong>
                                        </div>
                                        <div class="order-info-row">
                                            <span>Số phiếu giao nhận</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty purchaseOrder.deliveryNoteNumber}">
                                                        <c:out value="${purchaseOrder.deliveryNoteNumber}" />
                                                    </c:when>
                                                    <c:otherwise>Không có</c:otherwise>
                                                </c:choose>
                                            </strong>
                                        </div>
                                        <div class="order-info-row">
                                            <span>Người nhận hàng</span>
                                            <strong><c:out value="${purchaseOrder.receivedBy}" /></strong>
                                        </div>
                                    </div>
                                </article>

                                <article class="card admin-panel-card order-side-card" style="margin-top: 24px;">
                                    <div class="card-header order-section-header">
                                        <div>
                                            <h3>Ghi chú</h3>
                                            <p class="orders-card-subtitle">Thông tin bổ sung của phiếu nhập.</p>
                                        </div>
                                    </div>
                                    <div class="card-body">
                                        <c:choose>
                                            <c:when test="${not empty purchaseOrder.notes}">
                                                <div style="white-space: pre-wrap;"><c:out value="${purchaseOrder.notes}" /></div>
                                            </c:when>
                                            <c:otherwise>
                                                <p class="text-muted mb-0">Không có ghi chú.</p>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </article>
                            </aside>
                        </section>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>

<div id="cancelPurchaseOrderModal" class="modal orders-modal" aria-hidden="true">
    <div class="modal-content orders-modal-content">
        <div class="modal-header orders-modal-header text-danger">
            <div>
                <span class="orders-modal-kicker text-danger">Hành động hủy</span>
                <h3>Xác nhận hủy phiếu nhập</h3>
            </div>
            <button type="button" class="close-btn" onclick="closeCancelModal()" aria-label="Đóng">&times;</button>
        </div>
        <form method="post" action="${ctx}/admin/purchase-order-detail">
            <input type="hidden" name="action" value="cancel">
            <input type="hidden" name="id" id="cancelPurchaseOrderId">
            <div class="modal-body orders-modal-body">
                <p class="mb-0">
                    Phiếu nhập sẽ chuyển sang trạng thái đã hủy. Nếu phiếu đã hoàn tất, hệ thống sẽ tự hoàn lại tồn kho cho từng sản phẩm.
                </p>
            </div>
            <div class="modal-footer orders-modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCancelModal()">Quay lại</button>
                <button type="submit" class="btn btn-danger">Xác nhận hủy</button>
            </div>
        </form>
    </div>
</div>

<c:if test="${editMode}">
<template id="purchaseOrderItemTemplate">
    <div class="purchase-order-item border rounded-3 p-3 bg-white shadow-sm" data-item-row>
        <div class="row g-3 align-items-end">
            <div class="col-12 col-lg-5">
                <label class="form-label">Sản phẩm *</label>
                <select class="form-select js-item-product" name="productId" required>
                    <option value="">-- Chọn sản phẩm --</option>
                    <c:forEach items="${availableProducts}" var="optionProduct">
                        <option value="${optionProduct.id}" data-stock="${optionProduct.stockQuantity}" data-price="${optionProduct.originalPrice}">
                            <c:out value="${optionProduct.name}" />
                        </option>
                    </c:forEach>
                </select>
                <div class="form-text js-item-product-meta">Chọn sản phẩm từ danh sách.</div>
            </div>
            <div class="col-6 col-lg-2">
                <label class="form-label">Số lượng *</label>
                <input type="number" class="form-control js-item-quantity" name="quantity" min="1" step="1" value="1" required>
            </div>
            <div class="col-6 col-lg-2">
                <label class="form-label">Giá nhập *</label>
                <input type="number" class="form-control js-item-unit-cost" name="unitCost" min="0" step="0.01" required>
            </div>
            <div class="col-12 col-lg-2">
                <div class="small text-muted">Dự kiến tồn kho</div>
                <div class="fw-semibold js-item-stock-summary">-</div>
                <div class="small text-muted">Thành tiền: <span class="fw-semibold js-item-line-total">0đ</span></div>
            </div>
            <div class="col-12 col-lg-1 text-end">
                <button type="button" class="btn btn-outline-danger btn-sm w-100 js-remove-item">Xóa</button>
            </div>
        </div>
    </div>
</template>
</c:if>

<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>
<script>
    function showCancelModal(purchaseOrderId) {
        document.getElementById('cancelPurchaseOrderId').value = purchaseOrderId;
        document.getElementById('cancelPurchaseOrderModal').classList.add('show');
        document.getElementById('cancelPurchaseOrderModal').setAttribute('aria-hidden', 'false');
    }

    function closeCancelModal() {
        const modal = document.getElementById('cancelPurchaseOrderModal');
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
    }

    window.addEventListener('click', function(event) {
        const cancelModal = document.getElementById('cancelPurchaseOrderModal');
        if (event.target === cancelModal) {
            closeCancelModal();
        }
    });

    window.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeCancelModal();
        }
    });

    <c:if test="${editMode}">
    (function () {
        const purchaseOrderItemList = document.getElementById('purchaseOrderItemList');
        const addPurchaseOrderItemBtn = document.getElementById('addPurchaseOrderItemBtn');
        const purchaseOrderItemTemplate = document.getElementById('purchaseOrderItemTemplate');
        const vatRateInput = document.getElementById('vatRate');
        const purchaseOrderItemCount = document.getElementById('purchaseOrderItemCount');
        const purchaseOrderTotalQuantity = document.getElementById('purchaseOrderTotalQuantity');
        const purchaseOrderSubtotal = document.getElementById('purchaseOrderSubtotal');
        const purchaseOrderVatAmount = document.getElementById('purchaseOrderVatAmount');
        const purchaseOrderTotal = document.getElementById('purchaseOrderTotal');

        function formatCurrency(value) {
            return Number(value || 0).toLocaleString('vi-VN') + 'đ';
        }

        function getItemRows() {
            return Array.from(purchaseOrderItemList.querySelectorAll('[data-item-row]'));
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
                if (removeBtn) {
                    removeBtn.disabled = rows.length <= 1;
                }
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

        getItemRows().forEach(function (row) {
            attachRowHandlers(row);
        });
        ensureAtLeastOneRow();
        refreshRows();

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

        vatRateInput.addEventListener('input', refreshRows);
    })();
    </c:if>
</script>
<script src="${ctx}/assets/js/admin-main.js"></script>
</body>
</html>
