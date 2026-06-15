<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Mini Cloudinary Dashboard</title>
    <style>
        body { font-family: Arial; }
        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
            gap: 20px;
        }
        .card {
            border: 1px solid #ddd;
            padding: 10px;
            border-radius: 10px;
        }
        img {
            width: 100%;
            height: 150px;
            object-fit: cover;
        }
        button {
            margin-top: 5px;
            padding: 5px;
            width: 100%;
        }
    </style>
    <jsp:include page="/WEB-INF/layout/dark-mode.jsp"/>

    <script>
        function copy(url) {
            navigator.clipboard.writeText(url);
            alert("Copied!");
        }
    </script>
</head>
<body>

<button class="theme-toggle-button auth-theme-toggle"
        type="button"
        data-theme-toggle
        aria-label="Chuyển giao diện sáng tối"
        aria-pressed="false">
    <i class="fa-solid fa-moon" data-theme-icon aria-hidden="true"></i>
    <span class="theme-label" data-theme-label>Tối</span>
</button>

<h2>Mini Cloudinary Dashboard</h2>

<form action="upload" method="post" enctype="multipart/form-data">
    <input type="file" name="file" required />
    <button type="submit">Upload</button>
</form>

<hr/>

<div class="grid">
    <c:forEach var="img" items="${data.resources}">
        <div class="card">
            <img src="${img.secure_url}" />

            <button onclick="copy('${img.secure_url}')">Copy URL</button>

            <form method="post" action="dashboard">
                <input type="hidden" name="action" value="delete" />
                <input type="hidden" name="publicId" value="${img.public_id}" />
                <button type="submit">Delete</button>
            </form>
        </div>
    </c:forEach>
</div>

<br/>

<c:if test="${not empty data.next_cursor}">
    <a href="dashboard?cursor=${data.next_cursor}">Next Page →</a>
</c:if>

</body>
</html>
