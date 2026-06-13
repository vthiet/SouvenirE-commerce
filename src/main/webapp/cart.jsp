<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="cart-page">
    <div class="cart-toast" id="cartToast" hidden>
        <strong>Thông báo</strong>
        <span>Sản phẩm đã được xóa thành công.</span>
        <button type="button" aria-label="Đóng thông báo">
            <i class="fa-solid fa-xmark"></i>
        </button>
    </div>

    <main class="cart-shell">
        <section class="cart-products">
            <div class="cart-table-head">
                <label class="cart-check">
                    <input type="checkbox" id="selectAllCartItems">
                    <span></span>
                </label>
                <div class="cart-head-product">
                    Sản phẩm (<span id="cart-total-qty">${cart.totalQuantity()}</span> Sản phẩm)
                </div>
                <div>Đơn giá</div>
                <div>Số lượng</div>
                <div>Tổng giá</div>
                <button type="button"
                        class="cart-head-remove"
                        id="removeAllCartItems"
                        aria-label="Xóa tất cả sản phẩm">
                    <i class="fa-regular fa-trash-can"></i>
                </button>
            </div>

            <c:choose>
                <c:when test="${cart.totalQuantity() == 0}">
                    <div class="cart-empty-state" id="cartEmptyState">
                        <i class="fa-solid fa-cart-shopping"></i>
                        <p>Giỏ hàng của bạn đang trống.</p>
                        <a href="${pageContext.request.contextPath}/home">Tiếp tục mua sắm</a>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="cart-shop-card" id="cartItemsContainer">
                        <div class="cart-shop-head">
                            <label class="cart-check">
                                <input type="checkbox" class="shop-checkbox">
                                <span></span>
                            </label>

                            <div>
                                <strong>INOLA Souvenir</strong>
                                <p>Đặc sản và quà tặng Việt Nam</p>
                            </div>

                            <button type="button" class="cart-shop-remove" aria-label="Xóa sản phẩm đã chọn">
                                <i class="fa-regular fa-trash-can"></i>
                            </button>
                        </div>

                        <c:forEach items="${cart.items}" var="item">
                            <c:url var="cartItemImage" value="${item.product.imageUrl}"/>

                            <article class="cart-item-card"
                                     data-product-id="${item.product.id}"
                                     data-unit-price="${item.price}">
                                <label class="cart-check">
                                    <input type="checkbox" class="item-checkbox">
                                    <span></span>
                                </label>

                                <div class="cart-product-info">
                                    <img src="${cartItemImage}" alt="${item.product.name}">

                                    <div>
                                        <h3>${item.product.name}</h3>
                                        <p>Chọn thiết kế hộp: Mẫu mặc định</p>
                                    </div>
                                </div>

                                <div class="cart-unit-price">
                                    <fmt:formatNumber value="${item.price}" groupingUsed="true"/> đ
                                </div>

                                <div class="cart-quantity">
                                    <button type="button" class="qty-btn minus-btn" aria-label="Giảm số lượng">−</button>
                                    <input type="number"
                                           class="qty-input"
                                           value="${item.quantity}"
                                           min="1">
                                    <button type="button" class="qty-btn plus-btn" aria-label="Tăng số lượng">+</button>
                                </div>

                                <div class="item-price">
                                    <fmt:formatNumber value="${item.subTotal}" groupingUsed="true"/> đ
                                </div>

                                <button type="button"
                                        class="remove-item-btn"
                                        data-product-id="${item.product.id}"
                                        aria-label="Xóa ${item.product.name}">
                                    <i class="fa-regular fa-trash-can"></i>
                                </button>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <aside class="cart-sidebar">
            <section class="cart-summary-card">
                <h2>
                    <i class="fa-regular fa-rectangle-list"></i>
                    Tóm tắt đơn hàng
                </h2>

                <div class="summary-line">
                    <span>Tổng</span>
                    <strong id="cart-total-pay">0 đ</strong>
                </div>

                <a class="checkout-btn checkout-btn--disabled"
                   id="checkoutButton"
                   href="${pageContext.request.contextPath}/checkout"
                   aria-disabled="true">
                    Thanh toán
                </a>
            </section>

            <section class="cart-note-card">
                <div class="cart-note-art">
                    <i class="fa-regular fa-hand-peace"></i>
                </div>

                <p>
                    Mỗi sản phẩm tại INOLA được chuẩn bị và gửi đi từ các nhà cung cấp ở nhiều địa điểm khác nhau.
                    Phí giao hàng sẽ được áp dụng ở trang tiếp theo.
                </p>
            </section>
        </aside>
    </main>
</div>
