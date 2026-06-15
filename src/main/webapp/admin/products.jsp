<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="common/admin-access-guard.jspf" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý sản phẩm - Admin</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-pages.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-products.css">
</head>
<body>
<div class="admin-shell">
    <div class="sidebar-backdrop" data-sidebar-close></div>
    <jsp:include page="common/admin-sidebar.jsp"/>

    <div class="admin-main">
        <jsp:include page="common/admin-topbar.jsp"/>

        <main class="dashboard-content">
        <div class="container-fluid px-3 px-lg-4 py-4">
            <div class="content-header">
                <h1>Quản lý sản phẩm</h1>
                <c:if test="${canCreateProduct}">
                    <button class="btn-primary" onclick="openAddModal()">
                        <i class="fas fa-plus"></i> Thêm sản phẩm mới
                    </button>
                </c:if>
            </div>

            <c:if test="${not empty searchQuery}">
                <div class="product-filter-banner">
                    <span>Kết quả tìm kiếm cho: <strong>"${searchQuery}"</strong></span>
                    <a href="${pageContext.request.contextPath}/admin/products" class="product-filter-link">
                        <i class="fas fa-times"></i> Xóa bộ lọc
                    </a>
                </div>
            </c:if>

            <c:if test="${not empty message}">
                <div class="alert alert-${messageType}">
                        ${message}
                </div>
            </c:if>

            <div class="card">
                <div class="card-header">
                    <h3>Danh sách sản phẩm (${totalProducts} sản phẩm)</h3>
                </div>

                <div class="table-container">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th class="product-col-id">ID</th>
                            <th class="product-col-image">Hình ảnh</th>
                            <th>Tên sản phẩm</th>
                            <th class="product-col-price">Giá</th>
                            <th class="product-col-stock">Tồn kho</th>
                            <th class="product-col-sold">Đã bán</th>
                            <th class="product-col-rating">Đánh giá</th>
                            <th class="product-col-actions">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${products}" var="p">
                            <c:set var="resolvedImageUrl" value="${p.imageUrl}" />
                            <c:choose>
                                <c:when test="${empty p.imageUrl}">
                                    <c:set var="resolvedImageUrl" value="https://placehold.co/50x50?text=No+Image" />
                                </c:when>
                                <c:when test="${fn:startsWith(p.imageUrl, 'http://') or fn:startsWith(p.imageUrl, 'https://') or fn:startsWith(p.imageUrl, 'data:')}">
                                    <c:set var="resolvedImageUrl" value="${p.imageUrl}" />
                                </c:when>
                                <c:when test="${fn:startsWith(p.imageUrl, pageContext.request.contextPath)}">
                                    <c:set var="resolvedImageUrl" value="${p.imageUrl}" />
                                </c:when>
                                <c:otherwise>
                                    <c:set var="resolvedImageUrl" value="${pageContext.request.contextPath}/${p.imageUrl}" />
                                </c:otherwise>
                            </c:choose>
                            <tr>
                                <td>${p.id}</td>
                                <td>
                                    <img src="${resolvedImageUrl}"
                                         alt="${p.name}"
                                         class="product-thumb"
                                         onerror="this.src='https://placehold.co/50x50?text=No+Image'">
                                </td>
                                <td class="product-name">${p.name}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.discountPercent > 0 and p.salePrice != null}">
                                            <div class="product-price-stack">
                                                <span class="product-price-sale"><fmt:formatNumber value="${p.salePrice}" pattern="#,###"/>đ</span>
                                                <span class="product-price-original"><fmt:formatNumber value="${p.originalPrice}" pattern="#,###"/>đ</span>
                                                <span class="product-discount-badge">-${p.discountPercent}%</span>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${p.originalPrice}" pattern="#,###"/>đ
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="product-center">${p.stockQuantity}</td>
                                <td class="product-center">${p.totalSold}</td>
                                <td class="product-center">
                                    <i class="fas fa-star product-rating-icon"></i> ${p.avgRating}
                                </td>
                                <td>
                                    <div class="action-buttons">
                                        <c:if test="${canUpdateProduct}">
                                            <button
                                                    type="button"
                                                    class="btn-icon btn-edit"
                                                    title="Sửa"
                                                    data-product-id="${p.id}"
                                                    data-product-name="<c:out value='${p.name}' />"
                                                    data-product-desc="<c:out value='${p.description}' />"
                                                    data-product-category-id="${p.categoryId}"
                                                    data-product-price="${p.originalPrice}"
                                                    data-product-image="<c:out value='${p.imageUrl}' />"
                                                    data-product-stock="${p.stockQuantity}"
                                                    data-product-discount="${p.discountPercent}"
                                                    data-product-sale-price="${p.salePrice != null ? p.salePrice : 0}">
                                                <i class="fas fa-edit"></i>
                                            </button>
                                        </c:if>
                                        <c:if test="${canDeleteProduct}">
                                            <form action="${pageContext.request.contextPath}/admin/products" method="post" onsubmit="return confirm('Bạn có chắc muốn xóa sản phẩm này?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${p.id}">
                                                <button class="btn-icon btn-delete" title="Xóa">
                                                    <i class="fas fa-trash"></i>
                                                </button>
                                            </form>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- Pagination -->
                <c:if test="${totalPages > 1}">
                    <div class="product-pagination">
                        <c:if test="${currentPage > 1}">
                            <a href="?page=${currentPage - 1}" class="btn-icon product-pagination-link">
                                <i class="fas fa-chevron-left"></i>
                            </a>
                        </c:if>

                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <c:choose>
                                <c:when test="${i == currentPage}">
                                    <span class="product-pagination-current">${i}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="?page=${i}" class="product-pagination-page">${i}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <c:if test="${currentPage < totalPages}">
                            <a href="?page=${currentPage + 1}" class="btn-icon product-pagination-link">
                                <i class="fas fa-chevron-right"></i>
                            </a>
                        </c:if>
                    </div>
                </c:if>
            </div>
        </div>
        </main>

    </div>
