<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.moeread.model.*" %>
<%@ page import="java.util.List" %>
<%!
    // 字数格式化：超过万用"万"为单位
    String fmtWords(int w) {
        if (w >= 10000) return String.format("%.1f", w / 10000.0) + " 万字";
        return w + " 字";
    }
%>
<%
    User user = (User) request.getAttribute("user");
    int todayMinutes = (Integer) request.getAttribute("todayMinutes");
    int dailyGoal = (Integer) request.getAttribute("dailyGoal");
    int progressPercent = (Integer) request.getAttribute("progressPercent");
    int streakDays = (Integer) request.getAttribute("streakDays");
    @SuppressWarnings("unchecked")
    List<Book> recentBooks = (List<Book>) request.getAttribute("recentBooks");
    @SuppressWarnings("unchecked")
    List<Book> favoriteBooks = (List<Book>) request.getAttribute("favoriteBooks");
    Book recommend = (Book) request.getAttribute("recommend");
    int totalBooks = (Integer) request.getAttribute("totalBooks");
    String ctx = request.getContextPath();

    // 圆形进度环参数 (r=56)
    double circumference = 2 * Math.PI * 56;
    double dashOffset = circumference * (100 - progressPercent) / 100.0;
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>墨读 - 首页</title>
    <link rel="stylesheet" href="<%=ctx%>/assets/css/variables.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/layout.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/home.css">
</head>
<body>
<%@ include file="navbar.jsp" %>

