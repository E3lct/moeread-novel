<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.moeread.model.Book" %>
<%@ page import="com.moeread.model.Chapter" %>
<%@ page import="java.util.List" %>
<%
    Book book = (Book) request.getAttribute("book");
    Chapter chapter = (Chapter) request.getAttribute("chapter");
    @SuppressWarnings("unchecked")
    List<Chapter> toc = (List<Chapter>) request.getAttribute("toc");
    int chapterIndex = (Integer) request.getAttribute("chapterIndex");
    int totalChapters = (Integer) request.getAttribute("totalChapters");
    int scrollPercent = request.getAttribute("scrollPercent") != null ? (Integer) request.getAttribute("scrollPercent") : 0;
    boolean hasPrev = (Boolean) request.getAttribute("hasPrev");
    boolean hasNext = (Boolean) request.getAttribute("hasNext");
    String ctx = request.getContextPath();

    String content = chapter.getContent() == null ? "" : chapter.getContent();
    String[] paragraphs = content.split("\r?\n");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%=chapter.getTitle() == null ? "阅读" : chapter.getTitle()%> - <%=book.getTitle()%> - 墨读</title>
    <link rel="stylesheet" href="<%=ctx%>/assets/css/variables.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/layout.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/reader.css">
</head>
<body>
<div class="reader-root" id="readerRoot"
     data-book-id="<%=book.getId()%>"
     data-chapter-index="<%=chapterIndex%>">

    <!-- 顶部栏：返回 + 章节标题 + 目录切换 -->
    <div class="reader-topbar">
        <a class="reader-btn" href="<%=ctx%>/book" title="返回书架">
            <svg viewBox="0 0 16 16" fill="currentColor"><path d="M10.78 13.22a.75.75 0 0 1-1.06 1.06l-4.5-4.5a.75.75 0 0 1 0-1.06l4.5-4.5a.75.75 0 1 1 1.06 1.06L6.81 9.25l3.97 3.97z"/></svg>
            书架
        </a>

        <!-- 目录收起/展开按钮（< > 符号） -->
        <button type="button" class="toc-toggle-btn" id="tocToggleBtn" title="目录">
            <span class="toc-toggle-icon" id="tocToggleIcon">&lt;</span>
        </button>

        <div class="reader-topbar-title"><%=chapter.getTitle() == null ? "第"+chapterIndex+"章" : chapter.getTitle()%></div>

        <!-- 摸鱼模式入口（伪装成 VS Code 界面阅读） -->
        <a class="reader-stealth-btn" href="<%=ctx%>/stealth?bookId=<%=book.getId()%>&chapter=<%=chapterIndex%>" title="摸鱼模式 (VS Code 伪装界面)">
            摸鱼
        </a>
    </div>

    <div class="reader-body">
        <!-- 章节目录 - 固定定位，不跟随滚动 -->
        <aside class="reader-toc" id="readerToc">
            <div class="toc-header">目录 · 共 <%=totalChapters%> 章</div>
            <div class="toc-scroll">
                <ul class="toc-list">
                    <% for (Chapter c : toc) { %>
                        <li class="toc-item <%=c.getChapterIndex() == chapterIndex ? "active" : ""%>"
                            data-chapter="<%=c.getChapterIndex()%>"
                            onclick="goToChapter(<%=c.getChapterIndex()%>)">
                            <%=c.getChapterIndex()%>. <%=c.getTitle() == null ? "无标题" : c.getTitle()%>
                        </li>
                    <% } %>
                </ul>
            </div>
        </aside>

        <!-- 正文区 -->
        <div class="reader-content-wrap" id="readerScroll">
            <article class="reader-content">
                <h1 class="reader-chapter-title"><%=chapter.getTitle() == null ? "第"+chapterIndex+"章" : chapter.getTitle()%></h1>
                <div class="reader-text">
                    <% for (String p : paragraphs) {
                        String t = p.trim();
                        if (t.isEmpty()) continue;
                    %>
                        <p><%=t%></p>
                    <% } %>
                </div>

                <div class="reader-nav">
                    <button type="button" class="reader-nav-btn" id="prevBtn" <%=hasPrev ? "" : "disabled"%>>
                        &larr; 上一章
                    </button>
                    <span class="reader-nav-info"><%=chapterIndex%> / <%=totalChapters%></span>
                    <button type="button" class="reader-nav-btn" id="nextBtn" <%=hasNext ? "" : "disabled"%>>
                        下一章 &rarr;
                    </button>
                </div>

                <div class="reader-shortcut-hint">键盘 &larr; &rarr; 翻页</div>
            </article>
        </div>
    </div>

    <!-- 设置浮窗按钮（右下角唯一入口） -->
    <button type="button" class="reader-settings-fab" id="settingsFab" aria-label="阅读设置">
        <svg viewBox="0 0 16 16" fill="currentColor">
            <path d="M8 4a4 4 0 1 0 0 8 4 4 0 0 0 0-8zm0 1.5a2.5 2.5 0 1 1 0 5 2.5 2.5 0 0 1 0-5z"/>
            <path d="M13.5 8a5.5 5.5 0 0 1-.12 1.13l1.18.92-1 1.73-1.4-.46a5.5 5.5 0 0 1-1.95 1.13l-.26 1.46h-2l-.26-1.46a5.5 5.5 0 0 1-1.95-1.13l-1.4.46-1-1.73 1.18-.92A5.5 5.5 0 0 1 2.5 8c0-.39.04-.76.12-1.13l-1.18-.92 1-1.73 1.4.46A5.5 5.5 0 0 1 5.79 3.55l.26-1.46h2l.26 1.46a5.5 5.5 0 0 1 1.95 1.13l1.4-.46 1 1.73-1.18.92c.08.37.12.74.12 1.13z"/>
        </svg>
    </button>

    <!-- 设置面板 -->
    <div class="reader-settings-panel" id="settingsPanel">
        <div class="settings-group">
            <div class="settings-label">字号</div>
            <div class="settings-controls">
                <button type="button" class="settings-step-btn" data-act="font-dec">-</button>
                <span class="settings-value" id="fontVal">18px</span>
                <button type="button" class="settings-step-btn" data-act="font-inc">+</button>
            </div>
        </div>
        <div class="settings-group">
            <div class="settings-label">行距</div>
            <div class="settings-controls">
                <button type="button" class="settings-step-btn" data-act="lh-dec">-</button>
                <span class="settings-value" id="lhVal">1.9</span>
                <button type="button" class="settings-step-btn" data-act="lh-inc">+</button>
            </div>
        </div>
        <div class="settings-group">
            <div class="settings-label">主题</div>
            <div class="settings-controls theme-swatches">
                <button type="button" class="theme-swatch" data-theme="default" style="background:#FAF8F3" title="默认"></button>
                <button type="button" class="theme-swatch" data-theme="paper" style="background:#F0E6D2" title="纸黄"></button>
                <button type="button" class="theme-swatch" data-theme="green" style="background:#E8EDE0" title="护眼绿"></button>
                <button type="button" class="theme-swatch" data-theme="night" style="background:#1A1714" title="夜间"></button>
            </div>
        </div>
    </div>
