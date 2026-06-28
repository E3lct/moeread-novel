<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.moeread.model.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.*" %>
<%!
    String fmtDuration(int totalMin) {
        if (totalMin < 60) return totalMin + " 分钟";
        int h = totalMin / 60;
        int m = totalMin % 60;
        if (m == 0) return h + " 小时";
        return h + " 小时 " + m + " 分钟";
    }
    String fmtHours(int totalMin) {
        int h = totalMin / 60;
        int m = totalMin % 60;
        return h + "<span class=\"ov-unit\">时</span> " + m + "<span class=\"ov-unit\">分</span>";
    }
    String fmtWords(int w) {
        if (w >= 10000) return String.format("%.1f", w / 10000.0) + " 万字";
        return w + " 字";
    }
    String heatColor(int minutes) {
        if (minutes <= 0) return "heat-0";
        if (minutes <= 15) return "heat-1";
        if (minutes <= 30) return "heat-2";
        if (minutes <= 60) return "heat-3";
        return "heat-4";
    }
%>
<%
    User user = (User) request.getAttribute("user");
    int totalMinutes = (Integer) request.getAttribute("totalMinutes");
    int totalBooks = (Integer) request.getAttribute("totalBooks");
    int streakDays = (Integer) request.getAttribute("streakDays");
    int currentYear = (Integer) request.getAttribute("currentYear");
    @SuppressWarnings("unchecked")
    Map<String, Integer> yearlyData = (Map<String, Integer>) request.getAttribute("yearlyData");
    @SuppressWarnings("unchecked")
    List<Book> finishedBooks = (List<Book>) request.getAttribute("finishedBooks");
    @SuppressWarnings("unchecked")
    Map<Integer, Integer> progressMap = (Map<Integer, Integer>) request.getAttribute("progressMap");
    String ctx = request.getContextPath();

    // 热力图日期计算
    LocalDate today = LocalDate.now();
    LocalDate yearStart = LocalDate.of(currentYear, 1, 1);
    LocalDate yearEnd = LocalDate.of(currentYear, 12, 31);
    if (yearEnd.isAfter(today)) yearEnd = today;

    int startDow = yearStart.getDayOfWeek().getValue() - 1;
    LocalDate firstMonday = yearStart.minusDays(startDow);

    int totalWeeks = (int) java.time.temporal.ChronoUnit.WEEKS.between(firstMonday, yearEnd) + 1;
    if (totalWeeks > 53) totalWeeks = 53;

    // cell 常量
    int CELL = 11;
    int GAP  = 3;
    int STEP = CELL + GAP;
    int DOW_W = 28;
    int weeksWidth = totalWeeks * CELL + (totalWeeks - 1) * GAP;

    // 预计算每周数据
    String[][] weekData = new String[totalWeeks][7];
    int[] dowTotal = new int[7];  // 按星期累计
    int[] dowCount = new int[7];  // 按星期有阅读的天数
    int activeDays = 0;
    for (int col = 0; col < totalWeeks; col++) {
        for (int row = 0; row < 7; row++) {
            LocalDate d = firstMonday.plusDays(col * 7L + row);
            if (d.isBefore(yearStart) || d.isAfter(yearEnd)) {
                weekData[col][row] = null;
            } else {
                String key = String.format("%02d-%02d", d.getMonthValue(), d.getDayOfMonth());
                int minutes = yearlyData.getOrDefault(key, 0);
                String hc = heatColor(minutes);
                String tip = d.getMonthValue() + "月" + d.getDayOfMonth() + "日";
                if (minutes > 0) {
                    tip += " · " + minutes + "分钟";
                    activeDays++;
                    dowTotal[row] += minutes;
                    dowCount[row]++;
                } else {
                    tip += " · 未阅读";
                }
                weekData[col][row] = hc + "|" + tip;
            }
        }
    }

    // 找最佳阅读日
    int bestDow = 0;
    int bestMin = 0;
    for (int i = 0; i < 7; i++) {
        if (dowTotal[i] > bestMin) { bestMin = dowTotal[i]; bestDow = i; }
    }
    String[] dowFull = {"周一","周二","周三","周四","周五","周六","周日"};
    int maxDowTotal = bestMin > 0 ? bestMin : 1;

    // 月份标签位置
    String[] monthCn = {"1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月","12月"};
    int[] monthCol = new int[12];
    for (int m = 0; m < 12; m++) {
        LocalDate md = LocalDate.of(currentYear, m + 1, 1);
        if (md.isAfter(yearEnd)) { monthCol[m] = -1; continue; }
        int w = (int) java.time.temporal.ChronoUnit.WEEKS.between(firstMonday, md);
        if (w < 0) w = 0;
        monthCol[m] = w;
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>墨读 - 统计</title>
    <link rel="stylesheet" href="<%=ctx%>/assets/css/variables.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/layout.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/stats.css">
</head>
<body>
<%@ include file="navbar.jsp" %>

<div class="stats-page">
<div class="stats-content">

    <div class="page-header">
        <h1 class="page-title">阅读统计</h1>
        <p class="page-subtitle"><%=currentYear%> 年 · 共阅读 <%=fmtDuration(totalMinutes)%></p>
    </div>

    <!-- ===== 概览带：单卡三列 ===== -->
    <div class="overview-band">
        <div class="ov-item">
            <div class="ov-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/><polyline points="12,6 12,12 16,14"/>
                </svg>
            </div>
            <div class="ov-num"><%=fmtHours(totalMinutes)%></div>
            <div class="ov-label">总阅读时长</div>
        </div>
        <div class="ov-divider"></div>
        <div class="ov-item">
            <div class="ov-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                </svg>
            </div>
            <div class="ov-num"><%=totalBooks%><span class="ov-unit">本</span></div>
            <div class="ov-label">书籍总数</div>
        </div>
        <div class="ov-divider"></div>
        <div class="ov-item">
            <div class="ov-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
                </svg>
            </div>
            <div class="ov-num"><%=streakDays%><span class="ov-unit">天</span></div>
            <div class="ov-label">连续打卡</div>
        </div>
    </div>

    <!-- ===== 热力图 + 阅读习惯 ===== -->
    <div class="stats-section">
        <div class="sec-head">
            <h2 class="sec-title">阅读热力图</h2>
            <span class="sec-hint">每天阅读时长</span>
        </div>
        <div class="heatmap-row">
        <div class="heatmap-card">
            <div class="heatmap-scroll">
                <div class="heatmap-inner" style="width: <%=DOW_W + weeksWidth%>px;">
                    <div class="hm-months-row">
                        <span class="hm-dow-spacer" style="width:<%=DOW_W%>px;"></span>
                        <div class="hm-months" style="width:<%=weeksWidth%>px;position:relative;height:16px;">
                            <%
                            int lastCol = -10;
                            for (int m = 0; m < 12; m++) {
                                if (monthCol[m] < 0) break;
                                if (monthCol[m] - lastCol < 3 && m > 0) continue;
                                lastCol = monthCol[m];
                                int leftPx = monthCol[m] * STEP;
                            %>
                            <span class="hm-month" style="left:<%=leftPx%>px;"><%=monthCn[m]%></span>
                            <% } %>
                        </div>
                    </div>
                    <div class="hm-body-row">
                        <div class="hm-dow-col" style="width:<%=DOW_W%>px;">
                            <span class="hm-dow-label">一</span>
                            <span class="hm-dow-label"></span>
                            <span class="hm-dow-label">三</span>
                            <span class="hm-dow-label"></span>
                            <span class="hm-dow-label">五</span>
                            <span class="hm-dow-label"></span>
                            <span class="hm-dow-label">日</span>
                        </div>
                        <div class="hm-weeks" style="width:<%=weeksWidth%>px;">
                            <%
                            for (int col = 0; col < totalWeeks; col++) {
                            %>
                            <div class="hm-week">
                                <%
                                for (int row = 0; row < 7; row++) {
                                    String data = weekData[col][row];
                                    if (data == null) {
                                %>
                                <span class="heat-cell heat-empty"></span>
                                <%
                                    } else {
                                        String[] parts = data.split("\\|", 2);
                                %>
                                <span class="heat-cell <%=parts[0]%>" title="<%=parts[1]%>"></span>
                                <%
                                    }
                                }
                                %>
                            </div>
                            <%
                            }
                            %>
                        </div>
                    </div>
                </div>
            </div>
            <div class="hm-legend">
                <span>少</span>
                <span class="heat-cell heat-0"></span>
                <span class="heat-cell heat-1"></span>
                <span class="heat-cell heat-2"></span>
                <span class="heat-cell heat-3"></span>
                <span class="heat-cell heat-4"></span>
                <span>多</span>
            </div>
        </div>

        <!-- 阅读习惯面板 -->
        <div class="habit-card">
            <h3 class="habit-title">阅读习惯</h3>
            <div class="habit-stats">
                <div class="habit-stat">
                    <span class="habit-stat-num"><%=activeDays%></span>
                    <span class="habit-stat-label">活跃天数</span>
                </div>
                <div class="habit-stat">
                    <span class="habit-stat-num"><%=dowFull[bestDow]%></span>
                    <span class="habit-stat-label">最常阅读</span>
                </div>
            </div>
            <div class="habit-bars">
                <% for (int i = 0; i < 7; i++) {
                    int pct = maxDowTotal > 0 ? (dowTotal[i] * 100 / maxDowTotal) : 0;
                %>
                <div class="habit-bar-row">
                    <span class="habit-bar-label"><%=dowFull[i]%></span>
                    <div class="habit-bar-track">
                        <div class="habit-bar-fill" style="width:<%=pct%>%"></div>
                    </div>
                    <span class="habit-bar-val"><%=dowTotal[i]%>分</span>
                </div>
                <% } %>
            </div>
        </div>
        </div>
    </div>

    <!-- ===== 已读完 ===== -->
    <div class="stats-section">
        <div class="sec-head">
            <h2 class="sec-title">已读完</h2>
            <span class="sec-hint"><%=finishedBooks.size()%> 本</span>
        </div>
        <% if (finishedBooks.isEmpty()) { %>
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                    </svg>
                </div>
                <div class="empty-state-title">还没有读完的书</div>
                <div class="empty-state-desc">坚持阅读，第一本读完的书会出现在这里</div>
            </div>
        <% } else { %>
            <div class="fin-grid">
                <% for (Book b : finishedBooks) {
                    int cIdx = b.getId() % 6 + 1;
                    String coverStyle = "";
                    if (b.getCoverImagePath() != null && !b.getCoverImagePath().isEmpty()) {
                        coverStyle = "background-image: url('" + ctx + b.getCoverImagePath() + "'); background-size: cover; background-position: center;";
                    }
                    int totalCh = b.getChapterCount() > 0 ? b.getChapterCount() : 1;
                %>
                <a class="fin-card" href="<%=ctx%>/reader?bookId=<%=b.getId()%>">
                    <div class="fin-cover cover-<%=cIdx%>" style="<%=coverStyle%>">
                        <% if (coverStyle.isEmpty()) { %>
                            <span class="fin-cover-title"><%=b.getTitle()%></span>
                        <% } %>
                    </div>
                    <div class="fin-info">
                        <div class="fin-name"><%=b.getTitle()%></div>
                        <div class="fin-meta"><%=totalCh%> 章 · <%=fmtWords(b.getTotalWords())%></div>
                    </div>
                    <div class="fin-check">
                        <svg width="24" height="24" viewBox="0 0 24 24">
                            <circle cx="12" cy="12" r="11" fill="#10B981"/>
                            <polyline points="7,12 11,16 17,9" fill="none" stroke="#FFF"
                                      stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </div>
                </a>
                <% } %>
            </div>
        <% } %>
    </div>

</div>
</div>

</body>
</html>
