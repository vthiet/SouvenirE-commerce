<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết đơn hàng - Admin</title>
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
<c:set var="isProcessingOrLater" value="${orderView.status == 'Đang xử lý' or orderView.status == 'Đang giao' or orderView.status == 'Hoàn thành'}" />
<c:set var="isShippingOrLater" value="${orderView.status == 'Đang giao' or orderView.status == 'Hoàn thành'}" />
<c:set var="isCompleted" value="${orderView.status == 'Hoàn thành'}" />
<c:set var="isCanceled" value="${orderView.status == 'Đã hủy'}" />

<div class="admin-shell">
    <div class="sidebar-backdrop" data-sidebar-close></div>
    <jsp:include page="common/admin-sidebar.jsp"/>

    <div class="admin-main">
        <jsp:include page="common/admin-topbar.jsp"/>

        <main class="dashboard-content">
            <div class="container-fluid px-3 px-lg-4 py-4 admin-page order-detail-page">
                <section class="admin-page-hero order-detail-hero">
                    <div class="hero-copy">
                        <a href="${ctx}/admin/orders" class="detail-back-link">
                            <i class="fas fa-arrow-left"></i>
                            Trở về danh sách đơn hàng
                        </a>
                        <span class="page-eyebrow">Order detail</span>
                        <h1>Đơn hàng #${orderView.id}</h1>
                        <p class="page-lead">
                            Hồ sơ chi tiết của đơn hàng, bao gồm trạng thái, thanh toán, khách hàng và danh sách sản phẩm.
                        </p>
                        <div class="hero-meta">
                            <div class="meta-pill">
                                <span>Đặt lúc</span>
                                <strong><fmt:formatDate value="${orderView.orderDate}" pattern="dd/MM/yyyy HH:mm"/></strong>
                            </div>
                            <div class="meta-pill">
                                <span>Thanh toán</span>
                                <strong><c:choose><c:when test="${not empty orderView.paymentMethod}">${orderView.paymentMethod}</c:when><c:otherwise>COD</c:otherwise></c:choose></strong>
                            </div>
                            <div class="meta-pill">
                                <span>Tổng tiền</span>
                                <strong><fmt:formatNumber value="${orderView.totalAmount}" pattern="#,###"/>₫</strong>
                            </div>
                        </div>
                    </div>

                    <div class="hero-surface">
                        <span class="hero-surface-label">Trạng thái hiện tại</span>
                        <c:choose>
                            <c:when test="${orderView.status == 'Chờ xác nhận'}">
                                <div class="detail-status-pill is-warning">${orderView.status}</div>
                            </c:when>
                            <c:when test="${orderView.status == 'Đang xử lý'}">
                                <div class="detail-status-pill is-processing">${orderView.status}</div>
                            </c:when>
                            <c:when test="${orderView.status == 'Đang giao'}">
                                <div class="detail-status-pill is-shipping">${orderView.status}</div>
                            </c:when>
                            <c:when test="${orderView.status == 'Hoàn thành'}">
                                <div class="detail-status-pill is-success">${orderView.status}</div>
                            </c:when>
                            <c:when test="${orderView.status == 'Đã hủy'}">
                                <div class="detail-status-pill is-danger">${orderView.status}</div>
                            </c:when>
                            <c:otherwise>
                                <div class="detail-status-pill is-neutral">${orderView.status}</div>
                            </c:otherwise>
                        </c:choose>
                        <div class="hero-surface-note">Cập nhật trạng thái ngay từ đây nếu cần.</div>

                        <div class="order-detail-actions">
                            <button type="button" class="btn btn-outline-light order-action-btn" onclick="window.print()">
                                <i class="fas fa-print"></i>
                                In đơn hàng
                            </button>
                            <c:if test="${canUpdateOrder}">
                                <button type="button" class="btn btn-primary order-action-btn" onclick="showUpdateStatusModal(${orderView.id}, '${orderView.status}')">
                                    <i class="fas fa-pen-to-square"></i>
                                    Cập nhật trạng thái
                                </button>
                            </c:if>
                        </div>
                    </div>
                </section>

                <section class="order-summary-grid">
                    <article class="summary-card">
                        <span class="summary-label">Tổng tiền</span>
                        <div class="summary-value"><fmt:formatNumber value="${orderView.totalAmount}" pattern="#,###"/>₫</div>
                        <span class="summary-note">Giá trị đơn hàng hiện tại</span>
                    </article>
                    <article class="summary-card">
                        <span class="summary-label">Ngày đặt</span>
                        <div class="summary-value"><fmt:formatDate value="${orderView.orderDate}" pattern="dd/MM/yyyy"/></div>
                        <span class="summary-note">Thời điểm tạo đơn</span>
                    </article>
                    <article class="summary-card">
                        <span class="summary-label">Thanh toán</span>
                        <div class="summary-value"><c:choose><c:when test="${not empty orderView.paymentMethod}">${orderView.paymentMethod}</c:when><c:otherwise>COD</c:otherwise></c:choose></div>
                        <span class="summary-note">Phương thức mà khách chọn</span>
                    </article>
                    <article class="summary-card">
                        <span class="summary-label">Khách hàng</span>
                        <div class="summary-value">${orderView.customerName}</div>
                        <span class="summary-note">${orderView.customerEmail}</span>
                    </article>
                </section>

                <section class="order-detail-layout">
                    <div class="order-detail-main">
                        <article class="card admin-panel-card order-section-card">
                            <div class="card-header order-section-header">
                                <div>
                                    <h3>Sản phẩm trong đơn</h3>
                                    <p class="orders-card-subtitle">Danh sách mặt hàng được ghi nhận trong đơn hàng.</p>
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
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:choose>
                                        <c:when test="${not empty orderItemViews}">
                                            <c:forEach items="${orderItemViews}" var="item">
                                                <c:set var="resolvedItemName" value="${item.productName}" />
                                                <c:set var="resolvedItemImageUrl" value="${empty item.productImageUrl ? 'https://placehold.co/120x120?text=No+Image' : item.productImageUrl}" />
                                                <tr>
                                                    <td>
                                                        <div class="order-item-product">
                                                            <img src="${resolvedItemImageUrl}"
                                                                 alt="${resolvedItemName}"
                                                                 class="order-item-thumb"
                                                                 onerror="this.src='https://placehold.co/120x120?text=No+Image'">
                                                            <div class="order-item-copy">
                                                                <strong>${resolvedItemName}</strong>
                                                                <span>Mặt hàng trong đơn #${orderView.id}</span>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td><fmt:formatNumber value="${item.unitPrice}" pattern="#,###"/>₫</td>
                                                    <td>${item.quantity}</td>
                                                    <td><fmt:formatNumber value="${item.subtotal}" pattern="#,###"/>₫</td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="4" class="orders-empty-state">
                                                    <div class="orders-empty-card">
                                                        <i class="fas fa-box-open orders-empty-icon"></i>
                                                        <p>Chưa có dữ liệu sản phẩm cho đơn hàng này.</p>
                                                        <span class="orders-empty-note">Nếu controller chưa nạp danh sách mặt hàng, phần này sẽ tự chuyển sang trạng thái trống.</span>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                    </tbody>
                                </table>
                            </div>
                        </article>

                        <article class="card admin-panel-card order-section-card">
                            <div class="card-header order-section-header">
                                <div>
                                    <h3>Tiến trình xử lý</h3>
                                    <p class="orders-card-subtitle">Trạng thái đơn hàng được làm nổi bật theo mốc hiện tại.</p>
                                </div>
                            </div>

                            <div class="order-timeline">
                                <div class="timeline-step ${isCanceled ? '' : 'is-complete'}">
                                    <span class="timeline-dot"></span>
                                    <div class="timeline-copy">
                                        <strong>Đơn hàng được tạo</strong>
                                        <span>Hệ thống đã ghi nhận đơn hàng này.</span>
                                    </div>
                                </div>
                                <div class="timeline-step ${isProcessingOrLater ? 'is-complete' : (orderView.status == 'Chờ xác nhận' ? 'is-current' : '')}">
                                    <span class="timeline-dot"></span>
                                    <div class="timeline-copy">
                                        <strong>Đang xử lý</strong>
                                        <span>Đơn đang được xác nhận và chuẩn bị.</span>
                                    </div>
                                </div>
                                <div class="timeline-step ${isShippingOrLater ? 'is-complete' : (orderView.status == 'Đang xử lý' ? 'is-current' : '')}">
                                    <span class="timeline-dot"></span>
                                    <div class="timeline-copy">
                                        <strong>Đang giao</strong>
                                        <span>Đơn đã chuyển sang đơn vị vận chuyển.</span>
                                    </div>
                                </div>
                                <div class="timeline-step ${isCompleted ? 'is-complete is-current' : ''}">
                                    <span class="timeline-dot"></span>
                                    <div class="timeline-copy">
                                        <strong>Hoàn thành</strong>
                                        <span>Đơn hàng đã kết thúc trạng thái xử lý.</span>
                                    </div>
                                </div>
                            </div>
                        </article>
                    </div>

                    <aside class="order-detail-aside">
                        <article class="card admin-panel-card order-side-card">
                            <div class="card-header order-section-header">
                                <div>
                                    <h3>Khách hàng</h3>
                                    <p class="orders-card-subtitle">Thông tin liên hệ của người đặt hàng.</p>
                                </div>
                            </div>
                            <div class="order-info-list">
                                <div class="order-info-row">
                                    <span>Họ tên</span>
                                    <strong>${orderView.customerName}</strong>
                                </div>
                                <div class="order-info-row">
                                    <span>Email</span>
                                    <strong>${orderView.customerEmail}</strong>
                                </div>
                                <div class="order-info-row">
                                    <span>Điện thoại</span>
                                    <strong><c:choose><c:when test="${not empty orderView.customerPhone}">${orderView.customerPhone}</c:when><c:otherwise>Chưa cập nhật</c:otherwise></c:choose></strong>
                                </div>
                            </div>
                        </article>

                        <article class="card admin-panel-card order-side-card">
                            <div class="card-header order-section-header">
                                <div>
                                    <h3>Giao hàng</h3>
                                    <p class="orders-card-subtitle">Địa chỉ nhận hàng và ghi chú.</p>
                                </div>
                            </div>
                            <div class="order-info-list">
                                <div class="order-info-row">
                                    <span>Địa chỉ</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty orderView.shippingAddress}">${orderView.shippingAddress}</c:when>
                                            <c:otherwise>Chưa có thông tin</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                                <div class="order-info-row">
                                    <span>Ghi chú</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty orderView.note}">${orderView.note}</c:when>
                                            <c:otherwise>Không có ghi chú</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                            </div>
                        </article>

                        <article class="card admin-panel-card order-side-card">
                            <div class="card-header order-section-header">
                                <div>
                                    <h3>Thanh toán</h3>
                                    <p class="orders-card-subtitle">Tổng kết giao dịch của đơn hàng.</p>
                                </div>
                            </div>
                            <div class="order-totals">
                                <div class="order-info-row">
                                    <span>Phương thức</span>
                                    <strong><c:choose><c:when test="${not empty orderView.paymentMethod}">${orderView.paymentMethod}</c:when><c:otherwise>COD</c:otherwise></c:choose></strong>
                                </div>
                                <div class="order-info-row">
                                    <span>Trạng thái</span>
                                    <strong>${orderView.status}</strong>
                                </div>
                                <div class="order-info-row order-total-highlight">
                                    <span>Tổng tiền</span>
                                    <strong><fmt:formatNumber value="${orderView.totalAmount}" pattern="#,###"/>₫</strong>
                                </div>
                            </div>
                        </article>
                    </aside>
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
