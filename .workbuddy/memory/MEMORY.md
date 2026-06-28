# 墨读 Moeread 项目记忆

## 项目概况
- 大二 Java Web 期末作业，小说阅读器
- 技术栈：JSP + Servlet + JavaBean + JDBC + DAO + MVC, MySQL 8, Tomcat 9
- 不用框架，原生写，怕老师看出非本人所写
- GitHub: https://github.com/E3lct/moeread-novel.git (main 分支)

## 开发环境（接续必读）
- 项目根目录: D:\Work\Web
- Tomcat: D:\windows_tool\apache-tomcat-9.0.119 (端口 8080)
- JDK: 1.8.0_181（不能用 var，不能用 lambda 写法太复杂）
- 编译: PowerShell `& javac -encoding UTF-8 -source 1.8 -target 1.8 -cp $cp -d target\classes <java files>`（Git Bash 下 -cp 分号会出问题，用 PowerShell）
- 部署: cp target/classes 到 Tomcat webapps/moeread/WEB-INF/classes/，cp JSP/CSS 到对应目录，重启 Tomcat
- Tomcat war 已备份为 .bak，避免重启重新解包覆盖
- 测试账号: test / 123456 (bookId=1,2 有《红楼梦》等)
- 访问: http://localhost:8080/moeread/home

## 当前进度（6/8 页面已完成）
已完成:
1. 登录注册 (3e93f5c)
2. 导入功能 txt+zip (65d5246)
3. 书架: 毛玻璃底部遮盖卡片 + 标签分组视图 + 封面上传 + 编辑弹窗 + 搜索 (9e1e264, f5fa7bd)
4. 阅读器: 固定目录侧栏 + <>展开收起 + 字号/行距/主题 + 进度自动保存 + 摸鱼入口 (f5fa7bd)
5. 摸鱼模式: VS Code伪装 + 深浅色切换 + Alt翻页 + ESC退出 (f5fa7bd)
6. 首页: 今日阅读环(SVG) + 推荐位 + 最近在读 + 喜爱书籍 (d10dcdb)
章节识别: 移植 Legado 26条TXT目录规则 + selectBestRule算法 (f5fa7bd)

待做:
7. 统计页: 年度热力图(颜色深浅=阅读时长) + 总时长/书籍数/连续打卡(3卡片) + 已读完列表
8. 设置页: 看板娘导入+透明度 + 阅读目标调节 + 导入历史 + 修改密码/退出 + 关于
9. 答辩材料: PPT + 录屏 + Word报告

## 功能清单（设计规范）
1. 首页: 今日阅读环(琥珀黄背景白字) + 推荐位(浅琥珀+封面+简介) + 最近在读 + 喜爱书籍
2. 书架: 网格/分组切换 + 系统标签(喜欢/准备看/已看完/正在看) + 自定义标签 + 搜索 + 编辑书名封面
3. 阅读器: 章节目录侧栏(可收起) + 字号/行距/背景色/夜间 + 键盘翻页 + 自动保存进度 + ESC摸鱼
4. 统计: 年度热力图 + 总时长/书籍数/连续打卡(3卡片) + 已读完列表(悬停显示阅读时长)
5. 设置: 看板娘+透明度 + 阅读目标(默认30min) + 导入历史 + 账号 + 关于
6. 导入: txt单文件 + ZIP批量，Legado章节识别
7. 摸鱼: VS Code伪装(深/浅可切)，阅读内容藏代码注释里

## 不做的功能
在线书源拉取、PDF/EPUB、轻小说对话排版、阅读笔记、PWA/APP封装、听歌歌词

## 设计规范
- 主色: #F59E0B琥珀黄, 浅 #FBBF24/#FEF3C7/#FFFBEB, 深 #D97706/#B45309/#92400E
- 背景: #FAF8F3 暖米白, 卡片 #FFFFFF白 + 柔和阴影
- 文字: 主 #3D2E1A深棕, 次 #78350F/#92400E, 提示 #A16207, 边框 #FDE68A
- 底部导航: 胶囊白底+琥珀阴影, 4文字标签(首页/书架/统计/设置), 选中琥珀渐变白字
- 书籍封面: 琥珀渐变, 白字书名; 卡片底部毛玻璃遮盖层(backdrop-filter:blur(16px))
- 圆形进度环: 白描边(琥珀背景上)/琥珀描边(白底上), hover显示
- 看板娘: 右下角fixed, 设置页可导入图片+透明度滑块
- 绝对不用emoji/火焰/导航图标, 只用文字标签。唯一允许图标是爱心SVG(#D97706)

## 数据库表（6张）
users / books / chapters / read_progress / reading_stats / book_tags
SQL: src/main/resources/moeread.sql

## 用户偏好（硬性规则）
- 上课没认真听，需要解释技术概念
- 时间不是问题，做好就行
- 担心UI不好看，要先审核设计稿再写代码
- 每完成大版本要 commit 到 GitHub
- 绝对禁止emoji，所有图标用 SVG/CSS
- 书籍卡片: 封面占满, 底部毛玻璃遮盖, 圆形进度环默认隐藏hover显示
