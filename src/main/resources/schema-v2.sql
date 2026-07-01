CREATE TABLE IF NOT EXISTS book_sources (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  source_key VARCHAR(80) NOT NULL,
  source_type VARCHAR(40) NOT NULL DEFAULT 'custom',
  base_url VARCHAR(500) DEFAULT '',
  search_url VARCHAR(800) DEFAULT '',
  content_url_template VARCHAR(800) DEFAULT '',
  description VARCHAR(500) DEFAULT '',
  language VARCHAR(20) DEFAULT 'custom',
  enabled TINYINT DEFAULT 1,
  is_preset TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_book_sources_user (user_id),
  INDEX idx_book_sources_key (user_id, source_key)
);

SET @db_name = DATABASE();
SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE book_sources ADD COLUMN search_url VARCHAR(800) DEFAULT ''''',
    'SELECT 1'
  )
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'book_sources' AND COLUMN_NAME = 'search_url'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE book_sources ADD COLUMN content_url_template VARCHAR(800) DEFAULT ''''',
    'SELECT 1'
  )
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'book_sources' AND COLUMN_NAME = 'content_url_template'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
