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
                        <span class="page-eyebrow">Tổng quan đơn hàng</span>
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
                        <span class="hero-surface-label">Doanh thu 6 tháng</span>
                        <div class="hero-surface-value">
                            <fmt:formatNumber value="${monthlyRevenueTotal}" pattern="#,###"/>₫
                        </div>
                        <div class="hero-surface-note">Chỉ tính đơn hoàn thành trong 6 tháng gần nhất</div>
                    </div>
                </section>

                <c:if test="${not empty param.success}">
                    <div class="alert alert-success">Cập nhật trạng thái đơn hàng thành công.</div>
                </c:if>
                <c:if test="${not empty param.error}">
                    <div class="alert alert-danger">Cập nhật trạng thái đơn hàng thất bại. Vui lòng thử lại.</div>
                </c:if>

                <section class="order-analytics-grid">
                    <article class="card admin-panel-card orders-chart-card">
                        <div class="card-header orders-card-header">
                            <div>
                                <h3>Biểu đồ doanh thu</h3>
                                <p class="orders-card-subtitle">
                                    Dữ liệu chỉ tính đơn hoàn thành. Chuyển giữa 7 tuần và 6 tháng gần nhất để theo dõi nhịp bán hàng.
                                </p>
                            </div>
                            <div class="orders-range-toggle" role="group" aria-label="Chọn chu kỳ thống kê">
                                <button type="button" class="range-toggle-button active" data-chart-range="weekly">
                                    7 tuần gần nhất
                                </button>
                                <button type="button" class="range-toggle-button" data-chart-range="monthly">
                                    6 tháng gần nhất
                                </button>
                            </div>
                        </div>

                        <div class="card-body orders-analytics-body">
                            <div class="orders-analytics-stats">
                                <article class="analytics-stat analytics-stat--weekly">
                                    <span>Doanh thu tuần</span>
                                    <strong><fmt:formatNumber value="${weeklyRevenueTotal}" pattern="#,###"/>₫</strong>
                                    <small>${weeklyOrderCount} đơn hoàn thành</small>
                                </article>

                                <article class="analytics-stat analytics-stat--monthly">
                                    <span>Doanh thu tháng</span>
                                    <strong><fmt:formatNumber value="${monthlyRevenueTotal}" pattern="#,###"/>₫</strong>
                                    <small>${monthlyOrderCount} đơn hoàn thành</small>
                                </article>
                            </div>

                            <div class="orders-chart-wrap">
                                <canvas id="orderRevenueChart" class="orders-revenue-chart"></canvas>
                            </div>
                        </div>
                    </article>

                    <aside class="card admin-panel-card orders-profit-card">
                        <div class="card-header orders-card-header">
                            <div>
                                <h3>Đơn lợi nhất</h3>
                                <p class="orders-card-subtitle">
                                    Sắp xếp theo lợi ước tính, dựa trên doanh thu hàng hóa sau khi trừ phí vận chuyển.
                                </p>
                            </div>
                            <span class="orders-profit-pill">KPI ước tính</span>
                        </div>

                        <div class="card-body orders-profit-body">
                            <c:choose>
                                <c:when test="${not empty mostProfitableOrder}">
                                    <div class="profit-order-hero">
                                        <div class="profit-order-id">#${mostProfitableOrder.orderId}</div>
                                        <div class="profit-order-code"><c:out value="${mostProfitableOrder.orderCode}"/></div>
                                    </div>

                                    <div class="profit-order-value">
                                        <span>Lợi ước tính</span>
                                        <strong><fmt:formatNumber value="${mostProfitableOrder.estimatedProfit}" pattern="#,###"/>₫</strong>
                                    </div>

                                    <div class="profit-order-meta">
                                        <div class="profit-order-row">
                                            <span>Khách hàng</span>
                                            <strong><c:out value="${mostProfitableOrder.customerName}"/></strong>
                                        </div>
                                        <div class="profit-order-row">
                                            <span>Trạng thái</span>
                                            <strong><c:out value="${mostProfitableOrder.status}"/></strong>
                                        </div>
                                        <div class="profit-order-row">
                                            <span>Ngày đặt</span>
                                            <strong><fmt:formatDate value="${mostProfitableOrder.orderDate}" pattern="dd/MM/yyyy HH:mm"/></strong>
                                        </div>
                                        <div class="profit-order-row">
                                            <span>Thanh toán</span>
                                            <strong><c:out value="${mostProfitableOrder.paymentMethod}"/></strong>
                                        </div>
                                        <div class="profit-order-row">
                                            <span>Phí vận chuyển</span>
                                            <strong><fmt:formatNumber value="${mostProfitableOrder.shippingFee}" pattern="#,###"/>₫</strong>
                                        </div>
                                        <div class="profit-order-row">
                                            <span>Tổng đơn</span>
                                            <strong><fmt:formatNumber value="${mostProfitableOrder.totalAmount}" pattern="#,###"/>₫</strong>
                                        </div>
                                        <div class="profit-order-row">
                                            <span>Số dòng hàng</span>
                                            <strong>${mostProfitableOrder.itemCount} dòng / ${mostProfitableOrder.totalQuantity} sản phẩm</strong>
                                        </div>
                                        <div class="profit-order-row">
                                            <span>Mặt hàng nổi bật</span>
                                            <strong><c:out value="${mostProfitableOrder.topItemName}"/></strong>
                                        </div>
                                    </div>

                                    <div class="profit-order-footnote">
                                        <c:out value="${profitRule}"/>
                                    </div>

                                    <a href="${ctx}/admin/order-detail?id=${mostProfitableOrder.orderId}" class="btn btn-primary w-100">
                                        Xem chi tiết đơn hàng
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <div class="orders-empty-card profit-empty-card">
                                        <i class="fas fa-chart-line orders-empty-icon"></i>
                                        <p>Chưa có đủ dữ liệu để xác định đơn lợi nhất.</p>
                                        <span class="orders-empty-note">Khi có đơn hoàn thành, hệ thống sẽ tự tính KPI ước tính cho bạn.</span>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </aside>
                </section>

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
                <span class="orders-modal-kicker">Thao tác đơn hàng</span>
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
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<script>
    (function () {
        const chartConfig = <c:out value="${orderAnalyticsJson}" escapeXml="false"/>;
        const chartCanvas = document.getElementById('orderRevenueChart');
        const toggleButtons = Array.from(document.querySelectorAll('[data-chart-range]'));

        if (!chartCanvas || !window.Chart || !chartConfig) {
            return;
        }

        const series = {
            weekly: Array.isArray(chartConfig.weekly) ? chartConfig.weekly : [],
            monthly: Array.isArray(chartConfig.monthly) ? chartConfig.monthly : []
        };
        let activeRange = 'weekly';
        let activePoints = series[activeRange];

        function formatCurrency(value) {
            return new Intl.NumberFormat('vi-VN').format(Number(value || 0)) + '₫';
        }

        function buildLabels(points) {
            return points.map(function (point) {
                return point.label || '';
            });
        }

        function buildValues(points) {
            return points.map(function (point) {
                return Number(point.revenue || 0);
            });
        }

        const chart = new Chart(chartCanvas, {
            type: 'line',
            data: {
                labels: buildLabels(activePoints),
                datasets: [{
                    label: 'Doanh thu',
                    data: buildValues(activePoints),
                    borderColor: '#2563eb',
                    backgroundColor: 'rgba(37, 99, 235, 0.14)',
                    pointBackgroundColor: '#0f172a',
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    tension: 0.38,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                const point = activePoints[context.dataIndex] || {};
                                return [
                                    formatCurrency(point.revenue),
                                    (point.orderCount || 0) + ' đơn hoàn thành'
                                ];
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        },
                        ticks: {
                            color: '#64748b'
                        }
                    },
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(148, 163, 184, 0.18)'
                        },
                        ticks: {
                            color: '#64748b',
                            callback: function (value) {
                                return new Intl.NumberFormat('vi-VN', {
                                    notation: 'compact',
                                    compactDisplay: 'short'
                                }).format(value) + '₫';
                            }
                        }
                    }
                }
            }
        });

        function setActiveRange(range) {
            activeRange = range;
            activePoints = series[range] || [];

            chart.data.labels = buildLabels(activePoints);
            chart.data.datasets[0].data = buildValues(activePoints);
            chart.data.datasets[0].borderColor = range === 'monthly' ? '#0ea5e9' : '#2563eb';
            chart.data.datasets[0].backgroundColor = range === 'monthly' ? 'rgba(14, 165, 233, 0.14)' : 'rgba(37, 99, 235, 0.14)';
            chart.update();

            toggleButtons.forEach(function (button) {
                const isActive = button.getAttribute('data-chart-range') === range;
                button.classList.toggle('active', isActive);
            });
        }

        toggleButtons.forEach(function (button) {
            button.addEventListener('click', function () {
                setActiveRange(this.getAttribute('data-chart-range'));
            });
        });

        setActiveRange('weekly');
    })();
</script>
</body>
</html>
