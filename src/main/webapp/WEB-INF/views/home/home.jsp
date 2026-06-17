<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="${requestScope.siteLocale}" scope="request"/>
<fmt:setBundle basename="messages" scope="request"/>

<div class="page-container home-page">

    <section class="home-hero" aria-label="<fmt:message key='home.hero.aria'/>">
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

            <button class="prev" type="button" aria-label="<fmt:message key='home.hero.prev'/>">
                &#10094;
            </button>

            <button class="next" type="button" aria-label="<fmt:message key='home.hero.next'/>">
                &#10095;
            </button>

            <div class="dots">
                <c:forEach var="item" items="${data.bannerCategories}" varStatus="status">
                    <fmt:message var="bannerDotLabel" key="home.hero.goto">
                        <fmt:param value="${status.count}"/>
                    </fmt:message>
                    <button
                            class="dot"
                            type="button"
                            data-slide="${status.index}"
                            aria-label="${bannerDotLabel}">
                    </button>
                </c:forEach>
            </div>

        </div>
    </section>

    <section class="home-quick-panels" aria-label="<fmt:message key='home.quick.aria'/>">
        <a class="home-quick-panel" href="${pageContext.request.contextPath}/products?panel=vi-que-binh-dinh">
            <span class="home-quick-panel__icon">
                <i class="fa-solid fa-basket-shopping"></i>
            </span>
            <span class="home-quick-panel__title"><fmt:message key="home.quick.vi_qua.title"/></span>
            <span class="home-quick-panel__subtitle"><fmt:message key="home.quick.vi_qua.subtitle"/></span>
        </a>

        <a class="home-quick-panel" href="${pageContext.request.contextPath}/products?panel=qua-tu-bien">
            <span class="home-quick-panel__icon">
                <i class="fa-solid fa-fish"></i>
            </span>
            <span class="home-quick-panel__title"><fmt:message key="home.quick.sea.title"/></span>
            <span class="home-quick-panel__subtitle"><fmt:message key="home.quick.sea.subtitle"/></span>
        </a>

        <a class="home-quick-panel" href="${pageContext.request.contextPath}/products?panel=huong-men-dat-vo">
            <span class="home-quick-panel__icon">
                <i class="fa-solid fa-wine-bottle"></i>
            </span>
            <span class="home-quick-panel__title"><fmt:message key="home.quick.spirit.title"/></span>
            <span class="home-quick-panel__subtitle"><fmt:message key="home.quick.spirit.subtitle"/></span>
        </a>

        <a class="home-quick-panel" href="${pageContext.request.contextPath}/products?panel=net-viet-lam-qua">
            <span class="home-quick-panel__icon">
                <i class="fa-solid fa-gift"></i>
            </span>
            <span class="home-quick-panel__title"><fmt:message key="home.quick.handmade.title"/></span>
            <span class="home-quick-panel__subtitle"><fmt:message key="home.quick.handmade.subtitle"/></span>
        </a>
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
                        <fmt:message key="home.section.no_products"/>
                    </p>
                </c:if>

            </div>

            <div class="home-section__more">
                <a class="see-more-btn"
                   href="${pageContext.request.contextPath}/category?id=${section.category.id}">
                    <fmt:message key="home.section.more"/>
                </a>
            </div>

        </section>

    </c:forEach>


    <section id="extension" class="product-section home-section home-category-strip">

        <div class="left-content home-category-strip__intro">
            <h2><fmt:message key="home.section.other_categories"/></h2>

            <a class="see-more-btn see-more-btn--light"
               href="${pageContext.request.contextPath}/category">
                <fmt:message key="home.section.more"/>
            </a>
        </div>

        <div class="right-content home-category-strip__content">

            <button class="slider-btn prev" id="extPrev" type="button" aria-label="<fmt:message key='home.hero.prev'/>">
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

            <button class="slider-btn next" id="extNext" type="button" aria-label="<fmt:message key='home.hero.next'/>">
                ›
            </button>

        </div>

    </section>


    <section class="section related-products home-section">

        <div class="home-section__header">
            <h2 class="related-title">
                <fmt:message key="home.section.top_rated"/>
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
                <fmt:message key="home.section.newest"/>
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
