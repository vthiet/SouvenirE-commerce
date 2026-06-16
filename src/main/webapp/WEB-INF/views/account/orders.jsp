<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="currentUser" value="${sessionScope.currentUser}"/>

<div class="account-heading">
    <div>
        <c:choose>
            <c:when test="${not empty requestScope.order}">
                <h1>Chi tiết đơn hàng #${requestScope.order.id}</h1>
            </c:when>
            <c:otherwise>
                <h1>Đơn hàng của tôi</h1>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- Order Detail Section -->
<c:if test="${not empty requestScope.order}">
    <section class="profile-panel">
        <c:if test="${not empty param.success}">
            <div class="alert alert-success" style="background-color: #d1e7dd; border-color: #badbcc; color: #0f5132; padding: 12px; border-radius: 4px; margin-bottom: 15px;">
                <i class="fa-solid fa-circle-check"></i> Thao tác xử lý đơn hàng thành công!
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger" style="background-color: #f8d7da; border-color: #f5c2c7; color: #842029; padding: 12px; border-radius: 4px; margin-bottom: 15px;">
                <i class="fa-solid fa-circle-exclamation"></i> Lỗi: <c:out value="${param.error}"/>
            </div>
        </c:if>
        <div class="order-detail-header">
            <div class="order-detail-info">
                <div class="info-item">
                    <label>Mã đơn hàng</label>
                    <p>#${requestScope.order.id}</p>
                </div>
                <div class="info-item">
                    <label>Ngày đặt hàng</label>
                    <p><fmt:formatDate value="${requestScope.order.orderDate}" pattern="dd/MM/yyyy HH:mm"/></p>
                </div>
                <div class="info-item">
                    <label>Trạng thái</label>
                    <p>
                        <c:set var="statusClass" value="PENDING"/>
                        <c:choose>
                            <c:when test="${requestScope.order.status eq 'Chờ xác nhận' || requestScope.order.status eq 'PENDING'}"><c:set var="statusClass" value="PENDING"/></c:when>
                            <c:when test="${requestScope.order.status eq 'Đang xử lý' || requestScope.order.status eq 'Đã xác nhận' || requestScope.order.status eq 'CONFIRMED'}"><c:set var="statusClass" value="CONFIRMED"/></c:when>
                            <c:when test="${requestScope.order.status eq 'Đang giao' || requestScope.order.status eq 'SHIPPED'}"><c:set var="statusClass" value="SHIPPED"/></c:when>
                            <c:when test="${requestScope.order.status eq 'Hoàn thành' || requestScope.order.status eq 'Đã giao' || requestScope.order.status eq 'DELIVERED'}"><c:set var="statusClass" value="DELIVERED"/></c:when>
                            <c:when test="${requestScope.order.status eq 'Đã hủy' || requestScope.order.status eq 'CANCELLED'}"><c:set var="statusClass" value="CANCELLED"/></c:when>
                            <c:otherwise><c:set var="statusClass" value="PENDING"/></c:otherwise>
                        </c:choose>
                        <span class="order-status order-status--${statusClass}">
                            <c:choose>
                                <c:when test="${requestScope.order.status eq 'PENDING'}">Chờ xác nhận</c:when>
                                <c:when test="${requestScope.order.status eq 'CONFIRMED'}">Đã xác nhận</c:when>
                                <c:when test="${requestScope.order.status eq 'SHIPPED'}">Đang gửi</c:when>
                                <c:when test="${requestScope.order.status eq 'DELIVERED'}">Đã giao</c:when>
                                <c:when test="${requestScope.order.status eq 'CANCELLED'}">Đã hủy</c:when>
                                <c:otherwise>${requestScope.order.status}</c:otherwise>
                            </c:choose>
                        </span>
                    </p>
                </div>
            </div>
            <div class="order-detail-total">
                <span>Tổng tiền:</span>
                <strong>
                    <fmt:formatNumber value="${requestScope.order.totalAmount}" type="currency" currencySymbol="₫"/>
                </strong>
            </div>
        </div>

        <h3 style="margin-top: 24px; margin-bottom: 16px;">Sản phẩm</h3>
        <div class="order-items">
            <c:forEach items="${requestScope.orderItems}" var="item">
                <div class="order-item">
                    <div class="item-name">
                        <strong>${item.productName}</strong>
                    </div>
                    <div class="item-details">
                        <span>x${item.quantity}</span>
                        <span class="item-price">
                            <fmt:formatNumber value="${item.priceAtPurchase}" type="currency" currencySymbol="₫"/>
                        </span>
                    </div>
                </div>
            </c:forEach>
        </div>

        <c:if test="${requestScope.order.status eq 'Chờ xác nhận' || requestScope.order.status eq 'PENDING' || requestScope.order.status eq 'Chờ thanh toán' || requestScope.order.status eq 'PENDING_PAYMENT'}">
            <div style="margin-top: 24px; border-top: 1px solid #e9ecef; padding-top: 16px;">
                <form id="cancelOrderForm" method="post" action="${pageContext.request.contextPath}/user/orders" onsubmit="return confirm('Bạn có chắc chắn muốn hủy đơn hàng này không?')">
                    <input type="hidden" name="action" value="cancel">
                    <input type="hidden" name="orderId" value="${requestScope.order.id}">
                    <button type="submit" class="danger-button" style="background-color: #dc3545; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; font-weight: 600; display: inline-flex; align-items: center; gap: 8px;">
                        <i class="fa-solid fa-ban"></i> Hủy đơn hàng
                    </button>
                </form>
            </div>
        </c:if>

        <h3 style="margin-top: 32px; margin-bottom: 16px;">Lịch sử xử lý đơn hàng</h3>
        <div class="order-history-timeline" style="border-left: 2px solid #e9ecef; padding-left: 15px; margin-left: 10px; margin-top: 16px; margin-bottom: 16px;">
            <c:forEach items="${requestScope.historyList}" var="history">
                <div class="timeline-item" style="margin-bottom: 15px; position: relative;">
                    <span style="position: absolute; left: -22px; top: 4px; width: 12px; height: 12px; border-radius: 50%; background-color: #0d6efd; border: 2px solid #fff;"></span>
                    <div style="font-size: 0.8rem; color: #6c757d;">
                        <fmt:formatDate value="${java.sql.Timestamp.valueOf(history.createdAt)}" pattern="dd/MM/yyyy HH:mm"/>
                        &middot; <c:out value="${history.performedBy}"/>
                    </div>
                    <div style="font-weight: 600; font-size: 0.9rem; color: #212529;">
                        Trạng thái: <c:out value="${history.status}"/>
                    </div>
                    <div style="font-size: 0.9rem; color: #495057; margin-top: 2px;">
                        <c:out value="${history.description}"/>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty requestScope.historyList}">
                <p class="text-muted" style="font-size: 0.9rem;">Chưa có lịch sử xử lý cho đơn hàng này.</p>
            </c:if>
        </div>
    </section>

    <div style="margin-top: 16px;">
        <a href="${pageContext.request.contextPath}/user/orders" class="text-button">
            <i class="fa-solid fa-arrow-left"></i>
            <span>Quay lại danh sách đơn hàng</span>
        </a>
    </div>
