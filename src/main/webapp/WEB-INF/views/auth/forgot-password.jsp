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
    <title><fmt:message key="auth.forgot.title"/></title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/signup.css?v=3">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
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
<button class="theme-toggle-button auth-theme-toggle"
        type="button"
        data-theme-toggle
        aria-label="<fmt:message key='theme.toggle'/>"
        aria-pressed="false">
    <i class="fa-solid fa-moon" data-theme-icon aria-hidden="true"></i>
    <span class="theme-label" data-theme-label><fmt:message key="theme.dark"/></span>
</button>
<main class="auth-shell signup-shell">
    <aside class="auth-media" aria-label="<fmt:message key='auth.forgot.media_aria'/>">
        <img src="${pageContext.request.contextPath}/assets/images/products/lang-nghe-thu-cong/bo-am-chen-bach-ngoc-hoa-sen-qua-tang-cao-cap.jpg" alt="<fmt:message key='auth.forgot.media_title'/>">
        <div class="media-caption">
            <strong><fmt:message key="auth.forgot.media_title"/></strong>
            <span><fmt:message key="auth.forgot.media_text"/></span>
        </div>
    </aside>

    <section class="auth-panel">
        <a class="brand-link" href="${pageContext.request.contextPath}/home" aria-label="<fmt:message key='auth.forgot.brand_aria'/>">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="<fmt:message key='site.name'/>">
        </a>

        <div class="auth-copy">
            <p class="eyebrow"><fmt:message key="auth.forgot.eyebrow"/></p>
            <h1><fmt:message key="auth.forgot.heading"/></h1>
            <p class="supporting-text"><fmt:message key="auth.forgot.supporting"/></p>
        </div>

        <div class="step-indicator" aria-label="<fmt:message key='auth.forgot.step_indicator'/>">
            <div class="step active" id="step1">
                <span>1</span>
                <small><fmt:message key="auth.forgot.step1"/></small>
            </div>
            <div class="step" id="step2">
                <span>2</span>
                <small><fmt:message key="auth.forgot.step2"/></small>
            </div>
            <div class="step" id="step3">
                <span>3</span>
                <small><fmt:message key="auth.forgot.step3"/></small>
            </div>
        </div>

        <div id="messageArea" class="message-area"></div>

        <form class="signup-form" id="forgotPasswordForm">
            <div class="form-step active" id="step1Content">
                <label class="field">
                    <span><fmt:message key="auth.forgot.email_label"/></span>
                    <span class="input-wrap">
                        <i class="fa fa-envelope"></i>
                        <input type="email" name="email" id="email" autocomplete="email" required>
                    </span>
                </label>

                <div class="loading" id="emailCheckLoading">
                    <div class="spinner"></div>
                    <span><fmt:message key="auth.forgot.email_checking"/></span>
                </div>

                <c:if test="${recaptchaConfigured}">
                    <div class="captcha-field">
                        <div class="g-recaptcha" data-sitekey="${recaptchaSiteKey}"></div>
                    </div>
                </c:if>

                <button type="button" class="primary-button" id="continueBtn">
                    <span><fmt:message key="auth.forgot.continue"/></span>
                    <i class="fa fa-arrow-right"></i>
                </button>

                <p class="switch-link">
                    <fmt:message key="auth.forgot.remember_password"/>
                    <a href="${pageContext.request.contextPath}/login"><fmt:message key="auth.forgot.login_now"/></a>
                </p>
            </div>

            <div class="form-step" id="step2Content">
                <div class="verified-email-card">
                    <span><fmt:message key="auth.forgot.selected_email"/></span>
                    <strong id="selectedEmail"></strong>
                </div>

                <label class="field">
                    <span><fmt:message key="auth.forgot.code_label"/></span>
                    <span class="otp-row">
                        <span class="input-wrap">
                            <i class="fa fa-shield-halved"></i>
                            <input type="text" name="verification_code" id="verificationCode" inputmode="numeric" maxlength="6" autocomplete="one-time-code">
                        </span>
                        <button type="button" class="secondary-button inline-button" id="sendCodeBtn">
                            <i class="fa fa-paper-plane"></i>
                            <span><fmt:message key="auth.forgot.send_code"/></span>
                        </button>
                    </span>
                </label>

                <div class="loading" id="codeLoading">
                    <div class="spinner"></div>
                    <span><fmt:message key="auth.signup.code_processing"/></span>
                </div>

                <div class="button-grid">
                    <button type="button" class="secondary-button" id="backToEmailBtn">
                        <i class="fa fa-arrow-left"></i>
                        <span><fmt:message key="auth.forgot.back"/></span>
                    </button>
                    <button type="button" class="primary-button" id="verifyCodeBtn">
                        <i class="fa fa-check"></i>
                        <span><fmt:message key="auth.forgot.verify"/></span>
                    </button>
                </div>
            </div>

            <div class="form-step" id="step3Content">
                <label class="field">
                    <span><fmt:message key="auth.forgot.new_password"/></span>
                    <span class="input-wrap password-wrap">
                        <i class="fa fa-lock"></i>
                        <input type="password" name="password" id="password" minlength="8" autocomplete="new-password" required>
                        <button type="button" class="eye-button" onclick="togglePassword('password', this)" aria-label="<fmt:message key='auth.signup.show_password'/>">
                            <i class="fa fa-eye"></i>
                        </button>
                    </span>
                    <span class="password-strength" id="passwordStrength"></span>
                </label>

                <label class="field">
                    <span><fmt:message key="auth.forgot.confirm_new_password"/></span>
                    <span class="input-wrap password-wrap">
                        <i class="fa fa-lock"></i>
                        <input type="password" name="confirm_password" id="confirm_password" minlength="8" autocomplete="new-password" required>
                        <button type="button" class="eye-button" onclick="togglePassword('confirm_password', this)" aria-label="<fmt:message key='auth.signup.show_confirm_password'/>">
                            <i class="fa fa-eye"></i>
                        </button>
                    </span>
                </label>

                <div class="loading" id="submitLoading">
                    <div class="spinner"></div>
                    <span><fmt:message key="auth.forgot.updating"/></span>
                </div>

                <div class="button-grid">
                    <button type="button" class="secondary-button" id="backBtn">
                        <i class="fa fa-arrow-left"></i>
                        <span><fmt:message key="auth.forgot.back"/></span>
                    </button>
                    <button type="button" class="primary-button" id="submitBtn">
                        <i class="fa fa-key"></i>
                        <span><fmt:message key="auth.forgot.reset"/></span>
                    </button>
                </div>
            </div>
        </form>
    </section>
