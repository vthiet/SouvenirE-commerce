<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý danh mục - Admin</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/vendors/bootstrap-icons/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-pages.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-categories.css">
</head>
<body>
<div class="admin-shell">
    <div class="sidebar-backdrop" data-sidebar-close></div>
    <jsp:include page="common/admin-sidebar.jsp"/>
    <div class="admin-main">
        <jsp:include page="common/admin-topbar.jsp"/>
        <main class="dashboard-content">
        <div class="container-fluid px-3 px-lg-4 py-4">
            <div class="content-header">
                <h1><i class="fas fa-folder"></i> Quản lý danh mục</h1>
                <c:if test="${canCreateCategory}"><button class="btn-primary" onclick="openAddModal()"><i class="fas fa-plus"></i> Thêm danh mục</button></c:if>
            </div>

            <c:if test="${not empty message}"><div class="alert alert-${messageType}">${message}</div></c:if>

            <div class="card">
                <div class="card-header"><h3>Danh sách danh mục (<span id="categoryCount">${categories.size()}</span>)</h3></div>
                <div class="category-grid">
                    <c:choose>
                        <c:when test="${empty categories}">
                            <div class="empty-state category-empty-state">
                                <i class="fas fa-inbox category-empty-icon"></i>
                                <p>Chưa có danh mục nào. <a href="javascript:openAddModal()">Thêm danh mục</a></p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${categories}" var="cat">
                                <div class="category-card" data-category-id="${cat.id}">
                                    <img src="${pageContext.request.contextPath}/${cat.image}" alt="${cat.categoryName}" class="category-image" onerror="this.src='https://placehold.co/250x150?text='+encodeURIComponent('${cat.categoryName}')">
                                    <div class="category-info">
                                        <div class="category-name">${cat.categoryName}</div>
                                        <div class="category-meta"><i class="fas fa-box"></i> ${cat.productCount} sản phẩm</div>
                                        <div class="action-buttons">
                                            <c:if test="${canUpdateCategory}">
                                                <button
                                                        type="button"
                                                        class="btn-icon btn-edit"
                                                        title="Sửa"
                                                        data-category-id="${cat.id}"
                                                        data-category-name="<c:out value='${cat.categoryName}' />"
                                                        data-category-image="<c:out value='${cat.image}' />">
                                                    <i class="fas fa-edit"></i> Sửa
                                                </button>
                                            </c:if>
                                            <c:if test="${canDeleteCategory}">
                                                <button class="btn-icon btn-delete" onclick="deleteCategory(${cat.id}, '${fn:escapeXml(cat.categoryName)}', ${cat.productCount})" title="Xóa" ${cat.productCount > 0 ? 'disabled' : ''}><i class="fas fa-trash"></i> Xóa</button>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
        </main>

    </div>
</div>

