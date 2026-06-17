let currentStep = 1;
const totalSteps = 3;
const contextPath = window.appContextPath || '';
const recaptchaEnabled = Boolean(window.recaptchaEnabled);
const i18n = window.authI18n || {};
let verifiedEmail = '';
let resendTimer = null;
let resendSeconds = 0;

function text(key, fallback) {
    return i18n[key] || fallback;
}

function formatTemplate(template, value) {
    return String(template || '').replace('{0}', value);
}

function showMessage(message, type) {
    const messageArea = $('#messageArea');
    const classes = {
        error: 'error-text',
        success: 'success-text',
        warning: 'warning-text'
    };
    messageArea.empty().append($('<p>').addClass(classes[type] || 'warning-text').text(message));
}

function clearMessage() {
    $('#messageArea').html('');
}

function togglePassword(inputId, button) {
    const input = document.getElementById(inputId);
    const icon = button.querySelector('i');
    input.type = (input.type === 'password') ? 'text' : 'password';
    icon.classList.toggle('fa-eye');
    icon.classList.toggle('fa-eye-slash');
}

function checkPasswordStrength(password) {
    const strengthDiv = $('#passwordStrength');

    if (!password) {
        strengthDiv.text('').removeClass().addClass('password-strength');
        return;
    }

    if (password.length < 8) {
        strengthDiv.text(text('passwordMin', 'Mật khẩu cần ít nhất 8 ký tự')).removeClass().addClass('password-strength weak');
        return;
    }

    let strength = 'weak';
    let message = text('passwordWeak', 'Mật khẩu yếu');

    if (/[a-z]/.test(password) && /[A-Z]/.test(password) && /[0-9]/.test(password)) {
        strength = 'strong';
        message = text('passwordStrong', 'Mật khẩu mạnh');
    } else if (/[a-zA-Z]/.test(password) && /[0-9]/.test(password)) {
        strength = 'medium';
        message = text('passwordMedium', 'Mật khẩu trung bình');
    }

    strengthDiv.text(message).removeClass().addClass('password-strength ' + strength);
}

function goToStep(step) {
    clearMessage();
    $('.form-step').removeClass('active');
    $(`#step${step}Content`).addClass('active');

    for (let i = 1; i <= totalSteps; i++) {
        $(`#step${i}`).removeClass('active completed');
        if (i < step) {
            $(`#step${i}`).addClass('completed');
        } else if (i === step) {
            $(`#step${i}`).addClass('active');
        }
    }

    currentStep = step;
}

function setLoading(target, loading) {
    $(target.loading).toggle(loading);
    target.buttons.forEach((button) => $(button).prop('disabled', loading));
}

function startResendCountdown() {
    resendSeconds = 60;
    $('#sendCodeBtn').prop('disabled', true);
    updateSendCodeButton();

    clearInterval(resendTimer);
    resendTimer = setInterval(() => {
        resendSeconds -= 1;
        updateSendCodeButton();

        if (resendSeconds <= 0) {
            clearInterval(resendTimer);
            $('#sendCodeBtn').prop('disabled', false).find('span').text(text('sendCode', 'Gửi mã'));
        }
    }, 1000);
}

function updateSendCodeButton() {
    $('#sendCodeBtn').find('span').text(formatTemplate(text('resendCodeTemplate', 'Gửi lại {0}s'), resendSeconds));
}

function getEmail() {
    return $('#email').val().trim();
}

function validateEmail(email) {
    if (!email) {
        showMessage(text('emailRequired', 'Email không được để trống'), 'error');
        return false;
    }

    if (!/^[A-Za-z0-9+_.-]+@(.+)$/.test(email)) {
        showMessage(text('emailInvalid', 'Email không hợp lệ'), 'error');
        return false;
    }

    return true;
}

function getRecaptchaToken() {
    if (!recaptchaEnabled) {
        return '';
    }
    if (!window.grecaptcha) {
        showMessage(text('captchaLoading', 'Captcha chưa tải xong. Vui lòng thử lại sau vài giây'), 'error');
        return null;
    }
    const token = grecaptcha.getResponse();
    if (!token) {
        showMessage(text('notRobot', 'Vui lòng xác nhận bạn không phải robot.'), 'error');
        return null;
    }
    return token;
}

