<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="currentUser" value="${sessionScope.currentUser != null ? sessionScope.currentUser : sessionScope.user != null ? sessionScope.user : sessionScope.userInSession}"/>

<div class="account-heading">
    <div>
        <h1><fmt:message key="change_password.heading"/></h1>
        <p><fmt:message key="change_password.supporting"/></p>
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

<div id="changePasswordI18n"
     hidden
     data-waiting-send="<fmt:message key='change_password.waiting_send'/>"
     data-verifying="<fmt:message key='change_password.verifying'/>"
     data-send-code-again="<fmt:message key='change_password.send_code_again'/>"
     data-send-code-again-template="<fmt:message key='change_password.send_code_again_template'/>"
     data-alert-current-required="<fmt:message key='change_password.alert.current_required'/>"
     data-alert-new-min="<fmt:message key='change_password.alert.new_min'/>"
     data-alert-confirm-mismatch="<fmt:message key='change_password.alert.confirm_mismatch'/>"
     data-alert-otp-required="<fmt:message key='change_password.alert.otp_required'/>"
     data-alert-otp-invalid="<fmt:message key='change_password.alert.otp_invalid'/>"
     data-alert-network-error="<fmt:message key='change_password.alert.network_error'/>"
     data-verify-otp="<fmt:message key='change_password.verify_otp'/>">
</div>

<section class="profile-panel" aria-labelledby="change-password-title">
    <div class="panel-header">
        <h2 id="change-password-title"><fmt:message key="change_password.panel_title"/></h2>
    </div>

    <div class="profile-form profile-form--list">
        <!-- Bước 1: Xác thực email -->
        <div class="verification-section" style="border-bottom: 1px dashed var(--border-color, #e5e7eb); padding-bottom: 24px; margin-bottom: 24px;">
            <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: var(--text-color, #1f2937);">
                <i class="fa-solid fa-envelope-shield" style="margin-right: 8px; color: var(--primary-color, #0284c7);"></i>
                <fmt:message key="change_password.step1_title"/>
            </h3>
            
            <div class="form-grid">
                <label class="field">
                    <span><fmt:message key="change_password.linked_email"/></span>
                    <input type="email" id="userEmail" value="${currentUser.email}" disabled style="background-color: var(--bg-secondary, #f3f4f6); cursor: not-allowed;">
                </label>
                
                <label class="field" id="otpFieldContainer">
                    <span><fmt:message key="change_password.otp_label"/></span>
                    <div style="display: flex; gap: 12px; align-items: center;">
                        <input type="text" id="otpCode" placeholder="<fmt:message key='change_password.otp_placeholder'/>" maxlength="6" pattern="[0-9]*" inputmode="numeric" style="flex: 1; text-align: center; letter-spacing: 2px; font-weight: 600;">
                        <button type="button" class="secondary-button" id="btnSendCode" style="white-space: nowrap; min-width: 120px;">
                            <i class="fa-solid fa-paper-plane" style="margin-right: 6px;"></i><fmt:message key="change_password.send_code"/>
                        </button>
                    </div>
                </label>
            </div>

            <div style="margin-top: 16px;" id="verificationActions">
                <button type="button" class="primary-button" id="btnVerifyCode" style="min-width: 140px;">
                    <i class="fa-solid fa-shield-check" style="margin-right: 6px;"></i><fmt:message key="change_password.verify_otp"/>
                </button>
            </div>
            
            <div id="verifiedBadge" style="display: none; align-items: center; gap: 8px; color: #059669; font-weight: 600; padding: 8px 12px; background: #ecfdf5; border-radius: 6px; width: fit-content; margin-top: 12px;">
                <i class="fa-solid fa-circle-check"></i> <fmt:message key="change_password.verified"/>
            </div>
        </div>

        <!-- Bước 2: Nhập mật khẩu mới -->
        <form id="changePasswordForm" action="${pageContext.request.contextPath}/user/change-password" method="post">
            <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: var(--text-color, #1f2937);">
                <i class="fa-solid fa-lock" style="margin-right: 8px; color: var(--primary-color, #0284c7);"></i>
                <fmt:message key="change_password.step2_title"/>
            </h3>
            
            <div class="form-grid">
                <label class="field password-field-wrapper" style="position: relative;">
                    <span><fmt:message key="change_password.current_password"/></span>
                    <div style="position: relative;">
                        <input type="password" id="currentPassword" name="currentPassword" required disabled style="padding-right: 40px;">
                        <button type="button" class="password-toggle-btn" onclick="togglePasswordVisibility('currentPassword', this)" style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; color: #9ca3af;">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                </label>
                
                <label class="field password-field-wrapper" style="position: relative;">
                    <span><fmt:message key="change_password.new_password"/></span>
                    <div style="position: relative;">
                        <input type="password" id="newPassword" name="newPassword" required disabled minlength="8" style="padding-right: 40px;">
                        <button type="button" class="password-toggle-btn" onclick="togglePasswordVisibility('newPassword', this)" style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; color: #9ca3af;">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                </label>
                
                <label class="field password-field-wrapper" style="position: relative;">
                    <span><fmt:message key="change_password.confirm_password"/></span>
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
                    <i class="fa-solid fa-key" style="margin-right: 6px;"></i><fmt:message key="change_password.submit"/>
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