<div id="categoryModal" class="modal category-modal" aria-hidden="true">
    <div class="modal-content admin-modal-content">
        <div class="modal-header admin-modal-header">
            <div class="admin-modal-heading">
                <span id="modalModeBadge" class="admin-modal-badge">Thêm mới</span>
                <div>
                    <h3 id="modalTitle">Thêm danh mục</h3>
                    <p id="modalSubtitle" class="admin-modal-subtitle">Tạo danh mục mới để sắp xếp sản phẩm trong cửa hàng.</p>
                </div>
            </div>
            <button type="button" class="close-btn" onclick="closeModal()" aria-label="Đóng">&times;</button>
        </div>
        <div class="modal-body admin-modal-body">
            <form id="categoryForm" onsubmit="handleFormSubmit(event)" class="admin-modal-form">
                <input type="hidden" id="formAction" name="action" value="add">
                <input type="hidden" id="categoryId" name="id">

                <div class="admin-modal-layout">
                    <section class="admin-modal-panel">
                        <div class="section-title">
                            <i class="fas fa-folder-open"></i>
                            Thông tin danh mục
                        </div>
                        <div class="form-group">
                            <label>Tên danh mục <span class="required">*</span></label>
                            <input type="text" id="categoryName" name="name" class="form-control" placeholder="Nhập tên danh mục" required>
                            <div class="error-message"></div>
                        </div>
                        <div class="form-group">
                            <label>URL hình ảnh <span class="required">*</span></label>
                            <input type="text" id="categoryImage" name="imageUrl" class="form-control" placeholder="assets/images/categories/..." required>
                            <div class="error-message"></div>
                        </div>
                    </section>

                    <section class="admin-modal-panel admin-modal-panel-accent">
                        <div class="section-title">
                            <i class="fas fa-image"></i>
                            Xem trước
                        </div>
                        <div class="admin-preview-card">
                            <div class="admin-preview-image-wrap">
                                <img id="categoryPreviewImage" src="https://placehold.co/640x420?text=Category" alt="Xem trước danh mục" class="admin-preview-image">
                            </div>
                            <div class="admin-preview-copy">
                                <strong id="categoryPreviewName">Danh mục mới</strong>
                                <span id="categoryPreviewMeta">Ảnh danh mục sẽ hiển thị ở đây cùng tên danh mục.</span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label>Tình trạng form</label>
                            <input type="text" class="form-control product-readonly" value="Sẵn sàng lưu" readonly>
                            <small class="product-help">Trường bắt buộc được kiểm tra trước khi gửi.</small>
                        </div>
                    </section>
                </div>

                <div class="admin-modal-footer">
                    <button type="button" class="btn-cancel" onclick="closeModal()">Hủy</button>
                    <button type="submit" class="btn-submit" id="submitBtn"><span id="submitBtnText">Lưu danh mục</span></button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    const CONTEXT_PATH = '${pageContext.request.contextPath}';
    const MODAL = document.getElementById('categoryModal');
    const FORM = document.getElementById('categoryForm');
    const SUBMIT_BTN = document.getElementById('submitBtn');

    function openAddModal(){ document.getElementById('modalTitle').innerText='Thêm danh mục mới'; document.getElementById('modalModeBadge').innerText='Thêm mới'; document.getElementById('modalSubtitle').innerText='Tạo danh mục mới để sắp xếp sản phẩm trong cửa hàng.'; document.getElementById('formAction').value='add'; document.getElementById('categoryId').value=''; document.getElementById('categoryName').value=''; document.getElementById('categoryImage').value=''; clearErrors(); MODAL.classList.add('show'); MODAL.setAttribute('aria-hidden','false'); refreshCategoryPreview(); }
    function openEditModal(id,name,image){ document.getElementById('modalTitle').innerText='Cập nhật danh mục'; document.getElementById('modalModeBadge').innerText='Cập nhật'; document.getElementById('modalSubtitle').innerText='Chỉnh sửa tên và hình ảnh đại diện của danh mục.'; document.getElementById('formAction').value='edit'; document.getElementById('categoryId').value=id; document.getElementById('categoryName').value=name; document.getElementById('categoryImage').value=image; clearErrors(); MODAL.classList.add('show'); MODAL.setAttribute('aria-hidden','false'); refreshCategoryPreview(); }
    function closeModal(){ MODAL.classList.remove('show'); MODAL.setAttribute('aria-hidden','true'); FORM.reset(); clearErrors(); refreshCategoryPreview(); }
    function handleFormSubmit(e){ e.preventDefault(); submitForm(); }
    function submitForm(){ if(!validateForm()) return; const fd=new FormData(FORM); SUBMIT_BTN.disabled=true; SUBMIT_BTN.innerText='Đang lưu...'; fetch(CONTEXT_PATH+'/admin/categories',{method:'POST',body:fd}).then(r=>r.json()).then(data=>{ if(data.success){ alert(data.message||'Thành công'); closeModal(); setTimeout(()=>location.reload(),600); } else alert(data.message||'Lỗi'); }).catch(e=>{console.error(e); alert('Lỗi kết nối');}).finally(()=>{SUBMIT_BTN.disabled=false; SUBMIT_BTN.innerText='Lưu danh mục';}); }
    function deleteCategory(id,name,productCount){ if(productCount>0){ alert(`Không thể xóa danh mục "${name}" vì đang có ${productCount} sản phẩm`); return; } if(!confirm(`Bạn có chắc muốn xóa danh mục "${name}"?`)) return; const fd=new FormData(); fd.append('action','delete'); fd.append('id',id); fetch(CONTEXT_PATH+'/admin/categories',{method:'POST',body:fd}).then(r=>r.json()).then(data=>{ if(data.success){ alert(data.message); document.querySelector(`[data-category-id="${id}"]`)?.remove(); updateCategoryCount(); } else alert(data.message); }).catch(e=>{console.error(e); alert('Lỗi xóa'); }); }
    function validateForm(){ clearErrors(); let valid=true; const name=document.getElementById('categoryName').value.trim(); if(!name){ showFieldError('categoryName','Tên danh mục không được để trống'); valid=false; } else if(name.length<2){ showFieldError('categoryName','Tên danh mục phải có ít nhất 2 ký tự'); valid=false; } else if(name.length>100){ showFieldError('categoryName','Tên danh mục không được vượt quá 100 ký tự'); valid=false; } const img=document.getElementById('categoryImage').value.trim(); if(!img){ showFieldError('categoryImage','URL hình ảnh không được để trống'); valid=false; } return valid; }
    function showFieldError(id,msg){ const f=document.getElementById(id); const g=f.closest('.form-group'); g.classList.add('has-error'); g.querySelector('.error-message').textContent=msg; }
    function clearErrors(){ document.querySelectorAll('.form-group').forEach(g=>{ g.classList.remove('has-error'); const e=g.querySelector('.error-message'); if(e) e.textContent=''; }); }
    function updateCategoryCount(){ document.getElementById('categoryCount').textContent=document.querySelectorAll('[data-category-id]').length; }
    function refreshCategoryPreview(){ const name=document.getElementById('categoryName').value.trim(); const image=document.getElementById('categoryImage').value.trim(); document.getElementById('categoryPreviewName').innerText=name||'Danh mục mới'; document.getElementById('categoryPreviewMeta').innerText=image||'Ảnh danh mục sẽ hiển thị ở đây cùng tên danh mục.'; document.getElementById('categoryPreviewImage').src=image||'https://placehold.co/640x420?text=Category'; }
    document.addEventListener('keydown',e=>{ if(e.key==='Escape') closeModal(); });
    document.getElementById('categoryName').addEventListener('input',()=>{ SUBMIT_BTN.disabled=!document.getElementById('categoryName').value.trim(); });
    document.getElementById('categoryImage').addEventListener('input',()=>{ SUBMIT_BTN.disabled=!document.getElementById('categoryImage').value.trim(); });
    document.getElementById('categoryName').addEventListener('input',refreshCategoryPreview);
    document.getElementById('categoryImage').addEventListener('input',refreshCategoryPreview);
    document.querySelectorAll('.btn-edit').forEach(button=>{ button.addEventListener('click',()=>openEditModal(button.dataset.categoryId,button.dataset.categoryName,button.dataset.categoryImage)); });
</script>
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/admin-main.js"></script>
</body>
</html>
