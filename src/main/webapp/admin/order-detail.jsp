<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="common/admin-access-guard.jspf" %>
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
                <c:if test="${not empty param.success}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert" style="margin-bottom: 20px;">
                        <i class="fas fa-check-circle"></i> Thao tác xử lý đơn hàng thành công!
                    </div>
                </c:if>
                <c:if test="${not empty param.error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert" style="margin-bottom: 20px;">
                        <i class="fas fa-exclamation-triangle"></i> Lỗi: <c:out value="${param.error}"/>
                    </div>
                </c:if>
                <section class="admin-page-hero order-detail-hero">
                    <div class="hero-copy">
                        <a href="${ctx}/admin/orders" class="detail-back-link">
                            <i class="fas fa-arrow-left"></i>
                            Trở về danh sách đơn hàng
                        </a>
                        <span class="page-eyebrow">Chi tiết đơn hàng</span>
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
                                <c:choose>
                                    <c:when test="${orderView.status == 'Chờ xác nhận'}">
                                        <form method="post" action="${ctx}/admin/order-detail" style="display:inline;">
                                            <input type="hidden" name="orderId" value="${orderView.id}">
                                            <input type="hidden" name="action" value="confirm">
                                            <button type="submit" class="btn btn-success order-action-btn">
                                                <i class="fas fa-check"></i> Nhận đơn
                                            </button>
                                        </form>
                                        <button type="button" class="btn btn-danger order-action-btn" onclick="showCancelModal(${orderView.id})">
                                            <i class="fas fa-times"></i> Từ chối đơn
                                        </button>
                                    </c:when>
                                    <c:when test="${orderView.status == 'Đang xử lý'}">
                                        <form method="post" action="${ctx}/admin/order-detail" style="display:inline;">
                                            <input type="hidden" name="orderId" value="${orderView.id}">
                                            <input type="hidden" name="action" value="ship">
                                            <button type="submit" class="btn btn-primary order-action-btn">
                                                <i class="fas fa-shipping-fast"></i> Bắt đầu giao hàng
                                            </button>
                                        </form>
                                        <button type="button" class="btn btn-danger order-action-btn" onclick="showCancelModal(${orderView.id})">
                                            <i class="fas fa-times"></i> Hủy đơn
                                        </button>
                                    </c:when>
                                    <c:when test="${orderView.status == 'Đang giao'}">
                                        <form method="post" action="${ctx}/admin/order-detail" style="display:inline;">
                                            <input type="hidden" name="orderId" value="${orderView.id}">
                                            <input type="hidden" name="action" value="complete">
                                            <button type="submit" class="btn btn-success order-action-btn">
                                                <i class="fas fa-check-circle"></i> Hoàn thành đơn hàng
                                            </button>
                                        </form>
                                        <button type="button" class="btn btn-danger order-action-btn" onclick="showCancelModal(${orderView.id})">
                                            <i class="fas fa-times"></i> Hủy đơn (Giao thất bại)
                                        </button>
                                        <a href="${ctx}/admin/order-detail?id=${orderView.id}&action=syncGhn" class="btn btn-info order-action-btn text-white">
                                            <i class="fas fa-sync"></i> Đồng bộ GHN
                                        </a>
                                    </c:when>
                                    <c:when test="${orderView.status == 'Chờ thanh toán'}">
                                        <button type="button" class="btn btn-danger order-action-btn" onclick="showCancelModal(${orderView.id})">
                                            <i class="fas fa-times"></i> Hủy đơn
                                        </button>
                                    </c:when>
                                </c:choose>
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

                        <article class="card admin-panel-card order-section-card" style="margin-top: 24px;">
                            <div class="card-header order-section-header">
                                <div>
                                    <h3>Lịch sử xử lý đơn hàng</h3>
                                    <p class="orders-card-subtitle">Nhật ký chi tiết các bước vận hành hệ thống.</p>
                                </div>
                            </div>
                            <div class="card-body">
                                <div class="order-history-timeline" style="margin-left: 20px; border-left: 2px solid #e9ecef; padding-left: 20px; position: relative;">
                                    <c:forEach items="${historyList}" var="history">
                                        <div class="history-item" style="margin-bottom: 20px; position: relative;">
                                            <span class="history-badge" style="position: absolute; left: -31px; top: 2px; width: 20px; height: 20px; border-radius: 50%; background-color: #0d6efd; border: 4px solid #fff;"></span>
                                            <div style="font-size: 0.85rem; color: #6c757d;">
                                                <fmt:formatDate value="${java.sql.Timestamp.valueOf(history.createdAt)}" pattern="dd/MM/yyyy HH:mm:ss"/> 
                                                &middot; Thực hiện bởi: <strong><c:out value="${history.performedBy}"/></strong>
                                            </div>
                                            <div style="font-weight: 600; color: #212529; margin-top: 2px;">
                                                Trạng thái: <span class="badge bg-secondary"><c:out value="${history.status}"/></span>
                                            </div>
                                            <div style="margin-top: 4px; color: #495057;">
                                                <c:out value="${history.description}"/>
                                            </div>
                                        </div>
                                    </c:forEach>
                                    <c:if test="${empty historyList}">
                                        <p class="text-muted">Chưa có nhật ký xử lý cho đơn hàng này.</p>
                                    </c:if>
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
                                    <p class="orders-card-subtitle">Địa chỉ nhận hàng, phí ship và trạng thái GHN.</p>
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
                                <div class="order-info-row">
                                    <span>Phí vận chuyển</span>
                                    <strong><fmt:formatNumber value="${orderView.shippingFee}" pattern="#,###"/>₫</strong>
                                </div>
                                <div class="order-info-row">
                                    <span>Mã vận đơn GHN</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty orderView.ghnOrderCode}">${orderView.ghnOrderCode}</c:when>
                                            <c:otherwise>Chưa tạo</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                                <div class="order-info-row">
                                    <span>Trạng thái GHN</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${orderView.ghnStatus eq 'ready_to_pick'}">Đang chờ lấy hàng</c:when>
                                            <c:when test="${orderView.ghnStatus eq 'picked'}">Đã lấy hàng</c:when>
                                            <c:when test="${orderView.ghnStatus eq 'transporting'}">Đang vận chuyển</c:when>
                                            <c:when test="${orderView.ghnStatus eq 'delivering'}">Đang giao</c:when>
                                            <c:when test="${orderView.ghnStatus eq 'delivered'}">Đã giao</c:when>
                                            <c:when test="${orderView.ghnStatus eq 'returned'}">Đã hoàn hàng</c:when>
                                            <c:when test="${orderView.ghnStatus eq 'create_failed'}">Tạo đơn GHN thất bại</c:when>
                                            <c:when test="${not empty orderView.ghnStatus}">${orderView.ghnStatus}</c:when>
                                            <c:otherwise>Chưa đồng bộ</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                                <div class="order-info-row">
                                    <span>Cập nhật gần nhất</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty orderView.ghnUpdatedAt}">
                                                <fmt:formatDate value="${orderView.ghnUpdatedAt}" pattern="dd/MM/yyyy HH:mm"/>
                                            </c:when>
                                            <c:otherwise>Chưa có dữ liệu</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                                <div class="order-info-row">
                                    <span>Dự kiến giao</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty orderView.ghnLeadtime}">
                                                <fmt:formatDate value="${orderView.ghnLeadtime}" pattern="dd/MM/yyyy HH:mm"/>
                                            </c:when>
                                            <c:otherwise>Chưa xác định</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                                <div class="order-info-row">
                                    <span>Hoàn tất giao</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty orderView.ghnFinishDate}">
                                                <fmt:formatDate value="${orderView.ghnFinishDate}" pattern="dd/MM/yyyy HH:mm"/>
                                            </c:when>
                                            <c:otherwise>Chưa hoàn tất</c:otherwise>
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

