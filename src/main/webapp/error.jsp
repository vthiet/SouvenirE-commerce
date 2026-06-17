<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    request.setAttribute("pageTitleKey", "error.page.title");
    request.setAttribute("pageCss", "error-page.css");
    request.setAttribute("contentPage", "/WEB-INF/views/error-content.jsp");

    Object statusCode = request.getAttribute("jakarta.servlet.error.status_code");
    Object requestUri = request.getAttribute("jakarta.servlet.error.request_uri");
    Object errorMessage = request.getAttribute("jakarta.servlet.error.message");

    request.setAttribute("errorStatusCode", statusCode);
    request.setAttribute("errorRequestUri", requestUri);
    request.setAttribute("errorMessageText", errorMessage);
%>
<jsp:include page="/WEB-INF/layout/base.jsp"/>
