<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

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

<div class="product-type-dynamic" id="productTypeResults" aria-live="polite">
    <div class="category-header">
        <h2>${data.category.categoryName}</h2>
        <span>${data.totalProducts} sản phẩm</span>
    </div>

    <c:set var="hasActiveFilter"
           value="${not empty data.minPrice or not empty data.maxPrice or not empty data.rating or data.sortParam != 'popular'}"/>

    <c:if test="${hasActiveFilter}">
        <div class="active-filter-bar">
            <span>Đang lọc:</span>

            <c:if test="${not empty data.minPrice or not empty data.maxPrice}">
                <strong>
                    <c:choose>
                        <c:when test="${empty data.minPrice}">
                            Dưới <fmt:formatNumber value="${data.maxPrice}" groupingUsed="true"/>đ
                        </c:when>
                        <c:when test="${empty data.maxPrice}">
                            Trên <fmt:formatNumber value="${data.minPrice}" groupingUsed="true"/>đ
                        </c:when>
                        <c:otherwise>
                            <fmt:formatNumber value="${data.minPrice}" groupingUsed="true"/>đ -
                            <fmt:formatNumber value="${data.maxPrice}" groupingUsed="true"/>đ
                        </c:otherwise>
                    </c:choose>
                </strong>
            </c:if>

            <c:if test="${not empty data.rating}">
                <strong>Từ ${data.rating} sao</strong>
            </c:if>

            <c:if test="${data.sortParam != 'popular'}">
                <strong>
                    <c:choose>
                        <c:when test="${data.sortParam == 'price_asc'}">Giá thấp đến cao</c:when>
                        <c:when test="${data.sortParam == 'price_desc'}">Giá cao đến thấp</c:when>
                        <c:when test="${data.sortParam == 'newest'}">Mới nhất</c:when>
                        <c:otherwise>Bán chạy</c:otherwise>
                    </c:choose>
                </strong>
            </c:if>

            <a class="active-filter-clear" href="${resetUrl}">Xóa tất cả</a>
        </div>
    </c:if>

    <section class="product-section">
        <div class="product-list">

            <c:forEach var="p" items="${data.products}">
                <c:set var="p" value="${p}" scope="request"/>
                <jsp:include page="/WEB-INF/views/product/product-card.jsp"/>
            </c:forEach>

            <c:if test="${empty data.products}">
                <p class="empty-state">Không có sản phẩm phù hợp với bộ lọc.</p>
            </c:if>

        </div>
    </section>

    <div class="pagination">
        <c:forEach begin="1" end="${data.totalPages}" var="i">
            <c:url var="pageUrl" value="${data.listingAction}">
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
                <c:param name="page" value="${i}"/>
                <c:param name="minPrice" value="${data.minPrice}"/>
                <c:param name="maxPrice" value="${data.maxPrice}"/>
                <c:param name="rating" value="${data.rating}"/>
                <c:param name="sort" value="${data.sortParam}"/>
            </c:url>
            <a href="${pageUrl}"
               class="${i == data.currentPage ? 'active' : ''}">
                    ${i}
            </a>
        </c:forEach>
    </div>
</div>
