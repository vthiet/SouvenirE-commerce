<%--
  Created by IntelliJ IDEA.
  User: vthiet
  Date: 6/2/26
  Time: 6:16 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="currentUser" value="${not empty sessionScope.currentUser ? sessionScope.currentUser : sessionScope.userInSession}"/>

<aside class="account-sidebar" aria-label="<fmt:message key='account.sidebar.aria'/>">
  <div class="account-user">
    <div class="account-avatarUrl">
      <c:choose>
        <c:when test="${not empty currentUser.avatarUrl && currentUser.avatarUrl ne 'default-avatarUrl.png' && fn:startsWith(currentUser.avatarUrl, 'http')}">
          <img src="${currentUser.avatarUrl}" alt="Ảnh đại diện của ${currentUser.fullName}">
        </c:when>
        <c:when test="${not empty currentUser.avatarUrl && currentUser.avatarUrl ne 'default-avatarUrl.png'}">
          <img src="${pageContext.request.contextPath}/assets/images/avatarUrl/${currentUser.avatarUrl}"
               alt="Ảnh đại diện của ${currentUser.fullName}">
        </c:when>
        <c:otherwise>
          <span aria-hidden="true"><i class="fa-solid fa-user"></i></span>
        </c:otherwise>
      </c:choose>
    </div>

    <div class="account-user__text">
      <strong><c:out value="${currentUser.fullName}"/></strong>
      <span><c:out value="${currentUser.email}"/></span>
    </div>

    <%--Change avatarUrl--%>
    <form class="avatarUrl-form"
          action="${pageContext.request.contextPath}/user/upload-avatar"
          method="post"
          enctype="multipart/form-data">
      <label class="avatarUrl-file">
        <span><fmt:message key="account.sidebar.choose_image"/></span>
        <input type="file" name="avatarFile" accept="image/*" required>
      </label>
      <button class="avatarUrl-button" type="submit">
        <span><fmt:message key="account.sidebar.upload"/></span>
      </button>
    </form>
  </div>

  <nav class="account-nav" aria-label="<fmt:message key='account.sidebar.aria'/>">
    <a href="${pageContext.request.contextPath}/user/profile"
       class="${requestScope.accountActive eq 'profile' or requestScope.pageTitle eq 'Hồ sơ' ? 'is-active' : ''}">
      <i class="fa-solid fa-user"></i>
      <span><fmt:message key="account.sidebar.profile"/></span>
    </a>
    <a href="${pageContext.request.contextPath}/user/orders"
       class="${requestScope.accountActive eq 'orders' or requestScope.pageTitle eq 'Đơn hàng' or requestScope.pageTitle eq 'Chi tiết đơn hàng' ? 'is-active' : ''}">
      <i class="fa-solid fa-receipt"></i>
      <span><fmt:message key="account.sidebar.orders"/></span>
    </a>
    <a href="${pageContext.request.contextPath}/user/address"
       class="${requestScope.accountActive eq 'address' or requestScope.pageTitle eq 'Địa chỉ' ? 'is-active' : ''}">
      <i class="fa-solid fa-location-dot"></i>
      <span><fmt:message key="account.sidebar.address"/></span>
    </a>
    <a href="${pageContext.request.contextPath}/user/change-password"
       class="${requestScope.accountActive eq 'change-password' or requestScope.pageTitle eq 'Đổi mật khẩu' ? 'is-active' : ''}">
      <i class="fa-solid fa-key"></i>
      <span><fmt:message key="account.sidebar.change_password"/></span>
    </a>
    <a href="${pageContext.request.contextPath}/logout">
      <i class="fa-solid fa-arrow-right-from-bracket"></i>
      <span><fmt:message key="account.sidebar.logout"/></span>
    </a>
  </nav>
</aside>
