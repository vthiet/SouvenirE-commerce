<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="common/admin-access-guard.jspf" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activePage" value="logs" scope="request" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhật ký hệ thống & hoạt động | INOLA Admin</title>
    <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-dashboard.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-pages.css">
</head>
<body>
<div class="admin-shell">
    <div class="sidebar-backdrop" data-sidebar-close></div>
    <jsp:include page="common/admin-sidebar.jsp">
        <jsp:param name="activePage" value="logs" />
    </jsp:include>
    <div class="admin-main">
        <jsp:include page="common/admin-topbar.jsp" />
        <main class="dashboard-content">
            <div class="container-fluid px-3 px-lg-4 py-4">
                <div class="content-header">
                    <div>
                        <h1>Nhật ký hệ thống & hoạt động</h1>
                        <p class="text-muted mb-0">Theo dõi nhật ký yêu cầu và các hoạt động của người dùng như đơn hàng, cập nhật ảnh đại diện, đổi mật khẩu và chỉnh sửa địa chỉ.</p>
                    </div>
                </div>

                <div class="card">
                    <div class="card-body">
                        <form method="get" action="${ctx}/admin/logs" class="row g-3 align-items-end">
                            <div class="col-12 col-md-2">
                                <label class="form-label" for="level">Mức độ</label>
                                <select class="form-select" id="level" name="level">
                                    <option value="" ${empty selectedLevel ? 'selected' : ''}>Tất cả mức độ</option>
                                    <option value="INFO" ${selectedLevel == 'INFO' ? 'selected' : ''}>INFO</option>
                                    <option value="WARN" ${selectedLevel == 'WARN' ? 'selected' : ''}>WARN</option>
                                    <option value="ERROR" ${selectedLevel == 'ERROR' ? 'selected' : ''}>ERROR</option>
                                    <option value="DEBUG" ${selectedLevel == 'DEBUG' ? 'selected' : ''}>DEBUG</option>
                                </select>
                            </div>
                            <div class="col-12 col-md-2">
                                <label class="form-label" for="entryType">Loại</label>
                                <select class="form-select" id="entryType" name="entryType">
                                    <option value="" ${empty selectedEntryType ? 'selected' : ''}>Tất cả bản ghi</option>
                                    <option value="ACTIVITY" ${selectedEntryType == 'ACTIVITY' ? 'selected' : ''}>Hoạt động</option>
                                    <option value="SYSTEM" ${selectedEntryType == 'SYSTEM' ? 'selected' : ''}>Hệ thống</option>
                                </select>
                            </div>
                            <div class="col-12 col-md-4">
                                <label class="form-label" for="q">Tìm kiếm</label>
                                <input type="search" class="form-control" id="q" name="q" value="${fn:escapeXml(query)}" placeholder="Mã đơn, người dùng, hành động, thông điệp">
                            </div>
                            <div class="col-12 col-md-2">
                                <label class="form-label" for="limit">Số dòng</label>
                                <input type="number" class="form-control" id="limit" name="limit" value="${limit}" min="25" max="500">
                            </div>
                            <div class="col-12 col-md-2">
                                <button type="submit" class="btn btn-primary w-100">Lọc</button>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="d-flex justify-content-between align-items-center my-3">
                    <div class="text-muted">Đang hiển thị ${logCount} bản ghi phù hợp</div>
                </div>

                <div class="card">
                    <div class="table-container">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Thời gian</th>
                                <th>Loại</th>
                                <th>Mức độ</th>
                                <th>Hành động</th>
                                <th>Nguồn log</th>
                                <th>Người dùng</th>
                                <th>Thông điệp</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${entries}" var="entry">
                                <tr>
                                    <td><c:out value="${entry.timestamp}" /></td>
                                    <td>
                                        <c:set var="typeBadgeClass" value="badge bg-secondary" />
                                        <c:if test="${entry.entryType == 'ACTIVITY'}"><c:set var="typeBadgeClass" value="badge bg-success" /></c:if>
                                        <span class="${typeBadgeClass}"><c:out value="${entry.typeLabel}" /></span>
                                    </td>
                                    <td>
                                        <c:set var="badgeClass" value="badge bg-primary" />
                                        <c:if test="${entry.level == 'WARN'}"><c:set var="badgeClass" value="badge bg-warning text-dark" /></c:if>
                                        <c:if test="${entry.level == 'ERROR'}"><c:set var="badgeClass" value="badge bg-danger" /></c:if>
                                        <c:if test="${entry.level == 'DEBUG'}"><c:set var="badgeClass" value="badge bg-secondary" /></c:if>
                                        <span class="${badgeClass}"><c:out value="${entry.level}" /></span>
                                    </td>
                                    <td><c:out value="${entry.actionLabel}" /></td>
                                    <td><c:out value="${entry.logger}" /></td>
                                    <td><c:out value="${entry.user}" /></td>
                                    <td><c:out value="${entry.displayMessage}" /></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty entries}">
                                <tr>
                                    <td colspan="7" class="text-center text-muted py-5">Không có bản ghi nào khớp với bộ lọc của bạn.</td>
                                </tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
<script src="${ctx}/assets/vendors/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/assets/js/admin-main.js"></script>
</body>
</html>
