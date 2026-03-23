package com.erwang.blog.controller;

import com.erwang.blog.common.Result;
import com.erwang.blog.entity.Article;
import com.erwang.blog.entity.Tag;
import com.erwang.blog.service.ArticleService;
import com.erwang.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {
    
    private final ArticleService articleService;
    private final TagService tagService;
    
    @GetMapping
    public Result<List<Article>> list() {
        return Result.success(articleService.listAll());
    }
    
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        
        List<Long> tagIds = articleService.getTagIdsByArticleId(id);
        List<Tag> tags = tagService.listAll();
        tags.removeIf(t -> !tagIds.contains(t.getId()));
        
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("article", article);
        data.put("tags", tags);
        
        return Result.success(data);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Article> create(@RequestBody Map<String, Object> params) {
        Article article = new Article();
        article.setTitle((String) params.get("title"));
        article.setSlug((String) params.get("slug"));
        article.setContent((String) params.get("content"));
        article.setSummary((String) params.get("summary"));
        article.setCover((String) params.get("cover"));
        article.setCategoryId(((Number) params.get("categoryId")).longValue());
        article.setStatus(1);
        article.setIsTop(0);
        
        List<Long> tagIds = null;
        if (params.containsKey("tagIds")) {
            tagIds = (List<Long>) params.get("tagIds");
        }
        
        return Result.success(articleService.create(article, tagIds));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Article> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        
        article.setTitle((String) params.get("title"));
        article.setSlug((String) params.get("slug"));
        article.setContent((String) params.get("content"));
        article.setSummary((String) params.get("summary"));
        article.setCover((String) params.get("cover"));
        if (params.containsKey("categoryId")) {
            article.setCategoryId(((Number) params.get("categoryId")).longValue());
        }
        
        List<Long> tagIds = null;
        if (params.containsKey("tagIds")) {
            tagIds = (List<Long>) params.get("tagIds");
        }
        
        return Result.success(articleService.update(article, tagIds));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }
}