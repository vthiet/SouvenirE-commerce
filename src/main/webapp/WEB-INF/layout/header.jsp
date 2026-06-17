<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="${requestScope.siteLocale}" scope="request"/>
<fmt:setBundle basename="messages" scope="request"/>

<c:set var="currentRequestPath"
       value="${not empty requestScope.currentRequestPath ? requestScope.currentRequestPath : pageContext.request.servletPath}"/>

<c:url var="languageViUrl" value="${currentRequestPath}">
    <c:forEach var="entry" items="${paramValues}">
        <c:if test="${entry.key != 'lang'}">
            <c:forEach var="value" items="${entry.value}">
                <c:param name="${entry.key}" value="${value}"/>
            </c:forEach>
        </c:if>
    </c:forEach>
    <c:param name="lang" value="vi"/>
</c:url>

<c:url var="languageEnUrl" value="${currentRequestPath}">
    <c:forEach var="entry" items="${paramValues}">
        <c:if test="${entry.key != 'lang'}">
            <c:forEach var="value" items="${entry.value}">
                <c:param name="${entry.key}" value="${value}"/>
            </c:forEach>
        </c:if>
    </c:forEach>
    <c:param name="lang" value="en"/>
</c:url>

<div id="siteI18n"
     hidden
     data-search-recent="<fmt:message key='header.search.recent'/>"
     data-search-clear-all="<fmt:message key='header.search.clear_all'/>"
     data-search-empty-recent="<fmt:message key='header.search.empty_recent'/>"
     data-search-empty-suggestions="<fmt:message key='header.search.empty_suggestions'/>"
     data-search-meta="<fmt:message key='header.search.meta'/>"
     data-search-clear="<fmt:message key='header.search.clear'/>"
     data-cart-empty="<fmt:message key='header.cart.empty'/>"
     data-cart-recent="<fmt:message key='header.cart.recent'/>"
     data-cart-view="<fmt:message key='header.cart.view'/>"
     data-cart-count-template="<fmt:message key='header.cart.count'/>"
     data-cart-add-success="<fmt:message key='cart.add_success'/>"
     data-cart-add-to-cart-error="<fmt:message key='cart.add_to_cart_error'/>"
     data-theme-light="<fmt:message key='theme.light'/>"
     data-theme-dark="<fmt:message key='theme.dark'/>"
     data-theme-toggle="<fmt:message key='theme.toggle'/>"
     data-theme-switch-to-light="<fmt:message key='theme.switch_to_light'/>"
     data-theme-switch-to-dark="<fmt:message key='theme.switch_to_dark'/>">
</div>

<c:if test="${headerMode == 'CHECKOUT_FLOW'}">
    <header class="checkout-flow-header">
        <div class="checkout-flow-shell">
            <a class="checkout-flow-logo"
               href="${pageContext.request.contextPath}/home"
               aria-label="<fmt:message key='header.checkout.home'/>">
                <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="<fmt:message key='site.name'/>">
            </a>

            <nav class="checkout-flow-steps" aria-label="<fmt:message key='header.checkout.steps'/>">
                <span class="checkout-flow-step ${checkoutStep == 'CART' ? 'active' : ''}"><fmt:message key="header.checkout.cart"/></span>
                <i class="fa-solid fa-chevron-right"></i>
                <span class="checkout-flow-step ${checkoutStep == 'CHECKOUT' ? 'active' : ''}"><fmt:message key="header.checkout.checkout"/></span>
                <i class="fa-solid fa-chevron-right"></i>
                <span class="checkout-flow-step ${checkoutStep == 'DONE' ? 'active' : ''}"><fmt:message key="header.checkout.done"/></span>
            </nav>

            <div class="checkout-flow-user header-account">

                <div class="header-language-switcher"
                     role="group"
                     aria-label="<fmt:message key='language.label'/>">
                    <div class="header-language-switcher__options">
                        <a class="header-language-switcher__option ${requestScope.siteLanguage == 'vi' ? 'is-active' : ''}"
                           href="${languageViUrl}">
                            <fmt:message key="language.vi"/>
                        </a>
                        <a class="header-language-switcher__option ${requestScope.siteLanguage == 'en' ? 'is-active' : ''}"
                           href="${languageEnUrl}">
                            <fmt:message key="language.en"/>
                        </a>
                    </div>
                </div>

                <c:choose>

                    <c:when test="${not empty authUser}">

                        <div class="user-box"
                             id="userToggle">

                            <i class="fa-solid fa-circle-user"></i>

                <span class="user-name">
                    <fmt:message key="header.greeting"/>, ${authUser.fullName}
                </span>

                            <i class="fa fa-caret-down"></i>

                        </div>

                        <div class="user-dropdown"
                             id="userDropdown">

                            <a href="${pageContext.request.contextPath}/user/profile">

                                <i class="fa fa-user"></i>

                                <fmt:message key="header.account"/>

                            </a>

                            <a href="${pageContext.request.contextPath}/user/orders">

                                <i class="fa fa-receipt"></i>

                                <fmt:message key="header.orders"/>

                            </a>

                            <a href="${pageContext.request.contextPath}/logout">

                                <i class="fa fa-sign-out-alt"></i>

                                <fmt:message key="header.logout"/>

                            </a>

                        </div>

                    </c:when>

                    <c:otherwise>

                        <a href="${pageContext.request.contextPath}/login">
                            <fmt:message key="header.login"/>
                        </a>

                        <span>|</span>

                        <a href="${pageContext.request.contextPath}/signup">
                            <fmt:message key="header.signup"/>
                        </a>

                    </c:otherwise>

                </c:choose>

            </div>
        </div>
    </header>
