# 墨读 Moeread v2

个人小说阅读器 - Spring Boot 正式版

## 技术栈

| 层 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3.2 |
| 数据库层 | MyBatis-Plus 3.5 |
| 鉴权 | JWT (jjwt 0.12) |
| 数据库 | MySQL 8 |
| 前端（后续） | Vue 3 + Element Plus |
| 部署 | Docker + Nginx |

## 项目结构

```
moeread-v2/
├── pom.xml
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
        └── application.yml             ← 配置文件
```

## 运行

```bash
# 1. 确保 MySQL 8 运行中，导入数据库
mysql -u root -p < src/main/resources/moeread.sql

# 2. 修改 application.yml 中的数据库密码

# 3. 启动（需要 JDK 17+）
mvn spring-boot:run

# 4. 访问 http://localhost:8080/api
```

## 对比作业版

| 维度 | 作业版 (v1) | 正式版 (v2) |
|---|---|---|
| 框架 | Servlet + JSP | Spring Boot 3 |
| 数据库 | JDBC 手写 SQL | MyBatis-Plus |
| 前端 | JSP 模板 | Vue 3 (后续) |
| API | Form 表单 | RESTful JSON |
| 鉴权 | Session | JWT |
| 部署 | 本地 Tomcat | Docker 云端 |