</div>

<script>
(function() {
    var ctx = '<%=ctx%>';
    var bookId = <%=book.getId()%>;
    var chapterIndex = <%=chapterIndex%>;
    var scrollRestore = <%=scrollPercent%>;
    var hasPrev = <%=hasPrev%>;
    var hasNext = <%=hasNext%>;

    var root = document.getElementById('readerRoot');
    var scrollWrap = document.getElementById('readerScroll');
    var toc = document.getElementById('readerToc');
    var tocToggleBtn = document.getElementById('tocToggleBtn');
    var tocToggleIcon = document.getElementById('tocToggleIcon');
    var settingsFab = document.getElementById('settingsFab');
    var settingsPanel = document.getElementById('settingsPanel');
    var fontVal = document.getElementById('fontVal');
    var lhVal = document.getElementById('lhVal');
    var prevBtn = document.getElementById('prevBtn');
    var nextBtn = document.getElementById('nextBtn');

    // ---- 持久化 ----
    var fontSize = parseInt(localStorage.getItem('moeread_font') || '18', 10);
    var lineHeight = parseFloat(localStorage.getItem('moeread_lh') || '1.9');
    var theme = localStorage.getItem('moeread_theme') || 'default';

    function applyAll() {
        root.style.setProperty('--reader-font-size', fontSize + 'px');
        root.style.setProperty('--reader-line-height', lineHeight.toFixed(1));
        fontVal.textContent = fontSize + 'px';
        lhVal.textContent = lineHeight.toFixed(1);
        root.classList.remove('theme-night', 'theme-paper', 'theme-green');
        if (theme !== 'default') root.classList.add('theme-' + theme);
        var sws = settingsPanel.querySelectorAll('.theme-swatch');
        for (var i = 0; i < sws.length; i++) {
            sws[i].classList.toggle('selected', sws[i].getAttribute('data-theme') === theme);
        }
    }
    applyAll();

    // ---- 目录展开/收起 (< > 切换) ----
    var tocCollapsed = false;
    if (window.innerWidth < 900) { tocCollapsed = true; toc.classList.add('collapsed'); }
    function updateTocIcon() {
        if (tocCollapsed) {
            tocToggleIcon.innerHTML = '&gt;';
        } else {
            tocToggleIcon.innerHTML = '&lt;';
        }
    }

    tocToggleBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        tocCollapsed = !tocCollapsed;
        toc.classList.toggle('collapsed', tocCollapsed);
        updateTocIcon();
    });
    updateTocIcon();

    // ---- 设置面板 ----
    function toggleSettings(open) {
        if (open === undefined) open = !settingsPanel.classList.contains('open');
        settingsPanel.classList.toggle('open', open);
    }
    settingsFab.addEventListener('click', function(e) { e.stopPropagation(); toggleSettings(); });
    document.addEventListener('click', function(e) {
        if (!settingsPanel.contains(e.target) && !settingsFab.contains(e.target)) {
            settingsPanel.classList.remove('open');
        }
    });

    // ---- 字号 / 行距 ----
    settingsPanel.addEventListener('click', function(e) {
        var btn = e.target.closest('.settings-step-btn');
        if (!btn) return;
        var act = btn.getAttribute('data-act');
        if (act === 'font-inc' && fontSize < 28) fontSize++;
        else if (act === 'font-dec' && fontSize > 14) fontSize--;
        else if (act === 'lh-inc') lineHeight = Math.min(2.6, lineHeight + 0.1);
        else if (act === 'lh-dec') lineHeight = Math.max(1.3, lineHeight - 0.1);
        localStorage.setItem('moeread_font', fontSize);
        localStorage.setItem('moeread_lh', lineHeight.toFixed(1));
        applyAll();
    });

    // ---- 主题 ----
    settingsPanel.addEventListener('click', function(e) {
        var sw = e.target.closest('.theme-swatch');
        if (!sw) return;
        theme = sw.getAttribute('data-theme');
        localStorage.setItem('moeread_theme', theme);
        applyAll();
    });

    // ---- 翻页 / 章节跳转 ----
    function goChapter(idx) {
        if (idx < 1) return;
        saveProgress(idx, 0, function() {
            window.location.href = ctx + '/reader?bookId=' + bookId + '&chapter=' + idx;
        });
    }
    // TOC 章节跳转（先保存当前进度）
    function goToChapter(idx) {
        if (idx === chapterIndex) return;
        saveProgressBeacon();
        window.location.href = ctx + '/reader?bookId=' + bookId + '&chapter=' + idx;
    }
    window.goToChapter = goToChapter;
    if (prevBtn) prevBtn.addEventListener('click', function() { goChapter(chapterIndex - 1); });
    if (nextBtn) nextBtn.addEventListener('click', function() { goChapter(chapterIndex + 1); });

    // ---- 键盘翻页 ----
    document.addEventListener('keydown', function(e) {
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;
        if (e.key === 'ArrowLeft' && hasPrev) { e.preventDefault(); goChapter(chapterIndex - 1); }
        else if (e.key === 'ArrowRight' && hasNext) { e.preventDefault(); goChapter(chapterIndex + 1); }
    });

    // ---- 进度保存 ----
    var lastSave = 0;
    var saveTimer = null;
    function saveProgress(idx, scrollPercent, callback) {
        var pct = scrollPercent != null ? scrollPercent : Math.round(scrollWrap.scrollTop / Math.max(1, scrollWrap.scrollHeight - scrollWrap.clientHeight) * 100);
        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;
        var body = 'action=save_progress&bookId=' + bookId + '&chapterIndex=' + (idx || chapterIndex) + '&scrollPercent=' + pct;
        fetch(ctx + '/reader', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body
        }).then(function() { if (callback) callback(); }).catch(function() { if (callback) callback(); });
    }

    // 用 sendBeacon 可靠地保存（页面卸载时）
    function saveProgressBeacon() {
        var pct = Math.round(scrollWrap.scrollTop / Math.max(1, scrollWrap.scrollHeight - scrollWrap.clientHeight) * 100);
        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;
        var params = new URLSearchParams();
        params.append('action', 'save_progress');
        params.append('bookId', bookId);
        params.append('chapterIndex', chapterIndex);
        params.append('scrollPercent', pct);
        try {
            navigator.sendBeacon(ctx + '/reader', params);
        } catch (ex) {}
    }

    // 滚动节流保存（2秒间隔）
    scrollWrap.addEventListener('scroll', function() {
        clearTimeout(saveTimer);
        saveTimer = setTimeout(function() {
            var now = Date.now();
            if (now - lastSave > 2000) { lastSave = now; saveProgress(chapterIndex, null); }
        }, 500);
    });

    // 定时保存（每10秒强制保存一次，防止只滚不停的情况遗漏）
    setInterval(function() {
        saveProgress(chapterIndex, null);
        lastSave = Date.now();
    }, 10000);

    // 页面退出/隐藏时保存
    window.addEventListener('beforeunload', saveProgressBeacon);
    document.addEventListener('visibilitychange', function() {
        if (document.visibilityState === 'hidden') saveProgressBeacon();
    });
    window.addEventListener('pagehide', saveProgressBeacon);

    // ---- 目录自动定位到当前章节 ----
    (function scrollTocToActive() {
        var tocScroll = document.querySelector('.toc-scroll');
        var activeItem = document.querySelector('.toc-item.active');
        if (tocScroll && activeItem) {
            var itemTop = activeItem.offsetTop;
            var scrollMid = tocScroll.clientHeight / 2;
            tocScroll.scrollTop = itemTop - scrollMid + activeItem.offsetHeight / 2;
        }
    })();

    // ---- 恢复上次滚动位置（等 DOM 渲染完成） ----
    function restoreScroll() {
        if (scrollRestore > 0 && scrollWrap) {
            var maxScroll = scrollWrap.scrollHeight - scrollWrap.clientHeight;
            if (maxScroll > 0) {
                scrollWrap.scrollTop = Math.round(maxScroll * scrollRestore / 100);
            }
        }
    }
    // 双重 rAF 确保浏览器完成布局计算后再设置 scrollTop
    requestAnimationFrame(function() {
        requestAnimationFrame(restoreScroll);
    });
})();
</script>
</body>
</html>
