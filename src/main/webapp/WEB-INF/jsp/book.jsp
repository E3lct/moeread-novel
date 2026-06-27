<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.moeread.model.Book" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%
    @SuppressWarnings("unchecked")
    List<Book> books = (List<Book>) request.getAttribute("books");
    String currentStatus = (String) request.getAttribute("currentStatus");
    String currentFavorite = (String) request.getAttribute("currentFavorite");
    String currentQ = (String) request.getAttribute("currentQ");

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

        <%-- 工具栏 --%>
        <div class="bookshelf-toolbar">
            <div class="bookshelf-search">
                <svg class="bookshelf-search-icon" viewBox="0 0 16 16" fill="currentColor">
                    <path d="M11.5 7a4.5 4.5 0 1 1-9 0 4.5 4.5 0 0 1 9 0Zm-.82 4.74a6 6 0 1 1 1.06-1.06l3.04 3.04a.75.75 0 1 1-1.06 1.06l-3.04-3.04Z"/>
                </svg>
                <form action="<%=ctx%>/book" method="get" id="searchForm">
                    <input type="text" name="q" value="<%=currentQ%>" placeholder="搜索书名..." autocomplete="off">
                </form>
            </div>
        </div>

        <%-- 筛选标签条 --%>
        <div class="bookshelf-filters">
            <a class="filter-tab <%=("all".equals(currentStatus) && !"1".equals(currentFavorite)) ? "active" : ""%>"
               href="<%=ctx%>/book">全部 <span class="filter-count"><%=countAll%></span></a>
            <a class="filter-tab <="reading".equals(currentStatus) ? "active" : ""%>"
               href="<%=ctx%>/book?status=reading">正在看 <span class="filter-count"><%=countReading%></span></a>
            <a class="filter-tab <="prepare".equals(currentStatus) ? "active" : ""%>"
               href="<%=ctx%>/book?status=prepare">准备看 <span class="filter-count"><%=countPrepare%></span></a>
            <a class="filter-tab <="finished".equals(currentStatus) ? "active" : ""%>"
               href="<%=ctx%>/book?status=finished">已看完 <span class="filter-count"><%=countFinished%></span></a>
            <a class="filter-tab <="1".equals(currentFavorite) ? "active" : ""%>"
               href="<%=ctx%>/book?favorite=1">喜欢 <span class="filter-count"><%=countFavorite%></span></a>
        </div>

        <%-- 书籍网格 / 空状态 --%>
        <% if (books == null || books.isEmpty()) { %>
            <div class="empty-state">
                <div class="empty-state-icon">+</div>
                <div class="empty-state-title">书架还是空的</div>
                <div class="empty-state-desc">去导入一些书籍开始阅读吧</div>
                <a href="<%=ctx%>/import" class="btn btn-primary">导入书籍</a>
            </div>
        <% } else { %>
            <div class="book-grid">
                <% int idx = 0;
                   for (Book b : books) {
                       int coverIdx = (idx % 6) + 1;
                       idx++;
                       String statusBadge = "";
                       String statusClass = "";
                       if ("reading".equals(b.getStatus())) {
                           statusBadge = "正在看"; statusClass = "badge-reading";
                       } else if ("prepare".equals(b.getStatus())) {
                           statusBadge = "准备看"; statusClass = "badge-prepare";
                       } else if ("finished".equals(b.getStatus())) {
                           statusBadge = "已看完"; statusClass = "badge-finished";
                       }
                %>
                    <div class="book-card" onclick="window.location.href='<%=ctx%>/reader?bookId=<%=b.getId()%>'">
                        <div class="book-cover cover-<%=coverIdx%>">
                            <%=b.getTitle()%>
                            <% if (b.getIsFavorite() == 1) { %>
                                <div class="book-favorite">
                                    <svg viewBox="0 0 16 16"><path d="M8 14s-5-3.5-5-7a3 3 0 0 1 5-2.5A3 3 0 0 1 13 7c0 3.5-5 7-5 7z"/></svg>
                                </div>
                            <% } %>
                        </div>
                        <div class="book-info">
                            <div class="book-title"><%=b.getTitle()%></div>
                            <div class="book-meta">
                                <span><%=b.getChapterCount()%> 章</span>
                                <span class="book-meta-dot"></span>
                                <span><%=b.getTotalWords() / 1000%>k 字</span>
                            </div>
                            <% if (!statusBadge.isEmpty()) { %>
                                <span class="book-status-badge <%=statusClass%>"><%=statusBadge%></span>
                            <% } %>
                        </div>
                    </div>
                <% } %>
            </div>
        <% } %>
    </div>
</body>
</html>