</c:if>

<c:if test="${headerMode != 'CHECKOUT_FLOW'}">
<header class="site-header">

    <div class="header-top">

        <div class="layout-shell header-top__inner">

            <div class="header-utility">

                <a href="${pageContext.request.contextPath}/home">
                    <c:out value="${not empty settings.site_name ? settings.site_name : 'INOLA Souvenir'}"/>
                </a>

                <span>|</span>

                <a href="${pageContext.request.contextPath}/admin/dashboard">
                    <fmt:message key="header.admin"/>
                </a>

            </div>


            <div class="header-account">

                <c:choose>

                    <c:when test="${not empty authUser}">

                        <div class="user-box"
                             id="userToggle">

                        <span class="user-name">
                                <fmt:message key="header.greeting"/>, ${authUser.fullName}
                        </span>

                            <i class="fa fa-caret-down"></i>

                        </div>

                        <div class="user-dropdown"
                             id="userDropdown">

                            <a href="${pageContext.request.contextPath}/user/profile">

                                <i class="fa fa-user"></i>

                                <fmt:message key="header.account"/>

                            </a>

                            <a href="${pageContext.request.contextPath}/user/orders">

                                <i class="fa fa-receipt"></i>

                                <fmt:message key="header.orders"/>

                            </a>

                            <a href="${pageContext.request.contextPath}/logout">

                                <i class="fa fa-sign-out-alt"></i>

                                <fmt:message key="header.logout"/>

                            </a>

                        </div>

                    </c:when>


                    <c:otherwise>

                        <a href="${pageContext.request.contextPath}/login">
                            <fmt:message key="header.login"/>
                        </a>

                        <span>|</span>

                        <a href="${pageContext.request.contextPath}/signup">
                            <fmt:message key="header.signup"/>
                        </a>

                    </c:otherwise>

                </c:choose>

            </div>

        </div>

    </div>


    <div class="layout-shell header-main">

        <div class="header-left">

            <button
                    id="headerMenuButton"
                    class="header-menu-button"
                    type="button"
                    aria-expanded="false"
                    aria-label="<fmt:message key='header.menu.open'/>">

                <i class="fa-solid fa-bars"></i>

            </button>


            <a href="${pageContext.request.contextPath}/home"
               class="header-logo">

                <img
                        src="${pageContext.request.contextPath}/assets/images/logo.png"
                        alt="<fmt:message key='site.name'/>">

            </a>


            <div
                    id="headerCategoryDropdown"
                    class="header-category-dropdown"
                    aria-hidden="true">

                <c:forEach
                        var="category"
                        items="${headerCategories}">

                    <a href="${pageContext.request.contextPath}/category?id=${category.id}">

                        <span class="header-category-icon">

                            <i class="fa-regular fa-square"></i>

                        </span>

                        <span class="header-category-name">

                                ${category.name}

                        </span>

                        <span class="header-category-arrow">

                            <i class="fa-solid fa-chevron-right"></i>

                        </span>

                    </a>

                </c:forEach>

            </div>

        </div>


        <form
                class="header-search"
                id="headerSearchForm"
                action="${pageContext.request.contextPath}/search"
                method="get">

            <input
                id="headerSearchInput"
                    type="search"
                    name="keyword"
                    value="${param.keyword}"
                    autocomplete="off"
                    placeholder="<fmt:message key='header.search.placeholder'/>">

            <button class="header-search-clear"
                    id="headerSearchClear"
                    type="button"
                    aria-label="<fmt:message key='header.search.clear'/>"
                    hidden>

                <i class="fa-solid fa-circle-xmark"></i>

            </button>

            <button class="header-search-submit"
                    type="submit">

                <i class="fa-solid fa-magnifying-glass"></i>

            </button>

            <div class="header-search-dropdown"
                 id="headerSearchDropdown"
                 hidden
                 aria-hidden="true">

                <div class="header-search-dropdown-head">
                    <strong><fmt:message key="header.search.recent"/></strong>
                    <button type="button"
                            id="headerSearchClearAll">
                        <fmt:message key="header.search.clear_all"/>
                    </button>
                </div>

                <div class="header-search-list"
                     id="headerSearchList"></div>

            </div>

        </form>


        <div class="header-actions">

            <div class="header-language-switcher"
                 role="group"
                 aria-label="<fmt:message key='language.label'/>">
                <span class="header-language-switcher__label">
                    <fmt:message key="language.label"/>
                </span>

                <div class="header-language-switcher__options">
                    <a class="header-language-switcher__option ${requestScope.siteLanguage == 'vi' ? 'is-active' : ''}"
                       href="${languageViUrl}">
                        <fmt:message key="language.vi"/>
                    </a>
                    <a class="header-language-switcher__option ${requestScope.siteLanguage == 'en' ? 'is-active' : ''}"
                       href="${languageEnUrl}">
                        <fmt:message key="language.en"/>
                    </a>
                </div>
            </div>

            <button class="theme-toggle-button"
                    type="button"
                    data-theme-toggle
                    aria-label="<fmt:message key='theme.toggle'/>"
                    aria-pressed="false">

                <i class="fa-solid fa-moon"
                   data-theme-icon
                   aria-hidden="true"></i>

                <span class="theme-label"
                      data-theme-label><fmt:message key="theme.dark"/></span>

            </button>

            <a class="header-icon-link"
               href="${pageContext.request.contextPath}/user/orders">

                <i class="fa-solid fa-receipt"></i>

            </a>


            <div class="header-cart-wrap">
                <button class="header-cart"
                        type="button"
                        data-cart-toggle="true"
                        aria-expanded="false"
                        aria-label="<fmt:message key='header.cart.open'/>">

                    <i class="fa-solid fa-cart-shopping"></i>

                    <span id="header-cart-count">

                        ${cartItemCount}

                    </span>

                </button>

                <div class="cart-login-popover"
                     id="cartLoginPopover"
                     hidden
                     aria-hidden="true">

                    <div class="cart-preview-content"
                         id="cartPreviewContent">

                        <c:choose>
                            <c:when test="${empty sessionScope.cart or sessionScope.cart.totalQuantity() == 0}">
                                <div class="cart-preview-empty">
                                    <div class="cart-preview-empty-art">
                                        <i class="fa-solid fa-cart-shopping"></i>
                                        <i class="fa-solid fa-star"></i>
                                    </div>

                                    <p><fmt:message key="header.cart.empty"/></p>
                                </div>
                            </c:when>

                            <c:otherwise>
                                <h3><fmt:message key="header.cart.recent"/></h3>

                                <div class="cart-preview-list">
                                    <c:forEach items="${sessionScope.cart.items}" var="item" varStatus="status">
                                        <c:if test="${status.index < 3}">
                                            <c:url var="cartPreviewImage" value="${item.product.imageUrl}"/>
                                            <div class="cart-preview-item">
                                                <img src="${cartPreviewImage}"
                                                     alt="${item.product.name}">

                                                <div class="cart-preview-info">
                                                    <p class="cart-preview-name">${item.product.name}</p>
                                                    <p class="cart-preview-price">
                                                        ${item.quantity} x
                                                        <fmt:formatNumber value="${item.price}" groupingUsed="true"/> ₫
                                                    </p>
                                                </div>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </div>

                                <div class="cart-preview-footer">
                                    <span>
                                        <fmt:message key="header.cart.count">
                                            <fmt:param value="${sessionScope.cart.totalQuantity()}"/>
                                        </fmt:message>
                                    </span>

                                    <a href="${pageContext.request.contextPath}/cart">
                                        <fmt:message key="header.cart.view"/>
                                    </a>
                                </div>
                            </c:otherwise>
                        </c:choose>

                    </div>

                </div>
            </div>

        </div>

    </div>


    <c:choose>

        <c:when test="${headerMode == 'MENU_BAR'}">

            <nav class="header-menu-bar">

                <div class="layout-shell header-menu-bar__inner">

                    <c:forEach
                            var="category"
                            items="${headerTopCategories}">

                        <a href="#Loai${category.id}">
                                ${category.name}

                        </a>

                    </c:forEach>

                </div>

            </nav>

        </c:when>


        <c:when test="${headerMode == 'BREADCRUMB'}">

            <div class="header-breadcrumb">

                <div class="layout-shell header-breadcrumb__inner">

                    <a href="${pageContext.request.contextPath}/home">

                        <fmt:message key="header.breadcrumb.home"/>

                    </a>

                    <c:if test="${not empty breadcrumbLabel}">

                        <span>/</span>

                        <span class="current">

                                ${breadcrumbLabel}
                        </span>

                    </c:if>


                    <c:if test="${not empty breadcrumbCategory}">

                        <span>/</span>

                        <a href="${pageContext.request.contextPath}/category?id=${breadcrumbCategory.id}">

                                ${breadcrumbCategory.categoryName}

                        </a>

                    </c:if>


                    <c:if test="${not empty breadcrumbProduct}">

                        <span>/</span>

                        <span class="current">

                                ${breadcrumbProduct.name}
                        </span>

                    </c:if>

                </div>

            </div>

        </c:when>

    </c:choose>

</header>


<div id="headerOverlay"
     class="header-overlay">
</div>
</c:if>
