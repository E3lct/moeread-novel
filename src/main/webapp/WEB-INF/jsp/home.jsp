<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>首页 - 墨读 Moeread</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css">
    <style>
        body { padding: 40px 20px; }
        .welcome-box {
            max-width: 500px;
            margin: 40px auto;
            background: var(--color-bg-card);
            border-radius: var(--radius-xl);
            padding: 36px;
            box-shadow: var(--shadow-lg);
            text-align: center;
        }
        .welcome-box h1 {
            color: var(--color-primary-dark);
            font-size: 24px;
            margin-bottom: 12px;
        }
        .welcome-box p {
            color: var(--color-text-hint);
            margin-bottom: 20px;
        }
        .logout-btn {
            display: inline-block;
            padding: 10px 24px;
            background: var(--color-primary-palest);
            color: var(--color-primary-dark);
            border-radius: var(--radius-sm);
            text-decoration: none;
            font-weight: 500;
            border: 1px solid var(--color-border);
        }
        .logout-btn:hover {
            background: var(--color-primary-lightest);
        }
    </style>
</head>
<body>
    <div class="welcome-box">
        <h1>登录成功</h1>
        <p>欢迎回来，${user.nickname}</p>
        <p style="font-size: 12px; color: var(--color-text-light);">用户名: ${user.username} | ID: ${user.id}</p>
        <br>
        <a href="${pageContext.request.contextPath}/logout" class="logout-btn">退出登录</a>
    </div>
</body>
</html>
