package com.erwang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erwang.blog.entity.Article;
import com.erwang.blog.entity.ArticleTag;
import com.erwang.blog.entity.Category;
import com.erwang.blog.entity.Tag;
import com.erwang.blog.mapper.ArticleMapper;
import com.erwang.blog.mapper.ArticleTagMapper;
import com.erwang.blog.mapper.CategoryMapper;
import com.erwang.blog.mapper.TagMapper;
import com.erwang.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    
    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    
    @Override
    public List<Article> listAll() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1);
        wrapper.orderByDesc(Article::getIsTop, Article::getCreatedAt);
        return articleMapper.selectList(wrapper);
    }

    @Override
    public IPage<Article> listPage(Integer page, Integer size, String keyword, String category, String tag, String startDate, String endDate) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty();
        boolean hasTag = tag != null && !tag.trim().isEmpty();
        boolean hasStartDate = startDate != null && !startDate.trim().isEmpty();
        boolean hasEndDate = endDate != null && !endDate.trim().isEmpty();

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1);
        if (hasKeyword) {
            String trimKeyword = keyword.trim();
            wrapper.and(w -> w.like(Article::getTitle, trimKeyword)
                    .or()
                    .like(Article::getContent, trimKeyword));
        }

        if (hasCategory) {
            QueryWrapper<Category> categoryWrapper = new QueryWrapper<>();
            categoryWrapper.select("id").eq("name", category.trim());
            List<Category> categories = categoryMapper.selectList(categoryWrapper);
            if (categories.isEmpty()) {
                return new Page<>(page, size);
            }
            List<Long> categoryIds = categories.stream().map(Category::getId).collect(Collectors.toList());
            wrapper.in(Article::getCategoryId, categoryIds);
        }

        if (hasTag) {
            QueryWrapper<Tag> tagWrapper = new QueryWrapper<>();
            tagWrapper.select("id").eq("name", tag.trim());
            List<Tag> tags = tagMapper.selectList(tagWrapper);
            if (tags.isEmpty()) {
                return new Page<>(page, size);
            }
            List<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());

            QueryWrapper<ArticleTag> articleTagWrapper = new QueryWrapper<>();
            articleTagWrapper.select("article_id").in("tag_id", tagIds);
            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagWrapper);
            if (articleTags.isEmpty()) {
                return new Page<>(page, size);
            }
            List<Long> articleIds = articleTags.stream().map(ArticleTag::getArticleId).distinct().collect(Collectors.toList());
            wrapper.in(Article::getId, articleIds);
        }

        if (hasStartDate) {
            wrapper.ge(Article::getCreatedAt, startDate.trim() + " 00:00:00");
        }
        if (hasEndDate) {
            wrapper.le(Article::getCreatedAt, endDate.trim() + " 23:59:59");
        }

        wrapper.orderByDesc(Article::getIsTop, Article::getCreatedAt);

        Page<Article> pageReq = new Page<>(page, size);
        return articleMapper.selectPage(pageReq, wrapper);
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
    
    @Override
    public List<ArticleTag> getTagIdsByArticleIds(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ArticleTag::getArticleId, articleIds);
        return articleTagMapper.selectList(wrapper);
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