function resetRecaptcha() {
    if (recaptchaEnabled && window.grecaptcha) {
        grecaptcha.reset();
    }
}

function sendVerificationCode(showSuccessMessage) {
    const email = getEmail();

    if (!validateEmail(email)) {
        return;
    }

    const recaptchaToken = getRecaptchaToken();
    if (recaptchaToken === null) {
        return;
    }

    setLoading({
        loading: '#codeLoading',
        buttons: ['#sendCodeBtn', '#verifyCodeBtn', '#backToEmailBtn']
    }, true);

    $.ajax({
        url: `${contextPath}/api/signup/send-code`,
        type: 'POST',
        data: {
            email: email,
            'g-recaptcha-response': recaptchaToken
        },
        dataType: 'json',
        timeout: 15000,
        success: function(response) {
            resetRecaptcha();
            setLoading({
                loading: '#codeLoading',
                buttons: ['#verifyCodeBtn', '#backToEmailBtn']
            }, false);

            if (response.status === 'success') {
                if (showSuccessMessage) {
                    showMessage(response.message || text('codeSent', 'Mã xác thực đã được gửi tới email của bạn'), 'success');
                }
                startResendCountdown();
                $('#verificationCode').focus();
            } else {
                $('#sendCodeBtn').prop('disabled', false);
                showMessage(response.message || text('codeSendFailed', 'Không gửi được mã xác thực. Vui lòng thử lại'), 'error');
            }
        },
        error: function() {
            resetRecaptcha();
            setLoading({
                loading: '#codeLoading',
                buttons: ['#sendCodeBtn', '#verifyCodeBtn', '#backToEmailBtn']
            }, false);
            showMessage(text('codeSendFailed', 'Không gửi được mã xác thực. Vui lòng thử lại'), 'error');
        }
    });
}

$('#continueBtn').click(function() {
    const email = getEmail();

    if (!validateEmail(email)) {
        return;
    }

    $('#emailCheckLoading').show();
    $('#continueBtn').prop('disabled', true);

    $.ajax({
        url: `${contextPath}/api/check-email`,
        type: 'POST',
        data: { email: email },
        dataType: 'json',
        timeout: 5000,
        success: function(response) {
            $('#emailCheckLoading').hide();
            $('#continueBtn').prop('disabled', false);

            if (response.status === 'success') {
                verifiedEmail = email;
                $('#selectedEmail').text(email);
                $('#verificationCode').val('');
                goToStep(2);
                sendVerificationCode(true);
            } else {
                showMessage(response.message || text('genericError', 'Có lỗi xảy ra. Vui lòng thử lại'), 'error');
            }
        },
        error: function() {
            $('#emailCheckLoading').hide();
            $('#continueBtn').prop('disabled', false);
            showMessage(text('requestFailed', 'Có lỗi xảy ra. Vui lòng thử lại'), 'error');
        }
    });
});

$('#sendCodeBtn').click(function() {
    sendVerificationCode(true);
});

$('#verifyCodeBtn').click(function() {
    const email = getEmail();
    const code = $('#verificationCode').val().trim();

    if (!validateEmail(email)) {
        return;
    }

    if (!/^[0-9]{6}$/.test(code)) {
        showMessage(text('codeSixDigits', 'Mã xác thực gồm 6 chữ số'), 'error');
        return;
    }

    setLoading({
        loading: '#codeLoading',
        buttons: ['#sendCodeBtn', '#verifyCodeBtn', '#backToEmailBtn']
    }, true);

    $.ajax({
        url: `${contextPath}/api/signup/verify-code`,
        type: 'POST',
        data: {
            email: email,
            code: code
        },
        dataType: 'json',
        timeout: 5000,
        success: function(response) {
            setLoading({
                loading: '#codeLoading',
                buttons: ['#sendCodeBtn', '#verifyCodeBtn', '#backToEmailBtn']
            }, false);

            if (response.status === 'success') {
                verifiedEmail = email;
                goToStep(3);
                showMessage(response.message || text('signupVerified', 'Email đã được xác thực. Vui lòng nhập thông tin tài khoản'), 'success');
            } else {
                showMessage(response.message || text('genericError', 'Có lỗi xảy ra. Vui lòng thử lại'), 'error');
            }
        },
        error: function() {
            setLoading({
                loading: '#codeLoading',
                buttons: ['#sendCodeBtn', '#verifyCodeBtn', '#backToEmailBtn']
            }, false);
            showMessage(text('requestFailed', 'Có lỗi xảy ra. Vui lòng thử lại'), 'error');
        }
    });
});