</c:if>

<!-- Order List Section -->
<c:if test="${empty requestScope.order}">
    <section class="orders-toolbar">
        <nav class="orders-status-tabs" aria-label="Lọc trạng thái đơn hàng">
            <c:forEach items="${requestScope.statusTabs}" var="tab">
                <c:url var="tabUrl" value="/user/orders">
                    <c:param name="status" value="${tab.code}"/>
                    <c:if test="${not empty requestScope.keyword}">
                        <c:param name="q" value="${requestScope.keyword}"/>
                    </c:if>
                </c:url>
                <a class="orders-status-tab ${requestScope.selectedStatus eq tab.code || (empty requestScope.selectedStatus && tab.code eq 'all') ? 'is-active' : ''}"
                   href="${tabUrl}">
                    <span>${tab.label}</span>
                    <em>${tab.count}</em>
                </a>
            </c:forEach>
        </nav>

        <form class="orders-search" method="get" action="${pageContext.request.contextPath}/user/orders">
            <c:if test="${not empty requestScope.selectedStatus && requestScope.selectedStatus ne 'all'}">
                <input type="hidden" name="status" value="${requestScope.selectedStatus}">
            </c:if>
            <label class="orders-search__field">
                <i class="fa-solid fa-magnifying-glass"></i>
                <input type="search"
                       name="q"
                       value="${fn:escapeXml(requestScope.keyword)}"
                       placeholder="Tìm theo mã đơn hàng hoặc tên sản phẩm">
            </label>
            <button type="submit" class="orders-search__button">Tìm kiếm</button>
            <c:if test="${not empty requestScope.keyword || (not empty requestScope.selectedStatus && requestScope.selectedStatus ne 'all')}">
                <a class="orders-search__reset" href="${pageContext.request.contextPath}/user/orders">Xóa lọc</a>
            </c:if>
        </form>
    </section>

    <c:choose>
        <c:when test="${not empty requestScope.orderList}">
            <section class="profile-panel orders-section">
                <div class="orders-list">
                    <c:forEach items="${requestScope.orderList}" var="order">
                        <article class="order-card">
                            <div class="order-card__header">
                                <div class="order-card__info">
                                    <h3>Đơn hàng #${order.orderId}</h3>
                                    <p class="order-date">
                                        <fmt:formatDate value="${order.orderDate}" pattern="dd/MM/yyyy HH:mm"/>
                                        <span>· ${order.itemCount} sản phẩm</span>
                                    </p>
                                </div>
                                <div class="order-card__status">
                                    <span class="order-status order-status--${order.statusClass}">
                                        ${order.statusText}
                                    </span>
                                </div>
                            </div>

                            <div class="order-card__body">
                                <div class="order-card__items">
                                    <c:forEach items="${order.items}" var="item">
                                        <div class="order-product">
                                            <c:url var="itemImageUrl" value="${item.productImagePath}"/>
                                            <img class="order-product__image"
                                                 src="${itemImageUrl}"
                                                 alt="${fn:escapeXml(item.productName)}"
                                                 loading="lazy">
                                            <div class="order-product__content">
                                                <a class="order-product__name"
                                                   href="${pageContext.request.contextPath}/product?id=${item.productId}">
                                                    ${item.productName}
                                                </a>
                                                <span class="order-product__quantity">Số lượng: ${item.quantity}</span>
                                            </div>
                                            <div class="order-product__price">
                                                <fmt:formatNumber value="${item.priceAtPurchase}" type="currency" currencySymbol="₫"/>
                                            </div>
                                        </div>
                                    </c:forEach>
                                    <c:if test="${empty order.items}">
                                        <div class="order-product order-product--empty">
                                            <span>Đơn hàng này chưa có thông tin sản phẩm.</span>
                                        </div>
                                    </c:if>
                                </div>
                            </div>

                            <div class="order-card__footer">
                                <div class="order-total">
                                    <span>Thành tiền:</span>
                                    <strong>
                                        <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫"/>
                                    </strong>
                                </div>
                                <div class="order-card__actions">
                                    <c:if test="${order.repayable}">
                                        <a href="${order.repayUrl}" class="secondary-button order-action-button">
                                            <i class="fa-solid fa-credit-card"></i>
                                            <span>Thanh toán lại</span>
                                        </a>
                                    </c:if>
                                    <a href="${pageContext.request.contextPath}/user/orders?action=detail&id=${order.orderId}"
                                       class="primary-button order-action-button">
                                        <i class="fa-solid fa-eye"></i>
                                        <span>Xem chi tiết</span>
                                    </a>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </section>
        </c:when>
        <c:otherwise>
            <section class="profile-panel">
                <div class="empty-state">
                    <i class="fa-regular fa-box-open"></i>
                    <p>Không tìm thấy đơn hàng phù hợp.</p>
                    <a href="${pageContext.request.contextPath}/user/orders" class="text-button">Xem tất cả đơn hàng</a>
                </div>
            </section>
        </c:otherwise>
    </c:choose>
</c:if>


