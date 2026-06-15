<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Cài đặt - Admin</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-pages.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-settings.css">
</head>
<body>
<c:set var="currentAdminUser" value="${not empty sessionScope.user ? sessionScope.user : sessionScope.userInSession}" />
<div class="admin-shell">
    <div class="sidebar-backdrop" data-sidebar-close></div>
    <jsp:include page="common/admin-sidebar.jsp"/>

    <div class="admin-main">
        <jsp:include page="common/admin-topbar.jsp"/>

        <main class="dashboard-content">
        <div class="container-fluid px-3 px-lg-4 py-4">
            <div class="content-header">
                <h1>Cài đặt</h1>
            </div>

            <c:if test="${not empty message}">
                <div class="alert alert-${messageType}">
                    <i class="fas fa-check-circle"></i> ${message}
                </div>
            </c:if>

            <div class="settings-grid">
                <!-- Profile Settings -->
                <div class="card">
                    <div class="card-header">
                        <h3>Thông tin cá nhân</h3>
                    </div>
                    <div class="settings-card-body">
                        <div class="settings-avatar-wrap">
                            <div class="profile-avatar">
                                <c:choose>
                                    <c:when test="${not empty currentAdminUser.fullName}">
                                        ${currentAdminUser.fullName.substring(0,1).toUpperCase()}
                                    </c:when>
                                    <c:otherwise>A</c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/settings" method="post">
                            <input type="hidden" name="action" value="updateProfile">

                            <div class="form-group">
                                <label>Họ và tên</label>
                                <input type="text" name="fullName" class="form-control"
                                       value="${currentAdminUser.fullName}" placeholder="Nhập họ tên" required>
                            </div>

                            <div class="form-group">
                                <label>Email</label>
                                <input type="email" name="email" class="form-control"
                                       value="${currentAdminUser.email}" placeholder="Nhập email" required>
                            </div>

                            <div class="form-group">
                                <label>Số điện thoại</label>
                                <input type="tel" name="phone" class="form-control"
                                       value="${currentAdminUser.phone}" placeholder="Nhập số điện thoại">
                            </div>

                            <c:if test="${canUpdateSettings}">
                                <button type="submit" class="btn-save">
                                    <i class="fas fa-save"></i> Lưu thay đổi
                                </button>
                            </c:if>
                        </form>
                    </div>
                </div>

                <!-- Password Settings -->
                <div class="card">
                    <div class="card-header">
                        <h3>Đổi mật khẩu</h3>
                    </div>
                    <div class="settings-card-body">
                        <form action="${pageContext.request.contextPath}/admin/settings" method="post">
                            <input type="hidden" name="action" value="changePassword">

                            <div class="form-group">
                                <label>Mật khẩu hiện tại</label>
                                <input type="password" name="currentPassword" class="form-control"
                                       placeholder="Nhập mật khẩu hiện tại" required>
                            </div>

                            <div class="form-group">
                                <label>Mật khẩu mới</label>
                                <input type="password" name="newPassword" class="form-control"
                                       placeholder="Nhập mật khẩu mới" required>
                            </div>

                            <div class="form-group">
                                <label>Xác nhận mật khẩu mới</label>
                                <input type="password" name="confirmPassword" class="form-control"
                                       placeholder="Nhập lại mật khẩu mới" required>
                            </div>

                            <c:if test="${canUpdateSettings}">
                                <button type="submit" class="btn-save">
                                    <i class="fas fa-key"></i> Đổi mật khẩu
                                </button>
                            </c:if>
                        </form>
                    </div>
                </div>
            </div>

            <!-- System Settings -->
            <div class="card settings-card-spaced">
                <div class="card-header">
                    <h3>Cài đặt hệ thống</h3>
                </div>
                <div class="settings-card-body">
                    <form action="${pageContext.request.contextPath}/admin/settings" method="post">
                        <input type="hidden" name="action" value="updateSystem">

                        <div class="settings-section">
                            <h3>Thông tin website</h3>
                            <div class="settings-grid settings-grid-2">
                                <div class="form-group">
                                    <label>Tên website</label>
                                    <input type="text" name="siteName" class="form-control" value="${settings.site_name}">
                                </div>
                                <div class="form-group">
                                    <label>Email liên hệ</label>
                                    <input type="email" name="siteEmail" class="form-control" value="${settings.site_email}">
                                </div>
                                <div class="form-group">
                                    <label>Số điện thoại</label>
                                    <input type="tel" name="sitePhone" class="form-control" value="${settings.site_phone}">
                                </div>
                                <div class="form-group">
                                    <label>Địa chỉ</label>
                                    <input type="text" name="siteAddress" class="form-control" value="${settings.site_address}">
                                </div>
                            </div>
                        </div>

                        <div class="settings-section">
                            <h3>Tùy chọn chung</h3>
                            <div class="settings-grid settings-grid-2">
                                <div class="form-group">
                                    <label>Ngôn ngữ mặc định</label>
                                    <select name="defaultLanguage" class="form-control">
                                        <option value="vi" ${settings.default_language == 'vi' ? 'selected' : ''}>Tiếng Việt</option>
                                        <option value="en" ${settings.default_language == 'en' ? 'selected' : ''}>English</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label>Tiền tệ mặc định</label>
                                    <select name="defaultCurrency" class="form-control">
                                        <option value="VND" ${settings.default_currency == 'VND' ? 'selected' : ''}>VND</option>
                                        <option value="USD" ${settings.default_currency == 'USD' ? 'selected' : ''}>USD</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label>Số mục trên trang</label>
                                    <input type="number" min="1" name="itemsPerPage" class="form-control" value="${settings.items_per_page}">
                                </div>
                                <div class="form-group">
                                    <label>Tỷ lệ thuế (%)</label>
                                    <input type="number" step="0.01" min="0" name="taxRate" class="form-control" value="${settings.tax_rate}">
                                </div>
                                <div class="form-group">
                                    <label>Chế độ bảo trì</label>
                                    <label class="settings-check-label"><input type="checkbox" name="maintenanceMode" ${settings.maintenance_mode == 'true' ? 'checked' : ''}> Bật chế độ bảo trì</label>
                                </div>
                                <div class="form-group">
                                    <label>Logo (URL)</label>
                                    <input type="text" name="siteLogoUrl" class="form-control" value="${settings.site_logo_url}">
                                </div>
                            </div>
                        </div>

                        <div class="settings-section">
                            <h3>SEO & Mô tả</h3>
                            <div class="form-group">
                                <label>Meta description</label>
                                <textarea name="metaDescription" class="form-control" rows="3">${settings.meta_description}</textarea>
                            </div>
                        </div>

                        <div class="settings-section">
                            <h3>Mạng xã hội</h3>
                            <div class="settings-grid settings-grid-2">
                                <div class="form-group">
                                    <label>Facebook</label>
                                    <input type="text" name="socialFacebook" class="form-control" value="${settings.social_facebook}">
                                </div>
                                <div class="form-group">
                                    <label>Instagram</label>
                                    <input type="text" name="socialInstagram" class="form-control" value="${settings.social_instagram}">
                                </div>
                            </div>
                        </div>

                        <div class="settings-section">
                            <h3>Cài đặt thanh toán</h3>
                            <div class="settings-grid settings-grid-4">
                                <label class="settings-check-list">
                                    <input type="checkbox" name="paymentCod" ${settings.payment_cod == 'true' ? 'checked' : ''}> COD
                                </label>
                                <label class="settings-check-list">
                                    <input type="checkbox" name="paymentVnpay" ${settings.payment_vnpay == 'true' ? 'checked' : ''}> VNPay
                                </label>
                                <label class="settings-check-list">
                                    <input type="checkbox" name="paymentMomo" ${settings.payment_momo == 'true' ? 'checked' : ''}> MoMo
                                </label>
                                <label class="settings-check-list">
                                    <input type="checkbox" name="paymentCard" ${settings.payment_card == 'true' ? 'checked' : ''}> Visa/Mastercard
                                </label>
                            </div>
                        </div>

                        <div class="settings-section">
                            <h3>Cài đặt vận chuyển</h3>
                            <div class="settings-grid settings-grid-3">
                                <label class="settings-check-list">
                                    <input type="checkbox" name="shippingGhn" ${settings.shipping_ghn == 'true' ? 'checked' : ''}> Giao Hàng Nhanh
                                </label>
                                <label class="settings-check-list">
                                    <input type="checkbox" name="shippingGhtk" ${settings.shipping_ghtk == 'true' ? 'checked' : ''}> Giao Hàng Tiết Kiệm
                                </label>
                                <label class="settings-check-list">
                                    <input type="checkbox" name="shippingJnt" ${settings.shipping_jnt == 'true' ? 'checked' : ''}> J&T Express
                                </label>
                            </div>
                        </div>

                        <c:if test="${canUpdateSettings}">
                            <button type="submit" class="btn-save">
                                <i class="fas fa-save"></i> Lưu cài đặt hệ thống
                            </button>
                        </c:if>
                    </form>
                </div>
            </div>
        </div>
        </main>

        <jsp:include page="common/admin-footer.jsp"/>
    </div>
</div>
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/admin-main.js"></script>
</body>
</html>
