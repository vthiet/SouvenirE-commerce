package nlu.fit.web.souvenirecommerce.legacy.dao.impl;

import nlu.fit.web.souvenirecommerce.model.enums.Gender;
import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.Role;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import nlu.fit.web.souvenirecommerce.model.entity.UserCredential;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import nlu.fit.web.souvenirecommerce.common.utils.PasswordUtil;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class UserDAOImpl {

    public User login(String loginDetail, String password) {
        if (loginDetail == null || loginDetail.isBlank() || password == null || password.isBlank()) return null;
        String hql = """
                select distinct u from User u
                left join fetch u.credentials
                left join fetch u.roles
                where (lower(u.email) = lower(:login) or u.phone = :login) and u.isActive = true
                """;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, User.class)
                    .setParameter("login", loginDetail.trim())
                    .uniqueResultOptional()
                    .filter(u -> u.getCredentials() != null
                            && PasswordUtil.checkPassword(password, u.getCredentials().getPasswordHash()))
                    .orElse(null);
        }
    }

    public boolean register(String email, String password, String fullName, String phone) {
        return insertUser(fullName, email, password, phone);
    }

    public boolean setResetCode(String accountInfo, String code) {
        if (accountInfo == null || accountInfo.isBlank() || code == null || code.isBlank()) return false;
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            String hql = """
                    select distinct u from User u
                    left join fetch u.credentials
                    where lower(u.email) = lower(:acc) or u.phone = :acc
                    """;
            User user = session.createQuery(hql, User.class)
                    .setParameter("acc", accountInfo.trim())
                    .uniqueResult();
            if (user == null || user.getCredentials() == null) {
                tx.rollback();
                return false;
            }
            user.getCredentials().setResetCode(code);
            user.getCredentials().setResetExpiresAt(LocalDateTime.now().plusMinutes(5));
            session.merge(user.getCredentials());
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean verifyCode(String accountInfo, String code) {
        String hql = """
                select count(uc.id) from UserCredential uc
                join uc.user u
                where (lower(u.email) = lower(:acc) or u.phone = :acc)
                and uc.resetCode = :code
                and uc.resetExpiresAt > :now
                """;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("acc", accountInfo == null ? "" : accountInfo.trim())
                    .setParameter("code", code)
                    .setParameter("now", LocalDateTime.now())
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    public boolean resetPassword(String accountInfo, String newPassword) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            String hql = """
                    select uc from UserCredential uc
                    join uc.user u
                    where lower(u.email) = lower(:acc) or u.phone = :acc
                    """;
            UserCredential credential = session.createQuery(hql, UserCredential.class)
                    .setParameter("acc", accountInfo == null ? "" : accountInfo.trim())
                    .uniqueResult();
            if (credential == null) {
                tx.rollback();
                return false;
            }
            credential.setPasswordHash(PasswordUtil.hashPassword(newPassword));
            credential.setResetCode(null);
            credential.setResetExpiresAt(null);
            session.merge(credential);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean checkPassword(int userId, String rawPassword) {
        String hql = "select uc.passwordHash from UserCredential uc where uc.user.id = :userId";
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            String hash = session.createQuery(hql, String.class).setParameter("userId", (long) userId).uniqueResult();
            return hash != null && PasswordUtil.checkPassword(rawPassword, hash);
        }
    }

    public boolean updatePasswordByUserId(int userId, String newPassword) {
        return updatePassword(userId, newPassword);
    }

    public boolean updateProfile(int userId, String fullName, String phone, String gender, String dob) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, (long) userId);
            if (user == null) {
                tx.rollback();
                return false;
            }
            applyFullName(user, fullName);
            user.setPhone(normalize(phone));
            if (gender != null && !gender.isBlank()) {
                user.setGender(parseGender(gender));
            }
            if (dob != null && !dob.isBlank()) {
                user.setDateOfBirth(LocalDate.parse(dob));
            }
            user.setUpdatedAt(LocalDateTime.now());
            session.merge(user);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean addAddress(int userId, String detail, String province, String district, String ward) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, (long) userId);
            if (user == null || detail == null || detail.isBlank()) {
                tx.rollback();
                return false;
            }
            Long count = session.createQuery("select count(a.id) from Address a where a.user.id = :uid", Long.class)
                    .setParameter("uid", user.getId())
                    .uniqueResult();
            Address address = Address.builder()
                    .user(user)
                    .receiverName(user.getFullName())
                    .receiverPhone(user.getPhone())
                    .addressDetail(detail)
                    .province(province == null ? "" : province)
                    .district(district == null ? "" : district)
                    .ward(ward == null ? "" : ward)
                    .isDefault(count == null || count == 0)
                    .build();
            session.persist(address);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public Address getAddressById(int id) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Address.class, (long) id);
        }
    }

    public boolean deleteAddress(int addressId, int userId) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int updated = session.createMutationQuery("delete from Address a where a.id = :id and a.user.id = :uid")
                    .setParameter("id", (long) addressId)
                    .setParameter("uid", (long) userId)
                    .executeUpdate();
            tx.commit();
            return updated > 0;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public List<Address> getAddressesByUserId(int userId) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Address a where a.user.id = :uid order by a.isDefault desc, a.id desc", Address.class)
                    .setParameter("uid", (long) userId)
                    .getResultList();
        }
    }

    public boolean updateAddress(int addressId, int userId, String detail, String ward, String district, String province) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Address address = session.createQuery("from Address a where a.id = :id and a.user.id = :uid", Address.class)
                    .setParameter("id", (long) addressId)
                    .setParameter("uid", (long) userId)
                    .uniqueResult();
            if (address == null) {
                tx.rollback();
                return false;
            }
            address.setAddressDetail(normalize(detail));
            address.setWard(normalize(ward));
            address.setDistrict(normalize(district));
            address.setProvince(normalize(province));
            session.merge(address);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean deleteAddress(int addressId) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Address address = session.get(Address.class, (long) addressId);
            if (address == null) {
                tx.rollback();
                return false;
            }
            session.remove(address);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public void setDefaultAddress(int userId, int addressId) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createMutationQuery("update Address a set a.isDefault = false where a.user.id = :uid")
                    .setParameter("uid", (long) userId)
                    .executeUpdate();
            session.createMutationQuery("update Address a set a.isDefault = true where a.id = :id and a.user.id = :uid")
                    .setParameter("id", (long) addressId)
                    .setParameter("uid", (long) userId)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            rollback(tx);
        }
    }

    public int getTotalCustomers() {
        String hql = """
                select count(distinct u.id) from User u
                join u.roles r
                where lower(r.name) = 'customer'
                """;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(hql, Long.class).uniqueResult();
            return count == null ? 0 : count.intValue();
        }
    }

    public List<User> getAllCustomers() {
        String hql = """
                select distinct u from User u
                left join fetch u.roles r
                where lower(r.name) = 'customer'
                order by u.id desc
                """;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, User.class).getResultList();
        }
    }

    public List<User> getAllUsers() {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select distinct u from User u left join fetch u.roles order by u.id desc", User.class)
                    .getResultList();
        }
    }

    public List<User> getCustomersWithPagination(int offset, int limit) {
        String hql = """
                select distinct u from User u
                join u.roles r
                where lower(r.name) = 'customer'
                order by u.id desc
                """;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, User.class)
                    .setFirstResult(Math.max(offset, 0))
                    .setMaxResults(Math.max(limit, 1))
                    .getResultList();
        }
    }

    public User getUserById(int id) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select distinct u from User u left join fetch u.roles where u.id = :id", User.class)
                    .setParameter("id", (long) id)
                    .uniqueResult();
        }
    }

    public boolean updateUserStatus(int userId, String status) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, (long) userId);
            if (user == null) {
                tx.rollback();
                return false;
            }
            user.setActive("Active".equalsIgnoreCase(status));
            session.merge(user);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean insertUser(String fullName, String email, String password, String phone) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Long exists = session.createQuery("select count(u.id) from User u where lower(u.email) = lower(:email)", Long.class)
                    .setParameter("email", email)
                    .uniqueResult();
            if (exists != null && exists > 0) {
                tx.rollback();
                return false;
            }

            User user = new User();
            applyFullName(user, fullName);
            user.setEmail(normalize(email));
            user.setPhone(normalize(phone));
            user.setGender(Gender.OTHER);
            user.setAvatarUrl("default-avatar.png");
            user.setActive(true);
            user.setRoles(new HashSet<>());
            session.persist(user);

            UserCredential credential = UserCredential.builder()
                    .user(user)
                    .passwordHash(PasswordUtil.hashPassword(password))
                    .emailVerified(true)
                    .build();
            user.setCredentials(credential);
            session.persist(credential);

            Optional<Role> customerRole = session.createQuery("from Role r where lower(r.name)= 'customer'", Role.class)
                    .uniqueResultOptional();
            customerRole.ifPresent(role -> {
                Set<Role> roles = user.getRoles() == null ? new HashSet<>() : user.getRoles();
                roles.add(role);
                user.setRoles(roles);
            });

            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean updateUser(int userId, String fullName, String email, String phone) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, (long) userId);
            if (user == null) {
                tx.rollback();
                return false;
            }
            applyFullName(user, fullName);
            user.setEmail(normalize(email));
            user.setPhone(normalize(phone));
            user.setUpdatedAt(LocalDateTime.now());
            session.merge(user);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, (long) userId);
            if (user == null) {
                tx.rollback();
                return false;
            }
            session.remove(user);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean updateAvatar(int userId, String avatar) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, (long) userId);
            if (user == null) {
                tx.rollback();
                return false;
            }
            user.setAvatarUrl(avatar);
            session.merge(user);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean updatePassword(int userId, String newPassword) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            UserCredential credential = session.createQuery("from UserCredential uc where uc.user.id = :uid", UserCredential.class)
                    .setParameter("uid", (long) userId)
                    .uniqueResult();
            if (credential == null) {
                tx.rollback();
                return false;
            }
            credential.setPasswordHash(PasswordUtil.hashPassword(newPassword));
            session.merge(credential);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public int getNewCustomerCount(int days) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            Long count = session.createQuery("""
                    select count(u.id) from User u
                    join u.roles r
                    where lower(r.name) = 'customer' and u.createdAt >= :since
                    """, Long.class)
                    .setParameter("since", since)
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        }
    }

    private void rollback(Transaction tx) {
        if (tx != null) {
            try {
                tx.rollback();
            } catch (Exception ignored) {
            }
        }
    }

    private void applyFullName(User user, String fullName) {
        String value = normalize(fullName);
        if (value == null) {
            user.setFirstName("");
            user.setLastName("");
            return;
        }
        String[] parts = value.split("\\s+");
        if (parts.length == 1) {
            user.setFirstName(parts[0]);
            user.setLastName(parts[0]);
            return;
        }
        user.setLastName(parts[parts.length - 1]);
        user.setFirstName(String.join(" ", new ArrayList<>(List.of(parts)).subList(0, parts.length - 1)));
    }

    private Gender parseGender(String gender) {
        try {
            return Gender.valueOf(gender.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return Gender.OTHER;
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
