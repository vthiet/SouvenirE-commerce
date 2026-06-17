package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.common.utils.PermissionHelper;
import nlu.fit.web.souvenirecommerce.legacy.dao.ReviewDAO;
import nlu.fit.web.souvenirecommerce.legacy.model.Review;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@WebServlet("/admin/reviews")
public class AdminReviewController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminReviewController.class);
    private ReviewDAO reviewDAO;

    @Override
    public void init() {
        reviewDAO = new ReviewDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!PermissionHelper.hasPermission(request, "review", "read")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem trang đánh giá.");
            return;
        }

        request.setAttribute("activePage", "reviews");

        String searchQuery = normalizeSearch(request.getParameter("search"));
        Integer ratingFilter = parseRating(request.getParameter("rating"));
        String sortFilter = normalizeSort(request.getParameter("sort"));

        int page = parsePage(request.getParameter("page"), 1);
        int pageSize = 12;

        int totalReviews = reviewDAO.countReviewsForAdmin(searchQuery, ratingFilter);
        int totalPages = totalReviews == 0 ? 0 : (int) Math.ceil((double) totalReviews / pageSize);
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }
        if (page < 1) {
            page = 1;
        }

        int offset = Math.max(0, (page - 1) * pageSize);
        List<Review> reviews = totalReviews == 0
                ? List.of()
                : reviewDAO.getReviewsForAdmin(searchQuery, ratingFilter, sortFilter, offset, pageSize);

        double averageRating = reviewDAO.averageRatingForAdmin(searchQuery, ratingFilter);
        int reviewedProducts = reviewDAO.countDistinctReviewedProductsForAdmin(searchQuery, ratingFilter);
        int recentReviewCount = reviewDAO.countRecentReviewsForAdmin(searchQuery, ratingFilter, LocalDateTime.now().minusDays(7));
        Map<String, Integer> ratingBreakdown = reviewDAO.countReviewsByRatingForAdmin(searchQuery, ratingFilter);
        int negativeReviewCount = ratingBreakdown.getOrDefault("1", 0) + ratingBreakdown.getOrDefault("2", 0);

        log.info("Loaded admin reviews page {} with {} reviews (search='{}', rating={}, sort={})",
                page, reviews.size(), searchQuery == null ? "" : searchQuery, ratingFilter, sortFilter);

        request.setAttribute("reviews", reviews);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalReviews", totalReviews);
        request.setAttribute("averageRating", averageRating);
        request.setAttribute("reviewedProducts", reviewedProducts);
        request.setAttribute("recentReviewCount", recentReviewCount);
        request.setAttribute("negativeReviewCount", negativeReviewCount);
        request.setAttribute("ratingBreakdown", ratingBreakdown);
        request.setAttribute("searchQuery", searchQuery);
        request.setAttribute("ratingFilter", ratingFilter);
        request.setAttribute("sortFilter", sortFilter);
        request.setAttribute("hasFilters", searchQuery != null || ratingFilter != null || !"newest".equals(sortFilter));

        request.getRequestDispatcher("/admin/reviews.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        String action = normalizeAction(request.getParameter("action"));
        User currentUser = resolveCurrentAdminUser(request);

        if ("delete".equals(action)) {
            handleDelete(request, response, currentUser);
            return;
        }

        log.warn("Unsupported admin review POST action: {}", action);
        AuditLogService.failure(
                AdminReviewController.class,
                currentUser,
                "REVIEW",
                "REVIEW_ACTION_FAILED",
                "REVIEW",
                AuditLogService.describe("action", action, "reason", "unsupported_action")
        );
        response.sendRedirect(buildRedirectUrl(request, "error=unsupported_action"));
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {

        if (!PermissionHelper.hasPermission(request, "review", "delete")) {
            log.warn("Forbidden review delete attempt by {}", currentUser == null ? "anonymous" : currentUser.getEmail());
            AuditLogService.failure(
                    AdminReviewController.class,
                    currentUser,
                    "REVIEW",
                    "REVIEW_DELETED",
                    "REVIEW",
                    AuditLogService.describe("reason", "forbidden")
            );
            response.sendRedirect(buildRedirectUrl(request, "error=forbidden"));
            return;
        }

        Long reviewId = parseLong(request.getParameter("reviewId"));
        if (reviewId == null) {
            log.warn("Invalid review id supplied for deletion: {}", request.getParameter("reviewId"));
            AuditLogService.failure(
                    AdminReviewController.class,
                    currentUser,
                    "REVIEW",
                    "REVIEW_DELETED",
                    "REVIEW",
                    AuditLogService.describe("reviewId", request.getParameter("reviewId"), "reason", "invalid_review_id")
            );
            response.sendRedirect(buildRedirectUrl(request, "error=invalid_review"));
            return;
        }

        Review review = reviewDAO.getReviewById(reviewId);
        if (review == null) {
            log.warn("Review not found for deletion: reviewId={}", reviewId);
            AuditLogService.failure(
                    AdminReviewController.class,
                    currentUser,
                    "REVIEW",
                    "REVIEW_DELETED",
                    "REVIEW",
                    AuditLogService.describe("reviewId", reviewId, "reason", "not_found")
            );
            response.sendRedirect(buildRedirectUrl(request, "error=not_found"));
            return;
        }

        boolean deleted = reviewDAO.deleteReview(reviewId);
        if (deleted) {
            log.info("Admin review deleted: reviewId={}, productId={}", reviewId, review.getProductId());
            AuditLogService.success(
                    AdminReviewController.class,
                    currentUser,
                    "REVIEW",
                    "REVIEW_DELETED",
                    "REVIEW",
                    AuditLogService.describe(
                            "reviewId", reviewId,
                            "productId", review.getProductId(),
                            "productName", review.getProductName(),
                            "userName", review.getUserName(),
                            "rating", review.getRating()
                    )
            );
            response.sendRedirect(buildRedirectUrl(request, "success=deleted"));
        } else {
            log.warn("Failed to delete review: reviewId={}", reviewId);
            AuditLogService.failure(
                    AdminReviewController.class,
                    currentUser,
                    "REVIEW",
                    "REVIEW_DELETED",
                    "REVIEW",
                    AuditLogService.describe("reviewId", reviewId, "reason", "delete_failed")
            );
            response.sendRedirect(buildRedirectUrl(request, "error=delete_failed"));
        }
    }

    private String buildRedirectUrl(HttpServletRequest request, String extraQuery) {
        StringBuilder url = new StringBuilder(request.getContextPath()).append("/admin/reviews");
        boolean hasQuery = false;

        String page = request.getParameter("page");
        if (page != null && !page.isBlank()) {
            hasQuery = appendParam(url, "page", page, hasQuery);
        }

        String search = normalizeSearch(request.getParameter("search"));
        if (search != null) {
            hasQuery = appendParam(url, "search", search, hasQuery);
        }

        Integer rating = parseRating(request.getParameter("rating"));
        if (rating != null) {
            hasQuery = appendParam(url, "rating", String.valueOf(rating), hasQuery);
        }

        String sort = normalizeSort(request.getParameter("sort"));
        if (sort != null && !sort.isBlank()) {
            hasQuery = appendParam(url, "sort", sort, hasQuery);
        }

        if (extraQuery != null && !extraQuery.isBlank()) {
            String[] parts = extraQuery.split("=", 2);
            if (parts.length == 2) {
                hasQuery = appendParam(url, parts[0], parts[1], hasQuery);
            }
        }

        return url.toString();
    }

    private boolean appendParam(StringBuilder url, String key, String value, boolean hasQuery) {
        if (!hasQuery) {
            url.append("?");
        } else {
            url.append("&");
        }
        url.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
        url.append("=");
        url.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        return true;
    }

    private String normalizeSearch(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeAction(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeSort(String value) {
        if (value == null || value.isBlank()) {
            return "newest";
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "oldest", "rating_asc", "rating_desc", "newest" -> normalized;
            default -> "newest";
        };
    }

    private Integer parseRating(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            int rating = Integer.parseInt(value.trim());
            return rating >= 1 && rating <= 5 ? rating : null;
        } catch (Exception e) {
            return null;
        }
    }

    private int parsePage(String value, int defaultValue) {
        try {
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            int page = Integer.parseInt(value.trim());
            return Math.max(page, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Long parseLong(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private User resolveCurrentAdminUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("user");
        if (user instanceof User) {
            return (User) user;
        }

        user = session.getAttribute("userInSession");
        if (user instanceof User) {
            return (User) user;
        }

        user = session.getAttribute("currentUser");
        return user instanceof User ? (User) user : null;
    }
}
