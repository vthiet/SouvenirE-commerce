<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${empty accessDeniedTitle ? 'Access denied' : accessDeniedTitle}</title>
    <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin-pages.css">
    <style>
        body {
            min-height: 100vh;
            margin: 0;
            background:
                radial-gradient(circle at top left, rgba(255, 255, 255, 0.12), transparent 32%),
                linear-gradient(135deg, #0f172a 0%, #1e293b 48%, #334155 100%);
            color: #e2e8f0;
        }

        .denied-shell {
            min-height: 100vh;
            display: grid;
            place-items: center;
            padding: 2rem 1rem;
        }

        .denied-card {
            width: min(100%, 720px);
            padding: 2rem;
            border-radius: 1.5rem;
            background: rgba(15, 23, 42, 0.82);
            border: 1px solid rgba(148, 163, 184, 0.25);
            box-shadow: 0 24px 80px rgba(2, 6, 23, 0.45);
            backdrop-filter: blur(18px);
        }

        .denied-badge {
            display: inline-flex;
            align-items: center;
            gap: .5rem;
            padding: .45rem .8rem;
            border-radius: 999px;
            background: rgba(248, 113, 113, 0.15);
            color: #fca5a5;
            font-weight: 600;
            margin-bottom: 1rem;
        }

        .denied-title {
            font-size: clamp(2rem, 4vw, 3.25rem);
            line-height: 1.05;
            margin: 0 0 .75rem;
            color: #f8fafc;
        }

        .denied-copy {
            font-size: 1.05rem;
            color: #cbd5e1;
            max-width: 58ch;
        }

        .denied-actions {
            display: flex;
            gap: .75rem;
            flex-wrap: wrap;
            margin-top: 1.5rem;
        }

        .denied-actions .btn {
            border-radius: .9rem;
            padding: .8rem 1.1rem;
            font-weight: 600;
        }
    </style>
</head>
<body>
<main class="denied-shell">
    <section class="denied-card" aria-labelledby="denied-title">
        <div class="denied-badge">
            <i class="bi bi-shield-lock"></i>
            Forbidden
        </div>
        <h1 id="denied-title" class="denied-title">
            ${empty accessDeniedTitle ? 'Bạn không có quyền truy cập trang này' : accessDeniedTitle}
        </h1>
        <p class="denied-copy">
            ${empty accessDeniedMessage ? 'Trang quản trị này chỉ dành cho Sales, Admin, Super Admin, hoặc tài khoản có quyền dashboard.read.' : accessDeniedMessage}
        </p>
        <div class="denied-actions">
            <a class="btn btn-light" href="${ctx}/admin/dashboard">
                <i class="bi bi-speedometer2 me-1"></i>
                Go to dashboard
            </a>
            <a class="btn btn-outline-light" href="${ctx}/logout">
                <i class="bi bi-box-arrow-right me-1"></i>
                Sign out
            </a>
        </div>
    </section>
</main>
</body>
</html>
