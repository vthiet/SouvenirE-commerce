<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="common/admin-access-guard.jspf" %>
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
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="admin-shell">
    <div class="sidebar-backdrop" data-sidebar-close></div>
    <jsp:include page="common/admin-sidebar.jsp"/>

    <div class="admin-main">
        <jsp:include page="common/admin-topbar.jsp"/>

        <main class="dashboard-content">
            <div class="container-fluid px-3 px-lg-4 py-4 admin-page orders-page">
                <section class="admin-page-hero orders-hero">
                    <div class="hero-copy">
                        <span class="page-eyebrow">Order center</span>
                        <h1>Quản lý đơn hàng</h1>
                        <p class="page-lead">
                            Theo dõi trạng thái, thanh toán và lịch sử đơn hàng trong một bảng điều khiển rõ ràng hơn.
                        </p>
                        <div class="hero-meta">
                            <div class="meta-pill">
                                <span>Tổng đơn</span>
                                <strong>${totalOrders}</strong>
                            </div>
                            <div class="meta-pill">
                                <span>Chờ xác nhận</span>
                                <strong>${pendingCount}</strong>
                            </div>
                            <div class="meta-pill">
                                <span>Đang xử lý</span>
                                <strong>${processingCount}</strong>
                            </div>
                        </div>
                    </div>

                    <div class="hero-surface">
                        <span class="hero-surface-label">Cập nhật gần nhất</span>
                        <div class="hero-surface-value">${not empty currentPage ? currentPage : 1}</div>
                        <div class="hero-surface-note">Trang hiện tại trong danh sách đơn hàng</div>
                    </div>
                </section>

                <c:if test="${not empty param.success}">
                    <div class="alert alert-success">Cập nhật trạng thái đơn hàng thành công.</div>
                    <c:if test="${not empty param.ghnOrderCode}">
                        <div class="alert alert-info" style="margin-top: 12px;">
                            Đơn GHN đã được tạo thành công. Mã vận đơn:
                            <strong><c:out value="${param.ghnOrderCode}"/></strong>
                        </div>
                    </c:if>
                </c:if>
                <c:if test="${not empty param.error}">
                    <div class="alert alert-danger">Cập nhật trạng thái đơn hàng thất bại. Vui lòng thử lại.</div>
                </c:if>

                <section class="stats-grid orders-stats">
                    <article class="stat-card orders-stat-pending">
                        <div class="stat-icon">
                            <i class="fas fa-clock"></i>
                        </div>
                        <div class="stat-info">
                            <h3>Chờ xác nhận</h3>
                            <p class="stat-value">${pendingCount}</p>
                            <span class="stat-caption">Đơn cần phản hồi sớm</span>
                        </div>
                    </article>

                    <article class="stat-card orders-stat-processing">
                        <div class="stat-icon">
                            <i class="fas fa-box"></i>
                        </div>
                        <div class="stat-info">
                            <h3>Đang xử lý</h3>
                            <p class="stat-value">${processingCount}</p>
                            <span class="stat-caption">Đơn đang được chuẩn bị</span>
                        </div>
                    </article>

                    <article class="stat-card orders-stat-shipping">
                        <div class="stat-icon">
                            <i class="fas fa-shipping-fast"></i>
                        </div>
                        <div class="stat-info">
                            <h3>Đang giao</h3>
                            <p class="stat-value">${shippingCount}</p>
                            <span class="stat-caption">Đơn đã bàn giao vận chuyển</span>
                        </div>
                    </article>

                    <article class="stat-card orders-stat-completed">
                        <div class="stat-icon">
                            <i class="fas fa-check-circle"></i>
                        </div>
                        <div class="stat-info">
                            <h3>Hoàn thành</h3>
                            <p class="stat-value">${completedCount}</p>
                            <span class="stat-caption">Đơn đã hoàn tất</span>
                        </div>
                    </article>
                </section>

                <section class="card admin-panel-card orders-list-card">
                    <div class="card-header orders-card-header">
                        <div>
                            <h3>Danh sách đơn hàng</h3>
                            <p class="orders-card-subtitle">Tổng đơn: ${totalOrders}</p>
                        </div>

                        <div class="orders-toolbar-actions">
                            <c:if test="${not empty statusFilter and statusFilter != 'all'}">
                                <span class="orders-active-filter">
                                    <i class="fas fa-filter"></i>
                                    Đang lọc: ${statusFilter}
                                </span>
                            </c:if>
                            <select class="form-select orders-filter-select" onchange="filterByStatus(this.value)">
                                <option value="all" ${empty statusFilter || statusFilter == 'all' ? 'selected' : ''}>Tất cả trạng thái</option>
                                <option value="Chờ xác nhận" ${statusFilter == 'Chờ xác nhận' ? 'selected' : ''}>Chờ xác nhận</option>
                                <option value="Đang xử lý" ${statusFilter == 'Đang xử lý' ? 'selected' : ''}>Đang xử lý</option>
                                <option value="Đang giao" ${statusFilter == 'Đang giao' ? 'selected' : ''}>Đang giao</option>
                                <option value="Hoàn thành" ${statusFilter == 'Hoàn thành' ? 'selected' : ''}>Hoàn thành</option>
                                <option value="Đã hủy" ${statusFilter == 'Đã hủy' ? 'selected' : ''}>Đã hủy</option>
                            </select>
                        </div>
                    </div>

                    <div class="table-container orders-table-wrap">
                        <table class="data-table orders-table">
                            <thead>
                            <tr>
                                <th>Mã đơn</th>
                                <th>Khách hàng</th>
                                <th>Ngày đặt</th>
                                <th>Thanh toán</th>
                                <th>Tổng tiền</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:choose>
                                <c:when test="${empty orders}">
                                    <tr>
                                        <td colspan="7" class="orders-empty-state">
                                            <div class="orders-empty-card">
                                                <i class="fas fa-inbox orders-empty-icon"></i>
                                                <p>Chưa có đơn hàng nào.</p>
                                                <span class="orders-empty-note">Đơn hàng sẽ hiển thị ở đây khi khách hàng đặt hàng.</span>
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${orders}" var="order">
                                        <tr>
                                            <td>
                                                <div class="order-id-cell">#${order.id}</div>
                                            </td>
                                            <td>
                                                <div class="orders-customer-name">${order.customerName}</div>
                                                <div class="orders-customer-email">${order.customerEmail}</div>
                                            </td>
                                            <td>
                                                <div class="order-date-cell">
                                                    <fmt:formatDate value="${order.orderDate}" pattern="dd/MM/yyyy HH:mm"/>
                                                </div>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty order.paymentMethod}">
                                                        <span class="order-payment-pill">${order.paymentMethod}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="order-payment-pill">COD</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="orders-total">
                                                <fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>₫
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${order.status == 'Chờ xác nhận'}">
                                                        <span class="order-status-pill is-warning">${order.status}</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'Đang xử lý'}">
                                                        <span class="order-status-pill is-processing">${order.status}</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'Đang giao'}">
                                                        <span class="order-status-pill is-shipping">${order.status}</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'Hoàn thành'}">
                                                        <span class="order-status-pill is-success">${order.status}</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'Đã hủy'}">
                                                        <span class="order-status-pill is-danger">${order.status}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="order-status-pill is-neutral">${order.status}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div class="action-buttons order-action-buttons">
                                                    <a href="${ctx}/admin/order-detail?id=${order.id}" class="btn-icon" title="Xem chi tiết" aria-label="Xem chi tiết đơn hàng">
                                                        <i class="fas fa-eye"></i>
                                                    </a>
                                                    <c:if test="${canUpdateOrder}">
                                                        <button type="button"
                                                                class="btn-icon"
                                                                title="Cập nhật trạng thái"
                                                                aria-label="Cập nhật trạng thái đơn hàng"
                                                                onclick="showUpdateStatusModal(${order.id}, '${order.status}')">
                                                            <i class="fas fa-pen-to-square"></i>
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

                    <c:if test="${totalPages > 1}">
                        <div class="pagination orders-pagination">
                            <c:forEach var="pageIndex" begin="1" end="${totalPages}">
                                <a class="pagination-link ${currentPage == pageIndex ? 'active' : ''}"
                                   href="${ctx}/admin/orders?page=${pageIndex}${not empty statusFilter && statusFilter != 'all' ? '&status=' + statusFilter : ''}">
                                    ${pageIndex}
                                </a>
                            </c:forEach>
                        </div>
                    </c:if>
                </section>
            </div>
        </main>
    </div>