</main>

<script>
    window.appContextPath = '${pageContext.request.contextPath}';
    window.recaptchaEnabled = ${recaptchaConfigured ? 'true' : 'false'};
    window.authI18n = {
        emailRequired: '<fmt:message key="auth.js.email_required"/>',
        emailInvalid: '<fmt:message key="auth.js.email_invalid"/>',
        captchaLoading: '<fmt:message key="auth.js.captcha_loading"/>',
        notRobot: '<fmt:message key="auth.js.not_robot"/>',
        codeSent: '<fmt:message key="auth.js.code_sent"/>',
        codeSendFailed: '<fmt:message key="auth.js.code_send_failed"/>',
        requestFailed: '<fmt:message key="auth.js.request_failed"/>',
        codeSixDigits: '<fmt:message key="auth.js.code_six_digits"/>',
        forgotVerified: '<fmt:message key="auth.js.forgot_verified"/>',
        passwordResetSuccess: '<fmt:message key="auth.js.password_reset_success"/>',
        passwordResetTimeout: '<fmt:message key="auth.js.password_reset_timeout"/>',
        passwordMin: '<fmt:message key="auth.js.password_min"/>',
        passwordWeak: '<fmt:message key="auth.js.password_weak"/>',
        passwordMedium: '<fmt:message key="auth.js.password_medium"/>',
        passwordStrong: '<fmt:message key="auth.js.password_strong"/>',
        emailNotVerified: '<fmt:message key="auth.js.email_not_verified_forgot"/>',
        resendCodeTemplate: '<fmt:message key="auth.js.resend_code_template"/>',
        sendCode: '<fmt:message key="auth.js.send_code"/>',
        genericError: '<fmt:message key="auth.js.generic_error"/>'
    };
</script>
<c:if test="${recaptchaConfigured}">
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
</c:if>
<script src="${pageContext.request.contextPath}/assets/js/auth/forgot-password.js"></script>
</body>
</html>
