<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Banner - Admin</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-pages.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-banners.css">
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
                <h1>Quản lý Banner</h1>
                <c:if test="${canCreateBanner}">
                    <button class="btn-primary" onclick="openAddModal()">
                        <i class="fas fa-plus"></i> Thêm banner mới
                    </button>
                </c:if>
            </div>

            <div class="card">
                <div class="card-header">
                    <h3>Danh sách Banner</h3>
                </div>

                <div class="banner-grid">
                    <c:forEach items="${banners}" var="banner">
                        <div class="banner-card">
                            <img src="${pageContext.request.contextPath}/${banner.imageUrl}"
                                 alt="${banner.title}"
                                 class="banner-image"
                                 onerror="this.src='https://placehold.co/300x180?text=Banner'">
                            <div class="banner-info">
                                <div class="banner-title">${banner.title}</div>
                                <p class="banner-meta">
                                    Thứ tự: <strong>#${banner.position}</strong>
                                </p>
                                <p class="banner-status-row">
                                    Trạng thái:
                                    <c:choose>
                                        <c:when test="${banner.status}">
                                            <span class="badge badge-success">Đang hiển thị</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-danger">Đã ẩn</span>
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <div class="action-buttons banner-actions">
                                    <c:if test="${canUpdateBanner}">
                                        <button class="btn-icon btn-edit" onclick="openEditModal(${banner.id}, '${banner.title}', ${banner.position}, ${banner.status}, '${pageContext.request.contextPath}/${banner.imageUrl}')" title="Sửa">
                                            <i class="fas fa-edit"></i>
                                        </button>
                                        <form action="${pageContext.request.contextPath}/admin/banner" method="post" class="banner-form-inline">
                                            <input type="hidden" name="action" value="toggle">
                                            <input type="hidden" name="id" value="${banner.id}">
                                            <button class="btn-icon ${banner.status ? 'banner-toggle-active' : 'banner-toggle-inactive'}" title="${banner.status ? 'Ẩn' : 'Hiện'}">
                                                <i class="fas fa-${banner.status ? 'eye-slash' : 'eye'}"></i>
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${canDeleteBanner}">
                                        <form action="${pageContext.request.contextPath}/admin/banner" method="post" class="banner-form-inline" onsubmit="return confirm('Bạn có chắc muốn xóa banner này?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${banner.id}">
                                            <button class="btn-icon btn-delete" title="Xóa">
                                                <i class="fas fa-trash"></i>
                                            </button>
                                        </form>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </c:forEach>

                    <c:if test="${empty banners}">
                        <div class="banner-empty-state">
                            <i class="fas fa-images banner-empty-icon"></i>
                            <p>Chưa có banner nào. Hãy thêm mới ngay!</p>
                        </div>
                    </c:if>
                </div>
            </div>

            <div class="card banner-guidelines-card">
                <div class="card-header">
                    <h3>Hướng dẫn</h3>
                </div>
                <div class="banner-guidelines-body">
                    <ul class="banner-guidelines-list">
                        <li>Kích thước banner khuyến nghị: <strong>1920x600 pixels</strong></li>
                        <li>Định dạng: JPG, PNG, WebP</li>
                        <li>Dung lượng tối đa: 2MB</li>
                        <li>Banner sẽ tự động chuyển đổi trên trang chủ</li>
                    </ul>
                </div>
            </div>
        </div>
        </main>

        <jsp:include page="common/admin-footer.jsp"/>
    </div>
</div>

<!-- Modal Add/Edit -->
<div id="bannerModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h3 id="modalTitle">Thêm banner mới</h3>
            <button class="close-btn" onclick="closeModal()">&times;</button>
        </div>
        <div class="modal-body">
            <form action="${pageContext.request.contextPath}/admin/banner" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" id="formAction" value="add">
                <input type="hidden" name="id" id="bannerId">

                <div class="form-group">
                    <label>Tiêu đề *</label>
                    <input type="text" name="title" id="bannerTitle" class="form-control" required>
                </div>

                <div class="form-group">
                    <label>Hình ảnh</label>
                    <input type="file" name="imageFile" id="bannerFile" class="form-control" accept="image/*">
                    <small id="fileHint" class="banner-file-hint">* Chỉ chọn ảnh mới nếu bạn muốn thay đổi.</small>
                </div>

                <div class="form-group">
                    <label>Thứ tự hiển thị</label>
                    <input type="number" name="position" id="bannerPosition" class="form-control" value="1" min="1">
                </div>

                <div class="form-group">
                    <label>Trạng thái</label>
                    <select name="status" id="bannerStatus" class="form-control">
                        <option value="true">Hiển thị ngay</option>
                        <option value="false">Tạm ẩn</option>
                    </select>
                </div>

                <button type="submit" class="btn-submit">Lưu banner</button>
            </form>
        </div>
    </div>
</div>

<script>
    const modal = document.getElementById('bannerModal');

    function openAddModal() {
        document.getElementById('modalTitle').innerText = 'Thêm banner mới';
        document.getElementById('formAction').value = 'add';
        document.getElementById('bannerId').value = '';
        document.getElementById('bannerTitle').value = '';
        document.getElementById('bannerPosition').value = '1';
        document.getElementById('bannerStatus').value = 'true';
        document.getElementById('bannerFile').value = '';
        document.getElementById('bannerFile').required = true;
        document.getElementById('fileHint').classList.add('is-hidden');
        modal.classList.add('show');
    }

    function openEditModal(id, title, position, status, imageUrl) {
        document.getElementById('modalTitle').innerText = 'Cập nhật banner';
        document.getElementById('formAction').value = 'update';
        document.getElementById('bannerId').value = id;
        document.getElementById('bannerTitle').value = title;
        document.getElementById('bannerPosition').value = position;
        document.getElementById('bannerStatus').value = status;
        document.getElementById('bannerFile').value = '';
        document.getElementById('bannerFile').required = false;
        document.getElementById('fileHint').classList.remove('is-hidden');
        modal.classList.add('show');
    }

    function closeModal() {
        modal.classList.remove('show');
    }

    window.onclick = function(event) {
        if (event.target == modal) closeModal();
    }
</script>
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/admin-main.js"></script>
</body>
</html>
