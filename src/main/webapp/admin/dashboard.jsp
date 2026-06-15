<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard | INOLA Admin</title>
    <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-dashboard.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-pages.css">
</head>
<body>
<div class="admin-shell">
    <div class="sidebar-backdrop" data-sidebar-close></div>

    <jsp:include page="common/admin-sidebar.jsp">
        <jsp:param name="activePage" value="dashboard" />
    </jsp:include>

    <div class="admin-main">
        <jsp:include page="common/admin-topbar.jsp" />

        <main class="dashboard-content">
            <div class="container-fluid px-3 px-lg-4 py-4">
                <div class="page-heading">
                    <div class="page-heading-copy">
                        <span class="page-icon"><i class="bi bi-speedometer2" aria-hidden="true"></i></span>
                        <div>
                            <p class="eyebrow mb-1">Overview</p>
                            <h1 class="h3 mb-1">Dashboard</h1>
                            <p class="text-muted mb-0">Monitor performance, sales, users, and support from one clean workspace.</p>
                        </div>
                    </div>

                    <div class="heading-actions">
                        <div class="dashboard-report-wrap">
                            <button type="button" id="reportMenuButton" class="btn btn-outline-secondary btn-sm" onclick="toggleReportMenu(event)" aria-haspopup="true" aria-expanded="false" aria-controls="reportMenu">
                                <i class="bi bi-download" aria-hidden="true"></i> Export report
                            </button>
                            <div id="reportMenu" role="menu" class="dropdown-menu shadow-sm dashboard-report-menu">
                                <a class="dropdown-item" href="${ctx}/admin/export-report?type=summary" role="menuitem">Summary report</a>
                                <a class="dropdown-item" href="${ctx}/admin/export-report?type=products" role="menuitem">Product report</a>
                                <a class="dropdown-item" href="${ctx}/admin/export-report?type=orders" role="menuitem">Order report</a>
                                <a class="dropdown-item" href="${ctx}/admin/export-report?type=customers" role="menuitem">Customer report</a>
                            </div>
                        </div>

                        <c:if test="${canCreateProduct}">
                            <a class="btn btn-primary btn-sm" href="${ctx}/admin/products?action=add">
                                <i class="bi bi-plus-lg" aria-hidden="true"></i> Add product
                            </a>
                        </c:if>
                    </div>
                </div>

                <section class="stats-grid" aria-label="Dashboard metrics">
                    <article class="metric-card">
                        <div class="metric-top">
                            <span class="metric-label">Revenue this month</span>
                            <span class="metric-icon"><i class="bi bi-currency-dollar" aria-hidden="true"></i></span>
                        </div>
                        <div class="metric-value">
                            <fmt:formatNumber value="${totalRevenue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                        </div>
                        <div class="metric-meta">
                            <span class="text-success">+12.5%</span>
                            <span>vs last month</span>
                        </div>
                    </article>

                    <article class="metric-card">
                        <div class="metric-top">
                            <span class="metric-label">New orders</span>
                            <span class="metric-icon"><i class="bi bi-bag-check" aria-hidden="true"></i></span>
                        </div>
                        <div class="metric-value">${totalOrders}</div>
                        <div class="metric-meta">
                            <span class="text-success">Growth</span>
                            <span>latest month</span>
                        </div>
                    </article>

                    <article class="metric-card">
                        <div class="metric-top">
                            <span class="metric-label">Products</span>
                            <span class="metric-icon"><i class="bi bi-box-seam" aria-hidden="true"></i></span>
                        </div>
                        <div class="metric-value">${totalProducts}</div>
                        <div class="metric-meta">
                            <span>Total catalog items</span>
                        </div>
                    </article>

                    <article class="metric-card">
                        <div class="metric-top">
                            <span class="metric-label">Customers</span>
                            <span class="metric-icon"><i class="bi bi-people" aria-hidden="true"></i></span>
                        </div>
                        <div class="metric-value">${totalCustomers}</div>
                        <div class="metric-meta">
                            <span>Total registered users</span>
                        </div>
                    </article>
                </section>

                <div class="tabs">
                    <div class="tabs-list">
                        <button class="tabs-trigger active" data-tab="overview" type="button">Overview</button>
                        <button class="tabs-trigger" data-tab="orders" type="button">Recent orders</button>
                        <button class="tabs-trigger" data-tab="products" type="button">Best sellers</button>
                    </div>

                    <section class="tabs-content active" id="overview">
                        <div class="row g-3">
                            <div class="col-12 col-xl-8">
                                <div class="panel h-100">
                                    <div class="panel-header">
                                        <div>
                                            <h2 class="section-title"><i class="bi bi-graph-up-arrow" aria-hidden="true"></i><span>Revenue trend</span></h2>
                                            <p class="text-muted mb-0">Monthly revenue for the latest six months.</p>
                                        </div>
                                    </div>
                                    <div class="panel-body">
                                        <canvas id="revenueChart" class="dashboard-revenue-chart"></canvas>
                                    </div>
                                </div>
                            </div>

                            <div class="col-12 col-xl-4">
                                <div class="panel h-100">
                                    <div class="panel-header">
                                        <div>
                                            <h2 class="section-title"><i class="bi bi-clock-history" aria-hidden="true"></i><span>Recent orders</span></h2>
                                            <p class="text-muted mb-0">Five newest orders.</p>
                                        </div>
                                    </div>
                                    <div class="panel-body">
                                        <c:choose>
                                            <c:when test="${empty recentOrders}">
                                                <div class="text-center py-5 text-muted">
                                                    <p class="mb-0">No recent orders yet.</p>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach items="${recentOrders}" var="order" varStatus="status">
                                                    <div class="d-flex align-items-center justify-content-between gap-3 py-3 ${!status.last ? 'border-bottom' : ''}">
                                                        <div>
                                                            <p class="fw-semibold mb-1">#${order.id}</p>
                                                            <p class="small text-muted mb-0"><c:out value="${order.customerName}"/></p>
                                                        </div>
                                                        <div class="text-end">
                                                            <p class="fw-bold mb-0">
                                                                <fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>₫
                                                            </p>
                                                        </div>
                                                    </div>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>

                    <section class="tabs-content" id="orders">
                        <div class="panel">
                            <div class="panel-header">
                                <div>
                                    <h2 class="section-title"><i class="bi bi-bag-check" aria-hidden="true"></i><span>Recent orders</span></h2>
                                    <p class="text-muted mb-0">Latest order activity and status.</p>
                                </div>
                            </div>
                            <div class="panel-body table-container">
                                <c:choose>
                                    <c:when test="${empty recentOrders}">
                                        <div class="text-center py-5 text-muted">
                                            <p class="mb-0">No recent orders yet.</p>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <table class="data-table">
                                            <thead>
                                            <tr>
                                                <th scope="col">Order</th>
                                                <th scope="col">Customer</th>
                                                <th scope="col">Date</th>
                                                <th scope="col">Total</th>
                                                <th scope="col">Status</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach items="${recentOrders}" var="order">
                                                <tr>
                                                    <td>#${order.id}</td>
                                                    <td><c:out value="${order.customerName}"/></td>
                                                    <td><fmt:formatDate value="${order.orderDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                                    <td><fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>₫</td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${order.status == 'Đang xử lý'}">
                                                                <span class="badge badge-warning"><c:out value="${order.status}"/></span>
                                                            </c:when>
                                                            <c:when test="${order.status == 'Đang giao'}">
                                                                <span class="badge badge-info"><c:out value="${order.status}"/></span>
                                                            </c:when>
                                                            <c:when test="${order.status == 'Hoàn thành'}">
                                                                <span class="badge badge-success"><c:out value="${order.status}"/></span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge badge-secondary"><c:out value="${order.status}"/></span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </section>

                    <section class="tabs-content" id="products">
                        <div class="panel">
                            <div class="panel-header">
                                <div>
                                    <h2 class="section-title"><i class="bi bi-stars" aria-hidden="true"></i><span>Best-selling products</span></h2>
                                    <p class="text-muted mb-0">Top 10 products by sales volume.</p>
                                </div>
                            </div>
                            <div class="panel-body table-container">
                                <c:choose>
                                    <c:when test="${empty topProducts}">
                                        <div class="text-center py-5 text-muted">
                                            <p class="mb-0">No top-selling products available.</p>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <table class="data-table">
                                            <thead>
                                            <tr>
                                                <th scope="col">Product</th>
                                                <th scope="col">Category</th>
                                                <th scope="col">Price</th>
                                                <th scope="col">Sold</th>
                                                <th scope="col">Stock</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach items="${topProducts}" var="product">
                                                <tr>
                                                    <td><c:out value="${product.name}"/></td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty product.category}">
                                                                <c:out value="${product.category.categoryName}"/>
                                                            </c:when>
                                                            <c:otherwise>Uncategorized</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td><fmt:formatNumber value="${product.originalPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></td>
                                                    <td>${product.totalSold}</td>
                                                    <td>${product.stockQuantity}</td>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </section>
                </div>
            </div>
        </main>

        <jsp:include page="common/admin-footer.jsp" />
    </div>
