package com.erwang.blog.service;

import com.erwang.blog.entity.Article;
import java.util.List;

public interface ArticleService {
    List<Article> listAll();
    Article getById(Long id);
    Article create(Article article, List<Long> tagIds);
    Article update(Article article, List<Long> tagIds);
    void delete(Long id);
    List<Long> getTagIdsByArticleId(Long articleId);
}