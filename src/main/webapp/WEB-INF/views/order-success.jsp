<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${requestScope.siteLocale}" scope="request"/>
<fmt:setBundle basename="messages" scope="request"/>
<!DOCTYPE html>
<html lang="${not empty requestScope.siteLocale ? requestScope.siteLocale.language : 'vi'}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="order.success.title"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/Base.css?v=2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout/header.css?v=12">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout/footer.css?v=2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/Payment.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <jsp:include page="/WEB-INF/layout/dark-mode.jsp"/>
</head>
<body>
<div class="page-container">
    <jsp:include page="/WEB-INF/layout/header.jsp"/>
    <main id="main-content" class="payment-page">
        <section class="payment-panel payment-result payment-result--success">
            <div class="payment-result__icon">
                <i class="fa-solid fa-check" aria-hidden="true"></i>
            </div>
            <h1><fmt:message key="order.success.heading"/></h1>
            <p class="payment-result__message"><fmt:message key="order.success.message"/></p>
            <c:if test="${not empty orderCode}">
                <dl class="payment-result__details">
                    <div><dt><fmt:message key="order.success.order_code"/></dt><dd><c:out value="${orderCode}"/></dd></div>
                    <div><dt><fmt:message key="order.success.payment_method"/></dt><dd><fmt:message key="order.success.cod"/></dd></div>
                </dl>
            </c:if>
            <div class="payment-result__actions">
                <a class="payment-action payment-action--primary" href="${pageContext.request.contextPath}/user/orders">
                    <fmt:message key="order.success.view_orders"/>
                </a>
                <a class="payment-action" href="${pageContext.request.contextPath}/home"><fmt:message key="order.success.continue_shopping"/></a>
            </div>
        </section>
    </main>
    <jsp:include page="/WEB-INF/layout/footer.jsp"/>
</div>
</body>
</html>
