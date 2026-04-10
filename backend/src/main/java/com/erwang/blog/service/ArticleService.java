package com.erwang.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erwang.blog.entity.Article;
import com.erwang.blog.entity.ArticleTag;
import java.util.List;

public interface ArticleService {
    List<Article> listAll();
    IPage<Article> listPage(Integer page, Integer size, String keyword, String category, String tag);
    Article getById(Long id);
    Article create(Article article, List<Long> tagIds);
    Article update(Article article, List<Long> tagIds);
    void delete(Long id);
    List<Long> getTagIdsByArticleId(Long articleId);
    List<ArticleTag> getTagIdsByArticleIds(List<Long> articleIds);
}