</div>

<!-- Modal Add/Edit -->
<div id="productModal" class="modal product-modal">
    <div class="modal-content product-modal-content">
        <div class="modal-header product-modal-header">
            <div class="product-modal-heading">
                <span id="modalModeBadge" class="product-modal-badge">Thêm mới</span>
                <div>
                    <h3 id="modalTitle">Thêm sản phẩm mới</h3>
                    <p id="modalSubtitle" class="product-modal-subtitle">Nhập thông tin sản phẩm để hiển thị trong danh sách quản trị.</p>
                </div>
            </div>
            <button type="button" class="close-btn" onclick="closeModal()" aria-label="Đóng">&times;</button>
        </div>
        <div class="modal-body product-modal-body">
            <form action="${pageContext.request.contextPath}/admin/products" method="post" class="product-modal-form">
                <input type="hidden" name="action" id="formAction" value="add">
                <input type="hidden" name="id" id="productId">

                <div class="product-modal-layout">
                    <section class="product-modal-panel">
                        <div class="section-title">
                            <i class="fas fa-box-open"></i>
                            Thông tin sản phẩm
                        </div>

                        <div class="form-group">
                            <label>Tên sản phẩm *</label>
                            <input type="text" name="name" id="productName" class="form-control" required placeholder="Ví dụ: Balo vải canvas">
                        </div>

                        <div class="form-group">
                            <label>Danh mục *</label>
                            <select name="categoryId" id="categoryId" class="form-control" required>
                                <option value="">-- Chọn danh mục --</option>
                                <c:forEach items="${categories}" var="cat">
                                    <option value="${cat.id}">${cat.categoryName}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Mô tả</label>
                            <textarea name="description" id="productDesc" class="form-control" rows="5" placeholder="Mô tả ngắn gọn, rõ ràng về sản phẩm"></textarea>
                        </div>

                        <div class="product-grid-two">
                            <div class="form-group">
                                <label>Giá gốc (VNĐ) *</label>
                                <input type="number" name="price" id="productPrice" class="form-control" required min="0" placeholder="0">
                            </div>

                            <div class="form-group">
                                <label>Số lượng tồn kho *</label>
                                <input type="number" name="stock" id="productStock" class="form-control" required min="0" placeholder="0">
                            </div>
                        </div>
                    </section>

                    <section class="product-modal-panel product-modal-panel-accent">
                        <div class="section-title">
                            <i class="fas fa-chart-line"></i>
                            Giá và hình ảnh
                        </div>

                        <div class="product-preview-card">
                            <div class="product-preview-image-wrap">
                                <img id="productPreviewImage" src="https://placehold.co/640x420?text=Preview" alt="Xem trước sản phẩm" class="product-preview-image">
                            </div>
                            <div class="product-preview-copy">
                                <strong id="productPreviewName">Xem trước sản phẩm</strong>
                                <span id="productPreviewMeta">Chọn tên, giá và ảnh để xem nhanh bố cục hiển thị.</span>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Giảm giá (%)</label>
                            <input type="number" name="discountPercent" id="discountPercent" class="form-control" min="0" max="100" value="0" onchange="calculateSalePrice()" placeholder="0">
                            <small class="product-help">Nhập 0 nếu không có giảm giá</small>
                        </div>

                        <div class="product-grid-two">
                            <div class="form-group">
                                <label>Giá sau giảm (VNĐ)</label>
                                <input type="number" name="salePrice" id="salePrice" class="form-control product-readonly" min="0" readonly>
                                <small class="product-help">Tự động tính theo giá gốc và % giảm</small>
                            </div>

                            <div class="form-group">
                                <label>Ảnh chính *</label>
                                <input type="text" name="imageUrl" id="productImage" class="form-control" required placeholder="assets/images/products/...">
                            </div>
                        </div>
                    </section>
                </div>

                <div class="product-modal-footer">
                    <p class="product-modal-note">
                        <i class="fas fa-circle-info"></i>
                        Thông tin được lưu ngay vào bảng quản trị sau khi bấm nút lưu.
                    </p>
                    <button type="submit" class="btn-submit">Lưu sản phẩm</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    const modal = document.getElementById('productModal');
    const contextPath = '${pageContext.request.contextPath}';

    function normalizeImageUrl(imageUrl) {
        const value = (imageUrl || '').trim();

        if (!value) {
            return 'https://placehold.co/640x420?text=Preview';
        }

        if (value.startsWith('http://') || value.startsWith('https://') || value.startsWith('data:')) {
            return value;
        }

        if (value.startsWith(contextPath + '/')) {
            return value;
        }

        const normalized = value.startsWith('/') ? value : '/' + value;
        return contextPath + normalized;
    }

    function calculateSalePrice() {
        const price = parseFloat(document.getElementById('productPrice').value) || 0;
        const discount = parseFloat(document.getElementById('discountPercent').value) || 0;
        const image = document.getElementById('productImage').value.trim();

        if (discount > 0 && price > 0) {
            const salePrice = price * (1 - discount / 100);
            document.getElementById('salePrice').value = Math.round(salePrice);
        } else {
            document.getElementById('salePrice').value = '';
        }

        updateProductPreview({
            name: document.getElementById('productName').value.trim(),
            price: price,
            salePrice: document.getElementById('salePrice').value,
            discount: discount,
            stock: document.getElementById('productStock').value.trim(),
            image: image
        });
    }

    function openAddModal() {
        document.getElementById('modalTitle').innerText = 'Thêm sản phẩm mới';
        document.getElementById('modalModeBadge').innerText = 'Thêm mới';
        document.getElementById('modalSubtitle').innerText = 'Nhập thông tin sản phẩm để hiển thị trong danh sách quản trị.';
        document.getElementById('formAction').value = 'add';
        document.getElementById('productId').value = '';
        document.getElementById('productName').value = '';
        document.getElementById('categoryId').value = '';
        document.getElementById('productDesc').value = '';
        document.getElementById('productPrice').value = '';
        document.getElementById('discountPercent').value = '0';
        document.getElementById('salePrice').value = '';
        document.getElementById('productStock').value = '';
        document.getElementById('productImage').value = '';
        modal.classList.add('show');
        refreshProductPreview();
    }

    function openEditModal(id, name, desc, catId, price, image, stock, discount, salePrice) {
        document.getElementById('modalTitle').innerText = 'Cập nhật sản phẩm';
        document.getElementById('modalModeBadge').innerText = 'Cập nhật';
        document.getElementById('modalSubtitle').innerText = 'Điều chỉnh thông tin sản phẩm đang hiển thị trong cửa hàng.';
        document.getElementById('formAction').value = 'edit';
        document.getElementById('productId').value = id;
        document.getElementById('productName').value = name;
        document.getElementById('categoryId').value = catId;
        document.getElementById('productDesc').value = desc || '';
        document.getElementById('productPrice').value = price;
        document.getElementById('discountPercent').value = discount || 0;
        document.getElementById('salePrice').value = salePrice || '';
        document.getElementById('productStock').value = stock;
        document.getElementById('productImage').value = image;
        modal.classList.add('show');
        refreshProductPreview();
    }

    function openEditModalFromButton(button) {
        openEditModal(
            button.dataset.productId,
            button.dataset.productName,
            button.dataset.productDesc,
            button.dataset.productCategoryId,
            button.dataset.productPrice,
            button.dataset.productImage,
            button.dataset.productStock,
            button.dataset.productDiscount,
            button.dataset.productSalePrice
        );
    }

    function closeModal() {
        modal.classList.remove('show');
    }

    function refreshProductPreview() {
        updateProductPreview({
            name: document.getElementById('productName').value.trim(),
            price: document.getElementById('productPrice').value,
            salePrice: document.getElementById('salePrice').value,
            discount: document.getElementById('discountPercent').value,
            stock: document.getElementById('productStock').value.trim(),
            image: document.getElementById('productImage').value.trim()
        });
    }

    function updateProductPreview(data) {
        const previewName = document.getElementById('productPreviewName');
        const previewMeta = document.getElementById('productPreviewMeta');
        const previewImage = document.getElementById('productPreviewImage');

        previewName.innerText = data.name || 'Xem trước sản phẩm';

        const parts = [];
        if (data.price) parts.push('Giá: ' + Number(data.price).toLocaleString('vi-VN') + 'đ');
        if (data.salePrice) parts.push('Sau giảm: ' + Number(data.salePrice).toLocaleString('vi-VN') + 'đ');
        if (data.discount && Number(data.discount) > 0) parts.push('Giảm ' + data.discount + '%');
        if (data.stock) parts.push('Tồn kho: ' + data.stock);
        previewMeta.innerText = parts.length ? parts.join(' • ') : 'Chọn tên, giá và ảnh để xem nhanh bố cục hiển thị.';

        if (data.image) {
            previewImage.src = normalizeImageUrl(data.image);
        } else {
            previewImage.src = 'https://placehold.co/640x420?text=Preview';
        }
    }

    window.onclick = function(event) {
        if (event.target == modal) closeModal();
    }

    // Auto calculate when price changes
    document.getElementById('productPrice').addEventListener('input', calculateSalePrice);
    document.getElementById('productName').addEventListener('input', refreshProductPreview);
    document.getElementById('productStock').addEventListener('input', refreshProductPreview);
    document.getElementById('productImage').addEventListener('input', refreshProductPreview);
    document.getElementById('discountPercent').addEventListener('input', calculateSalePrice);

    document.querySelectorAll('.btn-edit').forEach(function (button) {
        button.addEventListener('click', function () {
            openEditModalFromButton(button);
        });
    });

    // Check if page loaded with action=add parameter
    window.addEventListener('DOMContentLoaded', function() {
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('action') === 'add') {
            openAddModal();
            // Clean URL without reloading
            window.history.replaceState({}, document.title, window.location.pathname);
        }
    });
</script>
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/admin-main.js"></script>
</body>
</html>
