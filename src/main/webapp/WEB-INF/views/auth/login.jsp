<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - INOLA</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/login.css">
    <jsp:include page="/WEB-INF/layout/dark-mode.jsp"/>
</head>
<body class="auth-page">
<button class="theme-toggle-button auth-theme-toggle"
        type="button"
        data-theme-toggle
        aria-label="Chuyển giao diện sáng tối"
        aria-pressed="false">
    <i class="fa-solid fa-moon" data-theme-icon aria-hidden="true"></i>
    <span class="theme-label" data-theme-label>Tối</span>
</button>
<main class="auth-shell">
    <section class="auth-panel">
        <a class="brand-link" href="${pageContext.request.contextPath}/home" aria-label="Về trang chủ">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="INOLA">
        </a>

        <div class="auth-copy">
            <p class="eyebrow">Tài khoản INOLA</p>
            <h1>Đăng nhập</h1>
            <p class="supporting-text">Theo dõi đơn hàng, lưu giỏ hàng và tiếp tục mua sắm các đặc sản yêu thích.</p>
        </div>

        <c:if test="${not empty error}">
            <div class="form-message error-message">
                <i class="fa fa-circle-exclamation"></i>
                <span>${error}</span>
            </div>
        </c:if>

        <c:if test="${not empty success}">
            <div class="form-message success-message">
                <i class="fa fa-circle-check"></i>
                <span>${success}</span>
            </div>
        </c:if>

        <form class="auth-form" action="${pageContext.request.contextPath}/login" method="post">
            <label class="field">
                <span>Email hoặc số điện thoại</span>
                <span class="input-wrap">
                    <i class="fa fa-envelope"></i>
                    <input type="text" name="loginDetail" autocomplete="username" required>
                </span>
            </label>

            <label class="field">
                <span>Mật khẩu</span>
                <span class="input-wrap">
                    <i class="fa fa-lock"></i>
                    <input type="password" name="password" autocomplete="current-password" required>
                </span>
            </label>

            <div class="form-row">
                <label class="remember-option">
                    <input type="checkbox" name="remember">
                    <span>Ghi nhớ đăng nhập</span>
                </label>
                <a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>
            </div>

            <c:if test="${recaptchaConfigured}">
                <div class="captcha-field">
                    <div class="g-recaptcha" data-sitekey="${recaptchaSiteKey}"></div>
                </div>
            </c:if>

            <button type="submit" class="primary-button">
                <i class="fa fa-arrow-right-to-bracket"></i>
                <span>Đăng nhập</span>
            </button>

            <a href="${googleAuthUrl}" class="secondary-button">
                <i class="fa-brands fa-google"></i>
                <span>Đăng nhập bằng Google</span>
            </a>

            <a href="${pageContext.request.contextPath}/signup" class="secondary-button">
                <i class="fa fa-user-plus"></i>
                <span>Tạo tài khoản mới</span>
            </a>
        </form>
    </section>

    <aside class="auth-media" aria-label="Sản phẩm INOLA">
        <img src="${pageContext.request.contextPath}/assets/images/products/banh-keo-dac-san/banh-it-la-gai-ngon.jpg"
             alt="Đặc sản Bình Định">
        <div class="media-caption">
            <strong>Đặc sản chọn lọc</strong>
            <span>Giỏ hàng và đơn mua của bạn được lưu theo tài khoản.</span>
        </div>
    </aside>
</main>
<c:if test="${recaptchaConfigured}">
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
</c:if>
</body>
</html>