</div>

<div id="updateStatusModal" class="modal orders-modal" aria-hidden="true">
    <div class="modal-content orders-modal-content">
        <div class="modal-header orders-modal-header">
            <div>
                <span class="orders-modal-kicker">Order action</span>
                <h3>Cập nhật trạng thái đơn hàng</h3>
            </div>
            <button type="button" class="close-btn" onclick="closeUpdateStatusModal()" aria-label="Đóng">&times;</button>
        </div>
        <form id="updateStatusForm" method="post" action="${ctx}/admin/orders">
            <input type="hidden" name="action" value="updateStatus">
            <input type="hidden" name="orderId" id="updateOrderId">
            <div class="modal-body orders-modal-body">
                <div class="form-group">
                    <label>Trạng thái hiện tại</label>
                    <p id="currentStatus" class="orders-current-status"></p>
                </div>
                <div class="form-group">
                    <label>Chọn trạng thái mới</label>
                    <select name="status" id="newStatus" class="form-control" required>
                        <option value="">-- Chọn trạng thái --</option>
                        <option value="Chờ xác nhận">Chờ xác nhận</option>
                        <option value="Đang xử lý">Đang xử lý</option>
                        <option value="Đang giao">Đang giao</option>
                        <option value="Hoàn thành">Hoàn thành</option>
                        <option value="Đã hủy">Đã hủy</option>
                    </select>
                </div>
            </div>
            <div class="modal-footer orders-modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeUpdateStatusModal()">Hủy</button>
                <button type="submit" class="btn btn-primary">Cập nhật</button>
            </div>
        </form>
    </div>
</div>

<script>
    function filterByStatus(status) {
        const baseUrl = '${ctx}/admin/orders';
        if (status === 'all') {
            window.location.href = baseUrl;
            return;
        }
        window.location.href = baseUrl + '?status=' + encodeURIComponent(status);
    }

    function showUpdateStatusModal(orderId, currentStatus) {
        document.getElementById('updateOrderId').value = orderId;
        document.getElementById('currentStatus').textContent = currentStatus;
        document.getElementById('newStatus').value = currentStatus;
        document.getElementById('updateStatusModal').classList.add('show');
        document.getElementById('updateStatusModal').setAttribute('aria-hidden', 'false');
    }

    function closeUpdateStatusModal() {
        const modal = document.getElementById('updateStatusModal');
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
        document.getElementById('updateStatusForm').reset();
    }

    window.addEventListener('click', function(event) {
        const modal = document.getElementById('updateStatusModal');
        if (event.target === modal) {
            closeUpdateStatusModal();
        }
    });

    window.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeUpdateStatusModal();
        }
    });
</script>
<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/assets/js/admin-main.js"></script>
</body>
</html>
