<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<input type="hidden" id="productId" value="${data.product.id}">
<input type="hidden" id="contextPath" value="${pageContext.request.contextPath}">

<div id="productDetailI18n"
     hidden
     data-toast-title="<fmt:message key='cart.toast.title'/>"
     data-toast-close="<fmt:message key='cart.toast.close'/>"
     data-review-login-required="<fmt:message key='review.login_required'/>"
     data-review-invalid-product="<fmt:message key='review.invalid_product'/>"
     data-review-invalid-rating="<fmt:message key='review.invalid_rating'/>"
     data-review-comment-too-short="<fmt:message key='review.comment_too_short'/>"
     data-review-not-eligible="<fmt:message key='review.not_eligible'/>"
     data-review-save-failed="<fmt:message key='review.save_failed'/>"
     data-review-success="<fmt:message key='review.success'/>"
     data-review-load-failed="<fmt:message key='review.load_failed'/>">
</div>

<main class="product-page">

    <section class="section">
        <div class="main-container">

            <div class="product-top">

                <div class="product-left-wrapper">
                    <div class="product-gallery">
                        <c:url var="productDetailImage" value="${data.product.image}"/>

                        <img class="product-main-image"
                             src="${productDetailImage}"
                             alt="${data.product.name}">

                        <button type="button" class="btn-zoom">
                            <i class="fa-solid fa-magnifying-glass"></i>
                        </button>
                    </div>
                </div>

                <div class="product-right">

                    <h1 class="product-title">${data.product.name}</h1>

                    <div class="product-detail-meta">
                        <span class="product-meta-chip product-meta-chip--rating">
                            <i class="fa-solid fa-star"></i>
                            ${data.avgRating}
                        </span>

                        <span class="product-meta-chip product-meta-chip--sold">
                            <i class="fa-solid fa-cart-shopping"></i>
                            <fmt:message key="product.detail.sold_prefix">
                                <fmt:param value="${data.product.totalSold}"/>
                            </fmt:message>
                        </span>

                        <c:choose>
                            <c:when test="${data.product.stockQuantity > 0}">
                                <span class="product-meta-chip product-meta-chip--stock">
                                    <i class="fa-solid fa-box"></i>
                                    <fmt:message key="product.detail.stock_prefix">
                                        <fmt:param value="${data.product.stockQuantity}"/>
                                    </fmt:message>
                                </span>
                            </c:when>

                            <c:otherwise>
                                <span class="product-meta-chip product-meta-chip--out">
                                    <i class="fa-solid fa-circle-exclamation"></i>
                                    <fmt:message key="product.detail.out_of_stock"/>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="product-price">
                        <c:choose>
                            <c:when test="${data.promotion != null}">
                                <span class="old-price">
                                    <fmt:formatNumber value="${data.product.originalPrice}"/> ₫
                                </span>

                                <span class="sale-price">
                                    <fmt:formatNumber value="${data.discountedPrice}"/> ₫
                                </span>

                                <span class="discount">
                                    -${data.promotion.discountPercent}%
                                </span>
                            </c:when>

                            <c:otherwise>
                                <span class="normal-price">
                                    <fmt:formatNumber value="${data.product.originalPrice}"/> ₫
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <c:if test="${not empty data.product.shortDescription}">
                        <p class="short-description">
                                ${data.product.shortDescription}
                        </p>
                    </c:if>

                    <div class="product-shipping">
                        <div class="product-shipping__label">
                            <i class="fa-solid fa-truck-fast"></i>
                            <span><fmt:message key="product.detail.shipping.title"/></span>
                        </div>

                        <div class="product-shipping__content">
                            <c:choose>
                                <c:when test="${not empty shippingAddressText}">
                                    <p>
                                        <fmt:message key="product.detail.shipping.to"/>
                                        <strong>${shippingAddressText}</strong>
                                    </p>

                                    <c:choose>
                                        <c:when test="${not empty shippingFeeAmount}">
                                            <p>
                                                <fmt:message key="product.detail.shipping.estimated"/>
                                                <strong>
                                                    <fmt:formatNumber value="${shippingFeeAmount}"/>đ
                                                </strong>
                                            </p>
                                        </c:when>

                                        <c:otherwise>
                                            <p>
                                                <fmt:message key="product.detail.shipping.reference"/>
                                                <strong>22.000đ - 55.000đ</strong>
                                            </p>
                                            <span><fmt:message key="product.detail.shipping.address_not_enough"/></span>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>

                                <c:otherwise>
                                    <p>
                                        <fmt:message key="product.detail.shipping.reference"/>
                                        <strong>22.000đ - 55.000đ</strong>
                                    </p>
                                    <span><fmt:message key="product.detail.shipping.login_or_address"/></span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <form action="${pageContext.request.contextPath}/cart/add"
                          method="post"
                          class="buy-form">

                        <input type="hidden" name="productId" value="${data.product.id}">

                        <div class="quantity-actions">
                            <div class="quantity-control">
                                <button type="button" class="qty-btn minus">-</button>

                                <input type="number"
                                       name="quantity"
                                       class="qty-input"
                                       value="1"
                                       min="1"
                                       max="${data.product.stockQuantity}">

                                <button type="button" class="qty-btn plus">+</button>
                            </div>

                            <button type="submit" class="add-cart">
                                <i class="fa-solid fa-cart-shopping"></i>
                                <fmt:message key="product.detail.add_to_cart"/>
                            </button>
                        </div>

                        <button type="submit"
                                name="buyNow"
                                value="true"
                                class="buy-now-full">
                            <fmt:message key="product.detail.buy_now"/>
                        </button>

                    </form>

                    <div class="store-note-block">
                        <p class="store-note-title">
                            <i class="fa-solid fa-box"></i>
                            <fmt:message key="product.detail.store_note_title"/>
                        </p>

                        <p class="store-note-text">
                            <fmt:message key="product.detail.store_note_text"/>
                        </p>
                    </div>

                </div>

            </div>

        </div>
    </section>

    <div class="divider"></div>

    <section class="section product-info-section">
        <div class="main-container">

            <div class="info-header">
                <h2 class="info-title"><fmt:message key="product.detail.info_title"/></h2>
            </div>

            <c:if test="${not empty data.product.description}">
                <div class="description-content">
                        ${data.product.description}
                </div>
            </c:if>

            <c:if test="${not empty data.specifications}">
                <table class="spec-table">
                    <tbody>
                    <c:forEach var="spec" items="${data.specifications}">
                        <tr>
                            <td class="spec-name">${spec.specName}</td>
                            <td class="spec-value">${spec.specValue}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <c:if test="${empty data.specifications}">
                <p class="empty-text"><fmt:message key="product.detail.no_details"/></p>
            </c:if>

        </div>
    </section>

    <div class="divider"></div>

    <section class="section product-reviews">
        <div class="main-container">

            <h2 class="reviews-main-title"><fmt:message key="product.detail.reviews_title"/></h2>

            <div class="review-content-wrap">

                <div class="review-mleft">
                    <div class="review-summary-block">
                        <p class="average-rating">${data.avgRating}</p>

                        <p class="review-count">
                            <fmt:message key="product.detail.based_on_reviews">
                                <fmt:param value="${data.totalReviews}"/>
                            </fmt:message>
                        </p>

                        <div class="rating-breakdown">

                            <c:set var="count5" value="${data.ratingCount['5']}"/>
                            <c:set var="percent5"
                                   value="${data.totalReviews > 0 ? (count5 * 100.0 / data.totalReviews) : 0}"/>
                            <div class="rating-row">
                                <span>5</span>
                                <i class="fa-solid fa-star"></i>
                                <div class="rating-bar">
                                    <span style="width:${percent5}%"></span>
                                </div>
                                <span>
            <fmt:formatNumber value="${percent5}" minFractionDigits="1" maxFractionDigits="1"/>%
        </span>
                            </div>

                            <c:set var="count4" value="${data.ratingCount['4']}"/>
                            <c:set var="percent4"
                                   value="${data.totalReviews > 0 ? (count4 * 100.0 / data.totalReviews) : 0}"/>
                            <div class="rating-row">
                                <span>4</span>
                                <i class="fa-solid fa-star"></i>
                                <div class="rating-bar">
                                    <span style="width:${percent4}%"></span>
                                </div>
                                <span>
            <fmt:formatNumber value="${percent4}" minFractionDigits="1" maxFractionDigits="1"/>%
        </span>
                            </div>

                            <c:set var="count3" value="${data.ratingCount['3']}"/>
                            <c:set var="percent3"
                                   value="${data.totalReviews > 0 ? (count3 * 100.0 / data.totalReviews) : 0}"/>
                            <div class="rating-row">
                                <span>3</span>
                                <i class="fa-solid fa-star"></i>
                                <div class="rating-bar">
                                    <span style="width:${percent3}%"></span>
                                </div>
                                <span>
            <fmt:formatNumber value="${percent3}" minFractionDigits="1" maxFractionDigits="1"/>%
        </span>
                            </div>

                            <c:set var="count2" value="${data.ratingCount['2']}"/>
                            <c:set var="percent2"
                                   value="${data.totalReviews > 0 ? (count2 * 100.0 / data.totalReviews) : 0}"/>
                            <div class="rating-row">
                                <span>2</span>
                                <i class="fa-solid fa-star"></i>
                                <div class="rating-bar">
                                    <span style="width:${percent2}%"></span>
                                </div>
                                <span>
            <fmt:formatNumber value="${percent2}" minFractionDigits="1" maxFractionDigits="1"/>%
        </span>
                            </div>

                            <c:set var="count1" value="${data.ratingCount['1']}"/>
                            <c:set var="percent1"
                                   value="${data.totalReviews > 0 ? (count1 * 100.0 / data.totalReviews) : 0}"/>
                            <div class="rating-row">
                                <span>1</span>
                                <i class="fa-solid fa-star"></i>
                                <div class="rating-bar">
                                    <span style="width:${percent1}%"></span>
                                </div>
                                <span>
                                    <fmt:formatNumber value="${percent1}" minFractionDigits="1" maxFractionDigits="1"/>%
                                </span>
                            </div>

                        </div>
                    </div>

                    <button type="button"
                            class="review-action-btn"
                            data-logged-in="${isLoggedIn}"
                            data-can-review="${canReview}">
                        <fmt:message key="product.detail.review_action"/>
                    </button>
                </div>

                <div class="review-mright">

                    <div class="review-filter-bar">
                        <button type="button" class="filter-btn active" data-rating=""><fmt:message key="product.detail.review_filter.all"/></button>
                        <button type="button" class="filter-btn" data-rating="5">5 ★</button>
                        <button type="button" class="filter-btn" data-rating="4">4 ★</button>
                        <button type="button" class="filter-btn" data-rating="3">3 ★</button>
                        <button type="button" class="filter-btn" data-rating="2">2 ★</button>
                        <button type="button" class="filter-btn" data-rating="1">1 ★</button>

                        <select class="sort-select">
                            <option value="newest"><fmt:message key="product.detail.review_filter.newest"/></option>
                            <option value="oldest"><fmt:message key="product.detail.review_filter.oldest"/></option>
                        </select>
                    </div>

                    <div id="reviewContainer"></div>

                    <button id="loadMoreReview" type="button">
                        <fmt:message key="product.detail.review_load_more"/>
                    </button>

                </div>

            </div>

        </div>
    </section>

