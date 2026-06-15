<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
                                            <button class="btn-icon btn-edit" onclick="openEditModal(${customer.id}, '${customer.fullName}', '${customer.email}', '${customer.phone}')" title="Sửa">
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

        <jsp:include page="common/admin-footer.jsp"/>
    </div>
</div>

<!-- Modal Add/Edit -->
<div id="customerModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h3 id="modalTitle">Thêm khách hàng mới</h3>
            <button class="close-btn" onclick="closeModal()">&times;</button>
        </div>
        <div class="modal-body">
            <form action="${pageContext.request.contextPath}/admin/customers" method="post">
                <input type="hidden" name="action" id="formAction" value="add">
                <input type="hidden" name="id" id="customerId">

                <div class="form-group">
                    <label>Họ tên *</label>
                    <input type="text" name="fullName" id="customerName" class="form-control" required>
                </div>

                <div class="form-group">
                    <label>Email *</label>
                    <input type="email" name="email" id="customerEmail" class="form-control" required>
                </div>

                <div class="form-group" id="passwordGroup">
                    <label>Mật khẩu *</label>
                    <input type="password" name="password" id="customerPassword" class="form-control">
                </div>

                <div class="form-group">
                    <label>Số điện thoại</label>
                    <input type="tel" name="phone" id="customerPhone" class="form-control">
                </div>

                <button type="submit" class="btn-submit">Lưu khách hàng</button>
            </form>
        </div>
    </div>
</div>

<script>
    const modal = document.getElementById('customerModal');

    function openAddModal() {
        document.getElementById('modalTitle').innerText = 'Thêm khách hàng mới';
        document.getElementById('formAction').value = 'add';
        document.getElementById('customerId').value = '';
        document.getElementById('customerName').value = '';
        document.getElementById('customerEmail').value = '';
        document.getElementById('customerPhone').value = '';
        document.getElementById('customerPassword').value = '';
        document.getElementById('customerPassword').required = true;
        document.getElementById('passwordGroup').style.display = 'block';
        modal.classList.add('show');
    }

    function openEditModal(id, name, email, phone) {
        document.getElementById('modalTitle').innerText = 'Cập nhật khách hàng';
        document.getElementById('formAction').value = 'edit';
        document.getElementById('customerId').value = id;
        document.getElementById('customerName').value = name;
        document.getElementById('customerEmail').value = email;
        document.getElementById('customerPhone').value = phone;
        document.getElementById('customerPassword').required = false;
        document.getElementById('passwordGroup').style.display = 'none';
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
