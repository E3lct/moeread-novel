-- 墨读 Moeread 数据库建表脚本
-- 数据库：moeread
-- 字符集：utf8mb4（支持 emoji 和特殊字符）

CREATE DATABASE IF NOT EXISTS moeread DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE moeread;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录名',
    password VARCHAR(32) NOT NULL COMMENT 'MD5加密密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar LONGTEXT COMMENT '头像(base64)',
    daily_goal INT DEFAULT 30 COMMENT '每日阅读目标(分钟)',
    mascot_image LONGTEXT COMMENT '背景图(base64)',
    mascot_opacity INT DEFAULT 80 COMMENT '背景透明度(0-100)',
    bg_scale INT DEFAULT 100 COMMENT '背景缩放百分比',
    bg_mirror TINYINT DEFAULT 0 COMMENT '背景镜像: 0否 1是',
    theme_mode VARCHAR(10) DEFAULT 'light' COMMENT '显示模式: light/dark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 图书表
CREATE TABLE IF NOT EXISTS books (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id INT NOT NULL COMMENT '外键->users.id',
    title VARCHAR(100) NOT NULL COMMENT '书名',
    author VARCHAR(50) COMMENT '作者',
    description TEXT COMMENT '简介',
    cover_color VARCHAR(7) DEFAULT '#F59E0B' COMMENT '封面色',
    cover_image_path VARCHAR(200) COMMENT '封面图路径(预留正式版)',
    source_type VARCHAR(10) DEFAULT 'import' COMMENT '来源: import导入/public公版',
    status VARCHAR(20) DEFAULT 'prepare' COMMENT '系统标签: reading在读/prepare准备看/finished已看完',
    is_favorite TINYINT DEFAULT 0 COMMENT '是否喜欢: 0否 1是',
    series_id INT COMMENT '套书ID(预留正式版, nullable)',
    chapter_count INT DEFAULT 0 COMMENT '总章节数',
    total_words INT DEFAULT 0 COMMENT '总字数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '导入时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 3. 章节表
CREATE TABLE IF NOT EXISTS chapters (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    book_id INT NOT NULL COMMENT '外键->books.id',
    chapter_index INT NOT NULL COMMENT '第几章(从1开始)',
    title VARCHAR(200) COMMENT '章节标题',
    content LONGTEXT NOT NULL COMMENT '正文内容',
    word_count INT DEFAULT 0 COMMENT '字数',
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节表';

-- 4. 阅读进度表
CREATE TABLE IF NOT EXISTS read_progress (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id INT NOT NULL COMMENT '外键->users.id',
    book_id INT NOT NULL COMMENT '外键->books.id',
    chapter_index INT DEFAULT 1 COMMENT '读到第几章',
    scroll_percent INT DEFAULT 0 COMMENT '滚动位置百分比',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后阅读时间',
    UNIQUE KEY uk_user_book (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读进度表';

-- 5. 阅读统计表
CREATE TABLE IF NOT EXISTS reading_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id INT NOT NULL COMMENT '外键->users.id',
    stat_date DATE NOT NULL COMMENT '哪一天',
    read_minutes INT DEFAULT 0 COMMENT '当天阅读分钟数',
    is_checked_in TINYINT DEFAULT 0 COMMENT '是否已打卡: 0否 1是',
    UNIQUE KEY uk_user_date (user_id, stat_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读统计表';

-- 6. 自定义标签表（多对多关联）
CREATE TABLE IF NOT EXISTS book_tags (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id INT NOT NULL COMMENT '外键->users.id (标签归属用户)',
    book_id INT NOT NULL COMMENT '外键->books.id',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    INDEX idx_user_tag (user_id, tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义标签关联表';

-- ============ 预置测试用户 ============
-- 用户名: test  密码: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
INSERT INTO users (username, password, nickname) VALUES
('test', 'e10adc3949ba59abbe56e057f20f883e', '测试用户');