</main>

<section class="section related-products">
    <div class="main-container">

        <h2 class="related-title"><fmt:message key="product.detail.related_title"/></h2>

        <div class="related-grid">
            <c:forEach var="p" items="${data.relatedProductCards}">
                <%@ include file="product-card.jsp" %>
            </c:forEach>
        </div>

    </div>
</section>

<div class="image-modal" id="imageModal">
    <div class="modal-overlay"></div>

    <div class="modal-content">
        <img id="zoomImage" src="" alt="Zoom image">
        <button type="button" class="modal-close">&times;</button>
    </div>
</div>

<div class="review-modal" id="reviewModal">
    <div class="review-overlay"></div>

    <div class="review-box">
        <button type="button" class="review-close">&times;</button>

        <h3><fmt:message key="product.detail.review_modal_title"/></h3>

        <div class="rating-stars" data-rating="0">
            <i data-value="1">★</i>
            <i data-value="2">★</i>
            <i data-value="3">★</i>
            <i data-value="4">★</i>
            <i data-value="5">★</i>
        </div>

        <textarea id="reviewText"
                  maxlength="700"
                  placeholder="<fmt:message key='product.detail.review_placeholder'/>"></textarea>

        <button type="button" class="submit-review">
            <fmt:message key="product.detail.submit_review"/>
        </button>
    </div>
</div>
