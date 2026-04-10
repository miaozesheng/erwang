-- Performance indexes for hot public query paths
-- Date: 2026-04-10

-- Article: status + created_at for public article listing (most common query)
CREATE INDEX idx_article_status_created ON article(status, created_at DESC);

-- Article: category_id for category filtering
CREATE INDEX idx_article_category ON article(category_id);

-- Article: is_top for pinned articles
CREATE INDEX idx_article_top ON article(is_top DESC, created_at DESC);

-- Article_tag: article_id for fetching tags by article
CREATE INDEX idx_article_tag_article ON article_tag(article_id);

-- Article_tag: tag_id for fetching articles by tag
CREATE INDEX idx_article_tag_tag ON article_tag(tag_id);

-- GitHub project: primary_category + fetch_date for trending queries
CREATE INDEX idx_github_project_category_date ON github_project(primary_category, fetch_date DESC);

-- Quick link: sort for ordering (public API)
CREATE INDEX idx_quick_link_sort ON quick_link(sort);