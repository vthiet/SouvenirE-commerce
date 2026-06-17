<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:if test="${empty reviews}">
    <p class="text-muted"><fmt:message key="review.empty"/></p>
</c:if>

<c:forEach var="r" items="${reviews}">
    <div class="review-item">

        <div class="review-avatar">
                ${fn:substring(r.userName, 0, 1)}
        </div>

        <div class="review-body">

            <div class="review-header">
                <strong class="review-user">${r.userName}</strong>
                <span class="review-date">
                    <fmt:formatDate value="${r.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                </span>
            </div>

            <div class="review-stars">
                <c:forEach begin="1" end="${r.rating}">⭐</c:forEach>
            </div>

            <p class="review-text">
                    ${r.comment}
            </p>

        </div>
    </div>
</c:forEach>
