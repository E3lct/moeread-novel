<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>墨读 Moeread - 框架测试</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css">
    <style>
        .container {
            max-width: 800px;
            margin: 40px auto;
            padding: 0 20px;
        }
        .header {
            background: var(--color-bg-card);
            border-radius: var(--radius-lg);
            padding: 24px;
            box-shadow: var(--shadow-md);
            margin-bottom: 20px;
        }
        .header h1 {
            font-size: var(--font-size-3xl);
            color: var(--color-text);
            margin-bottom: 8px;
        }
        .header p {
            color: var(--color-text-hint);
            font-size: var(--font-size-md);
        }
        .status-card {
            background: var(--color-bg-card);
            border-radius: var(--radius-lg);
            padding: 20px;
            box-shadow: var(--shadow-md);
            margin-bottom: 20px;
        }
        .status-card h2 {
            font-size: var(--font-size-xl);
            color: var(--color-text);
            margin-bottom: 16px;
        }
        .status-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 0;
            border-bottom: 1px solid var(--color-border-light);
        }
        .status-item:last-child {
            border-bottom: none;
        }
        .status-label {
            color: var(--color-text-secondary);
            font-size: var(--font-size-lg);
        }
        .status-value {
            font-weight: var(--font-medium);
            color: var(--color-text);
        }
        .status-ok {
            color: var(--color-primary-dark);
        }
        .color-palette {
            display: grid;
            grid-template-columns: repeat(6, 1fr);
            gap: 10px;
            margin-top: 12px;
        }
        .color-swatch {
            height: 60px;
            border-radius: var(--radius-md);
            display: flex;
            align-items: flex-end;
            justify-content: center;
            padding-bottom: 6px;
            font-size: var(--font-size-xs);
            color: rgba(255, 255, 255, 0.8);
        }
        .btn {
            display: inline-block;
            padding: 10px 20px;
            background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
            color: #FFF;
            border-radius: var(--radius-md);
            font-size: var(--font-size-md);
            font-weight: var(--font-medium);
            box-shadow: var(--shadow-lg);
            cursor: pointer;
            border: none;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>墨读 Moeread</h1>
        <p>Java Web 期末作业 · 小说阅读器</p>
    </div>

    <div class="status-card">
        <h2>框架状态</h2>
        <div class="status-item">
            <span class="status-label">Maven 构建</span>
            <span class="status-value status-ok">就绪</span>
        </div>
        <div class="status-item">
            <span class="status-label">web.xml 配置</span>
            <span class="status-value status-ok">就绪</span>
        </div>
        <div class="status-item">
            <span class="status-label">编码过滤器</span>
            <span class="status-value status-ok">就绪</span>
        </div>
        <div class="status-item">
            <span class="status-label">登录拦截器</span>
            <span class="status-value status-ok">就绪</span>
        </div>
        <div class="status-item">
            <span class="status-label">DBUtil 数据库工具</span>
            <span class="status-value status-ok">就绪</span>
        </div>
        <div class="status-item">
            <span class="status-label">MD5 加密工具</span>
            <span class="status-value status-ok">就绪</span>
        </div>
        <div class="status-item">
            <span class="status-label">CSS 变量系统</span>
            <span class="status-value status-ok">就绪</span>
        </div>
        <div class="status-item">
            <span class="status-label">数据库表</span>
            <span class="status-value status-ok">6 张（含预留字段）</span>
        </div>
    </div>

    <div class="status-card">
        <h2>琥珀黄配色预览</h2>
        <p style="color: var(--color-text-hint); font-size: var(--font-size-md); margin-bottom: 8px;">主色阶（浅→深）</p>
        <div class="color-palette">
            <div class="color-swatch" style="background: #FFFBEB; color: #A16207;">FFFBEB</div>
            <div class="color-swatch" style="background: #FEF3C7; color: #92400E;">FEF3C7</div>
            <div class="color-swatch" style="background: #FCD34D;">FCD34D</div>
            <div class="color-swatch" style="background: #F59E0B;">F59E0B</div>
            <div class="color-swatch" style="background: #D97706;">D97706</div>
            <div class="color-swatch" style="background: #92400E;">92400E</div>
        </div>
        <p style="color: var(--color-text-hint); font-size: var(--font-size-md); margin: 16px 0 8px;">书籍封面渐变预览</p>
        <div class="color-palette">
            <div class="color-swatch" style="background: var(--cover-1);">封面1</div>
            <div class="color-swatch" style="background: var(--cover-2);">封面2</div>
            <div class="color-swatch" style="background: var(--cover-3);">封面3</div>
            <div class="color-swatch" style="background: var(--cover-4);">封面4</div>
            <div class="color-swatch" style="background: var(--cover-5);">封面5</div>
            <div class="color-swatch" style="background: var(--cover-6);">封面6</div>
        </div>
        <div style="margin-top: 20px;">
            <button class="btn">琥珀黄按钮示例</button>
        </div>
    </div>

    <div class="status-card">
        <h2>下一步</h2>
        <p style="color: var(--color-text-secondary); line-height: 1.8;">
            1. 在 MySQL 中执行 <code style="background: var(--color-primary-palest); padding: 2px 6px; border-radius: 4px; color: var(--color-primary-dark);">src/main/resources/moeread.sql</code> 建库建表<br>
            2. 修改 <code style="background: var(--color-primary-palest); padding: 2px 6px; border-radius: 4px; color: var(--color-primary-dark);">DBUtil.java</code> 里的数据库密码<br>
            3. 用 Maven 打包部署到 Tomcat 9<br>
            4. 访问本页面确认框架正常
        </p>
        <p style="color: var(--color-text-hint); margin-top: 12px; font-size: var(--font-size-md);">
            测试账号：test / 123456
        </p>
    </div>
</div>
</body>
</html>
