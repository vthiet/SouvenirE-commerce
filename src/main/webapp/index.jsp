<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String lang = request.getParameter("lang");
    StringBuilder target = new StringBuilder(request.getContextPath()).append("/home");
    if (lang != null && !lang.isBlank()) {
        target.append("?lang=")
                .append(java.net.URLEncoder.encode(lang, java.nio.charset.StandardCharsets.UTF_8));
    }
    response.sendRedirect(target.toString());
%>
