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

    String content = chapter.getContent() == null ? "" : chapter.getContent();
    String[] rawParas = content.split("\r?\n");

    // 过滤空段落，生成代码字符串
    List<String> paras = new java.util.ArrayList<>();
    for (String p : rawParas) {
        String t = p.trim();
        if (!t.isEmpty()) paras.add(t);
    }

    // 生成语法高亮的代码行
    List<String> codeLines = new java.util.ArrayList<>();
    // 静态头部
    codeLines.add("<span class=\"syn-kw\">import</span> { <span class=\"syn-type\">Logger</span> } <span class=\"syn-kw\">from</span> <span class=\"syn-str\">'./utils/logger'</span>;");
    codeLines.add("<span class=\"syn-kw\">import</span> { <span class=\"syn-fn\">formatOutput</span> } <span class=\"syn-kw\">from</span> <span class=\"syn-str\">'./formatter'</span>;");
    codeLines.add("");
    codeLines.add("<span class=\"syn-kw\">const</span> <span class=\"syn-prop\">logger</span> = <span class=\"syn-kw\">new</span> <span class=\"syn-fn\">Logger</span>(<span class=\"syn-str\">'chapter'</span>);");
    codeLines.add("");
    codeLines.add("<span class=\"syn-kw\">const</span> <span class=\"syn-prop\">config</span> = {");
    codeLines.add("    <span class=\"syn-prop\">index</span>: <span class=\"syn-num\">" + chapterIndex + "</span>,");
    codeLines.add("    <span class=\"syn-prop\">title</span>: <span class=\"syn-str\">\"" + escapeHtml(chapter.getTitle() == null ? "" : chapter.getTitle()) + "\"</span>,");
    codeLines.add("    <span class=\"syn-prop\">lines</span>: [");
    // 小说内容作为字符串数组元素
    for (String p : paras) {
        String escaped = escapeHtml(p);
        // 转义引号
        escaped = escaped.replace("\"", "&quot;");
        codeLines.add("        <span class=\"syn-str\">\"" + escaped + "\"</span>,");
    }
    codeLines.add("    ]");
    codeLines.add("};");
    codeLines.add("");
    codeLines.add("<span class=\"syn-kw\">class</span> <span class=\"syn-type\">Processor</span> {");
    codeLines.add("    <span class=\"syn-fn\">constructor</span>(<span class=\"syn-kw\">private</span> <span class=\"syn-prop\">config</span>: <span class=\"syn-type\">any</span>) {}");
    codeLines.add("");
    codeLines.add("    <span class=\"syn-kw\">async</span> <span class=\"syn-fn\">run</span>(): <span class=\"syn-type\">Promise</span>&lt;<span class=\"syn-kw\">void</span>&gt; {");
    codeLines.add("        <span class=\"syn-kw\">for</span> (<span class=\"syn-kw\">const</span> <span class=\"syn-prop\">line</span> <span class=\"syn-kw\">of</span> <span class=\"syn-kw\">this</span>.<span class=\"syn-prop\">config</span>.<span class=\"syn-prop\">lines</span>) {");
    codeLines.add("            <span class=\"syn-kw\">await</span> <span class=\"syn-fn\">formatOutput</span>(<span class=\"syn-prop\">line</span>);");
    codeLines.add("        }");
    codeLines.add("        <span class=\"syn-prop\">logger</span>.<span class=\"syn-fn\">info</span>(<span class=\"syn-str\">'Done'</span>);");
    codeLines.add("    }");
    codeLines.add("}");
    codeLines.add("");
    codeLines.add("<span class=\"syn-kw\">new</span> <span class=\"syn-type\">Processor</span>(<span class=\"syn-prop\">config</span>).<span class=\"syn-fn\">run</span>();");

    int totalLines = codeLines.size();
    String fileName = "chapter_" + String.format("%03d", chapterIndex) + ".ts";
    String bookTitle = book.getTitle() == null ? "moeread" : book.getTitle();
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
    <title><%=fileName%> - <%=bookTitle%> - Visual Studio Code</title>
    <link rel="stylesheet" href="<%=ctx%>/assets/css/stealth.css">