<div id="cancelOrderModal" class="modal orders-modal" aria-hidden="true">
    <div class="modal-content orders-modal-content">
        <div class="modal-header orders-modal-header text-danger">
            <div>
                <span class="orders-modal-kicker text-danger">Hành động hủy</span>
                <h3>Xác nhận hủy đơn hàng</h3>
            </div>
            <button type="button" class="close-btn" onclick="closeCancelModal()" aria-label="Đóng">&times;</button>
        </div>
        <form method="post" action="${ctx}/admin/order-detail">
            <input type="hidden" name="action" value="cancel">
            <input type="hidden" name="orderId" id="cancelOrderId">
            <div class="modal-body orders-modal-body">
                <div class="form-group" style="margin-bottom: 15px;">
                    <label for="cancelReason" style="font-weight:600; margin-bottom: 5px;">Lý do hủy đơn hàng <span class="text-danger">*</span></label>
                    <textarea name="reason" id="cancelReason" class="form-control" placeholder="Nhập lý do hủy đơn hàng (Khách yêu cầu, Hết hàng, Giao hàng thất bại...)" required rows="3"></textarea>
                </div>
            </div>
            <div class="modal-footer orders-modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCancelModal()">Quay lại</button>
                <button type="submit" class="btn btn-danger">Xác nhận hủy</button>
            </div>
        </form>
    </div>
</div>

<script>
    function showCancelModal(orderId) {
        document.getElementById('cancelOrderId').value = orderId;
        document.getElementById('cancelOrderModal').classList.add('show');
        document.getElementById('cancelOrderModal').setAttribute('aria-hidden', 'false');
    }

    function closeCancelModal() {
        const modal = document.getElementById('cancelOrderModal');
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
        document.getElementById('cancelReason').value = '';
    }

    window.addEventListener('click', function(event) {
        const cancelModal = document.getElementById('cancelOrderModal');
        if (event.target === cancelModal) {
            closeCancelModal();
        }
    });

    window.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeCancelModal();
        }
    });
</script>
<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/assets/js/admin-main.js"></script>
</body>
</html>
