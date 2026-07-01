CREATE TABLE IF NOT EXISTS book_sources (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  source_key VARCHAR(80) NOT NULL,
  source_type VARCHAR(40) NOT NULL DEFAULT 'custom',
  base_url VARCHAR(500) DEFAULT '',
  description VARCHAR(500) DEFAULT '',
  language VARCHAR(20) DEFAULT 'custom',
  enabled TINYINT DEFAULT 1,
  is_preset TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_book_sources_user (user_id),
  INDEX idx_book_sources_key (user_id, source_key)
);
