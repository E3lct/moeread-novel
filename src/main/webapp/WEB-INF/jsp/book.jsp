<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.moeread.model.Book" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    @SuppressWarnings("unchecked")
    List<Book> books = (List<Book>) request.getAttribute("books");
    @SuppressWarnings("unchecked")
    Map<String, List<Book>> groupedBooks = (Map<String, List<Book>>) request.getAttribute("groupedBooks");
    @SuppressWarnings("unchecked")
    List<String> allTags = (List<String>) request.getAttribute("allTags");
    @SuppressWarnings("unchecked")
    Map<Integer, List<String>> tagMap = (Map<Integer, List<String>>) request.getAttribute("tagMap");

    String currentStatus = (String) request.getAttribute("currentStatus");
    String currentFavorite = (String) request.getAttribute("currentFavorite");
    String currentQ = (String) request.getAttribute("currentQ");
    String currentTag = (String) request.getAttribute("currentTag");
    String currentView = (String) request.getAttribute("currentView");

    int countAll = (Integer) request.getAttribute("countAll");
    int countReading = (Integer) request.getAttribute("countReading");
    int countPrepare = (Integer) request.getAttribute("countPrepare");
    int countFinished = (Integer) request.getAttribute("countFinished");
    int countFavorite = (Integer) request.getAttribute("countFavorite");

    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>书架 - 墨读 Moeread</title>
    <link rel="stylesheet" href="<%=ctx%>/assets/css/variables.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/layout.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/bookshelf.css">
