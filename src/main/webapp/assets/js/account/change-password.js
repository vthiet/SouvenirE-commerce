$(document).ready(function () {
    const contextPath = $('meta[name="context-path"]').attr('content') || '';
    const changePasswordI18n = document.getElementById('changePasswordI18n')?.dataset || {};
    const text = (key, fallback = '') => changePasswordI18n[key] || fallback;
    
    const $btnSendCode = $('#btnSendCode');
    const $btnVerifyCode = $('#btnVerifyCode');
    const $otpCode = $('#otpCode');
    const $jsAlertArea = $('#jsAlertArea');
    const $verifiedBadge = $('#verifiedBadge');
    const $otpFieldContainer = $('#otpFieldContainer');
    const $verificationActions = $('#verificationActions');
    
    const $currentPassword = $('#currentPassword');
    const $newPassword = $('#newPassword');
    const $confirmPassword = $('#confirmPassword');
    const $btnSubmitChangePass = $('#btnSubmitChangePass');
    const $changePasswordForm = $('#changePasswordForm');

    let countdownInterval;

    function formatMessage(template, value) {
        return String(template || '').replace('{0}', value);
    }

    function showAlert(type, message) {
        $jsAlertArea.hide().removeClass('profile-alert--error profile-alert--success');
        
        let bgColor = '';
        let textColor = '';
        let borderColor = '';
        let icon = '';
        
        if (type === 'success') {
            bgColor = 'var(--success-bg, #edfdfd)';
            textColor = 'var(--success-text, #03543f)';
            borderColor = 'var(--success-border, #bbf7d0)';
            icon = '<i class="fa-solid fa-circle-check" style="margin-right: 8px;"></i>';
            $jsAlertArea.addClass('profile-alert--success');
        } else {
            bgColor = 'var(--error-bg, #fde8e8)';
            textColor = 'var(--error-text, #e02424)';
            borderColor = 'var(--error-border, #f8b4b4)';
            icon = '<i class="fa-solid fa-circle-exclamation" style="margin-right: 8px;"></i>';
            $jsAlertArea.addClass('profile-alert--error');
        }
        
        $jsAlertArea.css({
            'background-color': bgColor,
            'color': textColor,
            'border': '1px solid ' + borderColor,
            'display': 'block'
        }).html(icon + message);
        
        $('html, body').animate({ scrollTop: $jsAlertArea.offset().top - 100 }, 300);
    }

    function startCountdown(durationSeconds) {
        clearInterval(countdownInterval);
        let remaining = durationSeconds;
        $btnSendCode.prop('disabled', true).css('cursor', 'not-allowed');
        
        $btnSendCode.html(`<i class="fa-solid fa-paper-plane" style="margin-right: 6px;"></i>${formatMessage(text('sendCodeAgainTemplate', 'Resend in ({0}s)'), `${remaining}`)}`);
        
        countdownInterval = setInterval(function () {
            remaining--;
            if (remaining <= 0) {
                clearInterval(countdownInterval);
                $btnSendCode.prop('disabled', false).css('cursor', 'pointer');
                $btnSendCode.html(`<i class="fa-solid fa-paper-plane" style="margin-right: 6px;"></i>${text('sendCodeAgain', 'Resend code')}`);
            } else {
                $btnSendCode.html(`<i class="fa-solid fa-paper-plane" style="margin-right: 6px;"></i>${formatMessage(text('sendCodeAgainTemplate', 'Resend in ({0}s)'), `${remaining}`)}`);
            }
        }, 1000);
    }

    // Gửi mã OTP
    $btnSendCode.on('click', function () {
        showAlert('success', text('waitingSend', 'Sending a verification code to your email, please wait...'));
        $btnSendCode.prop('disabled', true);
        
        $.ajax({
            url: `${contextPath}/api/change-password/send-code`,
            type: 'POST',
            dataType: 'json',
            success: function (response) {
                if (response.status === 'success') {
                    showAlert('success', response.message);
                    startCountdown(60);
                } else {
                    showAlert('error', response.message);
                    $btnSendCode.prop('disabled', false);
                }
            },
            error: function () {
                showAlert('error', text('alertNetworkError', 'Something went wrong while verifying. Please try again.'));
                $btnSendCode.prop('disabled', false);
            }
        });
    });

    // Xác thực mã OTP
    $btnVerifyCode.on('click', function () {
        const code = $otpCode.val().trim();
        if (!code) {
            showAlert('error', text('alertOtpRequired', 'Please enter the OTP code.'));
            return;
        }
        if (!/^\d{6}$/.test(code)) {
            showAlert('error', text('alertOtpInvalid', 'The OTP code must be exactly 6 digits.'));
            return;
        }

        $btnVerifyCode.prop('disabled', true).text(text('verifying', 'Verifying...'));
        
        $.ajax({
            url: `${contextPath}/api/change-password/verify-code`,
            type: 'POST',
            data: { code: code },
            dataType: 'json',
            success: function (response) {
                $btnVerifyCode.prop('disabled', false).html(`<i class="fa-solid fa-shield-check" style="margin-right: 6px;"></i>${text('verifyOtp', 'Verify OTP')}`);
                if (response.status === 'success') {
                    showAlert('success', response.message);
                    
                    // Ẩn vùng OTP
                    $otpFieldContainer.hide();
                    $verificationActions.hide();
                    $verifiedBadge.css('display', 'flex');
                    
                    // Mở khóa các trường nhập mật khẩu
                    $currentPassword.prop('disabled', false);
                    $newPassword.prop('disabled', false);
                    $confirmPassword.prop('disabled', false);
                    $btnSubmitChangePass.prop('disabled', false);
                } else {
                    showAlert('error', response.message);
                }
            },
            error: function () {
                $btnVerifyCode.prop('disabled', false).html(`<i class="fa-solid fa-shield-check" style="margin-right: 6px;"></i>${text('verifyOtp', 'Verify OTP')}`);
                showAlert('error', text('alertNetworkError', 'Something went wrong while verifying. Please try again.'));
            }
        });
    });

    // Validate form đổi mật khẩu trước khi submit
    $changePasswordForm.on('submit', function (e) {
        const currentPass = $currentPassword.val();
        const newPass = $newPassword.val();
        const confirmPass = $confirmPassword.val();

        if (!currentPass) {
            showAlert('error', text('alertCurrentRequired', 'Please enter the current password.'));
            e.preventDefault();
            return false;
        }
        if (!newPass || newPass.length < 8) {
            showAlert('error', text('alertNewMin', 'The new password must be at least 8 characters long.'));
            e.preventDefault();
            return false;
        }
        if (newPass !== confirmPass) {
            showAlert('error', text('alertConfirmMismatch', 'The confirmation password does not match.'));
            e.preventDefault();
            return false;
        }
    });
});
