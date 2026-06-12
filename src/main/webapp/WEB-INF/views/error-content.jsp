<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="statusCode" value="${errorStatusCode}" />
<c:set var="requestUri" value="${errorRequestUri}" />
<c:set var="errorMessage" value="${errorMessageText}" />
<c:set var="errorTitle" value="Đã xảy ra lỗi" />
<c:set var="errorDescription" value="Trang bạn đang tìm kiếm tạm thời không thể hiển thị." />

<c:choose>
    <c:when test="${statusCode == 404}">
        <c:set var="errorTitle" value="Không tìm thấy trang" />
        <c:set var="errorDescription" value="Liên kết này có thể đã bị đổi địa chỉ hoặc đã bị xóa." />
    </c:when>
    <c:when test="${statusCode == 403}">
        <c:set var="errorTitle" value="Bạn không có quyền truy cập" />
        <c:set var="errorDescription" value="Tài khoản hiện tại chưa được cấp quyền cho trang này." />
    </c:when>
    <c:when test="${statusCode == 500}">
        <c:set var="errorTitle" value="Máy chủ đang gặp sự cố" />
        <c:set var="errorDescription" value="Hệ thống đã ghi nhận lỗi và đội ngũ kỹ thuật đang kiểm tra." />
    </c:when>
</c:choose>

<div class="error-page">
    <section class="home-hero error-page__hero" aria-label="Trang lỗi">
        <div class="slideshow-container error-page__stage">

            <div class="error-page__content">
                <div class="error-page__eyebrow">Oops</div>
                <div class="error-page__status">
                    <span class="error-page__status-code">${statusCode != null ? statusCode : '404'}</span>
                    <span class="error-page__status-note">HTTP Error</span>
                </div>

                <h1>${errorTitle}</h1>
                <p class="error-page__lead">${errorDescription}</p>
                <p class="error-page__body">
                    Nếu bạn vừa bấm một đường dẫn cũ, hãy quay lại trang chủ hoặc thử xem danh mục sản phẩm.
                </p>

                <div class="error-page__actions">
                    <a class="see-more-btn error-page__button error-page__button--primary" href="${pageContext.request.contextPath}/home">
                        <i class="bi bi-house-door-fill" aria-hidden="true"></i>
                        Về trang chủ
                    </a>
                    <a class="see-more-btn see-more-btn--light error-page__button error-page__button--ghost" href="${pageContext.request.contextPath}/category">
                        <i class="bi bi-grid-3x3-gap-fill" aria-hidden="true"></i>
                        Xem danh mục
                    </a>
                </div>

                <c:if test="${not empty requestUri or not empty errorMessage}">
                    <div class="error-page__meta">
                        <c:if test="${not empty requestUri}">
                            <div class="error-page__meta-item">
                                <span class="error-page__meta-label">Đường dẫn</span>
                                <span class="error-page__meta-value">${fn:escapeXml(requestUri)}</span>
                            </div>
                        </c:if>
                        <c:if test="${not empty errorMessage}">
                            <div class="error-page__meta-item">
                                <span class="error-page__meta-label">Thông báo</span>
                                <span class="error-page__meta-value">${fn:escapeXml(errorMessage)}</span>
                            </div>
                        </c:if>
                    </div>
                </c:if>
            </div>
        </div>
    </section>

    <section class="home-section error-page__help">
        <div class="home-section__header">
            <h2>Điều hướng nhanh</h2>
        </div>

        <div class="error-page__cards">
            <article class="error-page__mini-card">
                <strong>Khám phá lại</strong>
                <span>Đi đến các danh mục đang bán chạy.</span>
            </article>
            <article class="error-page__mini-card">
                <strong>Tiếp tục mua sắm</strong>
                <span>Quay về trang chủ để xem các bộ sưu tập mới.</span>
            </article>
            <article class="error-page__mini-card">
                <strong>Kiểm tra liên kết</strong>
                <span>Đường dẫn bạn mở có thể đã thay đổi hoặc hết hạn.</span>
            </article>
        </div>
    </section>
</div>
