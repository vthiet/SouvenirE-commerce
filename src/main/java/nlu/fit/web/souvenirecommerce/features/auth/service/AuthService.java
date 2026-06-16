package nlu.fit.web.souvenirecommerce.features.auth.service;

import jakarta.mail.MessagingException;
import nlu.fit.web.souvenirecommerce.features.auth.dto.GooglePojo;
import nlu.fit.web.souvenirecommerce.features.auth.dto.GithubPojo;
import nlu.fit.web.souvenirecommerce.features.auth.dto.FacebookPojo;
import nlu.fit.web.souvenirecommerce.features.auth.repository.AuthRepository;
import nlu.fit.web.souvenirecommerce.features.auth.service.impl.EmailServiceImpl;
import nlu.fit.web.souvenirecommerce.features.auth.util.GoogleUtils;
import nlu.fit.web.souvenirecommerce.features.auth.util.GithubUtils;
import nlu.fit.web.souvenirecommerce.features.auth.util.FacebookUtils;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthRepository authRepository;
    private final EmailServiceImpl emailService;

    public AuthService() {
        this(new AuthRepository(), new EmailServiceImpl());
    }

    public AuthService(AuthRepository authRepository) {
        this(authRepository, new EmailServiceImpl());
    }

    public AuthService(AuthRepository authRepository, EmailServiceImpl emailService) {
        this.authRepository = authRepository;
        this.emailService = emailService;
    }

    public boolean hasEmailExist(String email) {
        return authRepository.hasEmailExist(email);
    }

    public boolean hasPhoneExist(String phone) {
        return authRepository.hasPhoneExist(phone);
    }

    public User loginWithUserCredential(String email, String password) {
        return authRepository.findByUserEmailAndPassword(email, password)
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không đúng."));
    }

    public GooglePojo processGoogleLogin(String code) throws IOException {
        if (code == null || code.isEmpty()){
            throw new IllegalArgumentException("Code is invalid");
        }

        String accessToken = GoogleUtils.getToken(code);

        GooglePojo googleUser = GoogleUtils.getUserInfo(accessToken);

        if (googleUser == null || googleUser.getId() == null) {
            throw new IllegalStateException("Google ID is null");
        }

        return googleUser;
    }

    public User loginWithGoogle(String code) throws IOException {
        GooglePojo googleUser = processGoogleLogin(code);
        return authRepository.upsertGoogleUser(
                googleUser.getId(),
                googleUser.getEmail(),
                googleUser.getGiven_name(),
                googleUser.getFamily_name(),
                googleUser.getPicture()
        );
    }

    public GithubPojo processGithubLogin(String code) throws IOException {
        if (code == null || code.isEmpty()){
            throw new IllegalArgumentException("Code is invalid");
        }

        String accessToken = GithubUtils.getToken(code);

        GithubPojo githubUser = GithubUtils.getUserInfo(accessToken);

        if (githubUser == null || githubUser.getId() == null) {
            throw new IllegalStateException("GitHub ID is null");
        }

        if (githubUser.getEmail() == null || githubUser.getEmail().isBlank()) {
            String email = GithubUtils.getEmail(accessToken);
            githubUser.setEmail(email);
        }

        return githubUser;
    }

    public User loginWithGithub(String code) throws IOException {
        GithubPojo githubUser = processGithubLogin(code);

        String name = githubUser.getName();
        String login = githubUser.getLogin();
        String firstName = "GitHub";
        String lastName = "User";
        if (name != null && !name.trim().isEmpty()) {
            String trimmed = name.trim();
            int lastSpaceIndex = trimmed.lastIndexOf(' ');
            if (lastSpaceIndex > 0) {
                firstName = trimmed.substring(0, lastSpaceIndex).trim();
                lastName = trimmed.substring(lastSpaceIndex + 1).trim();
            } else {
                firstName = trimmed;
                lastName = "User";
            }
        } else if (login != null && !login.trim().isEmpty()) {
            firstName = login.trim();
            lastName = "User";
        }

        return authRepository.upsertGithubUser(
                githubUser.getId(),
                githubUser.getEmail(),
                firstName,
                lastName,
                githubUser.getAvatarUrl()
        );
    }

    public FacebookPojo processFacebookLogin(String code) throws IOException {
        if (code == null || code.isEmpty()){
            throw new IllegalArgumentException("Code is invalid");
        }

        String accessToken = FacebookUtils.getToken(code);

        FacebookPojo facebookUser = FacebookUtils.getUserInfo(accessToken);

        if (facebookUser == null || facebookUser.getId() == null) {
            throw new IllegalStateException("Facebook ID is null");
        }

        if (facebookUser.getEmail() == null || facebookUser.getEmail().isBlank()) {
            facebookUser.setEmail(facebookUser.getId() + "@facebook.com");
        }

        return facebookUser;
    }

    public User loginWithFacebook(String code) throws IOException {
        FacebookPojo facebookUser = processFacebookLogin(code);
        return authRepository.upsertFacebookUser(
                facebookUser.getId(),
                facebookUser.getEmail(),
                facebookUser.getFirstName(),
                facebookUser.getLastName(),
                facebookUser.getAvatarUrl()
        );
    }

    public Optional<User> createUser(String email, String password, String firstName, String lastName, String phone, String gender) {
        return authRepository.createUser(email, password, firstName, lastName, phone, gender);
    }

    public LocalDateTime sendSignupVerificationCode(String email) throws MessagingException {
        if (hasEmailExist(email)) {
            throw new IllegalArgumentException("Email này đã được đăng ký");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        authRepository.createSignupVerificationCode(email, code, expiresAt);
        emailService.sendSignupVerificationCode(email, code);
        return expiresAt;
    }

    public boolean verifySignupCode(String email, String code) {
        if (hasEmailExist(email)) {
            throw new IllegalArgumentException("Email này đã được đăng ký");
        }
        return authRepository.verifySignupCode(email, code);
    }

    public LocalDateTime sendResetPasswordCode(String email) throws MessagingException {
        if (!hasEmailExist(email)) {
            throw new IllegalArgumentException("Email này chưa được đăng ký trong hệ thống");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        authRepository.createResetPasswordVerificationCode(email, code, expiresAt);
        emailService.sendResetPasswordCode(email, code);
        return expiresAt;
    }

    public boolean verifyResetPasswordCode(String email, String code) {
        if (!hasEmailExist(email)) {
            throw new IllegalArgumentException("Email này chưa được đăng ký trong hệ thống");
        }
        return authRepository.verifyResetPasswordCode(email, code);
    }

    public LocalDateTime sendChangePasswordCode(String email) throws MessagingException {
        if (!hasEmailExist(email)) {
            throw new IllegalArgumentException("Email này chưa được đăng ký trong hệ thống");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        authRepository.createChangePasswordVerificationCode(email, code, expiresAt);
        emailService.sendChangePasswordCode(email, code);
        return expiresAt;
    }

    public boolean verifyChangePasswordCode(String email, String code) {
        if (!hasEmailExist(email)) {
            throw new IllegalArgumentException("Email này chưa được đăng ký trong hệ thống");
        }
        return authRepository.verifyChangePasswordCode(email, code);
    }

    public boolean resetPassword(String email, String newPassword) {
        if (!hasEmailExist(email)) {
            throw new IllegalArgumentException("Email này chưa được đăng ký trong hệ thống");
        }
        return authRepository.resetPassword(email, newPassword);
    }
}
