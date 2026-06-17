<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

    <div class="payment-page">
        <div id="checkoutI18n"
             hidden
             data-select-province="<fmt:message key='payment.select2.province'/>"
             data-select-district="<fmt:message key='payment.select2.district'/>"
             data-select-ward="<fmt:message key='payment.select2.ward'/>"
             data-loading="<fmt:message key='payment.loading'/>"
             data-processing="<fmt:message key='payment.processing'/>"
             data-submit-cod="<fmt:message key='payment.submit.cod'/>"
             data-submit-vnpay="<fmt:message key='payment.submit.vnpay'/>"
             data-choose-address="<fmt:message key='payment.choose_address'/>"
             data-location-province-error="<fmt:message key='payment.location.province_error'/>"
             data-location-district-error="<fmt:message key='payment.location.district_error'/>"
             data-location-ward-error="<fmt:message key='payment.location.ward_error'/>"
             data-location-prefix="<fmt:message key='payment.location.prefix'/>"
             data-shipping-pending="<fmt:message key='payment.shipping.pending'/>"
             data-shipping-simulation="<fmt:message key='payment.shipping.simulation'/>">
        </div>

        <header class="payment-page__header">
            <a class="payment-back-link" href="${pageContext.request.contextPath}/cart">
                <i class="fa-solid fa-arrow-left" aria-hidden="true"></i>
                <fmt:message key="checkout.back"/>
            </a>
            <p class="payment-page__eyebrow"><fmt:message key="checkout.eyebrow"/></p>
            <h1><fmt:message key="checkout.heading"/></h1>
            <p><fmt:message key="checkout.supporting"/></p>
        </header>

        <c:if test="${not empty error}">
            <div class="payment-alert payment-alert--danger" role="alert">
                <i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i>
                <span><c:out value="${error}"/></span>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/checkout"
              method="post"
              id="checkoutForm"
              data-address-form
              data-locations-url="${pageContext.request.contextPath}/api/shipping/locations"
              data-shipping-fee-url="${pageContext.request.contextPath}/api/shipping/fee"
              data-subtotal="${cart.total()}">
            <c:forEach items="${checkoutProductIds}" var="productId">
                <input type="hidden" name="selectedProductId" value="${productId}">
            </c:forEach>

            <div class="checkout-layout">
                <div class="checkout-main">
                    <section class="payment-panel" aria-labelledby="shipping-title">
                        <div class="payment-panel__heading">
                            <span class="payment-step">1</span>
                            <div>
                                <h2 id="shipping-title"><fmt:message key="checkout.shipping.title"/></h2>
                                <p><fmt:message key="checkout.shipping.supporting"/></p>
                            </div>
                        </div>

                        <c:if test="${not empty savedAddresses}">
                            <fieldset class="address-list">
                                <legend><fmt:message key="checkout.saved_addresses"/></legend>
                                <label class="address-choice">
                                    <input type="radio" name="savedAddressId" value="" checked>
                                    <span class="address-choice__content">
                                        <strong><fmt:message key="checkout.new_address"/></strong>
                                        <span><fmt:message key="checkout.new_address.supporting"/></span>
                                    </span>
                                    <i class="fa-solid fa-circle-check" aria-hidden="true"></i>
                                </label>
                                <c:forEach items="${savedAddresses}" var="addr">
                                    <label class="address-choice">
                                        <input type="radio"
                                               name="savedAddressId"
                                               value="${addr.id}"
                                               data-district-id="${addr.carrierDistrictId}"
                                               data-ward-code="${addr.carrierWardCode}">
                                        <span class="address-choice__content">
                                            <strong><c:out value="${addr.receiverName}"/> · <c:out value="${addr.receiverPhone}"/></strong>
                                            <span><c:out value="${addr.addressDetail}"/>, <c:out value="${addr.ward}"/>, <c:out value="${addr.district}"/>, <c:out value="${addr.province}"/></span>
                                            <c:if test="${addr.isDefault()}"><small><fmt:message key="checkout.default_address"/></small></c:if>
                                        </span>
                                        <i class="fa-solid fa-circle-check" aria-hidden="true"></i>
                                    </label>
                                </c:forEach>
                            </fieldset>
                        </c:if>

                        <div id="newAddressFields" class="address-form">
                            <div class="payment-form-row">
                                <div class="payment-field">
                                    <label for="receiverName"><fmt:message key="checkout.receiver_name"/> <span aria-hidden="true">*</span></label>
                                    <input id="receiverName" type="text" name="receiverName"
                                           value="<c:out value="${currentUser.fullName}"/>" autocomplete="name" required>
                                </div>
                                <div class="payment-field">
                                    <label for="receiverPhone"><fmt:message key="checkout.receiver_phone"/> <span aria-hidden="true">*</span></label>
                                    <input id="receiverPhone" type="tel" name="receiverPhone"
                                           value="<c:out value="${currentUser.phone}"/>" autocomplete="tel"
                                           inputmode="tel" required>
                                </div>
                            </div>

                            <div class="payment-field">
                                <label for="addressDetail"><fmt:message key="checkout.address_detail"/> <span aria-hidden="true">*</span></label>
                                <input id="addressDetail" type="text" name="addressDetail"
                                       placeholder="<fmt:message key='checkout.address_placeholder'/>" autocomplete="street-address" required>
                            </div>

                            <div class="payment-form-row">
                                <div class="payment-field">
                                    <label for="provinceSelect"><fmt:message key="checkout.province"/> <span aria-hidden="true">*</span></label>
                                    <select name="carrierProvinceId" id="provinceSelect" required>
                                        <option value=""><fmt:message key="checkout.choose_province"/></option>
                                    </select>
                                    <input type="hidden" name="provinceName" id="provinceName">
                                </div>
                                <div class="payment-field">
                                    <label for="districtSelect"><fmt:message key="checkout.district"/> <span aria-hidden="true">*</span></label>
                                    <select name="carrierDistrictId" id="districtSelect" required disabled>
                                        <option value=""><fmt:message key="checkout.choose_district"/></option>
                                    </select>
                                    <input type="hidden" name="districtName" id="districtName">
                                </div>
                            </div>

                            <div class="payment-form-row">
                                <div class="payment-field">
                                    <label for="wardSelect"><fmt:message key="checkout.ward"/> <span aria-hidden="true">*</span></label>
                                    <select name="carrierWardCode" id="wardSelect" required disabled>
                                        <option value=""><fmt:message key="checkout.choose_ward"/></option>
                                    </select>
                                    <input type="hidden" name="wardName" id="wardName">
                                    <small id="wardStatus" class="payment-field__status" aria-live="polite"></small>
                                </div>
                            </div>
                        </div>

                        <div class="payment-field">
                            <label for="note"><fmt:message key="checkout.note_label"/></label>
                            <textarea id="note" name="note" placeholder="<fmt:message key='checkout.note_placeholder'/>"></textarea>
                        </div>
                    </section>

                    <section class="payment-panel" aria-labelledby="carrier-title">
                        <div class="payment-panel__heading">
                            <span class="payment-step">2</span>
                            <div>
                                <h2 id="carrier-title"><fmt:message key="checkout.carrier.title"/></h2>
                                <p><fmt:message key="checkout.carrier.supporting"/></p>
                            </div>
                        </div>

                        <div class="payment-method-list">
                            <c:forEach items="${shippingProviders}" var="provider" varStatus="vs">
                                <label class="payment-method carrier-method">
                                    <input type="radio" name="preferredCarrierCode" value="${provider.code}" ${vs.first ? 'checked' : ''} data-carrier-code="${provider.code}">
                                    <span class="payment-method__icon">
                                        <c:choose>
                                            <c:when test="${provider.code == 'GHN'}">
                                                <i class="fa-solid fa-truck-fast"></i>
                                            </c:when>
                                            <c:otherwise>
                                                <i class="fa-solid fa-truck-ramp-box"></i>
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                    <span class="payment-method__content">
                                        <strong><c:out value="${provider.name}"/></strong>
                                    </span>
                                    <i class="fa-solid fa-circle-check payment-method__check" aria-hidden="true"></i>
                                </label>
                            </c:forEach>
                        </div>
                    </section>

                    <section class="payment-panel" aria-labelledby="method-title">
                        <div class="payment-panel__heading">
                            <span class="payment-step">3</span>
                            <div>
                                <h2 id="method-title"><fmt:message key="checkout.payment.title"/></h2>
                                <p><fmt:message key="checkout.payment.supporting"/></p>
                            </div>
                        </div>

                        <div class="payment-method-list">
                            <label class="payment-method">
                                <input type="radio" name="paymentMethod" value="COD" checked>
                                <span class="payment-method__icon"><i class="fa-solid fa-money-bill-wave"></i></span>
                                <span class="payment-method__content">
                                    <strong><fmt:message key="checkout.cod"/></strong>
                                    <span><fmt:message key="checkout.cod_desc"/></span>
                                </span>
                                <i class="fa-solid fa-circle-check payment-method__check" aria-hidden="true"></i>
                            </label>

                            <c:choose>
                                <c:when test="${vnpayAvailable}">
                                    <label class="payment-method">
                                        <input type="radio" name="paymentMethod" value="VNPAY_QR">
                                        <span class="payment-method__icon payment-method__icon--vnpay">
                                            <img src="${pageContext.request.contextPath}/assets/images/Payment/vnpay.webp" alt="">
                                        </span>
                                        <span class="payment-method__content">
                                            <strong><fmt:message key="checkout.vnpay"/></strong>
                                            <span><fmt:message key="checkout.vnpay_desc"/></span>
                                        </span>
                                        <i class="fa-solid fa-circle-check payment-method__check" aria-hidden="true"></i>
                                    </label>
                                </c:when>
                                <c:otherwise>
                                    <div class="payment-method payment-method--disabled" aria-disabled="true">
                                        <span class="payment-method__icon payment-method__icon--vnpay">
                                            <img src="${pageContext.request.contextPath}/assets/images/Payment/vnpay.webp" alt="">
                                        </span>
                                        <span class="payment-method__content">
                                            <strong><fmt:message key="checkout.vnpay"/></strong>
                                            <span><fmt:message key="checkout.vnpay_unavailable"/></span>
                                        </span>
                                        <i class="fa-solid fa-lock payment-method__check" aria-hidden="true"></i>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>
                </div>

                <aside class="payment-panel order-summary" aria-labelledby="summary-title">
                    <div class="order-summary__heading">
                        <h2 id="summary-title"><fmt:message key="checkout.summary.title"/></h2>
                        <span><fmt:message key="checkout.summary.quantity"><fmt:param value="${cart.totalQuantity()}"/></fmt:message></span>
                    </div>

                    <div class="order-summary__items">
                        <c:forEach items="${cart.items}" var="item">
                            <div class="order-summary__item">
                                <div>
                                    <strong><c:out value="${item.product.name}"/></strong>
                                    <span><fmt:message key="checkout.item_quantity"><fmt:param value="${item.quantity}"/></fmt:message></span>
                                </div>
                                <b><fmt:formatNumber value="${item.subTotal}" groupingUsed="true"/>₫</b>
                            </div>
                        </c:forEach>
                    </div>

                    <dl class="order-summary__totals">
                        <div><dt><fmt:message key="checkout.subtotal"/></dt><dd><fmt:formatNumber value="${cart.total()}" groupingUsed="true"/>₫</dd></div>
                        <div>
                            <dt><fmt:message key="checkout.shipping_fee"/></dt>
                            <dd class="order-summary__free" id="shippingFeeText"><fmt:message key="checkout.choose_address"/></dd>
                        </div>
                        <div class="order-summary__grand-total">
                            <dt><fmt:message key="checkout.total"/></dt>
                            <dd id="grandTotalText"><fmt:formatNumber value="${cart.total()}" groupingUsed="true"/>₫</dd>
                        </div>
                    </dl>
                    <input type="hidden" name="shippingFee" id="shippingFeeInput" value="0">

                    <button type="submit" class="payment-submit" id="submitOrder">
                        <i class="fa-solid fa-shield-halved" aria-hidden="true"></i>
                        <span><fmt:message key="checkout.place_order"/></span>
                    </button>
                    <p class="payment-secure-note">
                        <i class="fa-solid fa-lock" aria-hidden="true"></i>
                        <fmt:message key="checkout.terms"/>
                    </p>
                </aside>
            </div>
        </form>
    </div>
