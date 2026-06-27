<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.moeread.model.User" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>首页 - 墨读 Moeread</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body>
    <%@ include file="/WEB-INF/jsp/navbar.jsp" %>

    <div class="main-content">
        <div class="page-header">
            <h1 class="page-title">首页</h1>
            <p class="page-subtitle">欢迎回来，${sessionScope.user.nickname}</p>
        </div>

        <div class="card" style="text-align: center; padding: 60px 24px;">
            <p style="font-size: 15px; color: var(--color-text-secondary); margin-bottom: 12px;">
                欢迎使用墨读 Moeread
            </p>
            <p style="font-size: 13px; color: var(--color-text-tertiary);">
                首页开发中，请先去 <a href="${pageContext.request.contextPath}/import">导入</a> 一些书籍吧
            </p>
        </div>
    </div>
</body>
</html>