</div>

<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/assets/js/admin-main.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<script>
    document.querySelectorAll('.tabs-trigger').forEach(function (trigger) {
        trigger.addEventListener('click', function () {
            var tabId = this.getAttribute('data-tab');

            document.querySelectorAll('.tabs-trigger').forEach(function (tab) {
                tab.classList.remove('active');
            });
            document.querySelectorAll('.tabs-content').forEach(function (content) {
                content.classList.remove('active');
            });

            this.classList.add('active');
            document.getElementById(tabId)?.classList.add('active');
        });
    });

    function toggleReportMenu(event) {
        event.stopPropagation();
        var menu = document.getElementById('reportMenu');
        var button = document.getElementById('reportMenuButton');
        var isOpen = menu.style.display === 'block';
        menu.style.display = isOpen ? 'none' : 'block';
        button?.setAttribute('aria-expanded', String(!isOpen));
    }

    document.addEventListener('click', function () {
        var menu = document.getElementById('reportMenu');
        var button = document.getElementById('reportMenuButton');
        if (menu && menu.style.display === 'block') {
            menu.style.display = 'none';
            button?.setAttribute('aria-expanded', 'false');
        }
    });

    document.getElementById('reportMenu')?.addEventListener('click', function (event) {
        event.stopPropagation();
    });

    var chartElement = document.getElementById('revenueChart');
    if (chartElement && window.Chart) {
        var revenueData = [
            <c:choose>
                <c:when test="${not empty monthlyRevenues}">
                    <c:forEach items="${monthlyRevenues}" var="revenue" varStatus="status">
                        ${revenue}<c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                </c:when>
                <c:otherwise>0,0,0,0,0,0</c:otherwise>
            </c:choose>
        ];

        var monthNames = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
        var currentMonth = new Date().getMonth();
        var labels = [];

        for (var i = 5; i >= 0; i--) {
            labels.push(monthNames[(currentMonth - i + 12) % 12]);
        }

        new Chart(chartElement, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Doanh thu (₫)',
                    data: revenueData,
                    borderColor: 'rgb(37, 99, 235)',
                    backgroundColor: 'rgba(37, 99, 235, 0.12)',
                    tension: 0.4,
                    fill: true,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    pointBackgroundColor: 'rgb(37, 99, 235)',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                return new Intl.NumberFormat('vi-VN').format(context.parsed.y) + '₫';
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
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
    }
</script>
</body>
</html>
