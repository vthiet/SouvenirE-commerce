<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="page-container home-page">

    <section class="home-hero" aria-label="Danh mục nổi bật">
        <div class="slideshow-container" id="headerSlideshow">

            <c:forEach var="item" items="${data.bannerCategories}" varStatus="status">
                <div class="slide">
                    <a href="${pageContext.request.contextPath}/category?id=${item.category.id}">
                        <img
                                src="${pageContext.request.contextPath}/assets/images/home_banner/${item.category.image}"
                                alt="${item.category.categoryName}"
                                loading="${status.first ? 'eager' : 'lazy'}"
                                decoding="async">
                    </a>
                </div>
            </c:forEach>

            <button class="prev" type="button" aria-label="Banner trước">
                &#10094;
            </button>

            <button class="next" type="button" aria-label="Banner kế tiếp">
                &#10095;
            </button>

            <div class="dots">
                <c:forEach var="item" items="${data.bannerCategories}" varStatus="status">
                    <button
                            class="dot"
                            type="button"
                            data-slide="${status.index}"
                            aria-label="Chuyển đến banner ${status.count}">
                    </button>
                </c:forEach>
            </div>

        </div>
    </section>


    <c:forEach var="section" items="${data.topCategorySections}">

        <section id="Loai${section.category.id}" class="product-section home-section">

            <div class="home-section__header">
                <h2>
                    <a href="${pageContext.request.contextPath}/category?id=${section.category.id}">
                            ${section.category.categoryName}
                    </a>
                </h2>
            </div>

            <div class="product-list">

                <c:forEach var="p" items="${section.productCards}">
                    <c:set var="p" value="${p}" scope="request"/>
                    <jsp:include page="/WEB-INF/views/product/product-card.jsp"/>
                </c:forEach>

                <c:if test="${empty section.productCards}">
                    <p class="empty-state">
                        Chưa có sản phẩm cho danh mục này.
                    </p>
                </c:if>

            </div>

            <div class="home-section__more">
                <a class="see-more-btn"
                   href="${pageContext.request.contextPath}/category?id=${section.category.id}">
                    Xem thêm
                </a>
            </div>

        </section>

    </c:forEach>


    <section id="extension" class="product-section home-section home-category-strip">

        <div class="left-content home-category-strip__intro">
            <h2>Danh mục khác</h2>

            <a class="see-more-btn see-more-btn--light"
               href="${pageContext.request.contextPath}/category">
                Xem thêm
            </a>
        </div>

        <div class="right-content home-category-strip__content">

            <button class="slider-btn prev" id="extPrev" type="button" aria-label="Danh mục trước">
                ‹
            </button>

            <div class="product-slider-wrapper">
                <div class="product-slider" id="extSlider">

                    <c:forEach var="section" items="${data.extensionSections}">
                        <article class="category-card">
                            <a href="${pageContext.request.contextPath}/category?id=${section.category.id}">
                                <img
                                        src="${pageContext.request.contextPath}/assets/images/home_banner/${section.category.image}"
                                        alt="${section.category.categoryName}"
                                        loading="lazy"
                                        decoding="async">

                                <h3>${section.category.categoryName}</h3>
                            </a>
                        </article>
                    </c:forEach>

                </div>
            </div>

            <button class="slider-btn next" id="extNext" type="button" aria-label="Danh mục kế tiếp">
                ›
            </button>

        </div>

    </section>


    <section class="section related-products home-section">

        <div class="home-section__header">
            <h2 class="related-title">
                Sản phẩm đánh giá cao
            </h2>
        </div>

        <div class="related-grid product-list">
            <c:forEach var="p" items="${data.topRatedProductCards}">
                <c:set var="p" value="${p}" scope="request"/>
                <jsp:include page="/WEB-INF/views/product/product-card.jsp"/>
            </c:forEach>
        </div>

    </section>


    <section class="section related-products home-section">

        <div class="home-section__header">
            <h2 class="related-title">
                Sản phẩm mới
            </h2>
        </div>

        <div class="related-grid product-list">
            <c:forEach var="p" items="${data.newestProductCards}">
                <c:set var="p" value="${p}" scope="request"/>
                <jsp:include page="/WEB-INF/views/product/product-card.jsp"/>
            </c:forEach>
        </div>

    </section>

</div>