$('#backToEmailBtn').click(function() {
    goToStep(1);
});

$('#backBtn').click(function() {
    goToStep(2);
});

$('#submitBtn').click(function() {
    const email = getEmail();
    const lastName = $('#last_name').val().trim();
    const firstName = $('#first_name').val().trim();
    const fullName = `${lastName} ${firstName}`.trim();
    const phone = $('#phone').val().trim();
    const gender = $('input[name="gender"]:checked').val();
    const password = $('#password').val();
    const confirmPassword = $('#confirm_password').val();

    if (!verifiedEmail || verifiedEmail !== email) {
        showMessage(text('emailNotVerified', 'Vui lòng xác thực email trước khi đăng ký'), 'error');
        goToStep(1);
        return;
    }

    if (!lastName) {
        showMessage(text('lastNameRequired', 'Họ không được để trống'), 'error');
        return;
    }

    if (!firstName) {
        showMessage(text('firstNameRequired', 'Tên không được để trống'), 'error');
        return;
    }

    if (!phone || !/^[0-9]{10,20}$/.test(phone)) {
        showMessage(text('phoneInvalid', 'Số điện thoại phải từ 10-20 chữ số'), 'error');
        return;
    }

    if (!gender) {
        showMessage(text('genderRequired', 'Vui lòng chọn giới tính'), 'error');
        return;
    }

    if (!password || password.length < 8) {
        showMessage(text('passwordMin', 'Mật khẩu phải ít nhất 8 ký tự'), 'error');
        return;
    }

    if (password !== confirmPassword) {
        showMessage(text('passwordMismatch', 'Mật khẩu xác nhận không trùng khớp'), 'error');
        return;
    }

    $('#submitLoading').show();
    $('#submitBtn').prop('disabled', true);

     $.ajax({
         url: `${contextPath}/api/signup`,
         type: 'POST',
         data: {
             email: email,
             firstName: firstName,
             lastName: lastName,
             phone: phone,
             gender: gender,
             password: password,
             confirmPassword: confirmPassword
         },
         dataType: 'json',
         timeout: 15000,
         success: function(response) {
             $('#submitLoading').hide();
             $('#submitBtn').prop('disabled', false);

             if (response.status === 'success') {
                 showMessage(text('signupSuccess', 'Tạo tài khoản thành công. Đang chuyển sang trang đăng nhập...'), 'success');
                 setTimeout(() => {
                     window.location.href = `${contextPath}/login`;
                 }, 1600);
             } else {
                 showMessage(response.message || text('genericError', 'Có lỗi xảy ra. Vui lòng thử lại'), 'error');
             }
         },
         error: function(xhr, status, error) {
             $('#submitLoading').hide();
             $('#submitBtn').prop('disabled', false);

             let errorMessage = text('genericError', 'Có lỗi xảy ra. Vui lòng thử lại');

             // Try to parse error response
             if (xhr.responseJSON && xhr.responseJSON.message) {
                 errorMessage = xhr.responseJSON.message;
             } else if (xhr.responseText) {
                 try {
                     const parsed = JSON.parse(xhr.responseText);
                     if (parsed && parsed.message) {
                         errorMessage = parsed.message;
                     }
                 } catch (parseError) {
                     // Keep fallback error message
                 }
             } else if (status === 'timeout') {
                 errorMessage = text('signupTimeout', 'Yêu cầu quá lâu. Vui lòng thử lại');
             } else if (error) {
                 console.error('Signup error:', error, xhr.status);
             }

             showMessage(errorMessage, 'error');
         }
     });
});

$('#password').on('keyup', function() {
    checkPasswordStrength($(this).val());
});

$('#email').on('input', function() {
    verifiedEmail = '';
});

$('#verificationCode').on('input', function() {
    this.value = this.value.replace(/\D/g, '').slice(0, 6);
});

$('#email').keypress(function(e) {
    if (e.which === 13) {
        e.preventDefault();
        $('#continueBtn').click();
    }
});

$('#verificationCode').keypress(function(e) {
    if (e.which === 13) {
        e.preventDefault();
        $('#verifyCodeBtn').click();
    }
});
