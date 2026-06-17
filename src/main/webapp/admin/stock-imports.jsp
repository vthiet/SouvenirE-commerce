<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="common/admin-access-guard.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Nhập hàng | INOLA Admin</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-dashboard.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-pages.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-stock-imports.css">
</head>
<body>
<div class="admin-shell">
    <div class="sidebar-backdrop" data-sidebar-close></div>

    <jsp:include page="common/admin-sidebar.jsp">
        <jsp:param name="activePage" value="stock-imports" />
    </jsp:include>

    <div class="admin-main">
        <jsp:include page="common/admin-topbar.jsp" />

        <main class="dashboard-content">
            <div class="container-fluid px-3 px-lg-4 py-4">
                <div class="stock-import-hero">
                    <div class="stock-import-hero-copy">
                        <div>
                            <p class="eyebrow mb-2">Tồn kho</p>
                            <h1 class="h3 mb-2">Nhập hàng</h1>
                            <p class="text-muted mb-0">
                                Tăng tồn kho, lưu lịch sử nhập và theo dõi ngay những sản phẩm đang xuống thấp.
                            </p>
                        </div>
                        <div class="stock-import-hero-actions">
                            <a class="btn btn-primary btn-sm" href="#importForm">
                                <i class="bi bi-box-arrow-in-down" aria-hidden="true"></i> Nhập hàng mới
                            </a>
                            <a class="btn btn-outline-secondary btn-sm" href="${ctx}/admin/products">
                                <i class="bi bi-box-seam" aria-hidden="true"></i> Xem sản phẩm
                            </a>
                        </div>
                    </div>
                    <div class="stock-import-hero-panel">
                        <div class="stock-import-hero-kpi">
                            <span class="stock-import-hero-kpi-label">Cập nhật theo</span>
                            <strong>tháng hiện tại</strong>
                        </div>
                        <div class="stock-import-hero-meta">
                            <span>Ngưỡng cảnh báo:</span>
                            <strong>${lowStockThreshold} sản phẩm</strong>
                        </div>
                    </div>
                </div>

                <c:if test="${not empty message}">
                    <div class="alert alert-${messageType}">
                        <c:out value="${message}"/>
                    </div>
                </c:if>

                <section class="stats-grid stock-import-stats" aria-label="Thống kê nhập hàng">
                    <article class="metric-card">
                        <div class="metric-top">
                            <span class="metric-label">Phiếu nhập tháng này</span>
                            <span class="metric-icon"><i class="bi bi-receipt" aria-hidden="true"></i></span>
                        </div>
                        <div class="metric-value">${stockImportSummary.importCount}</div>
                        <div class="metric-meta">
                            <span>Lần tạo phiếu</span>
                        </div>
                    </article>

                    <article class="metric-card">
                        <div class="metric-top">
                            <span class="metric-label">Số lượng nhập</span>
                            <span class="metric-icon"><i class="bi bi-boxes" aria-hidden="true"></i></span>
                        </div>
                        <div class="metric-value">${stockImportSummary.totalQuantity}</div>
                        <div class="metric-meta">
                            <span>Đơn vị hàng hóa</span>
                        </div>
                    </article>

                    <article class="metric-card">
                        <div class="metric-top">
                            <span class="metric-label">Giá trị nhập</span>
                            <span class="metric-icon"><i class="bi bi-currency-dollar" aria-hidden="true"></i></span>
                        </div>
                        <div class="metric-value">
                            <fmt:formatNumber value="${stockImportSummary.totalCost}" pattern="#,###"/>đ
                        </div>
                        <div class="metric-meta">
                            <span>Tổng tiền chi trong tháng</span>
                        </div>
                    </article>

                    <article class="metric-card">
                        <div class="metric-top">
                            <span class="metric-label">Sản phẩm sắp hết</span>
                            <span class="metric-icon"><i class="bi bi-exclamation-triangle" aria-hidden="true"></i></span>
                        </div>
                        <div class="metric-value">${stockImportSummary.lowStockCount}</div>
                        <div class="metric-meta">
                            <span>Đang ở ngưỡng ≤ ${lowStockThreshold}</span>
                        </div>
                    </article>
                </section>

                <div class="stock-import-layout">
                    <section class="panel" id="importForm">
                        <div class="panel-header">
                            <div>
                                <h2 class="section-title">
                                    <i class="bi bi-plus-square-dotted" aria-hidden="true"></i>
                                    <span>Phiếu nhập mới</span>
                                </h2>
                                <p class="text-muted mb-0">Chọn sản phẩm, nhập số lượng và đơn giá để cập nhật tồn kho ngay.</p>
                            </div>
                        </div>
                        <div class="panel-body">
                            <form action="${ctx}/admin/stock-imports" method="post" class="stock-import-form" id="stockImportForm">
                                <input type="hidden" name="action" value="import">

                                <div class="stock-import-form-grid">
                                    <div class="form-group">
                                        <label for="importProductId" class="form-label">Sản phẩm *</label>
                                        <select name="productId" id="importProductId" class="form-select" required>
                                            <option value="">-- Chọn sản phẩm --</option>
                                            <c:forEach items="${products}" var="product">
                                                <c:set var="productCategoryName" value="${not empty product.category ? product.category.categoryName : ''}" />
                                                <option value="${product.id}"
                                                        data-stock="${product.stockQuantity}"
                                                        data-name="${fn:escapeXml(product.name)}"
                                                        data-category="${fn:escapeXml(productCategoryName)}"
                                                        ${selectedProductId != null and selectedProductId == product.id ? 'selected' : ''}>
                                                    <c:out value="${product.name}"/> - tồn hiện tại ${product.stockQuantity}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>

                                    <div class="stock-import-two-cols">
                                        <div class="form-group">
                                            <label for="importQuantity" class="form-label">Số lượng nhập *</label>
                                            <input type="number"
                                                   min="1"
                                                   step="1"
                                                   name="quantity"
                                                   id="importQuantity"
                                                   class="form-control"
                                                   required
                                                   value="${formQuantity}"
                                                   placeholder="Ví dụ: 20">
                                        </div>
                                        <div class="form-group">
                                            <label for="importUnitCost" class="form-label">Đơn giá nhập *</label>
                                            <input type="number"
                                                   min="0"
                                                   step="0.01"
                                                   name="unitCost"
                                                   id="importUnitCost"
                                                   class="form-control"
                                                   required
                                                   value="${formUnitCost}"
                                                   placeholder="Ví dụ: 45000">
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="importNote" class="form-label">Ghi chú</label>
                                        <textarea name="note"
                                                  id="importNote"
                                                  class="form-control"
                                                  rows="4"
                                                  placeholder="Ví dụ: Nhập thêm dịp lễ, hàng mới về từ nhà cung cấp..."><c:out value="${formNote}"/></textarea>
                                    </div>
                                </div>

                                <div class="stock-import-preview">
                                    <div class="stock-import-preview-header">
                                        <div>
                                            <p class="stock-import-preview-kicker mb-1">Xem trước phiếu nhập</p>
                                            <h3 class="stock-import-preview-title" id="selectedProductName">Chưa chọn sản phẩm</h3>
                                            <p class="stock-import-preview-subtitle mb-0" id="selectedProductCategory">
                                                Hãy chọn một sản phẩm để xem tồn kho hiện tại và số lượng sau khi nhập.
                                            </p>
                                        </div>
                                        <div class="stock-import-preview-badge">
                                            <i class="bi bi-arrow-repeat" aria-hidden="true"></i>
                                            Tự động cập nhật
                                        </div>
                                    </div>

                                    <div class="stock-import-preview-grid">
                                        <div class="stock-import-preview-stat">
                                            <span class="stock-import-preview-label">Tồn hiện tại</span>
                                            <strong class="stock-import-preview-value" id="selectedProductStock">0</strong>
                                        </div>
                                        <div class="stock-import-preview-stat">
                                            <span class="stock-import-preview-label">Sau khi nhập</span>
                                            <strong class="stock-import-preview-value" id="selectedProductProjected">0</strong>
                                        </div>
                                        <div class="stock-import-preview-stat stock-import-preview-stat-wide">
                                            <span class="stock-import-preview-label">Tổng chi phí dự kiến</span>
                                            <strong class="stock-import-preview-value" id="selectedImportCost">0đ</strong>
                                        </div>
                                    </div>

                                    <div class="stock-import-preview-note">
                                        <i class="bi bi-info-circle" aria-hidden="true"></i>
                                        Giá trị bên trên sẽ thay đổi ngay khi bạn nhập số lượng hoặc đơn giá.
                                    </div>
                                </div>

                                <div class="stock-import-form-footer">
                                    <p class="stock-import-form-note mb-0">
                                        <i class="bi bi-info-circle" aria-hidden="true"></i>
                                        Tồn kho sẽ được cộng trực tiếp vào sản phẩm và ghi lại lịch sử nhập hàng.
                                    </p>
                                    <button type="submit" class="btn btn-primary">
                                        <i class="bi bi-check2-circle" aria-hidden="true"></i> Lưu phiếu nhập
                                    </button>
                                </div>
                            </form>
                        </div>
                    </section>

                    <aside class="panel">
                        <div class="panel-header">
                            <div>
                                <h2 class="section-title">
                                    <i class="bi bi-lightning-charge" aria-hidden="true"></i>
                                    <span>Gợi ý nhập nhanh</span>
                                </h2>
                                <p class="text-muted mb-0">Các sản phẩm đang chạm ngưỡng thấp, nên ưu tiên bổ sung trước.</p>
                            </div>
                        </div>
                        <div class="panel-body">
                            <c:choose>
                                <c:when test="${empty lowStockProducts}">
                                    <div class="stock-import-empty">
                                        <p class="mb-1 fw-semibold">Tất cả sản phẩm đều đang ở mức an toàn.</p>
                                        <p class="mb-0 text-muted">Danh sách này sẽ tự động xuất hiện khi tồn kho giảm xuống ngưỡng cảnh báo.</p>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="stock-import-quick-list">
                                        <c:forEach items="${lowStockProducts}" var="product">
                                            <div class="stock-import-quick-item">
                                                <div class="stock-import-quick-copy">
                                                    <strong><c:out value="${product.name}"/></strong>
                                                    <span class="stock-import-quick-meta">
                                                        <c:choose>
                                                            <c:when test="${not empty product.category}">
                                                                <c:out value="${product.category.categoryName}"/>
                                                            </c:when>
                                                            <c:otherwise>Chưa phân loại</c:otherwise>
                                                        </c:choose>
                                                        • Tồn: ${product.stockQuantity}
                                                    </span>
                                                </div>
                                                <button type="button"
                                                        class="btn btn-outline-primary btn-sm"
                                                        data-product-id="${product.id}"
                                                        onclick="selectQuickProduct(this)">
                                                    Chọn
                                                </button>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </aside>
                </div>

                <section class="panel mt-4">
                    <div class="panel-header">
                        <div>
                            <h2 class="section-title">
                                <i class="bi bi-clock-history" aria-hidden="true"></i>
                                <span>Lịch sử nhập gần đây</span>
                            </h2>
                            <p class="text-muted mb-0">15 phiếu nhập mới nhất được ghi nhận trong hệ thống.</p>
                        </div>
                        <div class="stock-import-table-note">
                            Lần nhập gần nhất:
                            <c:choose>
                                <c:when test="${not empty stockImportSummary.latestImportAt}">
                                    <fmt:formatDate value="${stockImportSummary.latestImportAt}" pattern="dd/MM/yyyy HH:mm"/>
                                </c:when>
                                <c:otherwise>Chưa có dữ liệu</c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="panel-body table-container">
                        <c:choose>
                            <c:when test="${empty recentImports}">
                                <div class="stock-import-empty">
                                    <p class="mb-0 text-muted">Chưa có phiếu nhập nào. Hãy tạo phiếu đầu tiên ở khối bên trên.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <table class="data-table">
                                    <thead>
                                    <tr>
                                        <th scope="col">Thời gian</th>
                                        <th scope="col">Sản phẩm</th>
                                        <th scope="col">Số lượng</th>
                                        <th scope="col">Đơn giá</th>
                                        <th scope="col">Tổng tiền</th>
                                        <th scope="col">Tồn kho</th>
                                        <th scope="col">Người nhập</th>
                                        <th scope="col">Ghi chú</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${recentImports}" var="record">
                                        <tr>
                                            <td>
                                                <fmt:formatDate value="${record.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                            </td>
                                            <td>
                                                <div class="stock-import-product-cell">
                                                    <strong><c:out value="${record.productNameSnapshot}"/></strong>
                                                    <span class="stock-import-product-id">#${record.productId}</span>
                                                </div>
                                            </td>
                                            <td class="stock-import-center">${record.quantity}</td>
                                            <td>
                                                <fmt:formatNumber value="${record.unitCost}" pattern="#,###"/>đ
                                            </td>
                                            <td>
                                                <fmt:formatNumber value="${record.totalCost}" pattern="#,###"/>đ
                                            </td>
                                            <td>
                                                <div class="stock-import-stock-range">
                                                    <span>${record.stockBefore}</span>
                                                    <i class="bi bi-arrow-right" aria-hidden="true"></i>
                                                    <strong>${record.stockAfter}</strong>
                                                </div>
                                            </td>
                                            <td>
                                                <c:out value="${record.importedByName}"/>
                                            </td>
                                            <td>
                                                <span class="stock-import-note">
                                                    <c:choose>
                                                        <c:when test="${not empty record.note}">
                                                            <c:out value="${record.note}"/>
                                                        </c:when>
                                                        <c:otherwise>Không có ghi chú</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>
            </div>
        </main>
    </div>
