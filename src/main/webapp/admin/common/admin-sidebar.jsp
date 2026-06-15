<%@ page contentType="text/html;charset=UTF-8" language="java" import="nlu.fit.web.souvenirecommerce.common.utils.PermissionHelper" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    request.setAttribute("canViewDashboard", true);
    request.setAttribute("canViewProducts", PermissionHelper.hasAnyPermission(request, "product"));
    request.setAttribute("canViewOrders", PermissionHelper.hasAnyPermission(request, "order"));
    request.setAttribute("canUpdateOrder", PermissionHelper.hasPermission(request, "order", "update"));
    request.setAttribute("canViewCustomers", PermissionHelper.hasAnyPermission(request, "customer"));
    request.setAttribute("canViewCategories", PermissionHelper.hasAnyPermission(request, "category"));
    request.setAttribute("canViewBanners", PermissionHelper.hasAnyPermission(request, "banner"));
    request.setAttribute("canViewSettings", PermissionHelper.hasAnyPermission(request, "settings"));
    request.setAttribute("canViewRoles", PermissionHelper.hasAnyPermission(request, "role"));
    request.setAttribute("canViewLogs", true);

    request.setAttribute("canCreateProduct", PermissionHelper.hasPermission(request, "product", "create"));
    request.setAttribute("canUpdateProduct", PermissionHelper.hasPermission(request, "product", "update"));
    request.setAttribute("canDeleteProduct", PermissionHelper.hasPermission(request, "product", "delete"));
    request.setAttribute("canCreateCategory", PermissionHelper.hasPermission(request, "category", "create"));
    request.setAttribute("canUpdateCategory", PermissionHelper.hasPermission(request, "category", "update"));
    request.setAttribute("canDeleteCategory", PermissionHelper.hasPermission(request, "category", "delete"));
    request.setAttribute("canCreateBanner", PermissionHelper.hasPermission(request, "banner", "create"));
    request.setAttribute("canUpdateBanner", PermissionHelper.hasPermission(request, "banner", "update"));
    request.setAttribute("canDeleteBanner", PermissionHelper.hasPermission(request, "banner", "delete"));
    request.setAttribute("canCreateCustomer", PermissionHelper.hasPermission(request, "customer", "create"));
    request.setAttribute("canUpdateCustomer", PermissionHelper.hasPermission(request, "customer", "update"));
    request.setAttribute("canDeleteCustomer", PermissionHelper.hasPermission(request, "customer", "delete"));
    request.setAttribute("canUpdateSettings", PermissionHelper.hasPermission(request, "settings", "update"));
    request.setAttribute("canCreateRole", PermissionHelper.hasPermission(request, "role", "create"));
    request.setAttribute("canUpdateRole", PermissionHelper.hasPermission(request, "role", "update"));
    request.setAttribute("canDeleteRole", PermissionHelper.hasPermission(request, "role", "delete"));
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="currentAdminUser" value="${not empty sessionScope.user ? sessionScope.user : sessionScope.userInSession}" />
<c:set var="navActivePage" value="${not empty requestScope.activePage ? requestScope.activePage : param.activePage}" />

<aside class="admin-sidebar" id="adminSidebar" aria-label="Main navigation">
    <div class="sidebar-header">
        <a class="brand-mark" href="${ctx}/admin/dashboard" aria-label="Souvenir Admin dashboard">
            <span class="brand-icon"><i class="bi bi-grid-1x2-fill" aria-hidden="true"></i></span>
            <span class="brand-copy">
                <span class="brand-title">INOLA Admin</span>
                <span class="brand-subtitle">Souvenir E-commerce</span>
            </span>
        </a>
    </div>

    <nav class="sidebar-nav">
        <a class="nav-link ${navActivePage == 'dashboard' ? 'active' : ''}" href="${ctx}/admin/dashboard" ${navActivePage == 'dashboard' ? 'aria-current="page"' : ''}>
            <span class="nav-icon"><i class="bi bi-speedometer2" aria-hidden="true"></i></span>
            <span class="nav-text">Dashboard</span>
        </a>
        <c:if test="${canViewProducts}">
            <a class="nav-link ${navActivePage == 'products' ? 'active' : ''}" href="${ctx}/admin/products" ${navActivePage == 'products' ? 'aria-current="page"' : ''}>
                <span class="nav-icon"><i class="bi bi-box-seam" aria-hidden="true"></i></span>
                <span class="nav-text">Products</span>
            </a>
        </c:if>
        <c:if test="${canViewOrders}">
            <a class="nav-link ${navActivePage == 'orders' ? 'active' : ''}" href="${ctx}/admin/orders" ${navActivePage == 'orders' ? 'aria-current="page"' : ''}>
                <span class="nav-icon"><i class="bi bi-bag-check" aria-hidden="true"></i></span>
                <span class="nav-text">Orders</span>
            </a>
        </c:if>
        <c:if test="${canViewCustomers}">
            <a class="nav-link ${navActivePage == 'customers' ? 'active' : ''}" href="${ctx}/admin/customers" ${navActivePage == 'customers' ? 'aria-current="page"' : ''}>
                <span class="nav-icon"><i class="bi bi-people" aria-hidden="true"></i></span>
                <span class="nav-text">Customers</span>
            </a>
        </c:if>
        <c:if test="${canViewCategories}">
            <a class="nav-link ${navActivePage == 'categories' ? 'active' : ''}" href="${ctx}/admin/categories" ${navActivePage == 'categories' ? 'aria-current="page"' : ''}>
                <span class="nav-icon"><i class="bi bi-grid-3x3-gap" aria-hidden="true"></i></span>
                <span class="nav-text">Categories</span>
            </a>
        </c:if>
        <c:if test="${canViewBanners}">
            <a class="nav-link ${navActivePage == 'banners' ? 'active' : ''}" href="${ctx}/admin/banner" ${navActivePage == 'banners' ? 'aria-current="page"' : ''}>
                <span class="nav-icon"><i class="bi bi-images" aria-hidden="true"></i></span>
                <span class="nav-text">Banners</span>
            </a>
        </c:if>
        <c:if test="${canViewRoles}">
            <a class="nav-link ${navActivePage == 'roles' ? 'active' : ''}" href="${ctx}/admin/roles" ${navActivePage == 'roles' ? 'aria-current="page"' : ''}>
                <span class="nav-icon"><i class="bi bi-shield-lock" aria-hidden="true"></i></span>
                <span class="nav-text">Roles</span>
            </a>
        </c:if>
        <c:if test="${canViewSettings}">
            <a class="nav-link ${navActivePage == 'settings' ? 'active' : ''}" href="${ctx}/admin/settings" ${navActivePage == 'settings' ? 'aria-current="page"' : ''}>
                <span class="nav-icon"><i class="bi bi-gear" aria-hidden="true"></i></span>
                <span class="nav-text">Settings</span>
            </a>
        </c:if>
        <c:if test="${canViewLogs}">
            <a class="nav-link ${navActivePage == 'logs' ? 'active' : ''}" href="${ctx}/admin/logs" ${navActivePage == 'logs' ? 'aria-current="page"' : ''}>
                <span class="nav-icon"><i class="bi bi-journal-text" aria-hidden="true"></i></span>
                <span class="nav-text">Logs</span>
            </a>
        </c:if>
    </nav>
</aside>
