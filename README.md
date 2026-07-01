# 墨读 Moeread

个人小说阅读器 - Spring Boot + Vue 正式版

## 技术栈

| 层 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3.2 |
| 数据库层 | MyBatis-Plus 3.5 |
| 鉴权 | JWT (jjwt 0.12) |
| 数据库 | MySQL 8 |
| 前端 | Vue 3 + Vite + Pinia |
| 部署 | 后端 Jar + 前端静态资源 / Docker + Nginx |

## 功能

- TXT 单文件、ZIP 批量导入
- 开放书源管理：预设公共目录、自定义书源、公开 TXT 地址导入
- 书架搜索、标签、喜欢、封面裁剪、状态管理
- 阅读器目录、主题、字号、行距、进度保存
- VS Code 摸鱼模式
- 今日阅读、连续打卡、年度热力图、周阅读习惯
- 头像、背景图、透明度、缩放和镜像设置

## 项目结构

```
moeread-v2/
├── pom.xml
├── frontend/                    ← Vue 3 前端
└── src/main/
    ├── java/com/moeread/
    │   ├── MoereadApplication.java    ← 启动类
    │   ├── config/                     ← 配置（CORS、JWT、MyBatis）
    │   ├── common/                     ← 公共（Result、异常处理）
    │   ├── entity/                     ← 实体类（6张表）
    │   ├── mapper/                     ← MyBatis-Plus Mapper
    │   ├── service/                    ← 业务逻辑（接口）
    │   │   └── impl/                   ← 业务逻辑（实现）
    │   ├── controller/                 ← REST API 控制器
    │   └── dto/                        ← 数据传输对象
    └── resources/
        ├── application.yml             ← 配置文件
        └── schema-v2.sql               ← v2 新增表
```

## 运行

```bash
# 1. 确保 MySQL 8 运行中，导入基础数据库后执行 v2 新增表
mysql -u root -p moeread < src/main/resources/schema-v2.sql

# 2. 修改 application.yml 中的数据库密码

# 3. 启动后端（需要 JDK 17+）
mvn spring-boot:run

# 4. 启动前端
cd frontend
npm install
npm run dev
```

后端默认端口 `8081`，接口前缀 `/api`；前端默认端口 `5173`。

## 分支策略

正式版将作为 GitHub `main` 分支展示和发布；旧 JSP 作业版建议保留到 `legacy-jsp` 分支，避免每次查看或发行都需要切换到 `v2`。
