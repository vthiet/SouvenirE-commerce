<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="statusCode" value="${errorStatusCode}" />
<c:set var="requestUri" value="${errorRequestUri}" />
<c:set var="errorMessage" value="${errorMessageText}" />
<c:set var="errorTitleKey" value="error.title.default" />
<c:set var="errorDescriptionKey" value="error.description.default" />

<c:choose>
    <c:when test="${statusCode == 404}">
        <c:set var="errorTitleKey" value="error.title.404" />
        <c:set var="errorDescriptionKey" value="error.description.404" />
    </c:when>
    <c:when test="${statusCode == 403}">
        <c:set var="errorTitleKey" value="error.title.403" />
        <c:set var="errorDescriptionKey" value="error.description.403" />
    </c:when>
    <c:when test="${statusCode == 500}">
        <c:set var="errorTitleKey" value="error.title.500" />
        <c:set var="errorDescriptionKey" value="error.description.500" />
    </c:when>
</c:choose>

<div class="error-page">
    <section class="home-hero error-page__hero" aria-label="<fmt:message key='error.page.title'/>">
        <div class="slideshow-container error-page__stage">

            <div class="error-page__content">
                <div class="error-page__eyebrow"><fmt:message key="error.eyebrow"/></div>
                <div class="error-page__status">
                    <span class="error-page__status-code">${statusCode != null ? statusCode : '404'}</span>
                    <span class="error-page__status-note"><fmt:message key="error.status_note"/></span>
                </div>

                <h1><fmt:message key="${errorTitleKey}"/></h1>
                <p class="error-page__lead"><fmt:message key="${errorDescriptionKey}"/></p>
                <p class="error-page__body">
                    <fmt:message key="error.body"/>
                </p>

                <div class="error-page__actions">
                    <a class="see-more-btn error-page__button error-page__button--primary" href="${pageContext.request.contextPath}/home">
                        <i class="bi bi-house-door-fill" aria-hidden="true"></i>
                        <fmt:message key="error.home"/>
                    </a>
                    <a class="see-more-btn see-more-btn--light error-page__button error-page__button--ghost" href="${pageContext.request.contextPath}/category">
                        <i class="bi bi-grid-3x3-gap-fill" aria-hidden="true"></i>
                        <fmt:message key="error.category"/>
                    </a>
                </div>

                <c:if test="${not empty requestUri or not empty errorMessage}">
                    <div class="error-page__meta">
                <c:if test="${not empty requestUri}">
                    <div class="error-page__meta-item">
                                <span class="error-page__meta-label"><fmt:message key="error.meta.path"/></span>
                                <span class="error-page__meta-value">${fn:escapeXml(requestUri)}</span>
                            </div>
                        </c:if>
                        <c:if test="${not empty errorMessage}">
                            <div class="error-page__meta-item">
                                <span class="error-page__meta-label"><fmt:message key="error.meta.message"/></span>
                                <span class="error-page__meta-value">${fn:escapeXml(errorMessage)}</span>
                            </div>
                        </c:if>
                    </div>
                </c:if>
            </div>
        </div>
    </section>

    <section class="home-section error-page__help">
        <div class="home-section__header">
            <h2><fmt:message key="error.quick.title"/></h2>
        </div>

        <div class="error-page__cards">
            <article class="error-page__mini-card">
                <strong><fmt:message key="error.card.explore"/></strong>
                <span><fmt:message key="error.card.explore_desc"/></span>
            </article>
            <article class="error-page__mini-card">
                <strong><fmt:message key="error.card.shopping"/></strong>
                <span><fmt:message key="error.card.shopping_desc"/></span>
            </article>
            <article class="error-page__mini-card">
                <strong><fmt:message key="error.card.check"/></strong>
                <span><fmt:message key="error.card.check_desc"/></span>
            </article>
        </div>
    </section>
</div>
