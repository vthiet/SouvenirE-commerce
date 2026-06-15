<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý đơn hàng - Admin</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-pages.css">
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
                <h1>Quản lý đơn hàng</h1>
            </div>

            <div class="stats-grid orders-stats">
                <div class="stat-card orders-stat-pending">
                    <div class="stat-icon">
                        <i class="fas fa-clock"></i>
                    </div>
                    <div class="stat-info">
                        <h3>Chờ xác nhận</h3>
                        <p class="stat-value">${pendingCount}</p>
                    </div>
                </div>

                <div class="stat-card orders-stat-processing">
                    <div class="stat-icon">
                        <i class="fas fa-box"></i>
                    </div>
                    <div class="stat-info">
                        <h3>Đang xử lý</h3>
                        <p class="stat-value">${processingCount}</p>
                    </div>
                </div>

                <div class="stat-card orders-stat-shipping">
                    <div class="stat-icon">
                        <i class="fas fa-shipping-fast"></i>
                    </div>
                    <div class="stat-info">
                        <h3>Đang giao</h3>
                        <p class="stat-value">${shippingCount}</p>
                    </div>
                </div>

                <div class="stat-card orders-stat-completed">
                    <div class="stat-icon">
                        <i class="fas fa-check-circle"></i>
                    </div>
                    <div class="stat-info">
                        <h3>Hoàn thành</h3>
                        <p class="stat-value">${completedCount}</p>
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="card-header orders-card-header">
                    <div>
                        <h3>Danh sách đơn hàng</h3>
                        <p class="orders-card-subtitle">Tổng đơn: ${totalOrders}</p>
                    </div>
                    <div class="filter-group">
                        <select class="form-select" onchange="filterByStatus(this.value)">
                            <option value="all" ${empty statusFilter || statusFilter == 'all' ? 'selected' : ''}>Tất cả trạng thái</option>
                            <option value="Chờ xác nhận" ${statusFilter == 'Chờ xác nhận' ? 'selected' : ''}>Chờ xác nhận</option>
                            <option value="Đang xử lý" ${statusFilter == 'Đang xử lý' ? 'selected' : ''}>Đang xử lý</option>
                            <option value="Đang giao" ${statusFilter == 'Đang giao' ? 'selected' : ''}>Đang giao</option>
                            <option value="Hoàn thành" ${statusFilter == 'Hoàn thành' ? 'selected' : ''}>Hoàn thành</option>
                            <option value="Đã hủy" ${statusFilter == 'Đã hủy' ? 'selected' : ''}>Đã hủy</option>
                        </select>
                    </div>
                </div>

                <div class="table-container">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>Mã đơn</th>
                            <th>Khách hàng</th>
                            <th>Ngày đặt</th>
                            <th>Tổng tiền</th>
                            <th>Trạng thái</th>
                            <th>Thanh toán</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty orders}">
                                <tr>
                                    <td colspan="7" class="orders-empty-state">
                                        <i class="fas fa-inbox orders-empty-icon"></i>
                                        <p>Chưa có đơn hàng nào</p>
                                        <p class="orders-empty-note">Đơn hàng sẽ hiển thị ở đây khi khách hàng đặt hàng</p>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${orders}" var="order">
                                    <tr>
                                        <td>#${order.id}</td>
                                        <td>
                                            <div class="orders-customer-name">${order.customerName}</div>
                                            <div class="orders-customer-email">${order.customerEmail}</div>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${order.orderDate}" pattern="dd/MM/yyyy HH:mm"/>
                                        </td>
                                        <td class="orders-total">
                                            <fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>₫
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${order.status == 'Đang xử lý'}">
                                                    <span class="badge badge-warning">${order.status}</span>
                                                </c:when>
                                                <c:when test="${order.status == 'Đang giao'}">
                                                    <span class="badge badge-info">${order.status}</span>
                                                </c:when>
                                                <c:when test="${order.status == 'Hoàn thành'}">
                                                    <span class="badge badge-success">${order.status}</span>
                                                </c:when>
                                                <c:when test="${order.status == 'Đã hủy'}">
                                                    <span class="badge badge-danger">${order.status}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-secondary">${order.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span class="badge badge-info">COD</span>
                                        </td>
                                        <td>
                                            <div class="action-buttons">
                                                <a href="${pageContext.request.contextPath}/admin/orders?action=view&id=${order.id}"
                                                   class="btn-icon" title="Xem chi tiết">
                                                    <i class="fas fa-eye"></i>
                                                </a>
                                                <c:if test="${canUpdateOrder}">
                                                    <button class="btn-icon" title="Cập nhật trạng thái"
                                                            onclick="showUpdateStatusModal(${order.id}, '${order.status}')">
                                                        <i class="fas fa-edit"></i>
                                                    </button>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal cập nhật trạng thái -->
<div id="updateStatusModal" class="modal orders-modal">
    <div class="modal-content orders-modal-content">
        <div class="modal-header">
            <h3>Cập nhật trạng thái đơn hàng</h3>
            <button class="close-btn" onclick="closeUpdateStatusModal()">&times;</button>
        </div>
        <form id="updateStatusForm" method="post" action="${pageContext.request.contextPath}/admin/orders">
            <input type="hidden" name="action" value="updateStatus">
            <input type="hidden" name="orderId" id="updateOrderId">

            <div class="form-group">
                <label>Trạng thái hiện tại:</label>
                <p id="currentStatus" class="orders-current-status"></p>
            </div>

            <div class="form-group">
                <label>Trạng thái mới: *</label>
                <select name="status" class="form-control" required>
                    <option value="">-- Chọn trạng thái --</option>
                    <option value="Chờ xác nhận">Chờ xác nhận</option>
                    <option value="Đang xử lý">Đang xử lý</option>
                    <option value="Đang giao">Đang giao</option>
                    <option value="Hoàn thành">Hoàn thành</option>
                    <option value="Đã hủy">Đã hủy</option>
                </select>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeUpdateStatusModal()">Hủy</button>
                <button type="submit" class="btn btn-primary">Cập nhật</button>
            </div>
        </form>
    </div>
</div>

<script>
    function filterByStatus(status) {
        window.location.href = '${pageContext.request.contextPath}/admin/orders?status=' + status;
    }

    function showUpdateStatusModal(orderId, currentStatus) {
        document.getElementById('updateOrderId').value = orderId;
        document.getElementById('currentStatus').textContent = currentStatus;
        document.getElementById('updateStatusModal').classList.add('show');
    }

    function closeUpdateStatusModal() {
        document.getElementById('updateStatusModal').classList.remove('show');
        document.getElementById('updateStatusForm').reset();
    }

    // Close modal when clicking outside
    window.onclick = function(event) {
        const modal = document.getElementById('updateStatusModal');
        if (event.target == modal) {
            closeUpdateStatusModal();
        }
    }
</script>

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/admin-main.js"></script>
</body>
</html>
