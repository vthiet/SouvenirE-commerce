<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="cart-page">
    <div id="cartI18n"
         hidden
         data-empty-title="<fmt:message key='cart.empty.title'/>"
         data-empty-desc="<fmt:message key='cart.empty.desc'/>"
         data-empty-cta="<fmt:message key='cart.empty.cta'/>"
         data-shop-title="<fmt:message key='cart.shop.title'/>"
         data-shop-desc="<fmt:message key='cart.shop.desc'/>"
         data-selected-remove="<fmt:message key='cart.selected_remove'/>"
         data-box-default="<fmt:message key='cart.box_default'/>"
         data-summary-title="<fmt:message key='cart.summary.title'/>"
         data-summary-total="<fmt:message key='cart.summary.total'/>"
         data-checkout="<fmt:message key='cart.checkout'/>"
         data-shipping-note="<fmt:message key='cart.shipping_note'/>"
         data-mobile-total="<fmt:message key='cart.mobile_total'/>"
         data-mobile-checkout="<fmt:message key='cart.mobile_checkout'/>"
         data-confirm-checkout="<fmt:message key='cart.confirm_checkout'/>"
         data-checkout-secure="<fmt:message key='cart.checkout_secure'/>"
         data-remove-failed="<fmt:message key='cart.remove_failed'/>"
         data-network-error="<fmt:message key='cart.network_error'/>"
         data-remove-all-confirm="<fmt:message key='cart.remove_all_confirm'/>">
    </div>

    <div class="cart-toast" id="cartToast" hidden>
        <strong><fmt:message key="cart.toast.title"/></strong>
        <span><fmt:message key="cart.toast.deleted"/></span>
        <button type="button" aria-label="<fmt:message key='cart.toast.close'/>">
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
                    <fmt:message key="cart.header.products_prefix"/>
                    <span id="cart-total-qty">${cart.totalQuantity()}</span>
                    <fmt:message key="cart.header.products_suffix"/>
                </div>
                <div><fmt:message key="cart.header.unit_price"/></div>
                <div><fmt:message key="cart.header.quantity"/></div>
                <div><fmt:message key="cart.header.total_price"/></div>
                <button type="button"
                        class="cart-head-remove"
                        id="removeAllCartItems"
                        aria-label="<fmt:message key='cart.remove_all'/>">
                    <i class="fa-regular fa-trash-can"></i>
                </button>
            </div>

            <c:choose>
                <c:when test="${cart.totalQuantity() == 0}">
                    <div class="cart-empty-state" id="cartEmptyState">
                        <i class="fa-solid fa-cart-shopping"></i>
                        <p><fmt:message key="cart.empty.title"/></p>
                        <span><fmt:message key="cart.empty.desc"/></span>
                        <a href="${pageContext.request.contextPath}/home"><fmt:message key="cart.empty.cta"/></a>
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
                                <strong><fmt:message key="cart.shop.title"/></strong>
                                <p><fmt:message key="cart.shop.desc"/></p>
                            </div>

                            <button type="button" class="cart-shop-remove" aria-label="<fmt:message key='cart.selected_remove'/>">
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
                                        <p><fmt:message key="cart.box_default"/></p>
                                        </div>
                                    </div>

                                <div class="cart-unit-price">
                                    <fmt:formatNumber value="${item.price}" groupingUsed="true"/> đ
                                </div>

                                <div class="cart-quantity">
                                    <button type="button" class="qty-btn minus-btn" aria-label="<fmt:message key='cart.qty_decrease'/>">−</button>
                                    <input type="number"
                                           class="qty-input"
                                           value="${item.quantity}"
                                           min="1">
                                    <button type="button" class="qty-btn plus-btn" aria-label="<fmt:message key='cart.qty_increase'/>">+</button>
                                </div>

                                <div class="item-price">
                                    <fmt:formatNumber value="${item.subTotal}" groupingUsed="true"/> đ
                                </div>

                                <button type="button"
                                        class="remove-item-btn"
                                        data-product-id="${item.product.id}"
                                        aria-label="<fmt:message key='cart.remove_item'><fmt:param value='${item.product.name}'/></fmt:message>">
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
                    <fmt:message key="cart.summary.title"/>
                </h2>

                <div class="summary-line">
                    <span><fmt:message key="cart.summary.total"/></span>
                    <strong id="cart-total-pay">0 đ</strong>
                </div>

                <a class="checkout-btn checkout-btn--disabled"
                   id="checkoutButton"
                   href="${pageContext.request.contextPath}/checkout"
                   aria-disabled="true">
                    <fmt:message key="cart.checkout"/>
                </a>
            </section>

            <section class="cart-note-card">
                <div class="cart-note-art">
                    <i class="fa-regular fa-hand-peace"></i>
                </div>

                <p>
                    <fmt:message key="cart.shipping_note"/>
                </p>
            </section>
        </aside>
    </main>
</div>
