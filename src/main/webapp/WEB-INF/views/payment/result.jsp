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
    <title><fmt:message key="payment.result.title"/></title>
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
        <section class="payment-panel payment-result payment-result--${paymentStatus eq 'SUCCESS' ? 'success' : paymentStatus eq 'FAILED' ? 'failed' : 'invalid'}">
            <div class="payment-result__icon">
                <i class="fa-solid ${paymentStatus eq 'SUCCESS' ? 'fa-check' : 'fa-xmark'}" aria-hidden="true"></i>
            </div>
            <h1>
                <c:choose>
                    <c:when test="${paymentStatus eq 'SUCCESS'}"><fmt:message key="payment.result.success_title"/></c:when>
                    <c:when test="${paymentStatus eq 'FAILED'}"><fmt:message key="payment.result.pending_title"/></c:when>
                    <c:otherwise><fmt:message key="payment.result.invalid_title"/></c:otherwise>
                </c:choose>
            </h1>
            <p class="payment-result__message"><c:out value="${paymentMessage}"/></p>

            <c:if test="${not empty paymentTransaction}">
                <dl class="payment-result__details">
                    <div><dt><fmt:message key="payment.result.order_code"/></dt><dd><c:out value="${order.orderCode}"/></dd></div>
                    <div><dt><fmt:message key="payment.result.amount"/></dt><dd><fmt:formatNumber value="${paymentTransaction.amount}" groupingUsed="true"/>₫</dd></div>
                    <c:if test="${not empty paymentTransaction.bankCode}">
                        <div><dt><fmt:message key="payment.result.bank"/></dt><dd><c:out value="${paymentTransaction.bankCode}"/></dd></div>
                    </c:if>
                    <c:if test="${not empty paymentTransaction.providerTransactionRef}">
                        <div><dt><fmt:message key="payment.result.vnpay_transaction"/></dt><dd><c:out value="${paymentTransaction.providerTransactionRef}"/></dd></div>
                    </c:if>
                </dl>
            </c:if>

            <div class="payment-result__actions">
                <c:if test="${paymentStatus eq 'FAILED' and not empty order}">
                    <a class="payment-action payment-action--primary"
                       href="${pageContext.request.contextPath}/payment/vnpay-create?orderId=${order.id}">
                        <i class="fa-solid fa-rotate-right" aria-hidden="true"></i>
                        <fmt:message key="payment.result.retry"/>
                     </a>
                </c:if>
                <a class="payment-action ${paymentStatus eq 'SUCCESS' ? 'payment-action--primary' : ''}"
                   href="${pageContext.request.contextPath}/user/orders">
                    <fmt:message key="payment.result.view_orders"/>
                </a>
                <a class="payment-action" href="${pageContext.request.contextPath}/home"><fmt:message key="payment.result.continue_shopping"/></a>
            </div>
        </section>
    </main>
    <jsp:include page="/WEB-INF/layout/footer.jsp"/>
</div>
</body>
</html>
