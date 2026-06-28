<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.moeread.model.User" %>
<%
    // 从 session 取当前登录用户
    User currentUser = null;
    String currentPath = request.getServletPath();
    if (session != null && session.getAttribute("user") != null) {
        currentUser = (User) session.getAttribute("user");
    }
%>

<%-- 顶部悬浮胶囊导航栏 (Mac 风格) --%>
<%@ include file="_background.jsp" %>
<div class="navbar" id="mainNavbar">
    <div class="navbar-logo">
        <div class="navbar-logo-circle">墨</div>
        <span class="navbar-logo-text">墨读</span>
    </div>

    <div class="navbar-divider"></div>

    <div class="navbar-nav">
        <a class="navbar-item <%= "/home".equals(currentPath) ? "active" : "" %>" href="${pageContext.request.contextPath}/home">首页</a>
        <a class="navbar-item <%= "/book".equals(currentPath) ? "active" : "" %>" href="${pageContext.request.contextPath}/book">书架</a>
        <a class="navbar-item <%= "/reader".equals(currentPath) ? "active" : "" %>" href="${pageContext.request.contextPath}/reader">阅读</a>
        <a class="navbar-item <%= "/stats".equals(currentPath) ? "active" : "" %>" href="${pageContext.request.contextPath}/stats">统计</a>
        <a class="navbar-item <%= "/settings".equals(currentPath) ? "active" : "" %>" href="${pageContext.request.contextPath}/settings">设置</a>
    </div>

    <div class="navbar-divider"></div>

    <div class="navbar-user" onclick="window.location.href='${pageContext.request.contextPath}/settings'">
        <div class="navbar-avatar">
            <% if (currentUser != null && currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) { %>
                <img src="<%=currentUser.getAvatar()%>" alt="头像" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">
            <% } else { %>
                <%= currentUser != null && currentUser.getNickname() != null ? currentUser.getNickname().substring(0,1) : "U" %>
            <% } %>
        </div>
        <span class="navbar-username"><%= currentUser != null ? currentUser.getNickname() : "未登录" %></span>
    </div>
</div>