</head>
<body class="stealth-root theme-dark" id="stealthRoot">

<!-- ===== 标题栏 ===== -->
<div class="st-titlebar">
    <div class="st-titlebar-left">
        <span class="st-vscode-logo"></span>
        <div class="st-menubar">
            <span class="st-menu-item">文件</span>
            <span class="st-menu-item">编辑</span>
            <span class="st-menu-item">选择</span>
            <span class="st-menu-item">查看</span>
            <span class="st-menu-item">转到</span>
            <span class="st-menu-item">运行</span>
            <span class="st-menu-item">终端</span>
            <span class="st-menu-item">帮助</span>
        </div>
    </div>
    <div class="st-titlebar-center">
        <%=fileName%> - <%=bookTitle%> - Visual Studio Code
    </div>
    <div class="st-titlebar-right">
        <button class="st-win-btn st-win-min" title="最小化"></button>
        <button class="st-win-btn st-win-max" title="最大化"></button>
        <button class="st-win-btn st-win-close" title="关闭" id="stCloseBtn"></button>
    </div>
</div>

<!-- ===== 主区域 ===== -->
<div class="st-main">
    <!-- 活动栏 -->
    <aside class="st-activitybar">
        <div class="st-act-item active" title="资源管理器">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M17.5 0h-9L7 1.5V6H2.5L1 7.5v15.07L2.5 24h12.07L16 22.57V18h4.7l1.3-1.43V4.5L17.5 0zm0 2.12l2.38 2.38H17.5V2.12zm-3 20.38h-12v-15H7v9.07L8.5 18h6v4.5zm6-6h-12v-15H16V6h4.5v10.5z"/></svg>
        </div>
        <div class="st-act-item" title="搜索">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M15.25 0a8.25 8.25 0 0 0-6.18 13.72L1 22.88l1.12 1L10.28 14.92A8.24 8.24 0 1 0 15.25 0zm0 15a6.75 6.75 0 1 1 0-13.5 6.75 6.75 0 0 1 0 13.5z"/></svg>
        </div>
        <div class="st-act-item" title="源代码管理">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M21.007 8.222A3.738 3.738 0 0 0 15.045 5.2a3.737 3.737 0 0 0 1.156 6.583 2.99 2.99 0 0 1-2.668 1.67h-2.99a4.456 4.456 0 0 0-2.989 1.165V5.4a3.737 3.737 0 1 0-1.494 0v13.2a3.737 3.737 0 1 0 1.494 0v-2.33a2.99 2.99 0 0 1 2.99-2.99h2.99a4.484 4.484 0 0 0 4.284-3.165 3.737 3.737 0 0 0 2.799-3.615zM4.565 3.738a2.242 2.242 0 1 1 4.484 0 2.242 2.242 0 0 1-4.484 0zm4.484 16.524a2.242 2.242 0 1 1-4.484 0 2.242 2.242 0 0 1 4.484 0zm8.221-9.339a2.242 2.242 0 1 1 0-4.485 2.242 2.242 0 0 1 0 4.485z"/></svg>
        </div>
        <div class="st-act-item" title="运行和调试">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M8.995 5.5v13l9-6.5-9-6.5z"/></svg>
        </div>
        <div class="st-act-item" title="扩展">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M13.5 1.5L11 4v3H8v3H5l-2.5 2.5V17H8v3.5L10.5 23H14v-3h3v-3h3l2.5-2.5V9H17V6h-3.5V1.5zm0 2.12l5.38 5.38v6l-5.38 5.38v-2.5H10v-3H7v-3h3V8.62h3.5V3.62z"/></svg>
        </div>
        <div class="st-act-item st-act-bottom" title="管理" id="stSettingsBtn">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M19.85 8.75l-1.43-2.47 1.12-3.4-2.36-2.36-3.4 1.12-2.47-1.43-.9-3.21H6.58l-.9 3.21-2.47 1.43L-.2 2.52l-2.36 2.36 1.12 3.4-1.43 2.47-3.21.9v3.32l3.21.9 1.43 2.47-1.12 3.4 2.36 2.36 3.4-1.12 2.47 1.43.9 3.21h3.32l.9-3.21 2.47-1.43 3.4 1.12 2.36-2.36-1.12-3.4 1.43-2.47 3.21-.9V9.65l-3.21-.9zM12 15.5A3.5 3.5 0 1 1 12 8.5a3.5 3.5 0 0 1 0 7z"/></svg>
        </div>
    </aside>

    <!-- 侧边栏（文件列表） -->
    <div class="st-sidebar" id="stSidebar">
        <div class="st-sb-header">
            <span>资源管理器</span>
            <span class="st-sb-actions">
                <span class="st-sb-action" id="sbCollapse" title="收起">&gt;</span>
            </span>
        </div>
        <div class="st-sb-body">
            <div class="st-sb-section">
                <div class="st-sb-section-header">
                    <span class="st-sb-arrow">&#9660;</span>
                    <span class="st-sb-name"><%=bookTitle%></span>
                </div>
                <div class="st-sb-files">
                    <div class="st-sb-folder">
                        <span class="st-sb-arrow">&#9660;</span>
                        <span class="st-sb-folder-icon">src</span>
                    </div>
                    <div class="st-sb-file-list">
                        <% for (Chapter c : toc) { %>
                            <a class="st-sb-file <%=c.getChapterIndex() == chapterIndex ? "active" : ""%>"
                               href="<%=ctx%>/stealth?bookId=<%=book.getId()%>&chapter=<%=c.getChapterIndex()%>">
                                <span class="st-file-icon ts">ts</span>
                                <span class="st-sb-fname">chapter_<%=String.format("%03d", c.getChapterIndex())%>.ts</span>
                            </a>
                        <% } %>
                    </div>
                    <div class="st-sb-folder">
                        <span class="st-sb-arrow">&#9658;</span>
                        <span class="st-sb-folder-icon">test</span>
                    </div>
                    <div class="st-sb-file">
                        <span class="st-file-icon ts">ts</span>
                        <span class="st-sb-fname">processor.test.ts</span>
                    </div>
                    <div class="st-sb-file">
                        <span class="st-file-icon json">{}</span>
                        <span class="st-sb-fname">package.json</span>
                    </div>
                    <div class="st-sb-file">
                        <span class="st-file-icon json">{}</span>
                        <span class="st-sb-fname">tsconfig.json</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 编辑器区域 -->
    <main class="st-editor-area">
        <!-- 标签栏 -->
        <div class="st-tabs">
            <div class="st-tab active">
                <span class="st-tab-icon ts">ts</span>
                <span class="st-tab-name"><%=fileName%></span>
                <span class="st-tab-close">&times;</span>
            </div>
            <div class="st-tab-nav">
                <% if (chapterIndex > 1) { %>
                    <a class="st-tab-nav-btn" href="<%=ctx%>/stealth?bookId=<%=book.getId()%>&chapter=<%=chapterIndex - 1%>" title="上一章 (Alt+Left)">&#8249;</a>
                <% } else { %>
                    <span class="st-tab-nav-btn disabled">&#8249;</span>
                <% } %>
                <% if (chapterIndex < totalChapters) { %>
                    <a class="st-tab-nav-btn" href="<%=ctx%>/stealth?bookId=<%=book.getId()%>&chapter=<%=chapterIndex + 1%>" title="下一章 (Alt+Right)">&#8250;</a>
                <% } else { %>
                    <span class="st-tab-nav-btn disabled">&#8250;</span>
                <% } %>
            </div>
        </div>

        <!-- 面包屑 -->
        <div class="st-breadcrumb">
            <span class="st-bc-item"><%=bookTitle%></span>
            <span class="st-bc-sep">&rsaquo;</span>
            <span class="st-bc-item">src</span>
            <span class="st-bc-sep">&rsaquo;</span>
            <span class="st-bc-item active"><%=fileName%></span>
        </div>

        <!-- 编辑器主体 -->
        <div class="st-editor" id="stEditor">
            <!-- 行号 -->
            <div class="st-gutter" id="stGutter">
                <%
                    int lc = 0;
                    for (int li = 0; li < codeLines.size(); li++) {
                        lc++;
                        if (lc > 5 && Math.random() > 0.85) { lc += (int)(Math.random() * 3); }
                %>
                    <div class="st-line-no"><%=lc%></div>
                <%
                    }
                %>
            </div>
            <!-- 代码内容 -->
            <div class="st-code-content" id="codeContent"><pre><%
                for (int li = 0; li < codeLines.size(); li++) {
                    out.print(codeLines.get(li));
                    if (li < codeLines.size() - 1) out.print("\n");
                }
            %></pre></div>
            <!-- Minimap -->
            <div class="st-minimap" id="stMinimap">
                <%
                    int mc = 0;
                    for (int li = 0; li < codeLines.size(); li++) {
                        mc++;
                        String line = codeLines.get(li);
                        String miniClass = "st-mm-line";
                        if (line.contains("syn-str")) miniClass += " st-mm-str";
                        else if (line.contains("syn-kw")) miniClass += " st-mm-kw";
                        else if (line.contains("syn-comment")) miniClass += " st-mm-comment";
                        else if (line.trim().isEmpty()) miniClass += " st-mm-blank";
                        int width = Math.min(100, Math.max(10, line.length() * 2));
                %>
                    <div class="<%=miniClass%>" style="width:<%=width%>%"></div>
                <%
                    }
                %>
            </div>
        </div>
    </main>
