# 墨读 Moeread

一个大二 Java Web 期末作业 —— 一个有自己特色的小说阅读器。

## 技术栈

- 前端：HTML + CSS + JavaScript（原生，不用框架）
- 后端：JSP + Servlet + JavaBean + JDBC + DAO + MVC
- 数据库：MySQL 8.0
- 服务器：Tomcat 9

## 功能

- [x] 用户注册 / 登录
- [x] 书架管理
- [x] 书源导入（上传 txt 自动解析章节）
- [x] 在线阅读（字号 / 主题 / 夜间模式切换）
- [x] 摸鱼模式（老板键一键伪装）
- [x] 阅读进度云同步
- [x] 阅读统计 + 打卡热力图（GitHub 风格）
- [x] 二次元看板娘背景

## 项目结构

```
src/main/java/com/moeread/
  controller/   -- Servlet 控制器层
  service/      -- 业务逻辑层
  dao/          -- 数据访问层
  model/        -- 实体类（JavaBean）
  util/         -- 工具类（DBUtil 等）
src/main/webapp/
  WEB-INF/      -- web.xml + jsp 页面
```

## 运行

1. 创建数据库 moeread，执行 sql/schema.sql
2. 修改 DBUtil 中的数据库连接信息
3. 用 IDEA 打开项目，配置 Tomcat 9 运行
