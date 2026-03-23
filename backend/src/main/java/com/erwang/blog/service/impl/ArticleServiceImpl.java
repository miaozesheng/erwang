package com.erwang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erwang.blog.entity.Article;
import com.erwang.blog.entity.ArticleTag;
import com.erwang.blog.mapper.ArticleMapper;
import com.erwang.blog.mapper.ArticleTagMapper;
import com.erwang.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    
    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    
    @Override
    public List<Article> listAll() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1);
        wrapper.orderByDesc(Article::getIsTop, Article::getCreatedAt);
        return articleMapper.selectList(wrapper);
    }
    
    @Override
    public Article getById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article != null) {
            articleMapper.update(null, 
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                .setSql("views = views + 1")
                .eq(Article::getId, id));
        }
        return article;
    }
    
    @Override
    @Transactional
    public Article create(Article article, List<Long> tagIds) {
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        article.setViews(0);
        articleMapper.insert(article);
        
        if (tagIds != null && !tagIds.isEmpty()) {
            saveArticleTags(article.getId(), tagIds);
        }
        
        return article;
    }
    
    @Override
    @Transactional
    public Article update(Article article, List<Long> tagIds) {
        article.setUpdatedAt(LocalDateTime.now());
        articleMapper.updateById(article);
        
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, article.getId());
        articleTagMapper.delete(wrapper);
        
        if (tagIds != null && !tagIds.isEmpty()) {
            saveArticleTags(article.getId(), tagIds);
        }
        
        return article;
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        articleMapper.deleteById(id);
    }
    
    @Override
    public List<Long> getTagIdsByArticleId(Long articleId) {
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(wrapper);
        
        List<Long> tagIds = new ArrayList<>();
        for (ArticleTag at : articleTags) {
            tagIds.add(at.getTagId());
        }
        return tagIds;
    }
    
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTag.setCreatedAt(LocalDateTime.now());
            articleTagMapper.insert(articleTag);
        }
    }
}