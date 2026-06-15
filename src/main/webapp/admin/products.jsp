<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
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
                            <tr>
                                <td>${p.id}</td>
                                <td>
                                    <img src="${p.imageUrl}"
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
                                            <button class="btn-icon btn-edit" onclick="openEditModal(${p.id}, '${p.name}', '${p.description}', ${p.categoryId}, ${p.originalPrice}, '${p.imageUrl}', ${p.stockQuantity}, ${p.discountPercent}, ${p.salePrice != null ? p.salePrice : 0})" title="Sửa">
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

        <jsp:include page="common/admin-footer.jsp"/>
    </div>
</div>

<!-- Modal Add/Edit -->
<div id="productModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h3 id="modalTitle">Thêm sản phẩm mới</h3>
            <button class="close-btn" onclick="closeModal()">&times;</button>
        </div>
        <div class="modal-body">
            <form action="${pageContext.request.contextPath}/admin/products" method="post">
                <input type="hidden" name="action" id="formAction" value="add">
                <input type="hidden" name="id" id="productId">

                <div class="form-group">
                    <label>Tên sản phẩm *</label>
                    <input type="text" name="name" id="productName" class="form-control" required>
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
                    <textarea name="description" id="productDesc" class="form-control" rows="3"></textarea>
                </div>

                <div class="form-group">
                    <label>Giá gốc (VNĐ) *</label>
                    <input type="number" name="price" id="productPrice" class="form-control" required min="0">
                </div>

                <div class="form-group">
                    <label>Giảm giá (%)</label>
                    <input type="number" name="discountPercent" id="discountPercent" class="form-control" min="0" max="100" value="0" onchange="calculateSalePrice()">
                    <small class="product-help">Nhập 0 nếu không có giảm giá</small>
                </div>

                <div class="form-group">
                    <label>Giá sau giảm (VNĐ)</label>
                    <input type="number" name="salePrice" id="salePrice" class="form-control product-readonly" min="0" readonly>
                    <small class="product-help">Tự động tính khi nhập % giảm giá</small>
                </div>

                <div class="form-group">
                    <label>Số lượng tồn kho *</label>
                    <input type="number" name="stock" id="productStock" class="form-control" required min="0">
                </div>

                <div class="form-group">
                    <label>URL hình ảnh *</label>
                    <input type="text" name="imageUrl" id="productImage" class="form-control" required placeholder="assets/images/products/...">
                </div>

                <button type="submit" class="btn-submit">Lưu sản phẩm</button>
            </form>
        </div>
    </div>
</div>

<script>
    const modal = document.getElementById('productModal');

    function calculateSalePrice() {
        const price = parseFloat(document.getElementById('productPrice').value) || 0;
        const discount = parseFloat(document.getElementById('discountPercent').value) || 0;

        if (discount > 0 && price > 0) {
            const salePrice = price * (1 - discount / 100);
            document.getElementById('salePrice').value = Math.round(salePrice);
        } else {
            document.getElementById('salePrice').value = '';
        }
    }

    function openAddModal() {
        document.getElementById('modalTitle').innerText = 'Thêm sản phẩm mới';
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
    }

    function openEditModal(id, name, desc, catId, price, image, stock, discount, salePrice) {
        document.getElementById('modalTitle').innerText = 'Cập nhật sản phẩm';
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
    }

    function closeModal() {
        modal.classList.remove('show');
    }

    window.onclick = function(event) {
        if (event.target == modal) closeModal();
    }

    // Auto calculate when price changes
    document.getElementById('productPrice').addEventListener('input', calculateSalePrice);

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
