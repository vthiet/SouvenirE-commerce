<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="currentUser" value="${sessionScope.currentUser != null ? sessionScope.currentUser : sessionScope.user != null ? sessionScope.user : sessionScope.userInSession}"/>

<div class="account-heading">
    <div>
        <h1>Đổi mật khẩu</h1>
        <p>Để bảo mật tài khoản, vui lòng xác thực email trước khi thay đổi mật khẩu</p>
    </div>
</div>

<c:if test="${not empty requestScope.error}">
    <div class="profile-alert profile-alert--error" style="margin-bottom: 20px; padding: 12px; border-radius: 6px; background-color: var(--error-bg, #fde8e8); color: var(--error-text, #e02424); border: 1px solid var(--error-border, #f8b4b4);">
        <i class="fa-solid fa-circle-exclamation" style="margin-right: 8px;"></i>
        <c:out value="${requestScope.error}"/>
    </div>
</c:if>
<c:if test="${not empty requestScope.success}">
    <div class="profile-alert profile-alert--success" style="margin-bottom: 20px; padding: 12px; border-radius: 6px; background-color: var(--success-bg, #edfdfd); color: var(--success-text, #03543f); border: 1px solid var(--success-border, #bbf7d0);">
        <i class="fa-solid fa-circle-check" style="margin-right: 8px;"></i>
        <c:out value="${requestScope.success}"/>
    </div>
</c:if>

<div id="jsAlertArea" style="display: none; margin-bottom: 20px; padding: 12px; border-radius: 6px;"></div>

<section class="profile-panel" aria-labelledby="change-password-title">
    <div class="panel-header">
        <h2 id="change-password-title">Xác thực tài khoản và Cập nhật mật khẩu</h2>
    </div>

    <div class="profile-form profile-form--list">
        <!-- Bước 1: Xác thực email -->
        <div class="verification-section" style="border-bottom: 1px dashed var(--border-color, #e5e7eb); padding-bottom: 24px; margin-bottom: 24px;">
            <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: var(--text-color, #1f2937);">
                <i class="fa-solid fa-envelope-shield" style="margin-right: 8px; color: var(--primary-color, #0284c7);"></i>
                Bước 1: Xác thực địa chỉ email
            </h3>
            
            <div class="form-grid">
                <label class="field">
                    <span>Email liên kết</span>
                    <input type="email" id="userEmail" value="${currentUser.email}" disabled style="background-color: var(--bg-secondary, #f3f4f6); cursor: not-allowed;">
                </label>
                
                <label class="field" id="otpFieldContainer">
                    <span>Nhập mã OTP (6 số)</span>
                    <div style="display: flex; gap: 12px; align-items: center;">
                        <input type="text" id="otpCode" placeholder="Nhập 6 chữ số" maxlength="6" pattern="[0-9]*" inputmode="numeric" style="flex: 1; text-align: center; letter-spacing: 2px; font-weight: 600;">
                        <button type="button" class="secondary-button" id="btnSendCode" style="white-space: nowrap; min-width: 120px;">
                            <i class="fa-solid fa-paper-plane" style="margin-right: 6px;"></i>Gửi mã
                        </button>
                    </div>
                </label>
            </div>

            <div style="margin-top: 16px;" id="verificationActions">
                <button type="button" class="primary-button" id="btnVerifyCode" style="min-width: 140px;">
                    <i class="fa-solid fa-shield-check" style="margin-right: 6px;"></i>Xác thực OTP
                </button>
            </div>
            
            <div id="verifiedBadge" style="display: none; align-items: center; gap: 8px; color: #059669; font-weight: 600; padding: 8px 12px; background: #ecfdf5; border-radius: 6px; width: fit-content; margin-top: 12px;">
                <i class="fa-solid fa-circle-check"></i> Đã xác thực email thành công! Bạn có thể đổi mật khẩu bên dưới.
            </div>
        </div>

        <!-- Bước 2: Nhập mật khẩu mới -->
        <form id="changePasswordForm" action="${pageContext.request.contextPath}/user/change-password" method="post">
            <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: var(--text-color, #1f2937);">
                <i class="fa-solid fa-lock" style="margin-right: 8px; color: var(--primary-color, #0284c7);"></i>
                Bước 2: Thay đổi mật khẩu mới
            </h3>
            
            <div class="form-grid">
                <label class="field password-field-wrapper" style="position: relative;">
                    <span>Mật khẩu hiện tại</span>
                    <div style="position: relative;">
                        <input type="password" id="currentPassword" name="currentPassword" required disabled style="padding-right: 40px;">
                        <button type="button" class="password-toggle-btn" onclick="togglePasswordVisibility('currentPassword', this)" style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; color: #9ca3af;">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                </label>
                
                <label class="field password-field-wrapper" style="position: relative;">
                    <span>Mật khẩu mới</span>
                    <div style="position: relative;">
                        <input type="password" id="newPassword" name="newPassword" required disabled minlength="8" style="padding-right: 40px;">
                        <button type="button" class="password-toggle-btn" onclick="togglePasswordVisibility('newPassword', this)" style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; color: #9ca3af;">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                </label>
                
                <label class="field password-field-wrapper" style="position: relative;">
                    <span>Xác nhận mật khẩu mới</span>
                    <div style="position: relative;">
                        <input type="password" id="confirmPassword" name="confirmPassword" required disabled minlength="8" style="padding-right: 40px;">
                        <button type="button" class="password-toggle-btn" onclick="togglePasswordVisibility('confirmPassword', this)" style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; color: #9ca3af;">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                </label>
            </div>

            <div class="form-actions" style="margin-top: 24px;">
                <button class="primary-button" type="submit" id="btnSubmitChangePass" disabled>
                    <i class="fa-solid fa-key" style="margin-right: 6px;"></i>Đổi mật khẩu
                </button>
            </div>
        </form>
    </div>
</section>

<script>
    function togglePasswordVisibility(inputId, btn) {
        var input = document.getElementById(inputId);
        var icon = btn.querySelector('i');
        if (input.type === "password") {
            input.type = "text";
            icon.classList.remove('fa-eye');
            icon.classList.add('fa-eye-slash');
        } else {
            input.type = "password";
            icon.classList.remove('fa-eye-slash');
            icon.classList.add('fa-eye');
        }
    }
</script>
