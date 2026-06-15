package nlu.fit.web.souvenirecommerce.legacy.dao;

import nlu.fit.web.souvenirecommerce.legacy.model.PermissionGroup;
import nlu.fit.web.souvenirecommerce.legacy.model.PermissionItem;
import nlu.fit.web.souvenirecommerce.model.entity.Permission;
import nlu.fit.web.souvenirecommerce.model.entity.Role;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthorizationDAO {

    public List<PermissionItem> getAllPermissions() {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Permission p order by p.resource, p.action", Permission.class)
                    .getResultList()
                    .stream()
                    .map(this::toPermissionItem)
                    .toList();
        }
    }

    public List<PermissionGroup> getAllRoleGroups() {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            List<Role> roles = session.createQuery("""
                    select distinct r from Role r
                    left join fetch r.permissions
                    order by r.isSystem desc, r.name asc
                    """, Role.class).getResultList();
            return roles.stream().map(this::toPermissionGroup).toList();
        }
    }

    public PermissionGroup getRoleById(Long roleId) {
        if (roleId == null) return null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            Role role = session.createQuery("""
                    select distinct r from Role r
                    left join fetch r.permissions
                    where r.id = :id
                    """, Role.class).setParameter("id", roleId).uniqueResult();
            return role == null ? null : toPermissionGroup(role);
        }
    }

    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        if (roleId == null) return List.of();
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
                    select p.id from Role r join r.permissions p
                    where r.id = :id
                    order by p.id
                    """, Long.class).setParameter("id", roleId).getResultList();
        }
    }

    public List<Long> getUserIdsByRoleId(Long roleId) {
        if (roleId == null) return List.of();
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
                    select u.id from User u join u.roles r
                    where r.id = :id
                    order by u.id
                    """, Long.class).setParameter("id", roleId).getResultList();
        }
    }

    public List<PermissionItem> getPermissionsByRoleId(Long roleId) {
        if (roleId == null) return List.of();
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
                    select p from Role r join r.permissions p
                    where r.id = :id
                    order by p.resource, p.action
                    """, Permission.class).setParameter("id", roleId).getResultList().stream()
                    .map(this::toPermissionItem).toList();
        }
    }

    public List<User> getUsersByRoleId(Long roleId) {
        if (roleId == null) return List.of();
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
                    select distinct u from User u
                    join fetch u.roles r
                    where r.id = :id
                    order by u.firstName asc, u.lastName asc
                    """, User.class).setParameter("id", roleId).getResultList();
        }
    }

    public boolean saveRole(Long roleId, String name, String description, List<Long> permissionIds) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Role role = roleId == null ? new Role() : session.get(Role.class, roleId);
            if (role == null) {
                tx.rollback();
                return false;
            }
            role.setName(name);
            role.setDescription(description);
            if (roleId == null) {
                role.setSystem(false);
            }

            Set<Permission> permissions = permissionIds == null ? new LinkedHashSet<>() : permissionIds.stream()
                    .filter(id -> id != null && id > 0)
                    .map(id -> session.get(Permission.class, id))
                    .filter(p -> p != null)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            role.setPermissions(permissions);

            if (roleId == null) {
                session.persist(role);
            } else {
                session.merge(role);
            }
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean deleteRole(Long roleId) {
        if (roleId == null) return false;
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Role role = session.get(Role.class, roleId);
            if (role == null || role.isSystem()) {
                tx.rollback();
                return false;
            }
            role.setPermissions(new LinkedHashSet<>());
            session.merge(role);
            session.remove(role);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean assignUsersToRole(Long roleId, List<Long> userIds) {
        if (roleId == null) return false;
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Role role = session.get(Role.class, roleId);
            if (role == null) {
                tx.rollback();
                return false;
            }

            List<User> currentUsers = session.createQuery("select distinct u from User u join fetch u.roles r where r.id = :rid", User.class)
                    .setParameter("rid", roleId)
                    .getResultList();
            for (User user : currentUsers) {
                if (user.getRoles() != null) {
                    user.getRoles().removeIf(r -> roleId.equals(r.getId()));
                    session.merge(user);
                }
            }

            if (userIds != null) {
                for (Long userId : userIds) {
                    if (userId == null) continue;
                    User user = session.createQuery("select distinct u from User u left join fetch u.roles where u.id = :uid", User.class)
                            .setParameter("uid", userId)
                            .uniqueResult();
                    if (user == null) continue;
                    if (user.getRoles() == null) {
                        user.setRoles(new LinkedHashSet<>());
                    }
                    user.getRoles().add(role);
                    session.merge(user);
                }
            }

            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            return false;
        }
    }

    public boolean hasPermission(long userId, String resource, String action) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("""
                    select count(p.id) from User u
                    join u.roles r
                    join r.permissions p
                    where u.id = :uid and p.resource = :res and p.action = :act
                    """, Long.class)
                    .setParameter("uid", userId)
                    .setParameter("res", resource)
                    .setParameter("act", action)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    public boolean hasAnyPermission(long userId, String... resources) {
        if (resources == null || resources.length == 0) return false;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("""
                    select count(distinct p.id) from User u
                    join u.roles r
                    join r.permissions p
                    where u.id = :uid and p.resource in :resources
                    """, Long.class)
                    .setParameter("uid", userId)
                    .setParameter("resources", List.of(resources))
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    public List<PermissionItem> getUserPermissions(long userId) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
                    select distinct p from User u
                    join u.roles r
                    join r.permissions p
                    where u.id = :uid
                    order by p.resource, p.action
                    """, Permission.class)
                    .setParameter("uid", userId)
                    .getResultList()
                    .stream()
                    .map(this::toPermissionItem)
                    .toList();
        }
    }

    public List<PermissionGroup> getUserRoles(long userId) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.createQuery("""
                    select distinct u from User u
                    left join fetch u.roles r
                    left join fetch r.permissions
                    where u.id = :uid
                    """, User.class)
                    .setParameter("uid", userId)
                    .uniqueResult();
            if (user == null || user.getRoles() == null) {
                return List.of();
            }
            return user.getRoles().stream()
                    .map(this::toPermissionGroup)
                    .toList();
        }
    }

    public Map<Long, List<Long>> getUserIdsByRoleIds(List<Long> roleIds) {
        Map<Long, List<Long>> result = new LinkedHashMap<>();
        if (roleIds == null) return result;
        for (Long roleId : roleIds) {
            result.put(roleId, getUserIdsByRoleId(roleId));
        }
        return result;
    }

    private PermissionItem toPermissionItem(Permission permission) {
        return PermissionItem.builder()
                .id(permission.getId())
                .resource(permission.getResource())
                .action(permission.getAction())
                .description(permission.getDescription())
                .build();
    }

    private PermissionGroup toPermissionGroup(Role role) {
        List<PermissionItem> permissions = role.getPermissions() == null ? new ArrayList<>() :
                role.getPermissions().stream().map(this::toPermissionItem).toList();
        List<User> users = getUsersByRoleId(role.getId());
        return PermissionGroup.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .system(role.isSystem())
                .permissions(permissions)
                .users(users)
                .userCount(users.size())
                .build();
    }

    private void rollback(Transaction tx) {
        if (tx != null) {
            try {
                tx.rollback();
            } catch (Exception ignored) {
            }
        }
    }
}
