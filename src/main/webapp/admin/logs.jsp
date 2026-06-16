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
    <title>System & Activity Logs | INOLA Admin</title>
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
                        <h1>System & Activity Logs</h1>
                        <p class="text-muted mb-0">Track request traces and user activities such as orders, avatar updates, password changes, and address edits.</p>
                    </div>
                </div>

                <div class="card mb-3">
                    <div class="card-body py-3">
                        <div class="small text-muted mb-1">Log source đang được đọc</div>
                        <div class="fw-semibold"><c:out value="${resolvedLogDir}" /></div>
                        <div class="small text-muted mt-2">
                            File:
                            <c:choose>
                                <c:when test="${empty resolvedLogFile}">
                                    <span class="text-warning">Không xác định được file log</span>
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${resolvedLogFile}" />
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <div class="card">
                    <div class="card-body">
                        <form method="get" action="${ctx}/admin/logs" class="row g-3 align-items-end">
                            <div class="col-12 col-md-2">
                                <label class="form-label" for="level">Level</label>
                                <select class="form-select" id="level" name="level">
                                    <option value="" ${empty selectedLevel ? 'selected' : ''}>All levels</option>
                                    <option value="INFO" ${selectedLevel == 'INFO' ? 'selected' : ''}>INFO</option>
                                    <option value="WARN" ${selectedLevel == 'WARN' ? 'selected' : ''}>WARN</option>
                                    <option value="ERROR" ${selectedLevel == 'ERROR' ? 'selected' : ''}>ERROR</option>
                                    <option value="DEBUG" ${selectedLevel == 'DEBUG' ? 'selected' : ''}>DEBUG</option>
                                </select>
                            </div>
                            <div class="col-12 col-md-2">
                                <label class="form-label" for="entryType">Type</label>
                                <select class="form-select" id="entryType" name="entryType">
                                    <option value="" ${empty selectedEntryType ? 'selected' : ''}>All entries</option>
                                    <option value="ACTIVITY" ${selectedEntryType == 'ACTIVITY' ? 'selected' : ''}>Activity</option>
                                    <option value="SYSTEM" ${selectedEntryType == 'SYSTEM' ? 'selected' : ''}>System</option>
                                </select>
                            </div>
                            <div class="col-12 col-md-4">
                                <label class="form-label" for="q">Search</label>
                                <input type="search" class="form-control" id="q" name="q" value="${fn:escapeXml(query)}" placeholder="Order code, user, action, message">
                            </div>
                            <div class="col-12 col-md-2">
                                <label class="form-label" for="limit">Limit</label>
                                <input type="number" class="form-control" id="limit" name="limit" value="${limit}" min="25" max="500">
                            </div>
                            <div class="col-12 col-md-2">
                                <button type="submit" class="btn btn-primary w-100">Filter</button>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="d-flex justify-content-between align-items-center my-3">
                    <div class="text-muted">Showing ${logCount} matching log entries</div>
                </div>

                <div class="card">
                    <div class="table-container">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Time</th>
                                <th>Type</th>
                                <th>Level</th>
                                <th>Action</th>
                                <th>Logger</th>
                                <th>User</th>
                                <th>Message</th>
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
                                    <td colspan="7" class="text-center text-muted py-5">No log entries matched your filters.</td>
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
