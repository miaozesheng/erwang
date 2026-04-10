CREATE TABLE IF NOT EXISTS `github_project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `repo_name` VARCHAR(200) NOT NULL COMMENT '仓库名，如 owner/repo',
    `full_name` VARCHAR(200) NOT NULL COMMENT '完整仓库名',
    `description` TEXT COMMENT '项目描述',
    `url` VARCHAR(500) NOT NULL COMMENT 'GitHub URL',
    `language` VARCHAR(50) DEFAULT NULL COMMENT '主要语言',
    `stars` INT DEFAULT 0 COMMENT 'star数量',
    `forks` INT DEFAULT 0 COMMENT 'fork数量',
    `open_issues` INT DEFAULT 0 COMMENT '开放issues数量',
    `watchers` INT DEFAULT 0 COMMENT 'watchers数量',
    `license` VARCHAR(100) DEFAULT NULL COMMENT '开源许可证',
    `topics` VARCHAR(500) DEFAULT NULL COMMENT 'topics标签，逗号分隔',
    `owner_avatar` VARCHAR(500) DEFAULT NULL COMMENT '作者头像URL',
    `owner_name` VARCHAR(100) DEFAULT NULL COMMENT '作者用户名',
    `primary_category` VARCHAR(50) DEFAULT NULL COMMENT '主要分类: trending/all_time/monthly/weekly',
    `fetch_date` DATE DEFAULT NULL COMMENT '抓取日期',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    UNIQUE KEY `uk_repo_category_date` (`repo_name`, `primary_category`, `fetch_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='GitHub项目表';

-- 索引用于查询
CREATE INDEX idx_stars ON github_project(stars DESC);
CREATE INDEX idx_fetch_date ON github_project(fetch_date);
CREATE INDEX idx_category ON github_project(primary_category);