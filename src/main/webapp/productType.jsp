<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<section class="section product-type-page">
    <div class="main-container">

        <div class="product-type-layout">

            <aside class="filter-sidebar">
                <h3>Bộ lọc</h3>

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
                        <div class="filter-group__title">Giá</div>
                        <div class="filter-choice-list">
                            <label class="filter-choice">
                                <input type="radio" name="priceRange" value="under-100"
                                       data-min-price="" data-max-price="100000">
                                <span>Dưới 100k</span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="priceRange" value="100-300"
                                       data-min-price="100000" data-max-price="300000">
                                <span>100k - 300k</span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="priceRange" value="300-500"
                                       data-min-price="300000" data-max-price="500000">
                                <span>300k - 500k</span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="priceRange" value="over-500"
                                       data-min-price="500000" data-max-price="">
                                <span>Trên 500k</span>
                            </label>
                        </div>

                        <div class="filter-price-row">
                            <label>
                                <span>Giá từ</span>
                                <input type="number" name="minPrice" value="${data.minPrice}" placeholder="100.000"/>
                            </label>
                            <span class="filter-price-separator">~</span>
                            <label>
                                <span>Đến</span>
                                <input type="number" name="maxPrice" value="${data.maxPrice}" placeholder="500.000"/>
                            </label>
                        </div>
                    </div>

                    <div class="filter-group">
                        <div class="filter-group__title">Đánh giá</div>
                        <div class="filter-choice-list">
                            <label class="filter-choice">
                                <input type="radio" name="rating" value="" ${empty data.rating ? 'checked' : ''}>
                                <span>Tất cả</span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="rating" value="5" ${data.rating == 5 ? 'checked' : ''}>
                                <span>Từ 5 sao</span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="rating" value="4" ${data.rating == 4 ? 'checked' : ''}>
                                <span>Từ 4 sao</span>
                            </label>
                            <label class="filter-choice">
                                <input type="radio" name="rating" value="3" ${data.rating == 3 ? 'checked' : ''}>
                                <span>Từ 3 sao</span>
                            </label>
                        </div>
                    </div>

                    <div class="filter-group">
                        <div class="filter-group__title">Sắp xếp</div>
                        <select name="sort">
                            <option value="popular" ${data.sortParam == 'popular' ? 'selected' : ''}>Bán chạy</option>
                            <option value="price_asc" ${data.sortParam == 'price_asc' ? 'selected' : ''}>Giá thấp đến cao
                            </option>
                            <option value="price_desc" ${data.sortParam == 'price_desc' ? 'selected' : ''}>Giá cao đến thấp
                            </option>
                            <option value="newest" ${data.sortParam == 'newest' ? 'selected' : ''}>Mới nhất</option>
                        </select>
                    </div>

                    <%-- Có thể bổ sung block checkbox thông tin sản phẩm tại đây khi mở rộng backend lấy distinct product_specifications theo ProductType hiện tại. --%>

                    <button class="filter-apply-button" type="submit">Áp dụng</button>
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
