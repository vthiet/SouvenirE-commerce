<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt hàng thành công - INOLA</title>
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
        <section class="payment-panel payment-result payment-result--success">
            <div class="payment-result__icon">
                <i class="fa-solid fa-check" aria-hidden="true"></i>
            </div>
            <h1>Đặt hàng thành công</h1>
            <p class="payment-result__message">
                Đơn hàng đã được ghi nhận. INOLA sẽ liên hệ xác nhận và xử lý giao hàng trong thời gian sớm nhất.
            </p>
            <c:if test="${not empty orderCode}">
                <dl class="payment-result__details">
                    <div><dt>Mã đơn hàng</dt><dd><c:out value="${orderCode}"/></dd></div>
                    <div><dt>Phương thức</dt><dd>Thanh toán khi nhận hàng</dd></div>
                </dl>
            </c:if>
            <div class="payment-result__actions">
                <a class="payment-action payment-action--primary" href="${pageContext.request.contextPath}/user/orders">
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
