<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="cart-page">
    <header class="cart-hero">
        <div>
            <a href="${pageContext.request.contextPath}/home" class="cart-back">
                <i class="fa-solid fa-arrow-left" aria-hidden="true"></i>
                Tiếp tục mua sắm
            </a>
            <p class="cart-eyebrow">Đơn hàng của bạn</p>
            <h1>Giỏ hàng</h1>
            <p><span id="cartPageQuantity">${cart.totalQuantity()}</span> sản phẩm đang chờ bạn thanh toán.</p>
        </div>
        <span class="cart-hero-icon"><i class="fa-solid fa-basket-shopping" aria-hidden="true"></i></span>
    </header>

    <c:choose>
        <c:when test="${cart.totalQuantity() == 0}">
            <section class="cart-empty">
                <i class="fa-solid fa-basket-shopping" aria-hidden="true"></i>
                <h2>Giỏ hàng đang trống</h2>
                <p>Khám phá những món quà mang nét đẹp Việt Nam dành cho bạn.</p>
                <a href="${pageContext.request.contextPath}/home" class="cart-primary-action">Khám phá sản phẩm</a>
            </section>
        </c:when>

        <c:otherwise>
            <div class="cart-layout" id="cartLayout">
                <section class="cart-list" aria-label="Sản phẩm trong giỏ hàng">
                    <c:forEach items="${cart.items}" var="item">
                        <article class="cart-item" data-cart-item data-product-id="${item.product.id}">
                            <c:url var="itemImage" value="${item.product.imageUrl}"/>
                            <a class="cart-item-image"
                               href="${pageContext.request.contextPath}/product?id=${item.product.id}">
                                <img src="${itemImage}" alt="<c:out value='${item.product.name}'/>">
                            </a>

                            <div class="cart-item-info">
                                <span class="cart-item-label">INOLA Souvenir</span>
                                <h2><c:out value="${item.product.name}"/></h2>
                                <span class="cart-unit-price">
                                    <fmt:formatNumber value="${item.price}" groupingUsed="true"/>₫ / sản phẩm
                                </span>
                            </div>

                            <form class="cart-quantity-form" data-cart-update-form
                                  action="${pageContext.request.contextPath}/cart/update"
                                  method="post">
                                <input type="hidden" name="productId" value="${item.product.id}">
                                <label for="quantity-${item.product.id}">Số lượng</label>
                                <div>
                                    <input id="quantity-${item.product.id}" type="number" name="quantity"
                                           value="${item.quantity}" min="1" max="${item.product.stockQuantity}" required>
                                    <button type="submit">Cập nhật</button>
                                </div>
                            </form>

                            <div class="cart-item-total">
                                <span>Thành tiền</span>
                                <strong data-item-subtotal><fmt:formatNumber value="${item.subTotal}" groupingUsed="true"/>₫</strong>
                            </div>

                            <form action="${pageContext.request.contextPath}/cart/update" method="post" data-cart-remove-form>
                                <input type="hidden" name="productId" value="${item.product.id}">
                                <input type="hidden" name="quantity" value="0">
                                <button class="cart-remove" type="submit" aria-label="Xóa sản phẩm">
                                    <i class="fa-regular fa-trash-can" aria-hidden="true"></i>
                                    Xóa
                                </button>
                            </form>
                        </article>
                    </c:forEach>
                </section>

                <aside class="cart-summary">
                    <h2>Tóm tắt đơn hàng</h2>
                    <div class="cart-summary-row">
                        <span>Sản phẩm</span>
                        <span id="cartSummaryQuantity">${cart.totalQuantity()}</span>
                    </div>
                    <div class="cart-summary-row">
                        <span>Tạm tính</span>
                        <strong data-cart-total><fmt:formatNumber value="${cart.total()}" groupingUsed="true"/>₫</strong>
                    </div>
                    <div class="cart-summary-note">
                        <i class="fa-solid fa-truck-fast" aria-hidden="true"></i>
                        Phí giao hàng sẽ được xác nhận ở bước tiếp theo.
                    </div>
                    <div class="cart-summary-total">
                        <span>Tổng cộng</span>
                        <strong data-cart-total><fmt:formatNumber value="${cart.total()}" groupingUsed="true"/>₫</strong>
                    </div>
                    <a href="${pageContext.request.contextPath}/checkout" class="cart-primary-action">
                        Tiến hành thanh toán
                        <i class="fa-solid fa-arrow-right" aria-hidden="true"></i>
                    </a>
                    <p class="cart-secure"><i class="fa-solid fa-shield-halved"></i> Thanh toán an toàn và bảo mật</p>
                </aside>
            </div>
        </c:otherwise>
    </c:choose>
</div>
