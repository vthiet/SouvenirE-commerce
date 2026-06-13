<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kết quả thanh toán - INOLA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/Base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/Payment.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <jsp:include page="/WEB-INF/layout/dark-mode.jsp"/>
</head>
<body>
<div class="page-container">
    <jsp:include page="/views/common/header-renew.jsp"/>
    <main id="main-content" class="payment-page">
        <section class="payment-panel payment-result payment-result--${paymentStatus eq 'SUCCESS' ? 'success' : paymentStatus eq 'FAILED' ? 'failed' : 'invalid'}">
            <div class="payment-result__icon">
                <i class="fa-solid ${paymentStatus eq 'SUCCESS' ? 'fa-check' : 'fa-xmark'}" aria-hidden="true"></i>
            </div>
            <h1>
                <c:choose>
                    <c:when test="${paymentStatus eq 'SUCCESS'}">Thanh toán thành công</c:when>
                    <c:when test="${paymentStatus eq 'FAILED'}">Thanh toán chưa hoàn tất</c:when>
                    <c:otherwise>Không thể xác thực thanh toán</c:otherwise>
                </c:choose>
            </h1>
            <p class="payment-result__message"><c:out value="${paymentMessage}"/></p>

            <c:if test="${not empty paymentTransaction}">
                <dl class="payment-result__details">
                    <div><dt>Mã đơn hàng</dt><dd><c:out value="${order.orderCode}"/></dd></div>
                    <div><dt>Số tiền</dt><dd><fmt:formatNumber value="${paymentTransaction.amount}" groupingUsed="true"/>₫</dd></div>
                    <c:if test="${not empty paymentTransaction.bankCode}">
                        <div><dt>Ngân hàng</dt><dd><c:out value="${paymentTransaction.bankCode}"/></dd></div>
                    </c:if>
                    <c:if test="${not empty paymentTransaction.providerTransactionRef}">
                        <div><dt>Mã giao dịch VNPay</dt><dd><c:out value="${paymentTransaction.providerTransactionRef}"/></dd></div>
                    </c:if>
                </dl>
            </c:if>

            <div class="payment-result__actions">
                <c:if test="${paymentStatus eq 'FAILED' and not empty order}">
                    <a class="payment-action payment-action--primary"
                       href="${pageContext.request.contextPath}/payment/vnpay-create?orderId=${order.id}">
                        <i class="fa-solid fa-rotate-right" aria-hidden="true"></i>
                        Thanh toán lại
                    </a>
                </c:if>
                <a class="payment-action ${paymentStatus eq 'SUCCESS' ? 'payment-action--primary' : ''}"
                   href="${pageContext.request.contextPath}/user/orders">
                    Xem đơn hàng
                </a>
                <a class="payment-action" href="${pageContext.request.contextPath}/home">Tiếp tục mua sắm</a>
            </div>
        </section>
    </main>
    <jsp:include page="/views/common/footer.jsp"/>
</div>
</body>
</html>
