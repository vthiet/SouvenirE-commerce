<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
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
<!DOCTYPE html>
<html lang="${not empty requestScope.siteLocale ? requestScope.siteLocale.language : 'vi'}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="auth.login.title"/></title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/login.css?v=4">
    <jsp:include page="/WEB-INF/layout/dark-mode.jsp"/>
</head>
<body class="auth-page">
<div id="siteI18n"
     hidden
     data-theme-light="<fmt:message key='theme.light'/>"
     data-theme-dark="<fmt:message key='theme.dark'/>"
     data-theme-toggle="<fmt:message key='theme.toggle'/>"
     data-theme-switch-to-light="<fmt:message key='theme.switch_to_light'/>"
     data-theme-switch-to-dark="<fmt:message key='theme.switch_to_dark'/>">
</div>
<div class="auth-utility-switcher">
    <div class="auth-language-switcher"
         role="group"
         aria-label="<fmt:message key='language.label'/>">
        <div class="auth-language-switcher__options">
            <a class="auth-language-switcher__option ${requestScope.siteLanguage == 'vi' ? 'is-active' : ''}"
               href="${languageViUrl}">
                <fmt:message key="language.vi"/>
            </a>
            <a class="auth-language-switcher__option ${requestScope.siteLanguage == 'en' ? 'is-active' : ''}"
               href="${languageEnUrl}">
                <fmt:message key="language.en"/>
            </a>
        </div>
    </div>
    <div class="theme-toggle-wrapper">
        <button class="theme-toggle-button auth-theme-toggle"
                type="button"
                data-theme-toggle
                aria-label="<fmt:message key='theme.toggle'/>"
                aria-pressed="false">
            <i class="fa-solid fa-moon" data-theme-icon aria-hidden="true"></i>
            <span class="theme-label" data-theme-label><fmt:message key="theme.dark"/></span>
        </button>
    </div>
</div>
<main class="auth-shell">
    <section class="auth-panel">
        <a class="brand-link" href="${pageContext.request.contextPath}/home" aria-label="<fmt:message key='auth.login.brand_aria'/>">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="<fmt:message key='site.name'/>">
        </a>

        <div class="auth-copy">
            <p class="eyebrow"><fmt:message key="auth.login.eyebrow"/></p>
            <h1><fmt:message key="auth.login.heading"/></h1>
            <p class="supporting-text"><fmt:message key="auth.login.supporting"/></p>
        </div>

        <c:if test="${not empty error}">
            <div class="form-message error-message">
                <i class="fa fa-circle-exclamation"></i>
                <span><c:out value="${error}"/></span>
            </div>
        </c:if>

        <c:if test="${not empty success}">
            <div class="form-message success-message">
                <i class="fa fa-circle-check"></i>
                <span><c:out value="${success}"/></span>
            </div>
        </c:if>

        <form class="auth-form" action="${pageContext.request.contextPath}/login" method="post">
            <label class="field">
                <span><fmt:message key="auth.login.identifier"/></span>
                <span class="input-wrap">
                    <i class="fa fa-envelope"></i>
                    <input type="text" name="loginDetail" autocomplete="username" required>
                </span>
            </label>

            <label class="field">
                <span><fmt:message key="auth.login.password"/></span>
                <span class="input-wrap">
                    <i class="fa fa-lock"></i>
                    <input type="password" name="password" autocomplete="current-password" required>
                </span>
            </label>

            <div class="form-row">
                <label class="remember-option">
                    <input type="checkbox" name="remember">
                    <span><fmt:message key="auth.login.remember"/></span>
                </label>
                <a href="${pageContext.request.contextPath}/forgot-password"><fmt:message key="auth.login.forgot_password"/></a>
            </div>

            <c:if test="${recaptchaConfigured}">
                <div class="captcha-field">
                    <div class="g-recaptcha" data-sitekey="${recaptchaSiteKey}"></div>
                </div>
            </c:if>

            <button type="submit" class="primary-button">
                <i class="fa fa-arrow-right-to-bracket"></i>
                <span><fmt:message key="auth.login.submit"/></span>
            </button>

            <c:if test="${not empty googleAuthUrl}">
                <a href="${googleAuthUrl}" class="secondary-button">
                    <i class="fa-brands fa-google"></i>
                    <span><fmt:message key="auth.login.google"/></span>
                </a>
            </c:if>

            <c:if test="${not empty githubAuthUrl}">
                <a href="${githubAuthUrl}" class="secondary-button">
                    <i class="fa-brands fa-github"></i>
                    <span><fmt:message key="auth.login.github"/></span>
                </a>
            </c:if>

            <c:if test="${not empty facebookAuthUrl}">
                <a href="${facebookAuthUrl}" class="secondary-button">
                    <i class="fa-brands fa-facebook-f"></i>
                    <span><fmt:message key="auth.login.facebook"/></span>
                </a>
            </c:if>

            <a href="${pageContext.request.contextPath}/signup" class="secondary-button">
                <i class="fa fa-user-plus"></i>
                <span><fmt:message key="auth.login.signup_cta"/></span>
            </a>
        </form>
    </section>

    <aside class="auth-media" aria-label="<fmt:message key='auth.login.media_aria'/>">
        <img src="${pageContext.request.contextPath}/assets/images/products/banh-keo-dac-san/banh-it-la-gai-ngon.jpg"
             alt="<fmt:message key='auth.login.media_title'/>">
        <div class="media-caption">
            <strong><fmt:message key="auth.login.media_title"/></strong>
            <span><fmt:message key="auth.login.media_text"/></span>
        </div>
    </aside>
</main>
<c:if test="${recaptchaConfigured}">
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
</c:if>
</body>
</html>