</div>

<!-- ===== 状态栏 ===== -->
<div class="st-statusbar">
    <div class="st-st-left">
        <span class="st-st-item st-st-branch">
            <svg viewBox="0 0 16 16" fill="currentColor" width="13" height="13"><path d="M11.75 2.5a.75.75 0 1 0 0 1.5.75.75 0 0 0 0-1.5zm-2.25.75a2.25 2.25 0 1 1 3 2.122v5.256a2.251 2.251 0 1 1-1.5 0V5.372A2.25 2.25 0 0 1 9.5 3.25zM4.25 2.5a.75.75 0 1 0 0 1.5.75.75 0 0 0 0-1.5zM2 3.25a2.25 2.25 0 1 1 3 2.122v5.256a2.251 2.251 0 1 1-1.5 0V5.372A2.25 2.25 0 0 1 2 3.25zm5.677 7.566a2.25 2.25 0 1 0 1.5 0V8.372a2.25 2.25 0 0 0-.5-.073H7.25v1.517z"/></svg>
            main
        </span>
        <span class="st-st-item">
            <svg viewBox="0 0 16 16" fill="currentColor" width="13" height="13"><path d="M3.5 1.75v11.5c0 .09.048.173.126.217a.75.75 0 0 1-.752 1.298A1.748 1.748 0 0 1 2 13.25V1.75C2 .784 2.784 0 3.75 0h5.586c.46 0 .903.184 1.229.51l2.914 2.914A1.75 1.75 0 0 1 14 4.586v8.664c0 .09.048.173.126.217a.75.75 0 0 1-.752 1.298A1.748 1.748 0 0 1 12.5 13.25V4.586L9.414 1.5H3.75a.25.25 0 0 0-.25.25z"/></svg>
            0 <svg viewBox="0 0 16 16" fill="currentColor" width="13" height="13" style="margin-left:6px"><path d="M3.72 2.22A.75.75 0 0 1 4.78 1.16L6.84 3.22a.75.75 0 0 1 0 1.06L4.78 6.34a.75.75 0 0 1-1.06-1.06l.97-.97H3.5A1.75 1.75 0 0 0 1.75 6.1v.65a.75.75 0 0 1-1.5 0V6.1A3.25 3.25 0 0 1 3.5 2.81h1.19l-.97-.97zM8 6a1 1 0 0 1 1 1v.5H8V6zm4.5 8a.5.5 0 0 1-.5.5H4a.5.5 0 0 1-.5-.5V8.83A2.99 2.99 0 0 1 5 8h6a2.99 2.99 0 0 1 1.5.83V14zm-7-6a2 2 0 0 0-2 2v3.75c0 .41.34.75.75.75h6.5a.75.75 0 0 0 .75-.75V10a2 2 0 0 0-2-2H5.5z"/></svg> 0
        </span>
        <span class="st-st-item">Ln <%=Math.min(totalLines, paras.size() > 0 ? paras.size() : 1)%>, Col 1</span>
    </div>
    <div class="st-st-right">
        <span class="st-st-item">空格: 4</span>
        <span class="st-st-item">UTF-8</span>
        <span class="st-st-item">LF</span>
        <span class="st-st-item st-st-lang">TypeScript</span>
        <span class="st-st-item st-st-chapter">第 <%=chapterIndex%>/<%=totalChapters%> 章</span>
        <span class="st-st-item st-st-theme-toggle" id="themeToggleBtn" title="切换主题">
            <svg viewBox="0 0 16 16" fill="currentColor" width="13" height="13"><path d="M8 1.5a6.5 6.5 0 1 0 0 13 .5.5 0 0 1 0-1 5.5 5.5 0 0 1 0-11 .5.5 0 0 1 0-1z"/><path d="M8 4a4 4 0 1 0 0 8V4z"/></svg>
        </span>
    </div>
