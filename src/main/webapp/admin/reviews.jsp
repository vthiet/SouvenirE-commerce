<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" import="nlu.fit.web.souvenirecommerce.common.utils.PermissionHelper" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="common/admin-access-guard.jspf" %>
<%
    request.setAttribute("canDeleteReview", PermissionHelper.hasPermission(request, "review", "delete"));
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activePage" value="reviews" scope="request" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý đánh giá - Admin</title>
    <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-dashboard.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-pages.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-reviews.css">
</head>
<body>
<div class="admin-shell">
    <div class="sidebar-backdrop" data-sidebar-close></div>
    <jsp:include page="common/admin-sidebar.jsp"/>

    <div class="admin-main">
        <jsp:include page="common/admin-topbar.jsp"/>

        <main class="dashboard-content">
            <div class="container-fluid px-3 px-lg-4 py-4 admin-page reviews-page">
                <section class="admin-page-hero reviews-hero">
                    <div class="hero-copy">
                        <span class="page-eyebrow">Kiểm duyệt nội dung</span>
                        <h1>Quản lý đánh giá sản phẩm</h1>
                        <p class="page-lead">
                            Theo dõi phản hồi của khách hàng, lọc theo sao hoặc từ khóa, và xóa các đánh giá không phù hợp ngay từ một màn hình duy nhất.
                        </p>
                        <div class="hero-meta">
                            <div class="meta-pill">
                                <span>Tổng đánh giá</span>
                                <strong>${totalReviews}</strong>
                            </div>
                            <div class="meta-pill">
                                <span>Điểm trung bình</span>
                                <strong><fmt:formatNumber value="${averageRating}" pattern="0.0"/></strong>
                            </div>
                            <div class="meta-pill">
                                <span>7 ngày gần nhất</span>
                                <strong>${recentReviewCount}</strong>
                            </div>
                            <div class="meta-pill">
                                <span>Đánh giá thấp</span>
                                <strong>${negativeReviewCount}</strong>
                            </div>
                        </div>
                    </div>

                    <div class="hero-surface reviews-hero-surface">
                        <span class="hero-surface-label">Phân bố đánh giá</span>

                        <div class="reviews-score-block">
                            <div class="reviews-score-value">
                                <fmt:formatNumber value="${averageRating}" pattern="0.0"/>
                            </div>
                            <div class="reviews-score-copy">
                                <strong>Dựa trên ${totalReviews} đánh giá</strong>
                                <span>${reviewedProducts} sản phẩm đã nhận phản hồi</span>
                            </div>
                        </div>

                        <c:set var="rating5Count" value="${ratingBreakdown['5']}" />
                        <c:set var="rating4Count" value="${ratingBreakdown['4']}" />
                        <c:set var="rating3Count" value="${ratingBreakdown['3']}" />
                        <c:set var="rating2Count" value="${ratingBreakdown['2']}" />
                        <c:set var="rating1Count" value="${ratingBreakdown['1']}" />

                        <c:set var="rating5Width" value="${totalReviews > 0 ? rating5Count * 100.0 / totalReviews : 0}" />
                        <c:set var="rating4Width" value="${totalReviews > 0 ? rating4Count * 100.0 / totalReviews : 0}" />
                        <c:set var="rating3Width" value="${totalReviews > 0 ? rating3Count * 100.0 / totalReviews : 0}" />
                        <c:set var="rating2Width" value="${totalReviews > 0 ? rating2Count * 100.0 / totalReviews : 0}" />
                        <c:set var="rating1Width" value="${totalReviews > 0 ? rating1Count * 100.0 / totalReviews : 0}" />

                        <div class="review-distribution-list" aria-label="Thống kê theo sao">
                            <div class="review-distribution-row">
                                <span class="review-distribution-label"><i class="fas fa-star"></i> 5 sao</span>
                                <div class="review-distribution-track"><span class="is-excellent" style="width:${rating5Width}%"></span></div>
                                <strong class="review-distribution-count">${rating5Count}</strong>
                            </div>
                            <div class="review-distribution-row">
                                <span class="review-distribution-label"><i class="fas fa-star"></i> 4 sao</span>
                                <div class="review-distribution-track"><span class="is-great" style="width:${rating4Width}%"></span></div>
                                <strong class="review-distribution-count">${rating4Count}</strong>
                            </div>
                            <div class="review-distribution-row">
                                <span class="review-distribution-label"><i class="fas fa-star"></i> 3 sao</span>
                                <div class="review-distribution-track"><span class="is-neutral" style="width:${rating3Width}%"></span></div>
                                <strong class="review-distribution-count">${rating3Count}</strong>
                            </div>
                            <div class="review-distribution-row">
                                <span class="review-distribution-label"><i class="fas fa-star"></i> 2 sao</span>
                                <div class="review-distribution-track"><span class="is-warning" style="width:${rating2Width}%"></span></div>
                                <strong class="review-distribution-count">${rating2Count}</strong>
                            </div>
                            <div class="review-distribution-row">
                                <span class="review-distribution-label"><i class="fas fa-star"></i> 1 sao</span>
                                <div class="review-distribution-track"><span class="is-danger" style="width:${rating1Width}%"></span></div>
                                <strong class="review-distribution-count">${rating1Count}</strong>
                            </div>
                        </div>

                        <div class="reviews-snapshot">
                            <span class="reviews-snapshot-label">Phạm vi lọc hiện tại</span>
                            <strong>${totalReviews} đánh giá</strong>
                            <small>${reviewedProducts} sản phẩm, ${recentReviewCount} đánh giá mới trong 7 ngày</small>
                        </div>
                    </div>
                </section>

                <c:if test="${param.success == 'deleted'}">
                    <div class="alert alert-success">Đã xóa đánh giá thành công.</div>
                </c:if>
                <c:if test="${not empty param.error}">
                    <div class="alert alert-danger">
                        <c:choose>
                            <c:when test="${param.error == 'invalid_review'}">Mã đánh giá không hợp lệ.</c:when>
                            <c:when test="${param.error == 'not_found'}">Không tìm thấy đánh giá để xóa.</c:when>
                            <c:when test="${param.error == 'delete_failed'}">Xóa đánh giá thất bại. Vui lòng thử lại.</c:when>
                            <c:when test="${param.error == 'forbidden'}">Bạn không có quyền xóa đánh giá này.</c:when>
                            <c:otherwise>Có lỗi xảy ra khi xử lý yêu cầu.</c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <section class="card admin-panel-card reviews-filter-card">
                    <div class="card-header reviews-card-header">
                        <div>
                            <h3>Bộ lọc đánh giá</h3>
                            <p class="reviews-card-subtitle">
                                Tìm theo sản phẩm, khách hàng, email hoặc nội dung nhận xét. Hiện có ${totalReviews} kết quả khớp bộ lọc.
                            </p>
                        </div>

                        <c:if test="${hasFilters}">
                            <a href="${ctx}/admin/reviews" class="btn btn-outline-secondary btn-sm">
                                <i class="fas fa-rotate-left"></i> Xóa bộ lọc
                            </a>
                        </c:if>
                    </div>

                    <div class="card-body">
                        <form class="reviews-filter-form" method="get" action="${ctx}/admin/reviews">
                            <div class="row g-3 align-items-end">
                                <div class="col-12 col-lg-5">
                                    <label class="form-label" for="search">Từ khóa</label>
                                    <input type="search"
                                           class="form-control"
                                           id="search"
                                           name="search"
                                           value="${fn:escapeXml(searchQuery)}"
                                           placeholder="Tìm theo sản phẩm, khách hàng, email hoặc nội dung">
                                </div>

                                <div class="col-6 col-lg-2">
                                    <label class="form-label" for="rating">Số sao</label>
                                    <select class="form-select" id="rating" name="rating">
                                        <option value="" ${empty ratingFilter ? 'selected' : ''}>Tất cả</option>
                                        <option value="5" ${ratingFilter == 5 ? 'selected' : ''}>5 sao</option>
                                        <option value="4" ${ratingFilter == 4 ? 'selected' : ''}>4 sao</option>
                                        <option value="3" ${ratingFilter == 3 ? 'selected' : ''}>3 sao</option>
                                        <option value="2" ${ratingFilter == 2 ? 'selected' : ''}>2 sao</option>
                                        <option value="1" ${ratingFilter == 1 ? 'selected' : ''}>1 sao</option>
                                    </select>
                                </div>

                                <div class="col-6 col-lg-2">
                                    <label class="form-label" for="sort">Sắp xếp</label>
                                    <select class="form-select" id="sort" name="sort">
                                        <option value="newest" ${sortFilter == 'newest' ? 'selected' : ''}>Mới nhất</option>
                                        <option value="oldest" ${sortFilter == 'oldest' ? 'selected' : ''}>Cũ nhất</option>
                                        <option value="rating_desc" ${sortFilter == 'rating_desc' ? 'selected' : ''}>Sao cao trước</option>
                                        <option value="rating_asc" ${sortFilter == 'rating_asc' ? 'selected' : ''}>Sao thấp trước</option>
                                    </select>
                                </div>

                                <div class="col-12 col-lg-3">
                                    <button type="submit" class="btn btn-primary w-100 review-filter-submit">
                                        <i class="fas fa-filter"></i> Áp dụng bộ lọc
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </section>

                <section class="card admin-panel-card reviews-table-card">
                    <div class="card-header reviews-card-header">
                        <div>
                            <h3>Danh sách đánh giá</h3>
                            <p class="reviews-card-subtitle">Tổng số bản ghi: ${totalReviews}</p>
                        </div>

                        <span class="reviews-result-pill">
                            <i class="fas fa-comments"></i>
                            ${totalReviews} đánh giá đang hiển thị
                        </span>
                    </div>

                    <div class="table-container reviews-table-wrap">
                        <table class="data-table reviews-table">
                            <thead>
                            <tr>
                                <th>Mã</th>
                                <th>Sản phẩm</th>
                                <th>Khách hàng</th>
                                <th>Nội dung</th>
                                <th>Đánh giá</th>
                                <th>Ngày gửi</th>
                                <th>Thao tác</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:choose>
                                <c:when test="${empty reviews}">
                                    <tr>
                                        <td colspan="7" class="reviews-empty-state">
                                            <div class="reviews-empty-card">
                                                <i class="fas fa-star-half-stroke reviews-empty-icon"></i>
                                                <p>Chưa có đánh giá nào khớp bộ lọc hiện tại.</p>
                                                <span class="reviews-empty-note">Hãy thay đổi bộ lọc hoặc quay lại sau khi có khách hàng đánh giá sản phẩm.</span>
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${reviews}" var="review">
                                        <c:set var="resolvedProductImage" value="${review.productImage}" />
                                        <c:choose>
                                            <c:when test="${empty review.productImage}">
                                                <c:set var="resolvedProductImage" value="https://placehold.co/96x96?text=Review" />
                                            </c:when>
                                            <c:when test="${fn:startsWith(review.productImage, 'http://') or fn:startsWith(review.productImage, 'https://') or fn:startsWith(review.productImage, 'data:')}">
                                                <c:set var="resolvedProductImage" value="${review.productImage}" />
                                            </c:when>
                                            <c:when test="${fn:startsWith(review.productImage, ctx)}">
                                                <c:set var="resolvedProductImage" value="${review.productImage}" />
                                            </c:when>
                                            <c:otherwise>
                                                <c:set var="resolvedProductImage" value="${ctx}/${review.productImage}" />
                                            </c:otherwise>
                                        </c:choose>

                                        <tr>
                                            <td>
                                                <div class="review-id-cell">#${review.id}</div>
                                            </td>
                                            <td>
                                                <div class="review-product-cell">
                                                    <img src="${resolvedProductImage}"
                                                         alt="${review.productName}"
                                                         class="review-product-thumb"
                                                         onerror="this.src='https://placehold.co/96x96?text=Review'">
                                                    <div class="review-product-copy">
                                                        <strong><c:out value="${review.productName}"/></strong>
                                                        <span>ID ${review.productId}</span>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                <div class="review-user-cell">
                                                    <strong><c:out value="${review.userName}"/></strong>
                                                    <span><c:out value="${review.userEmail}"/></span>
                                                </div>
                                            </td>
                                            <td class="review-comment-cell">
                                                <span title="${fn:escapeXml(review.comment)}">
                                                    <c:out value="${review.comment}"/>
                                                </span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${review.rating == 5}">
                                                        <span class="review-rating-pill is-excellent"><i class="fas fa-star"></i> ${review.rating}</span>
                                                    </c:when>
                                                    <c:when test="${review.rating == 4}">
                                                        <span class="review-rating-pill is-great"><i class="fas fa-star"></i> ${review.rating}</span>
                                                    </c:when>
                                                    <c:when test="${review.rating == 3}">
                                                        <span class="review-rating-pill is-neutral"><i class="fas fa-star"></i> ${review.rating}</span>
                                                    </c:when>
                                                    <c:when test="${review.rating <= 2}">
                                                        <span class="review-rating-pill is-danger"><i class="fas fa-star"></i> ${review.rating}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="review-rating-pill is-neutral"><i class="fas fa-star"></i> ${review.rating}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div class="review-date-cell">
                                                    <fmt:formatDate value="${review.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                                </div>
                                            </td>
                                            <td>
                                                <div class="action-buttons review-action-buttons">
                                                    <c:if test="${canDeleteReview}">
                                                        <form action="${ctx}/admin/reviews" method="post" onsubmit="return confirm('Bạn có chắc muốn xóa đánh giá này?');">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="reviewId" value="${review.id}">
                                                            <input type="hidden" name="page" value="${currentPage}">
                                                            <input type="hidden" name="search" value="${searchQuery}">
                                                            <input type="hidden" name="rating" value="${ratingFilter}">
                                                            <input type="hidden" name="sort" value="${sortFilter}">
                                                            <button type="submit" class="btn-icon btn-delete" title="Xóa">
                                                                <i class="fas fa-trash"></i>
                                                            </button>
                                                        </form>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="pagination reviews-pagination">
                            <c:forEach var="pageIndex" begin="1" end="${totalPages}">
                                <c:url var="pageUrl" value="/admin/reviews">
                                    <c:param name="page" value="${pageIndex}" />
                                    <c:if test="${not empty searchQuery}">
                                        <c:param name="search" value="${searchQuery}" />
                                    </c:if>
                                    <c:if test="${not empty ratingFilter}">
                                        <c:param name="rating" value="${ratingFilter}" />
                                    </c:if>
                                    <c:if test="${not empty sortFilter}">
                                        <c:param name="sort" value="${sortFilter}" />
                                    </c:if>
                                </c:url>
                                <a class="pagination-link ${currentPage == pageIndex ? 'active' : ''}" href="${pageUrl}">
                                    ${pageIndex}
                                </a>
                            </c:forEach>
                        </div>
                    </c:if>
                </section>
            </div>
        </main>
    </div>
</div>

<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/assets/js/admin-main.js"></script>
</body>
</html>
