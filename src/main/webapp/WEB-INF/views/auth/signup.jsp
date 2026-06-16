<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - INOLA</title>
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
            <strong>Tạo tài khoản INOLA</strong>
            <span>Lưu thông tin mua hàng, theo dõi đơn và nhận gợi ý sản phẩm phù hợp.</span>
        </div>
    </aside>

    <section class="auth-panel">
        <a class="brand-link" href="${pageContext.request.contextPath}/home" aria-label="Về trang chủ">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="INOLA">
        </a>

        <div class="auth-copy">
            <p class="eyebrow">Thành viên mới</p>
            <h1>Đăng ký</h1>
            <p class="supporting-text">Xác thực email trước khi hoàn tất thông tin tài khoản.</p>
        </div>

        <div class="step-indicator" aria-label="Tiến trình đăng ký">
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
                <small>Thông tin</small>
            </div>
        </div>

        <div id="messageArea" class="message-area"></div>

        <form class="signup-form" id="signupForm">
            <div class="form-step active" id="step1Content">
                <label class="field">
                    <span>Địa chỉ email</span>
                    <span class="input-wrap">
                        <i class="fa fa-envelope"></i>
                        <input type="email" name="email" id="email" autocomplete="email" required>
                    </span>
                </label>

                <div class="loading" id="emailCheckLoading">
                    <div class="spinner"></div>
                    <span>Đang kiểm tra email...</span>
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
                    Đã có tài khoản?
                    <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                </p>
            </div>

            <div class="form-step" id="step2Content">
                <div class="verified-email-card">
                    <span>Email đăng ký</span>
                    <strong id="selectedEmail"></strong>
                </div>

                <label class="field">
                    <span>Mã xác thực</span>
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
                <div class="name-grid">
                    <label class="field">
                        <span>Họ</span>
                        <span class="input-wrap">
                            <i class="fa fa-user"></i>
                            <input type="text" name="last_name" id="last_name" autocomplete="family-name" required>
                        </span>
                    </label>

                    <label class="field">
                        <span>Tên</span>
                        <span class="input-wrap">
                            <i class="fa fa-user"></i>
                            <input type="text" name="first_name" id="first_name" autocomplete="given-name" required>
                        </span>
                    </label>
                </div>

                <label class="field">
                    <span>Số điện thoại</span>
                    <span class="input-wrap">
                        <i class="fa fa-phone"></i>
                        <input type="tel" name="phone" id="phone" pattern="[0-9]{10,20}" autocomplete="tel" required>
                    </span>
                </label>

                <fieldset class="field gender-field">
                    <legend>Giới tính</legend>
                    <div class="gender-options">
                        <label>
                            <input type="radio" name="gender" value="MALE" checked>
                            <span>Nam</span>
                        </label>
                        <label>
                            <input type="radio" name="gender" value="FEMALE">
                            <span>Nữ</span>
                        </label>
                        <label>
                            <input type="radio" name="gender" value="OTHER">
                            <span>Khác</span>
                        </label>
                    </div>
                </fieldset>

                <label class="field">
                    <span>Mật khẩu</span>
                    <span class="input-wrap password-wrap">
                        <i class="fa fa-lock"></i>
                        <input type="password" name="password" id="password" minlength="8" autocomplete="new-password" required>
                        <button type="button" class="eye-button" onclick="togglePassword('password', this)" aria-label="Hiện hoặc ẩn mật khẩu">
                            <i class="fa fa-eye"></i>
                        </button>
                    </span>
                    <%--Hiển thị password mạnh, yếu--%>
                    <span class="password-strength" id="passwordStrength"></span>
                </label>

                <label class="field">
                    <span>Xác nhận mật khẩu</span>
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
                    <span>Đang xử lý đăng ký...</span>
                </div>

                <div class="button-grid">
                    <button type="button" class="secondary-button" id="backBtn">
                        <i class="fa fa-arrow-left"></i>
                        <span>Quay lại</span>
                    </button>
                    <button type="button" class="primary-button" id="submitBtn">
                        <i class="fa fa-user-plus"></i>
                        <span>Đăng ký</span>
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
<script src="${pageContext.request.contextPath}/assets/js/auth/signup.js"></script>
</body>
</html>