</div>

<script>
(function() {
    var root = document.getElementById('stealthRoot');
    var sbCollapse = document.getElementById('sbCollapse');
    var sidebar = document.getElementById('stSidebar');
    var themeToggleBtn = document.getElementById('themeToggleBtn');
    var stCloseBtn = document.getElementById('stCloseBtn');
    var stSettingsBtn = document.getElementById('stSettingsBtn');

    // 持久化主题
    var savedTheme = null;
    try { savedTheme = localStorage.getItem('moeread_stealth_theme'); } catch(e) {}
    if (savedTheme === 'light') {
        root.classList.remove('theme-dark');
        root.classList.add('theme-light');
    }

    // 侧边栏收起
    if (sbCollapse) sbCollapse.addEventListener('click', function() {
        sidebar.classList.toggle('collapsed');
        this.textContent = sidebar.classList.contains('collapsed') ? '<' : '>';
    });

    // 主题切换（状态栏小按钮 + 活动栏底部齿轮）
    function toggleTheme() {
        root.classList.toggle('theme-dark');
        root.classList.toggle('theme-light');
        var isDark = root.classList.contains('theme-dark');
        try { localStorage.setItem('moeread_stealth_theme', isDark ? 'dark' : 'light'); } catch(e) {}
    }
    if (themeToggleBtn) themeToggleBtn.addEventListener('click', toggleTheme);
    if (stSettingsBtn) stSettingsBtn.addEventListener('click', toggleTheme);

    // 关闭按钮 = 返回书架
    if (stCloseBtn) stCloseBtn.addEventListener('click', function() {
        window.location.href = '<%=ctx%>/book';
    });

    // 章节跳转
    var ctx = '<%=ctx%>';
    var bookId = <%=book.getId()%>;
    var chapterIndex = <%=chapterIndex%>;
    var totalChapters = <%=totalChapters%>;
    function gotoChapter(idx) {
        if (idx < 1 || idx > totalChapters) return;
        window.location.href = ctx + '/stealth?bookId=' + bookId + '&chapter=' + idx;
    }

    // 键盘
    document.addEventListener('keydown', function(e) {
        if (e.altKey && e.key === 'ArrowLeft') { e.preventDefault(); gotoChapter(chapterIndex - 1); }
        else if (e.altKey && e.key === 'ArrowRight') { e.preventDefault(); gotoChapter(chapterIndex + 1); }
        else if (e.key === 'Escape') { window.location.href = ctx + '/book'; }
    });

    // 同步行号和代码滚动
    var editor = document.getElementById('stEditor');
    var gutter = document.getElementById('stGutter');
    var codeContent = document.getElementById('codeContent');
    var minimap = document.getElementById('stMinimap');
    if (codeContent && gutter) {
        codeContent.addEventListener('scroll', function() {
            gutter.scrollTop = codeContent.scrollTop;
            if (minimap) minimap.scrollTop = codeContent.scrollTop * 0.15;
        });
    }
})();
</script>
</body>
</html>
