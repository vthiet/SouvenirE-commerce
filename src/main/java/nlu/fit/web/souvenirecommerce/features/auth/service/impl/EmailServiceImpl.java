package nlu.fit.web.souvenirecommerce.features.auth.service.impl;

import jakarta.mail.MessagingException;
import nlu.fit.web.souvenirecommerce.model.enums.EmailType;
import nlu.fit.web.souvenirecommerce.features.auth.service.IEmailService;
import nlu.fit.web.souvenirecommerce.common.utils.EmailUtil;

public class EmailServiceImpl implements IEmailService {

    public void sendSignupVerificationCode(String userEmail, String otp) throws MessagingException {
        String htmlContent = "<h1>Mã xác thực đăng ký INOLA</h1>" +
                "<p>Vui lòng nhập mã xác thực sau để tiếp tục tạo tài khoản:</p>" +
                "<p style='font-size:28px;font-weight:700;letter-spacing:6px'>" + otp + "</p>" +
                "<p>Mã có hiệu lực trong 10 phút. Nếu bạn không yêu cầu đăng ký, vui lòng bỏ qua email này.</p>";

        String subject = "Mã xác thực đăng ký INOLA";
        String type = EmailType.HTML.getMimeType();
        EmailUtil.send(userEmail, subject, htmlContent, type);
    }

    public void sendActivationEmail(String userEmail, String token, String otp) {
        String activationLink = "https://yourshop.com/verify?token=" + token;

        String htmlBody = "<h1>Kích hoạt tài khoản của bạn</h1>" +
                "<p>Vui lòng chọn 1 trong 2 cách sau để kích hoạt:</p>" +
                "<ul>" +
                "<li><b>Cách 1:</b> Click vào link: <a href='" + activationLink + "'>Kích hoạt ngay</a></li>" +
                "<li><b>Cách 2:</b> Nhập mã OTP: <b>" + otp + "</b></li>" +
                "</ul>" +
                "<p>Lưu ý: Mã và Link có hiệu lực trong 15 phút. Kiểm tra thư rác nếu không thấy!</p>";

        try {
            new Thread(() -> {
                try {
                    String subject = "Xác thực tài khoản Lưu Niệm";
                    String type = EmailType.PLAIN_TEXT.getMimeType();
                    EmailUtil.send(userEmail, subject, htmlBody, type);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(String to, String subject, String content, String type) throws MessagingException {
        EmailUtil.send(to, subject, content, type);
    }
}