</head>
<body>
    <%@ include file="/WEB-INF/jsp/navbar.jsp" %>

    <div class="main-content">
        <div class="page-header">
            <h1 class="page-title">书架</h1>
            <p class="page-subtitle">共 <%=countAll%> 本书</p>
        </div>

        <%-- 工具栏：搜索 + 视图切换 + 导入按钮 --%>
        <div class="bookshelf-toolbar">
            <div class="bookshelf-search">
                <svg class="bookshelf-search-icon" viewBox="0 0 16 16" fill="currentColor">
                    <path d="M11.5 7a4.5 4.5 0 1 1-9 0 4.5 4.5 0 0 1 9 0Zm-.82 4.74a6 6 0 1 1 1.06-1.06l3.04 3.04a.75.75 0 1 1-1.06 1.06l-3.04-3.04Z"/>
                </svg>
                <form action="<%=ctx%>/book" method="get" id="searchForm">
                    <% if (currentView != null && !"grid".equals(currentView)) { %>
                        <input type="hidden" name="view" value="<%=currentView%>">
                    <% } %>
                    <input type="text" name="q" value="<%=currentQ%>" placeholder="搜索书名..." autocomplete="off">
                    <% if (currentQ != null && !currentQ.trim().isEmpty()) { %>
                        <button type="button" class="bookshelf-search-clear" id="searchClear" aria-label="清除搜索">
                            <svg viewBox="0 0 16 16" fill="currentColor"><path d="M4.28 3.22a.75.75 0 0 0-1.06 1.06L6.94 8l-3.72 3.72a.75.75 0 1 0 1.06 1.06L8 9.06l3.72 3.72a.75.75 0 1 0 1.06-1.06L9.06 8l3.72-3.72a.75.75 0 0 0-1.06-1.06L8 6.94 4.28 3.22Z"/></svg>
                        </button>
                    <% } %>
                </form>
            </div>

            <div class="bookshelf-toolbar-right">
                <%-- 视图切换 --%>
                <div class="view-switcher" id="viewSwitcher">
                    <button type="button" class="view-switcher-btn <%= "grid".equals(currentView) ? "active" : "" %>" data-view="grid" title="网格视图">
                        <svg viewBox="0 0 16 16" fill="currentColor"><path d="M1 1h6v6H1zM9 1h6v6H9zM1 9h6v6H1zM9 9h6v6H9z"/></svg>
                    </button>
                    <button type="button" class="view-switcher-btn <%= "grouped".equals(currentView) ? "active" : "" %>" data-view="grouped" title="分组视图">
                        <svg viewBox="0 0 16 16" fill="currentColor"><path d="M1 2h14v2H1zM1 6h14v2H1zM1 10h14v2H1zM1 13h14v1H1z"/></svg>
                    </button>
                </div>
                <a href="<%=ctx%>/import" class="bookshelf-import-btn">
                    <svg viewBox="0 0 16 16" fill="currentColor"><path d="M8 2a.75.75 0 0 1 .75.75v4.5h4.5a.75.75 0 0 1 0 1.5h-4.5v4.5a.75.75 0 0 1-1.5 0v-4.5h-4.5a.75.75 0 0 1 0-1.5h4.5v-4.5A.75.75 0 0 1 8 2Z"/></svg>
                    导入书籍
                </a>
            </div>
        </div>

        <%-- 筛选标签条 --%>
        <div class="bookshelf-filters">
            <a class="filter-tab <%=("all".equals(currentStatus) && !"1".equals(currentFavorite)) ? "active" : ""%>"
               href="<%=ctx%>/book<%= "grouped".equals(currentView) ? "?view=grouped" : "" %>">全部 <span class="filter-count"><%=countAll%></span></a>
            <a class="filter-tab <%= "reading".equals(currentStatus) ? "active" : "" %>"
               href="<%=ctx%>/book?status=reading<%= "grouped".equals(currentView) ? "&view=grouped" : "" %>">正在看 <span class="filter-count"><%=countReading%></span></a>
            <a class="filter-tab <%= "prepare".equals(currentStatus) ? "active" : "" %>"
               href="<%=ctx%>/book?status=prepare<%= "grouped".equals(currentView) ? "&view=grouped" : "" %>">准备看 <span class="filter-count"><%=countPrepare%></span></a>
            <a class="filter-tab <%= "finished".equals(currentStatus) ? "active" : "" %>"
               href="<%=ctx%>/book?status=finished<%= "grouped".equals(currentView) ? "&view=grouped" : "" %>">已看完 <span class="filter-count"><%=countFinished%></span></a>
            <a class="filter-tab <%= "1".equals(currentFavorite) ? "active" : "" %>"
               href="<%=ctx%>/book?favorite=1<%= "grouped".equals(currentView) ? "&view=grouped" : "" %>">喜欢 <span class="filter-count"><%=countFavorite%></span></a>
            <% if (allTags != null && !allTags.isEmpty()) { %>
                <span class="filter-divider"></span>
                <% for (String t : allTags) { %>
                    <a class="filter-tab filter-tag <%= t.equals(currentTag) ? "active" : "" %>"
                       href="<%=ctx%>/book?tag=<%=t%><%= "grouped".equals(currentView) ? "&view=grouped" : "" %>"><%=t%></a>
                <% } %>
                <% if (currentTag != null && !currentTag.isEmpty()) { %>
                    <a class="filter-tab filter-tag-clear" href="<%=ctx%>/book<%= "grouped".equals(currentView) ? "?view=grouped" : "" %>">清除标签 ×</a>
                <% } %>
            <% } %>
        </div>

        <%-- 空状态 --%>
        <% if (books == null || books.isEmpty()) { %>
            <% if (countAll == 0) { %>
                <div class="empty-state">
                    <div class="empty-state-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                            <path d="M4 4.5A2.5 2.5 0 0 1 6.5 2H20v18H6.5A2.5 2.5 0 0 0 4 22.5z"/>
                            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                        </svg>
                    </div>
                    <div class="empty-state-title">书架还是空的</div>
                    <div class="empty-state-desc">导入一些书籍开始阅读吧</div>
                    <a href="<%=ctx%>/import" class="btn btn-primary">导入书籍</a>
                </div>
            <% } else { %>
                <div class="empty-state">
                    <div class="empty-state-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                            <circle cx="11" cy="11" r="7"/>
                            <path d="m21 21-4.3-4.3"/>
                            <path d="M8 11h6"/>
                        </svg>
                    </div>
                    <div class="empty-state-title">未找到匹配的书籍</div>
                    <div class="empty-state-desc">
                        <% if (currentQ != null && !currentQ.trim().isEmpty()) { %>
                            没有书名包含「<%=currentQ%>」的书籍
                        <% } else if (currentTag != null && !currentTag.isEmpty()) { %>
                            标签「<%=currentTag%>」下没有书籍
                        <% } else { %>
                            当前筛选条件下没有书籍
                        <% } %>
                    </div>
                    <a href="<%=ctx%>/book" class="btn btn-ghost">清除筛选</a>
                </div>
            <% } %>
        <% } else if ("grouped".equals(currentView)) { %>
            <%-- 分组视图 --%>
            <div class="grouped-view">
                <% for (Map.Entry<String, List<Book>> entry : groupedBooks.entrySet()) { %>
                    <section class="tag-group">
                        <div class="tag-group-header">
                            <h2 class="tag-group-name"><%=entry.getKey()%></h2>
                            <span class="tag-group-count"><%=entry.getValue().size()%> 本</span>
                        </div>
                        <div class="book-grid">
                            <% int idx = 0;
                               for (Book b : entry.getValue()) {
                                   int coverIdx = (idx % 6) + 1;
                                   idx++;
                                   List<String> bTags = tagMap == null ? null : tagMap.get(b.getId());
                            %>
                                <%@ include file="/WEB-INF/jsp/_book_card.jsp" %>
                            <% } %>
                        </div>
                    </section>
                <% } %>
            </div>
        <% } else { %>
            <%-- 网格视图 --%>
            <div class="book-grid">
                <% int idx = 0;
                   for (Book b : books) {
                       int coverIdx = (idx % 6) + 1;
                       idx++;
                       List<String> bTags = tagMap == null ? null : tagMap.get(b.getId());
                %>
                    <%@ include file="/WEB-INF/jsp/_book_card.jsp" %>
                <% } %>
            </div>
        <% } %>
    </div>

    <%-- 编辑弹窗 --%>
    <div class="modal-overlay" id="editModal">
        <div class="modal-card">
            <div class="modal-header">
                <h3 class="modal-title">编辑书籍</h3>
                <button type="button" class="modal-close" id="editModalClose" aria-label="关闭">
                    <svg viewBox="0 0 16 16" fill="currentColor"><path d="M4.28 3.22a.75.75 0 0 0-1.06 1.06L6.94 8l-3.72 3.72a.75.75 0 1 0 1.06 1.06L8 9.06l3.72 3.72a.75.75 0 1 0 1.06-1.06L9.06 8l3.72-3.72a.75.75 0 0 0-1.06-1.06L8 6.94 4.28 3.22Z"/></svg>
                </button>
            </div>
            <form id="editForm" class="modal-body">
                <input type="hidden" name="action" value="update_meta">
                <input type="hidden" name="bookId" id="editBookId">

                <div class="form-row">
                    <label class="form-label">书名</label>
                    <input type="text" name="title" id="editTitle" class="form-input" maxlength="100">
                </div>

                <div class="form-row">
                    <label class="form-label">封面色</label>
                    <div class="cover-color-picker" id="coverColorPicker">
                        <button type="button" class="cover-color-swatch" data-color="#F59E0B" style="background:#F59E0B"></button>
                        <button type="button" class="cover-color-swatch" data-color="#D97706" style="background:#D97706"></button>
                        <button type="button" class="cover-color-swatch" data-color="#B45309" style="background:#B45309"></button>
                        <button type="button" class="cover-color-swatch" data-color="#FBBF24" style="background:#FBBF24"></button>
                        <button type="button" class="cover-color-swatch" data-color="#92400E" style="background:#92400E"></button>
                        <button type="button" class="cover-color-swatch" data-color="#78350F" style="background:#78350F"></button>
                    </div>
                    <input type="hidden" name="coverColor" id="editCoverColor" value="#F59E0B">
                </div>

                <%-- 封面图片上传 --%>
                <div class="form-row">
                    <label class="form-label">自定义封面</label>
                    <div class="cover-upload-area">
                        <input type="file" id="coverFileInput" accept="image/png,image/jpeg,image/webp">
                        <div class="cover-upload-preview" id="coverPreview"></div>
                        <span class="cover-upload-hint">支持 PNG / JPG / WebP，最大 2MB</span>
                        <button type="button" class="btn btn-ghost btn-sm" id="uploadCoverBtn" style="margin-top:6px">上传封面</button>
                    </div>
                </div>

                <div class="form-row">
                    <label class="form-label">标签</label>
                    <div class="tag-editor">
                        <div class="tag-list" id="editTagList"></div>
                        <div class="tag-input-wrap">
                            <input type="text" id="tagInput" class="form-input tag-input" placeholder="输入标签名，回车添加" maxlength="20">
                            <button type="button" class="tag-add-btn" id="tagAddBtn">添加</button>
                        </div>
                    </div>
                </div>

                <div class="form-row form-row-danger">
                    <button type="button" class="btn btn-danger-ghost" id="deleteBookBtn">删除此书</button>
                </div>
            </form>
            <div class="modal-footer">
                <button type="button" class="btn btn-ghost" id="editCancel">取消</button>
                <button type="button" class="btn btn-primary" id="editSave">保存</button>
            </div>
        </div>
    </div>

    <script>
        (function() {
            var ctx = '<%=ctx%>';
            var currentView = '<%=currentView%>';

            // 清除搜索
            var sc = document.getElementById('searchClear');
            if (sc) sc.addEventListener('click', function() {
                window.location.href = ctx + '/book' + (currentView === 'grouped' ? '?view=grouped' : '');
            });

            // 视图切换
            var switcher = document.getElementById('viewSwitcher');
            if (switcher) switcher.addEventListener('click', function(e) {
                var btn = e.target.closest('.view-switcher-btn');
                if (!btn) return;
                var v = btn.getAttribute('data-view');
                var url = ctx + '/book?view=' + v;
                var params = [];
                var q = '<%=currentQ%>';
                var status = '<%=currentStatus%>';
                var fav = '<%=currentFavorite == null ? "" : currentFavorite%>';
                var tag = '<%=currentTag == null ? "" : currentTag%>';
                if (q) params.push('q=' + encodeURIComponent(q));
                if (status && status !== 'all') params.push('status=' + status);
                if (fav === '1') params.push('favorite=1');
                if (tag) params.push('tag=' + encodeURIComponent(tag));
                if (params.length) url += '&' + params.join('&');
                window.location.href = url;
            });

            // ---- 编辑弹窗 ----
            var modal = document.getElementById('editModal');
            var closeBtn = document.getElementById('editModalClose');
            var cancelBtn = document.getElementById('editCancel');
            var saveBtn = document.getElementById('editSave');
            var deleteBtn = document.getElementById('deleteBookBtn');
            var tagList = document.getElementById('editTagList');
            var tagInput = document.getElementById('tagInput');
            var tagAddBtn = document.getElementById('tagAddBtn');
            var colorPicker = document.getElementById('coverColorPicker');

            var currentBookId = null;
            var currentTags = [];

            function openModal(bookId, title, coverColor, tags) {
                currentBookId = bookId;
                currentTags = tags ? tags.slice() : [];
                document.getElementById('editBookId').value = bookId;
                document.getElementById('editTitle').value = title;
                document.getElementById('editCoverColor').value = coverColor || '#F59E0B';
                // 标记选中色
                var swatches = colorPicker.querySelectorAll('.cover-color-swatch');
                for (var i = 0; i < swatches.length; i++) {
                    swatches[i].classList.toggle('selected', swatches[i].getAttribute('data-color') === (coverColor || '#F59E0B'));
                }
                renderTags();
                modal.classList.add('open');
            }
            function closeModal() { modal.classList.remove('open'); }

            function renderTags() {
                tagList.innerHTML = '';
                for (var i = 0; i < currentTags.length; i++) {
                    (function(t, idx) {
                        var chip = document.createElement('span');
                        chip.className = 'tag-chip';
                        chip.innerHTML = '<span class="tag-chip-name">' + escapeHtml(t) + '</span><button type="button" class="tag-chip-remove" data-idx="' + idx + '" aria-label="移除">×</button>';
                        chip.querySelector('.tag-chip-remove').addEventListener('click', function() {
                            // 同步到后端
                            fetch(ctx + '/book', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                                body: 'action=remove_tag&bookId=' + currentBookId + '&tagName=' + encodeURIComponent(t)
                            });
                            currentTags.splice(idx, 1);
                            renderTags();
                        });
                        tagList.appendChild(chip);
                    })(currentTags[i], i);
                }
            }

            function addTag() {
                var v = tagInput.value.trim();
                if (!v) return;
                if (currentTags.indexOf(v) !== -1) { tagInput.value = ''; return; }
                fetch(ctx + '/book', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'action=add_tag&bookId=' + currentBookId + '&tagName=' + encodeURIComponent(v)
                });
                currentTags.push(v);
                tagInput.value = '';
                renderTags();
            }

            function escapeHtml(s) {
                return s.replace(/[&<>"']/g, function(c) {
                    return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c];
                });
            }

            // 颜色选择
            if (colorPicker) colorPicker.addEventListener('click', function(e) {
                var sw = e.target.closest('.cover-color-swatch');
                if (!sw) return;
                var c = sw.getAttribute('data-color');
                document.getElementById('editCoverColor').value = c;
                var all = colorPicker.querySelectorAll('.cover-color-swatch');
                for (var i = 0; i < all.length; i++) all[i].classList.remove('selected');
                sw.classList.add('selected');
            });

            // ---- 封面上传预览 ----
            var coverFileInput = document.getElementById('coverFileInput');
            var coverPreview = document.getElementById('coverPreview');
            var uploadCoverBtn = document.getElementById('uploadCoverBtn');
            var pendingCoverFile = null;

            if (coverFileInput) coverFileInput.addEventListener('change', function() {
                var f = this.files && this.files[0];
                if (!f) { coverPreview.innerHTML = ''; pendingCoverFile = null; return; }
                // 检查大小 2MB
                if (f.size > 2 * 1024 * 1024) { alert('图片不能超过 2MB'); this.value=''; return; }
                pendingCoverFile = f;
                var reader = new FileReader();
                reader.onload = function(e) {
                    coverPreview.innerHTML = '<img src="' + e.target.result + '" alt="preview">';
                };
                reader.readAsDataURL(f);
            });

            // 上传按钮（立即上传，不等保存）
            if (uploadCoverBtn) uploadCoverBtn.addEventListener('click', function() {
                if (!pendingCoverFile || !currentBookId) { alert('请先选择图片'); return; }
                var fd = new FormData();
                fd.append('action', 'upload_cover');
                fd.append('bookId', currentBookId);
                fd.append('file', pendingCoverFile);
                fetch(ctx + '/book', { method: 'POST', body: fd })
                    .then(function(r) { return r.json(); })
                    .then(function(data) {
                        if (data.success && data.url) {
                            alert('封面上传成功！');
                            pendingCoverFile = null;
                            window.location.reload();
                        } else {
                            alert('上传失败：' + (data.error || '未知错误'));
                        }
                    })
                    .catch(function() { alert('网络错误'); });
            });

            // 标签添加
            if (tagAddBtn) tagAddBtn.addEventListener('click', addTag);
            if (tagInput) tagInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') { e.preventDefault(); addTag(); }
            });

            // 关闭
            if (closeBtn) closeBtn.addEventListener('click', closeModal);
            if (cancelBtn) cancelBtn.addEventListener('click', closeModal);
            modal.addEventListener('click', function(e) { if (e.target === modal) closeModal(); });

            // 保存
            if (saveBtn) saveBtn.addEventListener('click', function() {
                var form = document.getElementById('editForm');
                var fd = new FormData(form);
                var parts = [];
                for (var p of fd.entries()) parts.push(encodeURIComponent(p[0]) + '=' + encodeURIComponent(p[1]));
                fetch(ctx + '/book', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: parts.join('&')
                }).then(function() { window.location.reload(); });
            });

            // 删除
            if (deleteBtn) deleteBtn.addEventListener('click', function() {
                if (!confirm('确定删除这本书吗？此操作不可恢复。')) return;
                fetch(ctx + '/book', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'action=delete&bookId=' + currentBookId
                }).then(function() { window.location.href = ctx + '/book'; });
            });

            // 暴露给卡片用
            window.__openBookEditor = openModal;
        })();
    </script>
</body>
</html>
