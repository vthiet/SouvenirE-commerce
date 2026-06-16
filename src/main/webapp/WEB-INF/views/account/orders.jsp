<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

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
    <c:choose>
        <c:when test="${not empty requestScope.orderList}">
            <section class="profile-panel orders-section">
                <div class="orders-list">
                    <c:forEach items="${requestScope.orderList}" var="order">
                        <article class="order-card">
                            <div class="order-card__header">
                                <div class="order-card__info">
                                    <h3>Đơn hàng #${order.id}</h3>
                                    <p class="order-date">
                                        <fmt:formatDate value="${order.orderDate}" pattern="dd/MM/yyyy HH:mm"/>
                                    </p>
                                </div>
                                <div class="order-card__status">
                                    <c:set var="listStatusClass" value="PENDING"/>
                                    <c:choose>
                                        <c:when test="${order.status eq 'Chờ xác nhận' || order.status eq 'PENDING'}"><c:set var="listStatusClass" value="PENDING"/></c:when>
                                        <c:when test="${order.status eq 'Đang xử lý' || order.status eq 'Đã xác nhận' || order.status eq 'CONFIRMED'}"><c:set var="listStatusClass" value="CONFIRMED"/></c:when>
                                        <c:when test="${order.status eq 'Đang giao' || order.status eq 'SHIPPED'}"><c:set var="listStatusClass" value="SHIPPED"/></c:when>
                                        <c:when test="${order.status eq 'Hoàn thành' || order.status eq 'Đã giao' || order.status eq 'DELIVERED'}"><c:set var="listStatusClass" value="DELIVERED"/></c:when>
                                        <c:when test="${order.status eq 'Đã hủy' || order.status eq 'CANCELLED'}"><c:set var="listStatusClass" value="CANCELLED"/></c:when>
                                        <c:otherwise><c:set var="listStatusClass" value="PENDING"/></c:otherwise>
                                    </c:choose>
                                    <span class="order-status order-status--${listStatusClass}">
                                        <c:choose>
                                            <c:when test="${order.status eq 'PENDING'}">Chờ xác nhận</c:when>
                                            <c:when test="${order.status eq 'CONFIRMED'}">Đã xác nhận</c:when>
                                            <c:when test="${order.status eq 'SHIPPED'}">Đang gửi</c:when>
                                            <c:when test="${order.status eq 'DELIVERED'}">Đã giao</c:when>
                                            <c:when test="${order.status eq 'CANCELLED'}">Đã hủy</c:when>
                                            <c:otherwise>${order.status}</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>

                            <div class="order-card__body">
                                <div class="order-total">
                                    <span>Tổng tiền:</span>
                                    <strong>
                                        <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫"/>
                                    </strong>
                                </div>
                            </div>

                            <div class="order-card__footer">
                                <a href="${pageContext.request.contextPath}/user/orders?action=detail&id=${order.id}"
                                   class="primary-button">
                                    <i class="fa-solid fa-eye"></i>
                                    <span>Xem chi tiết</span>
                                </a>
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
                    <p>Bạn chưa có đơn hàng nào.</p>
                </div>
            </section>
        </c:otherwise>
    </c:choose>
</c:if>


