<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <meta name="context-path"
          content="${pageContext.request.contextPath}">

    <title>
        <c:out value="${empty pageTitle ? 'INOLA' : pageTitle}"/>
    </title>

    <!-- ================= FAVICON ================= -->

    <link rel="icon"
          type="image/png"
          href="${pageContext.request.contextPath}/assets/images/logo.png">

    <!-- ================= GLOBAL CSS ================= -->

    <!-- Theme variables -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/theme.css?v=2">

    <!-- Base/reset/layout -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/Base.css?v=2">

    <!-- Header -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/layout/header.css?v=11">

    <!-- Footer -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/layout/footer.css?v=2">

    <!-- Font Awesome -->
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">

    <!-- Account Common CSS - shared by all account pages -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/account/account-common.css">

    <!-- ================= PAGE CSS ================= -->

    <c:if test="${not empty pageCss}">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/${pageCss}?v=16">
    </c:if>

    <c:if test="${not empty contentCss}">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/${contentCss}?v=2">
    </c:if>

    <c:if test="${requestScope.enableSelect2}">
        <link rel="stylesheet"
              href="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.13/css/select2.min.css">
    </c:if>

    <jsp:include page="/WEB-INF/layout/dark-mode.jsp"/>

</head>

<body>

<!-- ================= HEADER ================= -->

<jsp:include page="/WEB-INF/layout/header.jsp"/>

<!-- ================= MAIN CONTENT ================= -->

<main id="main-content">

    <jsp:include page="${contentPage}"/>

</main>

<!-- ================= FOOTER ================= -->

<c:if test="${headerMode != 'CHECKOUT_FLOW'}">
    <jsp:include page="/WEB-INF/layout/footer.jsp"/>
</c:if>

<!-- ================= GLOBAL JS ================= -->

<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>

<script src="${pageContext.request.contextPath}/assets/js/common.js"></script>

<script src="${pageContext.request.contextPath}/assets/js/header.js?v=10"></script>

<c:if test="${requestScope.enableSelect2}">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.13/js/select2.min.js"></script>
</c:if>

<!-- ================= PAGE JS ================= -->

<c:if test="${not empty pageJs}">
    <script src="${pageContext.request.contextPath}/assets/js/${pageJs}?v=13">
    </script>
</c:if>

</body>

</html>