</div>

<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/assets/js/admin-main.js"></script>
<script>
    const productSelect = document.getElementById('importProductId');
    const quantityInput = document.getElementById('importQuantity');
    const unitCostInput = document.getElementById('importUnitCost');
    const previewName = document.getElementById('selectedProductName');
    const previewCategory = document.getElementById('selectedProductCategory');
    const previewCurrentStock = document.getElementById('selectedProductStock');
    const previewProjectedStock = document.getElementById('selectedProductProjected');
    const previewTotalCost = document.getElementById('selectedImportCost');

    function formatCurrency(value) {
        return new Intl.NumberFormat('vi-VN').format(value) + 'đ';
    }

    function getSelectedProductOption() {
        if (!productSelect || productSelect.selectedIndex < 0) {
            return null;
        }
        const option = productSelect.options[productSelect.selectedIndex];
        if (!option || !option.value) {
            return null;
        }
        return option;
    }

    function updatePreview() {
        const option = getSelectedProductOption();
        const currentStock = option ? parseInt(option.dataset.stock || '0', 10) : 0;
        const selectedName = option ? (option.dataset.name || option.textContent.trim()) : 'Chưa chọn sản phẩm';
        const selectedCategory = option ? (option.dataset.category || 'Chưa phân loại') : 'Chưa chọn danh mục';
        const quantity = parseInt(quantityInput?.value || '0', 10) || 0;
        const unitCost = parseFloat(unitCostInput?.value || '0') || 0;
        const projectedStock = currentStock + quantity;
        const totalCost = quantity > 0 && unitCost > 0 ? quantity * unitCost : 0;

        if (previewName) {
            previewName.textContent = selectedName;
        }
        if (previewCategory) {
            previewCategory.textContent = selectedCategory;
        }
        if (previewCurrentStock) {
            previewCurrentStock.textContent = Number.isFinite(currentStock) ? currentStock : 0;
        }
        if (previewProjectedStock) {
            previewProjectedStock.textContent = Number.isFinite(projectedStock) ? projectedStock : currentStock;
        }
        if (previewTotalCost) {
            previewTotalCost.textContent = totalCost > 0 ? formatCurrency(totalCost) : '0đ';
        }
    }

    function selectQuickProduct(button) {
        if (!button || !productSelect) {
            return;
        }

        const productId = button.getAttribute('data-product-id');
        if (productId) {
            productSelect.value = productId;
            updatePreview();
            document.getElementById('importForm')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }

    if (productSelect) {
        productSelect.addEventListener('change', updatePreview);
    }
    if (quantityInput) {
        quantityInput.addEventListener('input', updatePreview);
    }
    if (unitCostInput) {
        unitCostInput.addEventListener('input', updatePreview);
    }

    window.selectQuickProduct = selectQuickProduct;

    updatePreview();
</script>
</body>
</html>
