<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="common/admin-access-guard.jspf" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý khách hàng - Admin</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-pages.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-customers.css">
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
                <h1>Quản lý khách hàng</h1>
                <c:if test="${canCreateCustomer}">
                    <button class="btn-primary" onclick="openAddModal()">
                        <i class="fas fa-plus"></i> Thêm khách hàng mới
                    </button>
                </c:if>
            </div>

            <c:if test="${not empty message}">
                <div class="alert alert-${messageType}">
                        ${message}
                </div>
            </c:if>

            <div class="card">
                <div class="card-header">
                    <h3>Danh sách khách hàng (${totalCustomers} khách hàng)</h3>
                </div>

                <div class="table-container">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th class="customer-col-id">ID</th>
                            <th>Họ tên</th>
                            <th>Email</th>
                            <th>Số điện thoại</th>
                            <th class="customer-col-status">Trạng thái</th>
                            <th class="customer-col-date">Ngày đăng ký</th>
                            <th class="customer-col-actions">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${customers}" var="customer">
                            <tr>
                                <td>${customer.id}</td>
                                <td class="customer-name">${customer.fullName}</td>
                                <td>${customer.email}</td>
                                <td>${customer.phone}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${customer.status == 'Active'}">
                                            <span class="badge badge-success">Hoạt động</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-danger">Bị cấm</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${customer.createdAt}</td>
                                <td>
                                    <div class="action-buttons">
                                        <c:if test="${canUpdateCustomer}">
                                            <button
                                                    type="button"
                                                    class="btn-icon btn-edit"
                                                    title="Sửa"
                                                    data-customer-id="${customer.id}"
                                                    data-customer-name="<c:out value='${customer.fullName}' />"
                                                    data-customer-email="<c:out value='${customer.email}' />"
                                                    data-customer-phone="<c:out value='${customer.phone}' />"
                                                    data-customer-status="${customer.status}">
                                                <i class="fas fa-edit"></i>
                                            </button>
                                            <form action="${pageContext.request.contextPath}/admin/customers" method="post" class="customer-form-inline">
                                                <input type="hidden" name="action" value="toggleStatus">
                                                <input type="hidden" name="id" value="${customer.id}">
                                                <input type="hidden" name="currentStatus" value="${customer.status}">
                                                <button class="btn-icon ${customer.status == 'Active' ? 'customer-toggle-active' : 'customer-toggle-banned'}" title="${customer.status == 'Active' ? 'Cấm' : 'Mở cấm'}">
                                                    <i class="fas fa-${customer.status == 'Active' ? 'ban' : 'check'}"></i>
                                                </button>
                                            </form>
                                        </c:if>
                                        <c:if test="${canDeleteCustomer}">
                                            <form action="${pageContext.request.contextPath}/admin/customers" method="post" class="customer-form-inline" onsubmit="return confirm('Bạn có chắc muốn xóa khách hàng này?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${customer.id}">
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
                    <div class="customer-pagination">
                        <c:if test="${currentPage > 1}">
                            <a href="?page=${currentPage - 1}" class="btn-icon customer-pagination-link">
                                <i class="fas fa-chevron-left"></i>
                            </a>
                        </c:if>

                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <c:choose>
                                <c:when test="${i == currentPage}">
                                    <span class="customer-pagination-current">${i}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="?page=${i}" class="customer-pagination-page">${i}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <c:if test="${currentPage < totalPages}">
                            <a href="?page=${currentPage + 1}" class="btn-icon customer-pagination-link">
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
<div id="customerModal" class="modal customer-modal">
    <div class="modal-content admin-modal-content">
        <div class="modal-header admin-modal-header">
            <div class="admin-modal-heading">
                <span id="modalModeBadge" class="admin-modal-badge">Thêm mới</span>
                <div>
                    <h3 id="modalTitle">Thêm khách hàng mới</h3>
                    <p id="modalSubtitle" class="admin-modal-subtitle">Tạo tài khoản khách hàng và lưu thông tin liên hệ trong hệ thống.</p>
                </div>
            </div>
            <button type="button" class="close-btn" onclick="closeModal()" aria-label="Đóng">&times;</button>
        </div>
        <div class="modal-body admin-modal-body">
            <form action="${pageContext.request.contextPath}/admin/customers" method="post" class="admin-modal-form">
                <input type="hidden" name="action" id="formAction" value="add">
                <input type="hidden" name="id" id="customerId">

                <div class="admin-modal-layout">
                    <section class="admin-modal-panel">
                        <div class="section-title">
                            <i class="fas fa-user"></i>
                            Thông tin khách hàng
                        </div>

                        <div class="form-group">
                            <label>Họ tên *</label>
                            <input type="text" name="fullName" id="customerName" class="form-control" required placeholder="Nhập họ và tên">
                        </div>

                        <div class="form-group">
                            <label>Email *</label>
                            <input type="email" name="email" id="customerEmail" class="form-control" required placeholder="ten@example.com">
                        </div>

                        <div class="admin-grid-two">
                            <div class="form-group">
                                <label>Số điện thoại</label>
                                <input type="tel" name="phone" id="customerPhone" class="form-control" placeholder="Số điện thoại liên hệ">
                            </div>

                            <div class="form-group" id="passwordGroup">
                                <label>Mật khẩu *</label>
                                <input type="password" name="password" id="customerPassword" class="form-control" placeholder="Tạo mật khẩu cho tài khoản">
                            </div>
                        </div>
                    </section>

                    <section class="admin-modal-panel admin-modal-panel-accent">
                        <div class="section-title">
                            <i class="fas fa-id-card"></i>
                            Xem nhanh
                        </div>

                        <div class="admin-preview-card">
                            <div class="admin-preview-image-wrap" style="display:grid;place-items:center;">
                                <div class="profile-avatar avatar-xl" style="background:#2563eb;color:#fff;" id="customerPreviewInitials">KH</div>
                            </div>
                            <div class="admin-preview-copy">
                                <strong id="customerPreviewName">Khách hàng mới</strong>
                                <span id="customerPreviewMeta">Email, số điện thoại và trạng thái sẽ được hiển thị ở đây.</span>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Trạng thái</label>
                            <input type="text" id="customerPreviewStatus" class="form-control product-readonly" value="Hoạt động" readonly>
                            <small class="product-help">Tài khoản mới được tạo sẽ ở trạng thái hoạt động.</small>
                        </div>
                    </section>
                </div>

                <div class="admin-modal-footer">
                    <p class="admin-modal-note">
                        <i class="fas fa-circle-info"></i>
                        Mật khẩu chỉ bắt buộc khi tạo mới, không cần nhập khi chỉnh sửa.
                    </p>
                    <button type="submit" class="btn-submit">Lưu khách hàng</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    const modal = document.getElementById('customerModal');

    function openAddModal() {
        document.getElementById('modalTitle').innerText = 'Thêm khách hàng mới';
        document.getElementById('modalModeBadge').innerText = 'Thêm mới';
        document.getElementById('modalSubtitle').innerText = 'Tạo tài khoản khách hàng và lưu thông tin liên hệ trong hệ thống.';
        document.getElementById('formAction').value = 'add';
        document.getElementById('customerId').value = '';
        document.getElementById('customerName').value = '';
        document.getElementById('customerEmail').value = '';
        document.getElementById('customerPhone').value = '';
        document.getElementById('customerPassword').value = '';
        document.getElementById('customerPassword').required = true;
        document.getElementById('passwordGroup').style.display = 'block';
        modal.classList.add('show');
        refreshCustomerPreview();
    }

    function openEditModal(id, name, email, phone) {
        document.getElementById('modalTitle').innerText = 'Cập nhật khách hàng';
        document.getElementById('modalModeBadge').innerText = 'Cập nhật';
        document.getElementById('modalSubtitle').innerText = 'Chỉnh sửa thông tin liên hệ của khách hàng trong hệ thống.';
        document.getElementById('formAction').value = 'edit';
        document.getElementById('customerId').value = id;
        document.getElementById('customerName').value = name;
        document.getElementById('customerEmail').value = email;
        document.getElementById('customerPhone').value = phone;
        document.getElementById('customerPassword').required = false;
        document.getElementById('passwordGroup').style.display = 'none';
        modal.classList.add('show');
        refreshCustomerPreview();
    }

    function closeModal() {
        modal.classList.remove('show');
    }

    function refreshCustomerPreview() {
        const name = document.getElementById('customerName').value.trim();
        const email = document.getElementById('customerEmail').value.trim();
        const phone = document.getElementById('customerPhone').value.trim();
        const previewName = document.getElementById('customerPreviewName');
        const previewMeta = document.getElementById('customerPreviewMeta');
        const previewInitials = document.getElementById('customerPreviewInitials');

        previewName.innerText = name || 'Khách hàng mới';
        previewMeta.innerText = [email, phone].filter(Boolean).join(' • ') || 'Email, số điện thoại và trạng thái sẽ được hiển thị ở đây.';
        previewInitials.innerText = name
            ? name.split(' ').map(function (part) { return part.charAt(0); }).join('').slice(0, 2).toUpperCase()
            : 'KH';
    }

    window.onclick = function(event) {
        if (event.target == modal) closeModal();
    }

    document.querySelectorAll('.btn-edit').forEach(function (button) {
        button.addEventListener('click', function () {
            openEditModal(
                button.dataset.customerId,
                button.dataset.customerName,
                button.dataset.customerEmail,
                button.dataset.customerPhone
            );
        });
    });

    document.getElementById('customerName').addEventListener('input', refreshCustomerPreview);
    document.getElementById('customerEmail').addEventListener('input', refreshCustomerPreview);
    document.getElementById('customerPhone').addEventListener('input', refreshCustomerPreview);
</script>
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/admin-main.js"></script>
</body>
</html>
