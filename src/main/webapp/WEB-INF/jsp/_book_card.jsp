<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // 此片段由 book.jsp 静态 include 引入
    // 外层循环已定义：Book b; int coverIdx; List<String> bTags; String ctx;
    String statusBadge = "";
    String statusClass = "";
    if ("reading".equals(b.getStatus())) {
        statusBadge = "正在看"; statusClass = "badge-reading";
    } else if ("prepare".equals(b.getStatus())) {
        statusBadge = "准备看"; statusClass = "badge-prepare";
    } else if ("finished".equals(b.getStatus())) {
        statusBadge = "已看完"; statusClass = "badge-finished";
    }
    // 拼 tags JSON 字符串，给编辑弹窗用
    StringBuilder tagsJson = new StringBuilder("[");
    if (bTags != null) {
        for (int ti = 0; ti < bTags.size(); ti++) {
            if (ti > 0) tagsJson.append(",");
            tagsJson.append("\"").append(bTags.get(ti).replace("\"", "\\\"")).append("\"");
        }
    }
    tagsJson.append("]");
    // 转义书名给 JS 字符串用
    String titleForJs = b.getTitle() == null ? "" : b.getTitle().replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
    String coverColor = b.getCoverColor() == null ? "#F59E0B" : b.getCoverColor();
    // 封面背景样式（图片或纯色）
    String coverImagePath = b.getCoverImagePath();
    StringBuilder coverInline = new StringBuilder();
    if (coverImagePath != null && !coverImagePath.isEmpty()) {
        coverInline.append("background-image:url(").append(ctx).append(coverImagePath).append(");background-size:cover;background-position:center;");
    } else if (b.getCoverColor() != null) {
        coverInline.append("background:").append(b.getCoverColor()).append(";");
    }
    // 转义书名给 HTML 用
    String titleForHtml = b.getTitle() == null ? "" : b.getTitle().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
%>
<div class="book-card"
     data-book-id="<%=b.getId()%>"
     data-title="<%=titleForHtml%>"
     data-cover="<%=coverColor%>"
     data-tags='<%=tagsJson%>'
     onclick="window.location.href='<%=ctx%>/reader?bookId=<%=b.getId()%>'">
    <div class="book-cover cover-<%=coverIdx%>" style="<%=coverInline%>">
        <!-- 上半部：封面标题 -->
        <span class="book-cover-title"><%=titleForHtml%></span>

        <%-- 右上角爱心 --%>
        <% if (b.getIsFavorite() == 1) { %>
            <button type="button" class="book-favorite" data-fav="1"
                    onclick="event.stopPropagation(); toggleFavorite(this, <%=b.getId()%>);" title="取消喜欢">
                <svg viewBox="0 0 16 16"><path d="M8 14.2s-5.3-3.7-5.3-7.5c0-1.6 1.1-2.7 2.6-2.7 1.1 0 2 .6 2.7 1.6.7-1 1.6-1.6 2.7-1.6 1.5 0 2.6 1.1 2.6 2.7 0 3.8-5.3 7.5-5.3 7.5z"/></svg>
            </button>
        <% } else { %>
            <button type="button" class="book-favorite book-favorite-off" data-fav="0"
                    onclick="event.stopPropagation(); toggleFavorite(this, <%=b.getId()%>);" title="喜欢">
                <svg viewBox="0 0 16 16"><path d="M8 14.2s-5.3-3.7-5.3-7.5c0-1.6 1.1-2.7 2.6-2.7 1.1 0 2 .6 2.7 1.6.7-1 1.6-1.6 2.7-1.6 1.5 0 2.6 1.1 2.6 2.7 0 3.8-5.3 7.5-5.3 7.5z"/></svg>
            </button>
        <% } %>

        <%-- 左上角编辑按钮 --%>
        <button type="button" class="book-edit-btn"
                onclick="event.stopPropagation(); window.__openBookEditor && window.__openBookEditor(<%=b.getId()%>, '<%=titleForJs%>', '<%=coverColor%>', <%=tagsJson%>);"
                aria-label="编辑">
            <svg viewBox="0 0 16 16" fill="currentColor"><path d="M11.13 1.587a1.6 1.6 0 0 1 2.262 2.262l-7.18 7.18-2.92.658.658-2.92 7.18-7.18zM11.5 2.5l-7 7-.5 2 2-.5 7-7-1-1z"/></svg>
        </button>

        <%-- 下半部：毛玻璃信息层 --%>
        <div class="book-cover-overlay">
            <div class="book-overlay-title"><%=titleForHtml%></div>
            <div class="book-overlay-meta">
                <span><%=b.getChapterCount()%> 章</span>
                <span>&middot;</span>
                <span><%=b.getTotalWords() / 1000%>k 字</span>
            </div>
            <% if (bTags != null && !bTags.isEmpty()) { %>
                <div class="book-overlay-tags">
                    <% for (int _i = 0; _i < bTags.size() && _i < 3; _i++) { %>
                        <span class="book-tag-mini"><%=bTags.get(_i)%></span>
                    <% } %>
                    <% if (bTags.size() > 3) { %>
                        <span style="font-size:10px;color:rgba(255,255,255,0.6)">+<%=bTags.size()-3%></span>
                    <% } %>
                </div>
            <% } else if (!statusBadge.isEmpty()) { %>
                <span class="book-status-badge <%=statusClass%>" style="margin-top:4px;font-size:10px;padding:1px 7px;display:inline-block;"><%=statusBadge%></span>
            <% } %>
        </div>
    </div>
</div>
