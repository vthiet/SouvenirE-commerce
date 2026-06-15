<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<section class="section product-type-page">
    <div class="main-container">

        <div class="product-type-layout">

            <aside class="filter-sidebar">
                <h3>Bộ lọc</h3>

                <form method="get" action="${pageContext.request.contextPath}${data.listingAction}">
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

                    <div class="filter-group">
                        <label>Giá từ</label>
                        <input type="number" name="minPrice" value="${data.minPrice}"/>
                    </div>

                    <div class="filter-group">
                        <label>Đến</label>
                        <input type="number" name="maxPrice" value="${data.maxPrice}"/>
                    </div>

                    <div class="filter-group">
                        <label>Đánh giá</label>
                        <select name="rating">
                            <option value="">Tất cả</option>
                            <option value="5" ${data.rating == 5 ? 'selected' : ''}>⭐ 5 sao</option>
                            <option value="4" ${data.rating == 4 ? 'selected' : ''}>⭐ 4 sao trở lên</option>
                            <option value="3" ${data.rating == 3 ? 'selected' : ''}>⭐ 3 sao trở lên</option>
                        </select>
                    </div>

                    <div class="filter-group">
                        <label>Sắp xếp</label>
                        <select name="sort">
                            <option value="popular" ${data.sortParam == 'popular' ? 'selected' : ''}>Bán chạy</option>
                            <option value="newest" ${data.sortParam == 'newest' ? 'selected' : ''}>Mới nhất</option>
                            <option value="price_asc" ${data.sortParam == 'price_asc' ? 'selected' : ''}>Giá tăng
                            </option>
                            <option value="price_desc" ${data.sortParam == 'price_desc' ? 'selected' : ''}>Giá giảm
                            </option>
                        </select>
                    </div>

                    <button type="submit">Áp dụng</button>
                </form>
            </aside>

            <main class="product-type-content">

                <c:if test="${not data.searchMode and not data.panelMode}">
                <div class="category-banner">
                    <img src="${pageContext.request.contextPath}/assets/images/home_banner/${data.category.image}"
                         alt="${data.category.categoryName}">
                </div>
                </c:if>

                <div class="category-header">
                    <h2>${data.category.categoryName}</h2>
                    <span>${data.totalProducts} sản phẩm</span>
                </div>

                <section class="product-section">
                    <div class="product-list">

                        <c:forEach var="p" items="${data.products}">
                            <c:set var="p" value="${p}" scope="request"/>
                            <jsp:include page="product-card.jsp"/>
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

            </main>
        </div>
    </div>
</section>
