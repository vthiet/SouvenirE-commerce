package nlu.fit.web.souvenirecommerce.features.auth.repository;

import jakarta.persistence.LockModeType;
import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.model.enums.Gender;
import nlu.fit.web.souvenirecommerce.model.enums.VerificationCodePurpose;
import nlu.fit.web.souvenirecommerce.common.utils.PasswordUtil;
import nlu.fit.web.souvenirecommerce.model.entity.OAuthAccount;
import nlu.fit.web.souvenirecommerce.model.entity.Role;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import nlu.fit.web.souvenirecommerce.model.entity.UserCredential;
import nlu.fit.web.souvenirecommerce.model.entity.VerificationCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class AuthRepository extends AbsBaseRepository<Long, User> {

    public AuthRepository() {
        super(User.class);
    }

    public boolean hasEmailExist(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        Long total = getSession().createQuery("""
                        select count(u.id) from User u
                        where lower(u.email) = lower(:email)
                        """, Long.class)
                .setParameter("email", email.trim())
                .uniqueResult();
        return total != null && total > 0;
    }

    public boolean hasPhoneExist(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }

        Long total = getSession().createQuery("""
                        select count(u.id) from User u
                        where u.phone = :phone
                        """, Long.class)
                .setParameter("phone", phone.trim())
                .uniqueResult();
        return total != null && total > 0;
    }

    public Optional<User> createUser(String email, String password, String firstName, String lastName, String phone, String gender) {
        if (email == null || email.isBlank()
                || password == null || password.isBlank()
                || firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()
                || phone == null || phone.isBlank()
                || gender == null || gender.isBlank()) {
            return Optional.empty();
        }

        if (hasEmailExist(email) || hasPhoneExist(phone)) {
            return Optional.empty();
        }

        User user = User.builder()
                .email(email.trim().toLowerCase())
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .phone(phone.trim())
                .gender(Gender.valueOf(gender.trim().toUpperCase()))
                .avatarUrl("default-avatar.png")
                .isActive(true)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(resolveOrCreateCustomerRole());

        getSession().persist(user);
        getSession().flush();

        UserCredential credential = UserCredential.builder()
                .user(user)
                .passwordHash(PasswordUtil.hashPassword(password))
                .emailVerified(true)
                .build();
        user.setCredentials(credential);
        getSession().persist(credential);

        getSession().createMutationQuery("""
                        update VerificationCode vc
                        set vc.user = :user
                        where lower(vc.email) = lower(:email)
                          and vc.purpose = :purpose
                          and vc.verifiedAt is not null
                          and vc.user is null
                        """)
                .setParameter("user", user)
                .setParameter("email", user.getEmail())
                .setParameter("purpose", VerificationCodePurpose.SIGNUP)
                .executeUpdate();

        return Optional.of(user);
    }

    public Optional<User> findByUserEmailAndPassword(String userEmail, String password) {
        if (userEmail == null || userEmail.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        String loginDetail = userEmail.trim();
        List<User> users = getSession().createQuery("""
                        select distinct u from User u
                        left join fetch u.credentials
                        left join fetch u.roles r
                        left join fetch r.permissions
                        left join fetch u.oauthAccounts
                        where lower(u.email) = lower(:loginDetail)
                           or u.phone = :loginDetail
                        order by u.id desc
                        """, User.class)
                .setParameter("loginDetail", loginDetail)
                .getResultList();

        return users.stream()
                .filter(u -> u.isActive()
                        && u.getCredentials() != null
                        && PasswordUtil.checkPassword(password, u.getCredentials().getPasswordHash()))
                .findFirst();
    }

    public Optional<User> findByUserEmail(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return Optional.empty();
        }

        return getSession().createQuery("""
                        select distinct u from User u
                        left join fetch u.credentials
                        left join fetch u.roles r
                        left join fetch r.permissions
                        left join fetch u.oauthAccounts
                        where lower(u.email) = lower(:email)
                        """, User.class)
                .setParameter("email", userEmail.trim())
                .uniqueResultOptional();
    }

    public User upsertGoogleUser(
            String providerUserId,
            String email,
            String firstName,
            String lastName,
            String avatarUrl
    ) {
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("Google providerUserId is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google email is required");
        }

        User user = getSession().createQuery("""
                        select distinct u from OAuthAccount oa
                        join oa.user u
                        left join fetch u.credentials
                        left join fetch u.roles r
                        left join fetch r.permissions
                        left join fetch u.oauthAccounts
                        where lower(oa.provider) = 'google'
                          and oa.providerUserId = :providerUserId
                        """, User.class)
                .setParameter("providerUserId", providerUserId.trim())
                .uniqueResultOptional()
                .orElse(null);

        if (user == null) {
            user = findByUserEmail(email).orElse(null);
        }

        if (user == null) {
            user = User.builder()
                    .email(email.trim().toLowerCase())
                    .firstName(normalizeName(firstName, "Google"))
                    .lastName(normalizeName(lastName, "User"))
                    .phone("0000000000")
                    .gender(Gender.OTHER)
                    .avatarUrl(avatarUrl == null || avatarUrl.isBlank() ? "default-avatar.png" : avatarUrl.trim())
                    .isActive(true)
                    .roles(new HashSet<>())
                    .build();
            user.getRoles().add(resolveOrCreateCustomerRole());
            getSession().persist(user);
        }

        OAuthAccount oauthAccount = getSession().createQuery("""
                        from OAuthAccount oa
                        where lower(oa.provider) = 'google'
                          and oa.providerUserId = :providerUserId
                        """, OAuthAccount.class)
                .setParameter("providerUserId", providerUserId.trim())
                .uniqueResultOptional()
                .orElse(null);

        if (oauthAccount == null) {
            oauthAccount = OAuthAccount.builder()
                    .user(user)
                    .provider("google")
                    .providerUserId(providerUserId.trim())
                    .providerEmail(email.trim().toLowerCase())
                    .tokenExpiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            getSession().persist(oauthAccount);
        } else {
            oauthAccount.setUser(user);
            oauthAccount.setProviderEmail(email.trim().toLowerCase());
            oauthAccount.setTokenExpiresAt(LocalDateTime.now().plusHours(1));
            getSession().merge(oauthAccount);
        }

        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        return getSession().merge(user);
    }

    public void createSignupVerificationCode(String email, String code, LocalDateTime expiresAt) {
        VerificationCode verificationCode = VerificationCode.builder()
                .email(email.trim().toLowerCase())
                .code(code)
                .purpose(VerificationCodePurpose.SIGNUP)
                .expiresAt(expiresAt)
                .build();
        getSession().persist(verificationCode);
    }

    public boolean verifySignupCode(String email, String code) {
        LocalDateTime now = LocalDateTime.now();

        Optional<VerificationCode> verificationCode = getSession().createQuery("""
                        select vc from VerificationCode vc
                        where lower(vc.email) = lower(:email)
                          and vc.code = :code
                          and vc.purpose = :purpose
                          and vc.verifiedAt is null
                          and vc.expiresAt >= :now
                        order by vc.createdAt desc
                        """, VerificationCode.class)
                .setParameter("email", email.trim().toLowerCase())
                .setParameter("code", code)
                .setParameter("purpose", VerificationCodePurpose.SIGNUP)
                .setParameter("now", now)
                .setMaxResults(1)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .uniqueResultOptional();

        if (verificationCode.isEmpty()) {
            return false;
        }

        verificationCode.get().setVerifiedAt(now);
        getSession().merge(verificationCode.get());
        return true;
    }

    private Role resolveOrCreateCustomerRole() {
        return getSession().createQuery("""
                        select r from Role r
                        where lower(r.name) = lower(:name)
                        """, Role.class)
                .setParameter("name", "Customer")
                .uniqueResultOptional()
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name("Customer")
                            .description("Default customer account")
                            .isSystem(true)
                            .build();
                    getSession().persist(role);
                    return role;
                });
    }

    private String normalizeName(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
