# 墨读 MoeRead

个人小说阅读器课程项目，开发过程中使用 AI 辅助。当前 `main` 为 Spring Boot + Vue 实现；仓库另保留 `legacy-jsp`（Servlet/JSP）。

## 页面展示

以下截图采集自实际部署页面（2026-10-24）。部署入口设有访问保护；已查看登录、本地导入和阅读统计页面，尚未完成全部业务流程验收。

![墨读登录界面](docs/images/login.jpg)

本地文件导入面板（界面区域截图）：

![本地文件导入面板](docs/images/local-import.jpg)

阅读统计中的年度热力图（界面区域截图）：

![年度阅读热力图](docs/images/reading-heatmap.jpg)

## 已有实现

- 用户注册、登录及 JWT 请求鉴权；书架搜索、状态、喜欢和标签管理。
- TXT、ZIP、EPUB 导入；章节目录、阅读主题及进度保存；阅读统计与热力图。
- 自定义书源、订阅及规则解析。外部站点可用性和内容授权需另行确认；演示请使用自有或已获授权的文本。

## 技术栈

| 部分 | 当前 main |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.2.6、MyBatis-Plus 3.5.7、jjwt 0.12.5 |
| 数据 | MySQL 8；8 个实体对应的表 |
| 前端 | Vue 3、Vite 5、Pinia 2、Vue Router 4、Axios、epub.js |
| 文本处理 | Jsoup、juniversalchardet、Java ZIP/XML 处理 |

`legacy-jsp` 使用 Servlet 4、JSP/JSTL、JDBC、MySQL，打包为 WAR。

## 本地启动（当前 main）

需要 JDK 17、Maven、Node.js 18+ 和 MySQL 8。请创建专用本地数据库账号，勿使用生产凭据。

1. 在 MySQL 客户端执行以下脚本。脚本仅用于初始化本地空库；已有数据库请先备份并检查结构差异。

```sql
SOURCE /absolute/path/to/moeread-novel/src/main/resources/schema-base.sql;
USE moeread;
SOURCE /absolute/path/to/moeread-novel/src/main/resources/schema-v2.sql;
```

2. 在同一终端设置环境变量后启动后端。以下为 PowerShell 示例（占位值需替换；Spring Boot 不会自动加载 `.env`）：

```powershell
$env:DB_USERNAME = 'your_local_user'
$env:DB_PASSWORD = 'your_local_password'
$env:JWT_SECRET = 'replace-with-a-random-secret-at-least-32-bytes'
mvn spring-boot:run
```

数据库默认地址为 `localhost:3306/moeread`，可用 `SPRING_DATASOURCE_URL` 覆盖。后端端口 `8081`，接口前缀 `/api`。

3. 另开终端启动前端：

```powershell
cd frontend
npm ci
npm run dev
```

打开 `http://localhost:5173`。Vite 将 `/api` 转发到后端。自行注册测试账号，无预置账号。使用自写 TXT 导入，检查书架、目录、进度和统计；书源联网功能单独验证。

## 构建与使用说明

```powershell
mvn test package
cd frontend
npm run build
```

仓库尚未提供自动化测试用例；运行后可按上述导入与阅读流程进行手动检查。密码仍采用历史兼容的 MD5 存储；上传目录公开映射，书源请求与资源访问仍需安全审查。仅建议隔离的本地学习演示。依赖审计存在告警，尚未完成升级及回归。

历史中出现过数据库及 JWT 明文配置，删除当前文件内容不能撤销历史泄露；若这些值曾被实际使用，应更换。

## 目录

```text
frontend/                         Vue 前端
src/main/java/com/moeread/         controller、service、entity、mapper、config
src/main/resources/application.yml
src/main/resources/schema-base.sql
src/main/resources/schema-v2.sql
```
