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
    String ctx = request.getContextPath();

    // 正文内容，按段落拆分
    String content = chapter.getContent() == null ? "" : chapter.getContent();
    String[] paragraphs = content.split("\r?\n");

    // 把内容伪装成代码注释
    StringBuilder codeLines = new StringBuilder();
    for (int i = 0; i < paragraphs.length; i++) {
        String line = paragraphs[i].trim();
        if (line.isEmpty()) continue;
        // 每行前面加注释标记，模拟不同语言
        switch ((i + 1) % 5) {
            case 1:  codeLines.append("// ").append(escapeHtml(line)); break;
            case 2:  codeLines.append("# ").append(escapeHtml(line)); break;
            case 3:  codeLines.append("-- ").append(escapeHtml(line)); break;
            case 4:  codeLines.append("; ").append(escapeHtml(line)); break;
            default: codeLines.append("* ").append(escapeHtml(line)); break;
        }
        codeLines.append("\n");
    }
%>
<%!
    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%=book.getTitle()%> - 墨读</title>
    <!-- 禁止 F12 开发者工具提示 -->
    <link rel="stylesheet" href="<%=ctx%>/assets/css/variables.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/stealth.css">
</head>
<body class="stealth-root theme-dark" id="stealthRoot">

<!-- ===== 标题栏 ===== -->
<div class="st-titlebar">
    <div class="st-titlebar-left">
        <span class="st-menu-icon st-mi-code"></span>
        <span class="st-titlebar-path">src/<%=book.getTitle()%>/chapter_<%=String.format("%03d", chapterIndex)%>.txt</span>
    </div>
    <div class="st-titlebar-center"></div>
    <div class="st-titlebar-right">
        <button type="button" class="st-tb-btn" id="themeToggleBtn" title="切换主题">
            <span class="st-theme-dot" id="themeDot"></span>
            切换
        </button>
    </div>
</div>

<!-- ===== 主区域 ===== -->
<div class="st-main">
    <!-- 左侧活动栏 -->
    <aside class="st-sidebar" id="stSidebar">
        <div class="st-sb-item active" title="文件管理器">
            <span class="st-sb-icon">F</span>
        </div>
        <div class="st-sb-item" title="搜索">
            <span class="st-sb-icon">S</span>
        </div>
        <div class="st-sb-item" title="源代码管理">
            <span class="st-sb-icon">G</span>
        </div>
        <div class="st-sb-item sb-bottom" title="扩展">
            <span class="st-sb-icon">E</span>
        </div>
    </aside>

    <!-- 侧边栏（文件列表） -->
    <div class="st-explorer" id="stExplorer">
        <div class="st-exp-header">
            <span>EXPLORER</span>
            <span class="st-exp-toggle" id="expToggle">&gt;</span>
        </div>
        <div class="st-exp-body" id="stExpBody">
            <div class="st-exp-section open">
                <div class="st-exp-name"><span class="st-exp-arrow">▼</span> <%=book.getTitle()%></div>
                <div class="st-exp-files">
                    <% for (Chapter c : toc) { %>
                        <a class="st-file <%=c.getChapterIndex() == chapterIndex ? "active" : ""%>"
                           href="<%=ctx%>/stealth?bookId=<%=book.getId()%>&chapter=<%=c.getChapterIndex()%>"
                           title="<%=c.getTitle() == null ? "无标题" : c.getTitle()%>">
                            <span class="st-ext">txt</span>
                            <span class="st-fname"><%=c.getChapterIndex()%>. <%=c.getTitle() == null ? "无标题" : c.getTitle()%></span>
                        </a>
                    <% } %>
                </div>
            </div>
        </div>
    </div>

    <!-- 编辑器区域 -->
    <main class="st-editor-area">
        <!-- 标签栏 -->
        <div class="st-tabs">
            <div class="st-tab active">
                <span class="st-tab-close">&times;</span>
                chapter_<%=chapterIndex%>.txt
            </div>
            <!-- 顶部右上角章切按钮（伪装成 VS Code 的分割按钮） -->
            <div class="st-tab-nav">
                <% if (chapterIndex > 1) { %>
                    <a class="st-tab-nav-btn" href="<%=ctx%>/stealth?bookId=<%=book.getId()%>&chapter=<%=chapterIndex - 1%>" title="上一章 (Alt+←)">&lsaquo; 上一章</a>
                <% } else { %>
                    <span class="st-tab-nav-btn disabled" title="已经是第一章">&lsaquo; 上一章</span>
                <% } %>
                <% if (chapterIndex < totalChapters) { %>
                    <a class="st-tab-nav-btn" href="<%=ctx%>/stealth?bookId=<%=book.getId()%>&chapter=<%=chapterIndex + 1%>" title="下一章 (Alt+→)">下一章 &rsaquo;</a>
                <% } else { %>
                    <span class="st-tab-nav-btn disabled" title="已经是最后一章">下一章 &rsaquo;</span>
                <% } %>
            </div>
        </div>

        <!-- 编辑器主体 -->
        <div class="st-editor">
            <div class="st-gutter-left" id="gutterLeft">
                <%
                    int lc = 0;
                    for (int pi = 0; pi < paragraphs.length; pi++) {
                        String p = paragraphs[pi].trim();
                        if (p.isEmpty()) continue;
                        lc++;
                        // 行号随机跳一些，看起来真实
                        if (lc > 1 && Math.random() > 0.7) { lc += (int)(Math.random() * 3); }
                %>
                    <div class="st-line-no"><%=lc%></div>
                <%
                    }
                %>
            </div>
            <div class="st-code-content" id="codeContent"><pre><%=codeLines.toString()%></pre></div>
        </div>
    </main>
