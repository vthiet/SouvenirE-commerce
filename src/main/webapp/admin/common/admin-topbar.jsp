<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<nav class="navbar admin-navbar navbar-expand bg-white">
    <div class="container-fluid px-3 px-lg-4">
        <button class="sidebar-toggle" type="button" data-sidebar-toggle aria-controls="adminSidebar" aria-expanded="true" aria-label="Ẩn/hiện thanh bên">
            <span></span>
            <span></span>
            <span></span>
        </button>

        <form class="d-none d-md-flex ms-3 flex-grow-1" role="search" action="${ctx}/admin/products" method="get">
            <input class="form-control search-input" type="search" name="search" placeholder="Tìm sản phẩm, đơn hàng, báo cáo" value="${param.search}" aria-label="Tìm kiếm">
        </form>

        <div class="navbar-actions ms-auto">
            <div class="dropdown">
                <button class="profile-button dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <span class="profile-name d-none d-sm-inline">Quản trị viên</span>
                </button>
                <ul class="dropdown-menu dropdown-menu-end">
                    <li><a class="dropdown-item" href="${ctx}/admin/settings">Hồ sơ</a></li>
                    <li><a class="dropdown-item" href="${ctx}/admin/settings">Cài đặt tài khoản</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item" href="${ctx}/logout">Đăng xuất</a></li>
                </ul>
            </div>
        </div>
    </div>
</nav>
