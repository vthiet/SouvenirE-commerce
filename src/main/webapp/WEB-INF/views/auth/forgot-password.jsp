<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quên mật khẩu - INOLA</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/signup.css?v=2">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
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
<main class="auth-shell signup-shell">
    <aside class="auth-media" aria-label="Sản phẩm INOLA">
        <img src="${pageContext.request.contextPath}/assets/images/products/lang-nghe-thu-cong/bo-am-chen-bach-ngoc-hoa-sen-qua-tang-cao-cap.jpg" alt="Quà tặng thủ công">
        <div class="media-caption">
            <strong>Khôi phục tài khoản INOLA</strong>
            <span>Đổi mật khẩu nhanh chóng để tiếp tục trải nghiệm mua sắm đặc sản.</span>
        </div>
    </aside>

    <section class="auth-panel">
        <a class="brand-link" href="${pageContext.request.contextPath}/home" aria-label="Về trang chủ">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="INOLA">
        </a>

        <div class="auth-copy">
            <p class="eyebrow">Quên mật khẩu</p>
            <h1>Khôi phục</h1>
            <p class="supporting-text">Nhận mã xác thực qua email để đặt lại mật khẩu của bạn.</p>
        </div>

        <div class="step-indicator" aria-label="Tiến trình khôi phục">
            <div class="step active" id="step1">
                <span>1</span>
                <small>Email</small>
            </div>
            <div class="step" id="step2">
                <span>2</span>
                <small>Xác thực</small>
            </div>
            <div class="step" id="step3">
                <span>3</span>
                <small>Mật khẩu mới</small>
            </div>
        </div>

        <div id="messageArea" class="message-area"></div>

        <form class="signup-form" id="forgotPasswordForm">
            <div class="form-step active" id="step1Content">
                <label class="field">
                    <span>Địa chỉ email tài khoản</span>
                    <span class="input-wrap">
                        <i class="fa fa-envelope"></i>
                        <input type="email" name="email" id="email" autocomplete="email" required>
                    </span>
                </label>

                <div class="loading" id="emailCheckLoading">
                    <div class="spinner"></div>
                    <span>Đang kiểm tra tài khoản...</span>
                </div>

                <c:if test="${recaptchaConfigured}">
                    <div class="captcha-field">
                        <div class="g-recaptcha" data-sitekey="${recaptchaSiteKey}"></div>
                    </div>
                </c:if>

                <button type="button" class="primary-button" id="continueBtn">
                    <span>Tiếp tục</span>
                    <i class="fa fa-arrow-right"></i>
                </button>

                <p class="switch-link">
                    Nhớ mật khẩu?
                    <a href="${pageContext.request.contextPath}/login">Đăng nhập ngay</a>
                </p>
            </div>

            <div class="form-step" id="step2Content">
                <div class="verified-email-card">
                    <span>Email khôi phục</span>
                    <strong id="selectedEmail"></strong>
                </div>

                <label class="field">
                    <span>Mã xác thực khôi phục</span>
                    <span class="otp-row">
                        <span class="input-wrap">
                            <i class="fa fa-shield-halved"></i>
                            <input type="text" name="verification_code" id="verificationCode" inputmode="numeric" maxlength="6" autocomplete="one-time-code">
                        </span>
                        <button type="button" class="secondary-button inline-button" id="sendCodeBtn">
                            <i class="fa fa-paper-plane"></i>
                            <span>Gửi mã</span>
                        </button>
                    </span>
                </label>

                <div class="loading" id="codeLoading">
                    <div class="spinner"></div>
                    <span>Đang xử lý mã xác thực...</span>
                </div>

                <div class="button-grid">
                    <button type="button" class="secondary-button" id="backToEmailBtn">
                        <i class="fa fa-arrow-left"></i>
                        <span>Quay lại</span>
                    </button>
                    <button type="button" class="primary-button" id="verifyCodeBtn">
                        <i class="fa fa-check"></i>
                        <span>Xác thực</span>
                    </button>
                </div>
            </div>

            <div class="form-step" id="step3Content">
                <label class="field">
                    <span>Mật khẩu mới</span>
                    <span class="input-wrap password-wrap">
                        <i class="fa fa-lock"></i>
                        <input type="password" name="password" id="password" minlength="8" autocomplete="new-password" required>
                        <button type="button" class="eye-button" onclick="togglePassword('password', this)" aria-label="Hiện hoặc ẩn mật khẩu">
                            <i class="fa fa-eye"></i>
                        </button>
                    </span>
                    <span class="password-strength" id="passwordStrength"></span>
                </label>

                <label class="field">
                    <span>Xác nhận mật khẩu mới</span>
                    <span class="input-wrap password-wrap">
                        <i class="fa fa-lock"></i>
                        <input type="password" name="confirm_password" id="confirm_password" minlength="8" autocomplete="new-password" required>
                        <button type="button" class="eye-button" onclick="togglePassword('confirm_password', this)" aria-label="Hiện hoặc ẩn mật khẩu xác nhận">
                            <i class="fa fa-eye"></i>
                        </button>
                    </span>
                </label>

                <div class="loading" id="submitLoading">
                    <div class="spinner"></div>
                    <span>Đang cập nhật mật khẩu...</span>
                </div>

                <div class="button-grid">
                    <button type="button" class="secondary-button" id="backBtn">
                        <i class="fa fa-arrow-left"></i>
                        <span>Quay lại</span>
                    </button>
                    <button type="button" class="primary-button" id="submitBtn">
                        <i class="fa fa-key"></i>
                        <span>Đặt lại mật khẩu</span>
                    </button>
                </div>
            </div>
        </form>
    </section>
</main>

<script>
    window.appContextPath = '${pageContext.request.contextPath}';
    window.recaptchaEnabled = ${recaptchaConfigured ? 'true' : 'false'};
</script>
<c:if test="${recaptchaConfigured}">
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
</c:if>
<script src="${pageContext.request.contextPath}/assets/js/auth/forgot-password.js"></script>
</body>
</html>
