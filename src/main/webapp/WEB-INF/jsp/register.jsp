<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册 - 墨读 Moeread</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>
    <div class="auth-wrapper">
        <div class="auth-card">
            <div class="auth-logo">
                <div class="auth-logo-circle">墨</div>
                <h1>创建账号</h1>
                <p>开始你的阅读之旅</p>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div class="message message-error" style="margin-bottom: 20px;">${error}</div>
            <% } %>

            <form class="auth-form" action="${pageContext.request.contextPath}/register" method="post">
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input type="text" id="username" name="username"
                           value="${requestScope.username}"
                           placeholder="3-20 个字符" autocomplete="off">
                </div>

                <div class="form-group">
                    <label for="nickname">昵称（可选）</label>
                    <input type="text" id="nickname" name="nickname"
                           value="${requestScope.nickname}"
                           placeholder="留空则用用户名" autocomplete="off">
                </div>

                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password"
                           placeholder="至少 6 位">
                </div>

                <div class="form-group">
                    <label for="confirmPassword">确认密码</label>
                    <input type="password" id="confirmPassword" name="confirmPassword"
                           placeholder="再次输入密码">
                </div>

                <button type="submit" class="auth-btn">注册</button>
            </form>

            <div class="auth-footer">
                已有账号？ <a href="${pageContext.request.contextPath}/login">返回登录</a>
            </div>
        </div>
    </div>
</body>
</html>
