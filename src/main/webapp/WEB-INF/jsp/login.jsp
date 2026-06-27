<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 墨读 Moeread</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>
    <div class="auth-wrapper">
        <div class="auth-card">
            <div class="auth-logo">
                <div class="auth-logo-circle">墨</div>
                <h1>墨读</h1>
                <p>你的私人阅读空间</p>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div class="message message-error" style="margin-bottom: 20px;">${error}</div>
            <% } %>

            <form class="auth-form" action="${pageContext.request.contextPath}/login" method="post">
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input type="text" id="username" name="username"
                           value="${requestScope.username}"
                           placeholder="请输入用户名" autocomplete="off">
                </div>

                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password"
                           placeholder="请输入密码">
                </div>

                <button type="submit" class="auth-btn">登录</button>
            </form>

            <div class="auth-footer">
                还没有账号？ <a href="${pageContext.request.contextPath}/register">注册新账号</a>
            </div>
        </div>
    </div>
</body>
</html>
