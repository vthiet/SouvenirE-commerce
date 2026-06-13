<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="page-wrapper cart-page">
    <div class="cart-layout">

        <%-- ======================================================
             LEFT PANEL: Cart Items
             ====================================================== --%>
        <div class="cart-items-section">
            <div class="cart-items-inner">

                <div class="continue-shopping">
                    <a href="${pageContext.request.contextPath}/home">
                        <i class="fa-solid fa-arrow-left"></i> Tiếp tục mua sắm
                    </a>
                </div>

                <div class="section-header">
                    <h2>Giỏ hàng của bạn</h2>
                    <span id="cart-total-qty">
                        <i class="fa-solid fa-bag-shopping" style="font-size:11px;opacity:0.8;"></i>
                        ${cart.totalQuantity()} sản phẩm
                    </span>
                </div>

                <%-- Empty state --%>
                <c:if test="${cart.totalQuantity() == 0}">
                    <div class="empty-cart">
                        <div class="empty-cart-icon">
                            <i class="fa-solid fa-cart-shopping"></i>
                        </div>
                        <div class="empty-cart-title">Giỏ hàng của bạn đang trống</div>
                        <div class="empty-cart-desc">
                            Hãy tiếp tục khám phá các đặc sản, quà tặng và đồ thủ công độc đáo của chúng tôi nhé!
                        </div>
                        <a class="btn-empty-back"
                           href="${pageContext.request.contextPath}/home">
                            <i class="fa-solid fa-store"></i> Quay lại mua sắm
                        </a>
                    </div>
                </c:if>

                <%-- Item list --%>
                <c:forEach items="${cart.items}" var="item">
                    <div class="cart-item-card" data-product-id="${item.product.id}">

                        <%-- Image + Name + Unit price --%>
                        <div class="item-main-info">
                            <div class="item-image">
                                <img src="${item.product.imageUrl}" alt="${item.product.name}">
                            </div>

                            <div class="item-details">
                                <h3>${item.product.name}</h3>
                                <p class="item-unit-price">
                                    Đơn giá:
                                    <strong><fmt:formatNumber value="${item.price}" groupingUsed="true"/>₫</strong>
                                </p>
                            </div>
                        </div>

                        <%-- Qty selector + subtotal --%>
                        <div class="item-price-quantity">
                            <div class="quantity-selector">
                                <button type="button" class="qty-btn minus-btn" aria-label="Giảm số lượng">
                                    <i class="fa-solid fa-minus" style="font-size:11px;"></i>
                                </button>

                                <input type="number"
                                       class="qty-input"
                                       value="${item.quantity}"
                                       min="1"
                                       aria-label="Số lượng sản phẩm">

                                <button type="button" class="qty-btn plus-btn" aria-label="Tăng số lượng">
                                    <i class="fa-solid fa-plus" style="font-size:11px;"></i>
                                </button>
                            </div>

                            <span class="item-price">
                                <fmt:formatNumber value="${item.subTotal}" groupingUsed="true"/>₫
                            </span>
                        </div>

                        <%-- Remove button --%>
                        <button type="button"
                                class="remove-item-btn"
                                data-product-id="${item.product.id}"
                                aria-label="Xóa sản phẩm">
                            <i class="fa-solid fa-trash-can"></i>
                            <span class="remove-text">Xóa</span>
                        </button>

                    </div>
                </c:forEach>

            </div><%-- /cart-items-inner --%>
        </div><%-- /cart-items-section --%>

        <%-- ======================================================
             RIGHT PANEL: Order Summary (desktop)
             ====================================================== --%>
        <div class="order-summary-section">
            <div class="summary-card">

                <div class="summary-card-header">
                    <h2 class="summary-card-title">
                        <i class="fa-solid fa-receipt" style="margin-right:8px;opacity:0.85;"></i>
                        Tóm tắt đơn hàng
                    </h2>
                </div>

                <div class="summary-card-body">
                    <div class="summary-row">
                        <span>Tạm tính</span>
                        <span id="cart-subtotal">
                            <fmt:formatNumber value="${cart.total()}" groupingUsed="true"/>₫
                        </span>
                    </div>

                    <div class="summary-row">
                        <span>Phí vận chuyển</span>
                        <span class="summary-shipping-note">Liên hệ sau</span>
                    </div>

                    <div class="summary-divider"></div>

                    <div class="summary-total-row">
                        <span class="summary-total-label">Tổng thanh toán</span>
                        <span id="cart-total-pay" class="final-total">
                            <fmt:formatNumber value="${cart.total()}" groupingUsed="true"/>₫
                        </span>
                    </div>

                    <a class="checkout-btn"
                       href="${pageContext.request.contextPath}/checkout"
                       onclick="return confirm('Xác nhận thanh toán?')">
                        <i class="fa-solid fa-lock" style="font-size:13px;"></i>
                        Xác nhận thanh toán
                    </a>

                    <div class="summary-trust">
                        <i class="fa-solid fa-shield-halved"></i>
                        Thanh toán an toàn &amp; bảo mật
                    </div>
                </div>

            </div><%-- /summary-card --%>
        </div><%-- /order-summary-section --%>

    </div><%-- /cart-layout --%>
</div><%-- /cart-page --%>

<%-- ============================================================
     MOBILE STICKY CHECKOUT BAR
     (only visible on ≤ 768px via CSS)
     ============================================================ --%>
<div class="mobile-checkout-bar">
    <div class="mobile-checkout-total">
        <div class="mobile-checkout-total-label">Tổng thanh toán</div>
        <div class="mobile-checkout-total-value">
            <fmt:formatNumber value="${cart.total()}" groupingUsed="true"/>₫
        </div>
    </div>
    <a class="mobile-checkout-btn"
       href="${pageContext.request.contextPath}/checkout"
       onclick="return confirm('Xác nhận thanh toán?')">
        <i class="fa-solid fa-lock" style="font-size:12px;"></i>
        Thanh toán
    </a>
</div>