</div>

<!-- ===== 底部状态栏 ===== -->
<div class="st-statusbar">
    <div class="st-st-left">
        <span class="st-st-item">Ln <%=paragraphs.length%>, Col <%=Math.max(1, paragraphs.length > 0 ? paragraphs[0].length() : 1)%></span>
        <span class="st-st-item">空格: 2 | UTF-8</span>
        <span class="st-st-item st-st-lang">Plain Text</span>
    </div>
    <div class="st-st-right">
        <span class="st-st-item">第 <%=chapterIndex%>/<%=totalChapters%> 章</span>
        <span class="st-st-item st-st-git clean">clean</span>
    </div>
</div>

<script>
(function() {
    var root = document.getElementById('stealthRoot');
    var sidebar = document.getElementById('stSidebar');
    var explorer = document.getElementById('stExplorer');
    var expToggle = document.getElementById('expToggle');
    var expBody = document.getElementById('stExpBody');
    var themeToggleBtn = document.getElementById('themeToggleBtn');
    var themeDot = document.getElementById('themeDot');

    // 持久化主题：默认深色
    var savedTheme = null;
    try { savedTheme = localStorage.getItem('moeread_stealth_theme'); } catch(e) {}
    if (savedTheme === 'light') {
        root.classList.remove('theme-dark');
        root.classList.add('theme-light');
        if (themeDot) themeDot.style.background = '#0066b8';
    }

    // ---- 侧边栏收起 ----
    expToggle.addEventListener('click', function() {
        explorer.classList.toggle('collapsed');
        this.textContent = explorer.classList.contains('collapsed') ? '<' : '>';
    });

    // ---- 主题切换 (深色 <-> 浅色) ----
    themeToggleBtn.addEventListener('click', function() {
        root.classList.toggle('theme-dark');
        root.classList.toggle('theme-light');
        var isDark = root.classList.contains('theme-dark');
        if (themeDot) themeDot.style.background = isDark ? '#333' : '#0066b8';
        try { localStorage.setItem('moeread_stealth_theme', isDark ? 'dark' : 'light'); } catch(e) {}
    });

    // ---- 章节跳转 URL（供键盘翻页使用） ----
    var ctx = '<%=ctx%>';
    var bookId = <%=book.getId()%>;
    var chapterIndex = <%=chapterIndex%>;
    var totalChapters = <%=totalChapters%>;
    function gotoChapter(idx) {
        if (idx < 1 || idx > totalChapters) return;
        window.location.href = ctx + '/stealth?bookId=' + bookId + '&chapter=' + idx;
    }

    // ---- 键盘翻页：Alt+Left 上一章 / Alt+Right 下一章 / Esc 返回书架 ----
    document.addEventListener('keydown', function(e) {
        if (e.altKey && e.key === 'ArrowLeft') {
            e.preventDefault();
            gotoChapter(chapterIndex - 1);
        } else if (e.altKey && e.key === 'ArrowRight') {
            e.preventDefault();
            gotoChapter(chapterIndex + 1);
        } else if (e.key === 'Escape') {
            // ESC：假装关掉编辑器，回到书架
            window.location.href = ctx + '/book';
        }
    });
})();
</script>

<style>
/* 内联小图标字体 */
.st-menu-icon::before { content:'\25CE'; font-style:normal; }
.st-sb-icon { font-size:14px !important; font-weight:500; letter-spacing:-0.02em; }
.st-ext { color:#8e949f; font-size:11px; margin-right:6px; flex-shrink:0; }
.st-theme-dot {
    width:10px;height:10px;border-radius:50%;background:#333;display:inline-block;
    transition:background 0.15s;margin-right:4px;vertical-align:middle;
}
/* 章节切换按钮（藏在标签栏右侧，伪装成 VS Code tab 操作按钮） */
.st-tab-nav {
    margin-left:auto;
    display:flex;
    align-items:center;
    gap:2px;
    padding-right:8px;
}
.st-tab-nav-btn {
    font-size:11px;
    color:#8e949f;
    padding:3px 8px;
    border-radius:3px;
    cursor:pointer;
    text-decoration:none;
    transition:background 0.12s, color 0.12s;
    user-select:none;
    white-space:nowrap;
}
.st-tab-nav-btn:hover { background:rgba(255,255,255,0.08); color:#c9d1d9; }
.st-tab-nav-btn.disabled { opacity:0.35; cursor:default; }
.st-tab-nav-btn.disabled:hover { background:transparent; color:#8e949f; }
.theme-light .st-tab-nav-btn { color:#616868; }
.theme-light .st-tab-nav-btn:hover { background:rgba(0,0,0,0.06); color:#3b3b3b; }
.theme-light .st-tab-nav-btn.disabled:hover { background:transparent; color:#616868; }
</style>
</body>
</html>
