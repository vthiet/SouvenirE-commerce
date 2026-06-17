<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<section class="section product-type-page">
    <div class="main-container">

        <div class="product-type-layout">

            <aside class="filter-sidebar">
                <h3><fmt:message key="productType.filter"/></h3>

                <form method="get" action="${pageContext.request.contextPath}${data.listingAction}">
                    <c:url var="resetUrl" value="${data.listingAction}">
                        <c:choose>
                            <c:when test="${data.searchMode}">
                                <c:param name="keyword" value="${data.searchKeyword}"/>
                            </c:when>
                            <c:when test="${data.panelMode}">
                                <c:param name="panel" value="${data.panelSlug}"/>
                            </c:when>
                            <c:otherwise>
                                <c:param name="id" value="${data.category.id}"/>
                            </c:otherwise>
                        </c:choose>
                    </c:url>

                    <c:choose>
                        <c:when test="${data.searchMode}">
                            <input type="hidden" name="keyword" value="${data.searchKeyword}"/>
                        </c:when>
                        <c:when test="${data.panelMode}">
                            <input type="hidden" name="panel" value="${data.panelSlug}"/>
                        </c:when>
                        <c:otherwise>
                            <input type="hidden" name="id" value="${data.category.id}"/>
                        </c:otherwise>
                    </c:choose>
                    <input type="hidden" name="page" value="1"/>

                    <div class="filter-group filter-group--price">
                        <div class="filter-group__title"><fmt:message key="productType.price"/></div>
                        <div class="filter-choice-list">
                            <label class="filter-choice">
                                <input type="radio" name="priceRange" value="under-100"
                                       data-min-price="" data-max-price="100000">
                                <span><fmt:message key="productType.under_100"/></span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="priceRange" value="100-300"
                                       data-min-price="100000" data-max-price="300000">
                                <span><fmt:message key="productType.range_100_300"/></span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="priceRange" value="300-500"
                                       data-min-price="300000" data-max-price="500000">
                                <span><fmt:message key="productType.range_300_500"/></span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="priceRange" value="over-500"
                                       data-min-price="500000" data-max-price="">
                                <span><fmt:message key="productType.over_500"/></span>
                            </label>
                        </div>

                        <div class="filter-price-row">
                            <label>
                                <span><fmt:message key="productType.price_from"/></span>
                                <input type="number" name="minPrice" value="${data.minPrice}" placeholder="<fmt:message key='productType.price_placeholder_min'/>"/>
                            </label>
                            <span class="filter-price-separator">~</span>
                            <label>
                                <span><fmt:message key="productType.price_to"/></span>
                                <input type="number" name="maxPrice" value="${data.maxPrice}" placeholder="<fmt:message key='productType.price_placeholder_max'/>"/>
                            </label>
                        </div>
                    </div>

                    <div class="filter-group">
                        <div class="filter-group__title"><fmt:message key="productType.rating"/></div>
                        <div class="filter-choice-list">
                            <label class="filter-choice">
                                <input type="radio" name="rating" value="" ${empty data.rating ? 'checked' : ''}>
                                <span><fmt:message key="productType.all"/></span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="rating" value="5" ${data.rating == 5 ? 'checked' : ''}>
                                <span><fmt:message key="productType.rating_5"/></span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="rating" value="4" ${data.rating == 4 ? 'checked' : ''}>
                                <span><fmt:message key="productType.rating_4"/></span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="rating" value="3" ${data.rating == 3 ? 'checked' : ''}>
                                <span><fmt:message key="productType.rating_3"/></span>
                            </label>
                        </div>
                    </div>

                    <div class="filter-group">
                        <div class="filter-group__title"><fmt:message key="productType.sort"/></div>
                        <select name="sort">
                            <option value="popular" ${data.sortParam == 'popular' ? 'selected' : ''}><fmt:message key="productType.sort.popular"/></option>
                            <option value="price_asc" ${data.sortParam == 'price_asc' ? 'selected' : ''}><fmt:message key="productType.sort.price_asc"/>
                            </option>
                            <option value="price_desc" ${data.sortParam == 'price_desc' ? 'selected' : ''}><fmt:message key="productType.sort.price_desc"/>
                            </option>
                            <option value="newest" ${data.sortParam == 'newest' ? 'selected' : ''}><fmt:message key="productType.sort.newest"/></option>
                        </select>
                    </div>

                    <%-- Có thể bổ sung block checkbox thông tin sản phẩm tại đây khi mở rộng backend lấy distinct product_specifications theo ProductType hiện tại. --%>

                    <button class="filter-apply-button" type="submit"><fmt:message key="productType.apply"/></button>
                </form>
            </aside>

            <main class="product-type-content">

                <c:if test="${not data.searchMode and not data.panelMode}">
                <div class="category-banner">
                    <img src="${pageContext.request.contextPath}/assets/images/home_banner/${data.category.image}"
                         alt="${data.category.categoryName}">
                </div>
                </c:if>

                <jsp:include page="/WEB-INF/views/product/product-type-results.jsp"/>

            </main>
        </div>
    </div>
</section>
