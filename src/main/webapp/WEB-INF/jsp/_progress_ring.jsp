<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.moeread.model.Book" %>
<%@ page import="java.util.Map" %>
<%--
    进度环片段：悬浮在封面正中间，hover 显示
    外层已定义: Book b; String ctx; Map<Integer,Integer> progressMap;
--%>
<%
    if (progressMap != null) {
        int chapterIdx = progressMap.getOrDefault(b.getId(), 0);
        int totalCh = b.getChapterCount() > 0 ? b.getChapterCount() : 1;
        int pct = 0;
        if ("finished".equals(b.getStatus())) {
            pct = 100;
        } else if (chapterIdx > 0) {
            pct = Math.min(100, chapterIdx * 100 / totalCh);
        }
        boolean isFinished = pct >= 100;
        boolean isStarted = pct > 0;
        // SVG 圆环参数 (r=20, 周长=2*PI*20≈125.66)
        double circ = 2 * Math.PI * 20;
        double dashLen = circ * pct / 100.0;
        String strokeColor = isFinished ? "rgba(74,222,128,0.95)" : "rgba(255,255,255,0.95)";
        String trackColor = isFinished ? "rgba(74,222,128,0.25)" : "rgba(255,255,255,0.25)";
        String numColor = isFinished ? "rgba(187,247,208,0.95)" : "rgba(255,255,255,0.95)";
        String pctColor = isFinished ? "rgba(187,247,208,0.8)" : "rgba(255,255,255,0.7)";
        if (!isStarted) {
            strokeColor = "transparent";
            numColor = "rgba(255,255,255,0.6)";
            pctColor = "rgba(255,255,255,0.5)";
        }
%>
<div class="book-progress-ring">
    <svg width="48" height="48" viewBox="0 0 48 48">
        <circle cx="24" cy="24" r="20" fill="none" stroke="<%=trackColor%>" stroke-width="3.5"/>
        <% if (isStarted) { %>
        <circle cx="24" cy="24" r="20" fill="none" stroke="<%=strokeColor%>" stroke-width="3.5"
                stroke-dasharray="<%=String.format("%.1f", dashLen)%> <%=String.format("%.1f", circ)%>"
                stroke-linecap="round" transform="rotate(-90 24 24)"/>
        <% } %>
        <text x="24" y="23" text-anchor="middle" font-size="13" font-weight="700"
              fill="<%=numColor%>" font-family="inherit"><%=pct%></text>
        <text x="24" y="33" text-anchor="middle" font-size="8"
              fill="<%=pctColor%>" font-family="inherit">%</text>
    </svg>
</div>
<% } %>