<div class="home-page">
    <div class="home-content">

        <!-- ===== 顶部：今日阅读 + 推荐位 ===== -->
        <div class="home-top">

            <!-- 今日阅读卡（满琥珀黄背景 + 白字 + SVG 圆形进度环） -->
            <div class="today-card">
                <div class="today-card-inner">
                    <div class="today-header">
                        <span class="today-label">今日阅读</span>
                        <span class="today-goal">目标 <%=dailyGoal%> 分钟</span>
                    </div>
                    <div class="today-ring-wrap">
                        <svg class="today-ring" width="140" height="140" viewBox="0 0 140 140">
                            <circle cx="70" cy="70" r="56" fill="none"
                                    stroke="rgba(255,255,255,0.22)" stroke-width="9"/>
                            <circle cx="70" cy="70" r="56" fill="none"
                                    stroke="#FFFFFF" stroke-width="9"
                                    stroke-linecap="round"
                                    stroke-dasharray="<%=String.format("%.1f", circumference)%>"
                                    stroke-dashoffset="<%=String.format("%.1f", dashOffset)%>"
                                    transform="rotate(-90 70 70)"
                                    class="ring-progress"/>
                            <text x="70" y="66" text-anchor="middle"
                                  fill="#FFFFFF" font-size="34" font-weight="700"
                                  font-family="inherit"><%=todayMinutes%></text>
                            <text x="70" y="88" text-anchor="middle"
                                  fill="rgba(255,255,255,0.78)" font-size="12"
                                  font-family="inherit">分钟</text>
                        </svg>
                        <% if (progressPercent >= 100) { %>
                            <div class="today-done-badge">已达标</div>
                        <% } %>
                    </div>
                    <div class="today-footer">
                        <% if (streakDays > 0) { %>
                            连续阅读 <span class="streak-num"><%=streakDays%></span> 天
                        <% } else { %>
                            点击书本开始今日阅读
                        <% } %>
                    </div>
                </div>
            </div>

            <!-- 推荐位（浅琥珀背景 + 封面 + 简介） -->
            <% if (recommend != null) { %>
            <a class="recommend-card" href="<%=ctx%>/reader?bookId=<%=recommend.getId()%>">
                <div class="recommend-label">为你推荐</div>
                <div class="recommend-body">
                    <div class="recommend-cover cover-<%=recommend.getId() % 6 + 1%>"
                        <% if (recommend.getCoverImagePath() != null && !recommend.getCoverImagePath().isEmpty()) { %>
                         style="background-image: url('<%=ctx%><%=recommend.getCoverImagePath()%>'); background-size: cover; background-position: center;"
                        <% } else if (recommend.getCoverColor() != null) { %>
                         style="background: <%=recommend.getCoverColor()%>;"
                        <% } %>>
                        <span class="recommend-cover-title"><%=recommend.getTitle()%></span>
                    </div>
                    <div class="recommend-info">
                        <div class="recommend-title"><%=recommend.getTitle()%></div>
                        <div class="recommend-desc text-ellipsis-3">
                            <%=recommend.getDescription() != null && !recommend.getDescription().trim().isEmpty()
                              ? recommend.getDescription() : "一本书，一个世界。点击开始阅读。"%>
                        </div>
                        <div class="recommend-meta">
                            <%=recommend.getChapterCount()%> 章 · <%=fmtWords(recommend.getTotalWords())%>
                        </div>
                    </div>
                </div>
            </a>
            <% } else if (totalBooks == 0) { %>
            <div class="recommend-card recommend-empty">
                <div class="recommend-label">开始使用</div>
                <div class="recommend-empty-body">
                    <div class="recommend-empty-text">导入第一本小说</div>
                    <div class="recommend-empty-hint">支持 TXT 单文件 / ZIP 批量导入</div>
                    <a class="recommend-empty-btn" href="<%=ctx%>/import">去导入</a>
                </div>
            </div>
            <% } %>
        </div>

        <!-- ===== 最近在读 ===== -->
        <% if (!recentBooks.isEmpty()) { %>
        <section class="home-section">
            <div class="section-header">
                <h2 class="section-title">最近在读</h2>
                <a class="section-more" href="<%=ctx%>/book">查看全部</a>
            </div>
            <div class="book-row">
                <% for (Book b : recentBooks) {
                    int cIdx = b.getId() % 6 + 1;
                    String coverStyle = "";
                    if (b.getCoverImagePath() != null && !b.getCoverImagePath().isEmpty()) {
                        coverStyle = "background-image: url('" + ctx + b.getCoverImagePath() + "'); background-size: cover; background-position: center;";
                    }
                %>
                <a class="home-book" href="<%=ctx%>/reader?bookId=<%=b.getId()%>">
                    <div class="home-book-cover cover-<%=cIdx%>" style="<%=coverStyle%>">
                        <% if (coverStyle.isEmpty()) { %>
                            <span class="home-book-cover-title"><%=b.getTitle()%></span>
                        <% } %>
                        <% if (b.getIsFavorite() == 1) { %>
                            <svg class="home-book-fav" viewBox="0 0 16 16" fill="#FFFFFF" stroke="#D97706" stroke-width="0.5">
                                <path d="M8 14.2s-5.3-3.7-5.3-7.5c0-1.6 1.1-2.7 2.6-2.7 1.1 0 2 .6 2.7 1.6.7-1 1.6-1.6 2.7-1.6 1.5 0 2.6 1.1 2.6 2.7 0 3.8-5.3 7.5-5.3 7.5z"/>
                            </svg>
                        <% } %>
                    </div>
                    <div class="home-book-info">
                        <div class="home-book-title text-ellipsis"><%=b.getTitle()%></div>
                        <div class="home-book-meta"><%=b.getChapterCount()%> 章 · <%=fmtWords(b.getTotalWords())%></div>
                    </div>
                </a>
                <% } %>
            </div>
        </section>
        <% } %>

        <!-- ===== 喜爱书籍 ===== -->
        <% if (!favoriteBooks.isEmpty()) { %>
        <section class="home-section">
            <div class="section-header">
                <h2 class="section-title">
                    <svg class="section-heart" viewBox="0 0 16 16" fill="#D97706">
                        <path d="M8 14.2s-5.3-3.7-5.3-7.5c0-1.6 1.1-2.7 2.6-2.7 1.1 0 2 .6 2.7 1.6.7-1 1.6-1.6 2.7-1.6 1.5 0 2.6 1.1 2.6 2.7 0 3.8-5.3 7.5-5.3 7.5z"/>
                    </svg>
                    喜爱书籍
                </h2>
                <a class="section-more" href="<%=ctx%>/book">查看全部</a>
            </div>
            <div class="book-row">
                <% for (Book b : favoriteBooks) {
                    int cIdx = b.getId() % 6 + 1;
                    String coverStyle = "";
                    if (b.getCoverImagePath() != null && !b.getCoverImagePath().isEmpty()) {
                        coverStyle = "background-image: url('" + ctx + b.getCoverImagePath() + "'); background-size: cover; background-position: center;";
                    }
                %>
                <a class="home-book" href="<%=ctx%>/reader?bookId=<%=b.getId()%>">
                    <div class="home-book-cover cover-<%=cIdx%>" style="<%=coverStyle%>">
                        <% if (coverStyle.isEmpty()) { %>
                            <span class="home-book-cover-title"><%=b.getTitle()%></span>
                        <% } %>
                    </div>
                    <div class="home-book-info">
                        <div class="home-book-title text-ellipsis"><%=b.getTitle()%></div>
                        <div class="home-book-meta"><%=b.getChapterCount()%> 章 · <%=fmtWords(b.getTotalWords())%></div>
                    </div>
                </a>
                <% } %>
            </div>
        </section>
        <% } %>

        <!-- ===== 没有最近在读 且 没有喜爱书籍 时显示引导 ===== -->
        <% if (recentBooks.isEmpty() && favoriteBooks.isEmpty() && totalBooks > 0) { %>
        <section class="home-section">
            <div class="section-header">
                <h2 class="section-title">开始阅读</h2>
            </div>
            <div class="home-guide">
                <div class="guide-text">你的书架里有 <%=totalBooks%> 本书，挑一本开始读吧</div>
                <a class="guide-btn" href="<%=ctx%>/book">去书架</a>
            </div>
        </section>
        <% } %>

    </div>
</div>

<script>
// 进度环动画：页面加载后从 0 渐变到目标值
(function() {
    var ring = document.querySelector('.ring-progress');
    if (!ring) return;
    var target = parseFloat(ring.getAttribute('stroke-dashoffset'));
    var circ = parseFloat(ring.getAttribute('stroke-dasharray'));
    ring.style.strokeDashoffset = circ;
    requestAnimationFrame(function() {
        ring.style.transition = 'stroke-dashoffset 1s ease-out';
        ring.style.strokeDashoffset = target;
    });
})();
</script>
</body>
</html>
