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
    @SuppressWarnings("unchecked")
    Map<Integer, Integer> progressMap = (Map<Integer, Integer>) request.getAttribute("progressMap");

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
                        <div class="cover-upload-preview" id="coverPreview">
                            <span class="cover-upload-placeholder">点击选择图片</span>
                        </div>
                        <input type="file" id="coverFileInput" accept="image/png,image/jpeg,image/webp" style="display:none">
                        <div class="cover-upload-actions">
                            <button type="button" class="btn btn-ghost btn-sm" id="selectCoverBtn">选择图片</button>
                            <button type="button" class="btn btn-ghost btn-sm" id="removeCoverBtn" style="display:none">移除封面</button>
                        </div>
                        <span class="cover-upload-hint">支持 PNG / JPG / WebP，最大 2MB，自动裁剪为 3:4</span>
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

    <%-- 封面剪裁弹窗 --%>
    <div class="modal-overlay" id="cropModal">
        <div class="modal-card modal-card-sm">
            <div class="modal-header">
                <h3 class="modal-title">裁剪封面</h3>
                <button type="button" class="modal-close" id="cropModalClose" aria-label="关闭">
                    <svg viewBox="0 0 16 16" fill="currentColor"><path d="M4.28 3.22a.75.75 0 0 0-1.06 1.06L6.94 8l-3.72 3.72a.75.75 0 1 0 1.06 1.06L8 9.06l3.72 3.72a.75.75 0 1 0 1.06-1.06L9.06 8l3.72-3.72a.75.75 0 0 0-1.06-1.06L8 6.94 4.28 3.22Z"/></svg>
                </button>
            </div>
            <div class="modal-body">
                <div class="crop-container" id="cropContainer">
                    <div class="crop-frame" id="cropFrame">
                        <img id="cropImage" src="" alt="待裁剪图片">
                        <div class="crop-overlay-top"></div>
                        <div class="crop-overlay-bottom"></div>
                    </div>
                </div>
                <div class="crop-controls">
                    <label class="crop-zoom-label">
                        <span>缩放</span>
                        <input type="range" id="cropZoom" min="100" max="300" value="100" step="5">
                        <span class="crop-zoom-val" id="cropZoomVal">100%</span>
                    </label>
                    <span class="crop-hint">拖动图片选择区域 · 滚轮或滑块缩放 · 框内即剪裁结果</span>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-ghost" id="cropCancel">取消</button>
                <button type="button" class="btn btn-primary" id="cropConfirm">确认裁剪并上传</button>
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

            // ---- 封面选择 + 剪裁 ----
            var coverFileInput = document.getElementById('coverFileInput');
            var coverPreview = document.getElementById('coverPreview');
            var selectCoverBtn = document.getElementById('selectCoverBtn');
            var removeCoverBtn = document.getElementById('removeCoverBtn');
            var cropModal = document.getElementById('cropModal');
            var cropModalClose = document.getElementById('cropModalClose');
            var cropCancel = document.getElementById('cropCancel');
            var cropConfirm = document.getElementById('cropConfirm');
            var cropImage = document.getElementById('cropImage');
            var cropFrame = document.getElementById('cropFrame');
            var cropContainer = document.getElementById('cropContainer');
            var cropZoom = document.getElementById('cropZoom');
            var pendingCoverFile = null;
            var cropImgNaturalW = 0, cropImgNaturalH = 0;
            var cropOffsetX = 0, cropOffsetY = 0;
            var cropDragging = false;
            var cropDragStartX = 0, cropDragStartY = 0;
            var cropStartOffsetX = 0, cropStartOffsetY = 0;
            var cropBaseScale = 1;  // 图片 cover frame 时的基准缩放（原图很大时 < 1）

            // 打开编辑弹窗时加载当前封面
            var origOpenModal = openModal;
            openModal = function(bookId, title, coverColor, tags) {
                origOpenModal(bookId, title, coverColor, tags);
                // 检查当前书是否有封面图片
                var card = document.querySelector('.book-card[data-book-id="' + bookId + '"]');
                var coverDiv = card && card.querySelector('.book-cover');
                var bgStyle = coverDiv ? coverDiv.style.backgroundImage : '';
                if (bgStyle && bgStyle !== 'none' && bgStyle.indexOf('url(') !== -1) {
                    var url = bgStyle.replace(/^url\(["']?/, '').replace(/["']?\)$/, '');
                    coverPreview.innerHTML = '<img src="' + url + '" alt="封面预览">';
                    coverPreview.classList.add('has-image');
                    removeCoverBtn.style.display = '';
                } else {
                    coverPreview.innerHTML = '<span class="cover-upload-placeholder">点击选择图片</span>';
                    coverPreview.classList.remove('has-image');
                    removeCoverBtn.style.display = 'none';
                }
                pendingCoverFile = null;
            };
            window.__openBookEditor = openModal;

            if (selectCoverBtn) selectCoverBtn.addEventListener('click', function() {
                coverFileInput.click();
            });
            coverPreview.addEventListener('click', function() {
                coverFileInput.click();
            });

            if (coverFileInput) coverFileInput.addEventListener('change', function() {
                var f = this.files && this.files[0];
                if (!f) return;
                if (f.size > 2 * 1024 * 1024) { alert('图片不能超过 2MB'); this.value=''; return; }
                pendingCoverFile = f;
                var reader = new FileReader();
                reader.onload = function(e) {
                    cropImage.src = e.target.result;
                    cropImage.onload = function() {
                        cropImgNaturalW = cropImage.naturalWidth;
                        cropImgNaturalH = cropImage.naturalHeight;
                        // 计算 cover 基准 scale：让图片刚好覆盖整个剪裁框
                        var frameW = cropFrame.clientWidth;
                        var frameH = cropFrame.clientHeight;
                        cropBaseScale = Math.max(frameW / cropImgNaturalW, frameH / cropImgNaturalH);
                        // 滑块范围：100% = 刚好 cover，300% = 放大 3 倍
                        cropZoom.min = 100;
                        cropZoom.max = 300;
                        cropZoom.step = 5;
                        cropZoom.value = 100;
                        // 居中显示
                        var dispW = cropImgNaturalW * cropBaseScale;
                        var dispH = cropImgNaturalH * cropBaseScale;
                        cropOffsetX = (frameW - dispW) / 2;
                        cropOffsetY = (frameH - dispH) / 2;
                        updateCropImage();
                        cropModal.classList.add('open');
                    };
                };
                reader.readAsDataURL(f);
                this.value = '';
            });

            // 实际显示 scale = baseScale * (zoom / 100)
            function getActualScale() {
                return cropBaseScale * (parseInt(cropZoom.value, 10) / 100);
            }

            function updateCropImage() {
                var scale = getActualScale();
                var dispW = cropImgNaturalW * scale;
                var dispH = cropImgNaturalH * scale;
                cropImage.style.width = dispW + 'px';
                cropImage.style.height = dispH + 'px';
                var frameW = cropFrame.clientWidth;
                var frameH = cropFrame.clientHeight;
                // 图片必须覆盖 frame（不允许出现空白），所以 offset 限制在 [frame-dim, 0]
                cropOffsetX = Math.max(Math.min(cropOffsetX, 0), frameW - dispW);
                cropOffsetY = Math.max(Math.min(cropOffsetY, 0), frameH - dispH);
                cropImage.style.left = cropOffsetX + 'px';
                cropImage.style.top = cropOffsetY + 'px';
            }

            // 缩放滑块：以剪裁框中心为锚点
            cropZoom.addEventListener('input', function() {
                var frameW = cropFrame.clientWidth;
                var frameH = cropFrame.clientHeight;
                var oldScale = cropBaseScale * (parseInt(this.dataset.lastZoom || '100', 10) / 100);
                var newScale = getActualScale();
                // 中心点在图片上的相对位置（0-1）
                var centerXRatio = (frameW / 2 - cropOffsetX) / (cropImgNaturalW * oldScale);
                var centerYRatio = (frameH / 2 - cropOffsetY) / (cropImgNaturalH * oldScale);
                // 缩放后保持中心点位置
                var newDispW = cropImgNaturalW * newScale;
                var newDispH = cropImgNaturalH * newScale;
                cropOffsetX = frameW / 2 - centerXRatio * newDispW;
                cropOffsetY = frameH / 2 - centerYRatio * newDispH;
                this.dataset.lastZoom = this.value;
                // 更新百分比显示
                var valEl = document.getElementById('cropZoomVal');
                if (valEl) valEl.textContent = this.value + '%';
                updateCropImage();
            });

            // 拖动图片
            cropFrame.addEventListener('mousedown', function(e) {
                cropDragging = true;
                cropDragStartX = e.clientX;
                cropDragStartY = e.clientY;
                cropStartOffsetX = cropOffsetX;
                cropStartOffsetY = cropOffsetY;
                e.preventDefault();
            });
            document.addEventListener('mousemove', function(e) {
                if (!cropDragging) return;
                var dx = e.clientX - cropDragStartX;
                var dy = e.clientY - cropDragStartY;
                cropOffsetX = cropStartOffsetX + dx;
                cropOffsetY = cropStartOffsetY + dy;
                var frameW = cropFrame.clientWidth;
                var frameH = cropFrame.clientHeight;
                var imgW = cropImage.offsetWidth;
                var imgH = cropImage.offsetHeight;
                // 图片必须覆盖 frame，不允许出现空白边
                cropOffsetX = Math.max(Math.min(cropOffsetX, 0), frameW - imgW);
                cropOffsetY = Math.max(Math.min(cropOffsetY, 0), frameH - imgH);
                cropImage.style.left = cropOffsetX + 'px';
                cropImage.style.top = cropOffsetY + 'px';
            });
            document.addEventListener('mouseup', function() { cropDragging = false; });

            // 滚轮缩放（更符合直觉）
            cropFrame.addEventListener('wheel', function(e) {
                e.preventDefault();
                var delta = e.deltaY < 0 ? 10 : -10;
                var newVal = Math.max(100, Math.min(300, parseInt(cropZoom.value, 10) + delta));
                if (newVal === parseInt(cropZoom.value, 10)) return;
                cropZoom.value = newVal;
                cropZoom.dispatchEvent(new Event('input'));
            }, { passive: false });

            // 触屏拖动
            cropFrame.addEventListener('touchstart', function(e) {
                if (e.touches.length !== 1) return;
                cropDragging = true;
                cropDragStartX = e.touches[0].clientX;
                cropDragStartY = e.touches[0].clientY;
                cropStartOffsetX = cropOffsetX;
                cropStartOffsetY = cropOffsetY;
            });
            document.addEventListener('touchmove', function(e) {
                if (!cropDragging || e.touches.length !== 1) return;
                var dx = e.touches[0].clientX - cropDragStartX;
                var dy = e.touches[0].clientY - cropDragStartY;
                cropOffsetX = cropStartOffsetX + dx;
                cropOffsetY = cropStartOffsetY + dy;
                var frameW = cropFrame.clientWidth;
                var frameH = cropFrame.clientHeight;
                var imgW = cropImage.offsetWidth;
                var imgH = cropImage.offsetHeight;
                cropOffsetX = Math.max(Math.min(cropOffsetX, 0), frameW - imgW);
                cropOffsetY = Math.max(Math.min(cropOffsetY, 0), frameH - imgH);
                cropImage.style.left = cropOffsetX + 'px';
                cropImage.style.top = cropOffsetY + 'px';
                e.preventDefault();
            }, { passive: false });
            document.addEventListener('touchend', function() { cropDragging = false; });

            // 确认裁剪 → canvas → 上传
            if (cropConfirm) cropConfirm.addEventListener('click', function() {
                if (!pendingCoverFile || !currentBookId) { alert('请先选择图片'); return; }
                var frameW = cropFrame.clientWidth;
                var frameH = cropFrame.clientHeight;
                var scale = getActualScale();
                // 计算裁剪区域在原图上的坐标
                var srcX = (-cropOffsetX) / scale;
                var srcY = (-cropOffsetY) / scale;
                var srcW = frameW / scale;
                var srcH = frameH / scale;
                // 边界保护
                if (srcX < 0) srcX = 0;
                if (srcY < 0) srcY = 0;
                if (srcX + srcW > cropImgNaturalW) srcW = cropImgNaturalW - srcX;
                if (srcY + srcH > cropImgNaturalH) srcH = cropImgNaturalH - srcY;
                // 输出 360x480 (3:4)
                var canvas = document.createElement('canvas');
                canvas.width = 360;
                canvas.height = 480;
                var cctx = canvas.getContext('2d');
                cctx.drawImage(cropImage, srcX, srcY, srcW, srcH, 0, 0, 360, 480);
                canvas.toBlob(function(blob) {
                    if (!blob) { alert('裁剪失败'); return; }
                    var fd = new FormData();
                    fd.append('action', 'upload_cover');
                    fd.append('bookId', currentBookId);
                    fd.append('file', blob, 'cover_' + currentBookId + '.jpg');
                    fetch(ctx + '/book', { method: 'POST', body: fd })
                        .then(function(r) { return r.json(); })
                        .then(function(data) {
                            if (data.success) {
                                cropModal.classList.remove('open');
                                pendingCoverFile = null;
                                window.location.reload();
                            } else {
                                alert('上传失败：' + (data.error || '未知错误'));
                            }
                        })
                        .catch(function() { alert('网络错误'); });
                }, 'image/jpeg', 0.9);
            });

            // 关闭裁剪弹窗
            if (cropModalClose) cropModalClose.addEventListener('click', function() { cropModal.classList.remove('open'); });
            if (cropCancel) cropCancel.addEventListener('click', function() { cropModal.classList.remove('open'); });
            cropModal.addEventListener('click', function(e) { if (e.target === cropModal) cropModal.classList.remove('open'); });

            // 移除封面
            if (removeCoverBtn) removeCoverBtn.addEventListener('click', function() {
                if (!confirm('确定移除自定义封面，恢复纯色封面？')) return;
                fetch(ctx + '/book', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'action=remove_cover&bookId=' + currentBookId
                }).then(function(r) { return r.json(); })
                .then(function() { window.location.reload(); });
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

            // ---- 喜爱切换 ----
            window.toggleFavorite = function(btn, bookId) {
                var isFav = btn.getAttribute('data-fav') === '1';
                fetch(ctx + '/book', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'action=toggle_favorite&bookId=' + bookId
                }).then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data.success) {
                        if (isFav) {
                            btn.setAttribute('data-fav', '0');
                            btn.classList.add('book-favorite-off');
                            btn.title = '喜欢';
                        } else {
                            btn.setAttribute('data-fav', '1');
                            btn.classList.remove('book-favorite-off');
                            btn.title = '取消喜欢';
                        }
                        // 刷新页面以更新计数
                        setTimeout(function() { window.location.reload(); }, 300);
                    }
                });
            };
        })();
    </script>
</body>
</html>
