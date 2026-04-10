package com.erwang.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erwang.blog.common.Result;
import com.erwang.blog.entity.Article;
import com.erwang.blog.entity.ArticleTag;
import com.erwang.blog.entity.Category;
import com.erwang.blog.entity.Tag;
import com.erwang.blog.service.ArticleService;
import com.erwang.blog.service.CategoryService;
import com.erwang.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {
    
    private static final int EXCERPT_MAX_LENGTH = 140;
    
    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;
    
    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(required = false) String tag) {
        boolean noParams = page == null
                && size == null
                && isBlank(keyword)
                && isBlank(category)
                && isBlank(tag);

        int pageNo = page == null ? 1 : page;
        int pageSize = size == null ? 10 : Math.min(size, 100);

        IPage<Article> articlePage = articleService.listPage(pageNo, pageSize, keyword, category, tag);
        List<Article> articles = articlePage.getRecords();
        
        List<Category> categories = categoryService.listAll();
        List<Tag> tags = tagService.listAll();

        Map<Long, String> categoryNameById = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, String> tagNameById = tags.stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));
        
        List<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toList());
        Map<Long, List<Long>> tagIdsByArticleId = new HashMap<>();
        if (!articleIds.isEmpty()) {
            List<ArticleTag> allArticleTags = articleService.getTagIdsByArticleIds(articleIds);
            tagIdsByArticleId = allArticleTags.stream()
                    .collect(Collectors.groupingBy(ArticleTag::getArticleId, 
                            Collectors.mapping(ArticleTag::getTagId, Collectors.toList())));
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Article article : articles) {
            Map<String, Object> item = buildArticleResponse(article, categoryNameById, tagNameById, tagIdsByArticleId, false);
            list.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", articlePage.getTotal());
        data.put("page", noParams ? 1 : pageNo);
        data.put("size", noParams ? list.size() : pageSize);

        return Result.success(data);
    }
    
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        
        List<Category> categories = categoryService.listAll();
        List<Tag> tags = tagService.listAll();
        Map<Long, String> categoryNameById = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, String> tagNameById = tags.stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));
        
        Map<Long, List<Long>> tagIdsByArticleId = new HashMap<>();
        List<ArticleTag> articleTags = articleService.getTagIdsByArticleIds(Arrays.asList(id));
        tagIdsByArticleId = articleTags.stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId, 
                        Collectors.mapping(ArticleTag::getTagId, Collectors.toList())));

        Map<String, Object> data = buildArticleResponse(article, categoryNameById, tagNameById, tagIdsByArticleId, true);
        return Result.success(data);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Article> create(@RequestBody Map<String, Object> params) {
        Article article = new Article();
        article.setTitle((String) params.get("title"));
        String slug = (String) params.get("slug");
        if (slug == null || slug.trim().isEmpty()) {
            slug = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
        article.setSlug(slug);
        article.setContent((String) params.get("content"));
        String summary = (String) params.get("summary");
        if (summary == null) {
            summary = (String) params.get("excerpt");
        }
        article.setSummary(summary);
        article.setCover((String) params.getOrDefault("cover", ""));
        article.setCategoryId(resolveCategoryId(params));
        article.setStatus(1);
        article.setIsTop(0);
        
        List<Long> tagIds = resolveTagIds(params);
        
        return Result.success(articleService.create(article, tagIds));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Article> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        
        article.setTitle((String) params.get("title"));
        if (params.containsKey("slug")) {
            article.setSlug((String) params.get("slug"));
        }
        article.setContent((String) params.get("content"));
        String summary = (String) params.get("summary");
        if (summary == null && params.containsKey("excerpt")) {
            summary = (String) params.get("excerpt");
        }
        if (summary != null) {
            article.setSummary(summary);
        }
        if (params.containsKey("cover")) {
            article.setCover((String) params.get("cover"));
        }

        Long categoryId = resolveCategoryId(params);
        if (categoryId != null || params.containsKey("categoryId") || params.containsKey("category")) {
            article.setCategoryId(categoryId);
        }
        
        List<Long> tagIds = resolveTagIds(params);
        
        return Result.success(articleService.update(article, tagIds));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    private Long resolveCategoryId(Map<String, Object> params) {
        if (params.containsKey("categoryId") && params.get("categoryId") != null) {
            return ((Number) params.get("categoryId")).longValue();
        }

        Object categoryName = params.get("category");
        if (categoryName instanceof String) {
            String name = ((String) categoryName).trim();
            if (!name.isEmpty()) {
                return categoryService.listAll().stream()
                        .filter(c -> name.equals(c.getName()))
                        .map(Category::getId)
                        .findFirst()
                        .orElseGet(() -> categoryService.createByName(name));
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Long> resolveTagIds(Map<String, Object> params) {
        if (params.containsKey("tagIds") && params.get("tagIds") != null) {
            List<?> list = (List<?>) params.get("tagIds");
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(v -> ((Number) v).longValue())
                    .collect(Collectors.toList());
        }

        if (params.containsKey("tags") && params.get("tags") != null) {
            List<?> names = (List<?>) params.get("tags");
            List<String> tagNames = names.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            if (tagNames.isEmpty()) {
                return new ArrayList<>();
            }

            Map<String, Long> existingTagMap = tagService.listAll().stream()
                    .collect(Collectors.toMap(Tag::getName, Tag::getId));
            List<Long> result = new ArrayList<>();
            for (String name : tagNames) {
                Long tagId = existingTagMap.get(name);
                if (tagId != null) {
                    result.add(tagId);
                } else {
                    result.add(tagService.createByName(name));
                }
            }
            return result;
        }
        return null;
    }

    private Map<String, Object> buildArticleResponse(Article article,
                                                     Map<Long, String> categoryNameById,
                                                     Map<Long, String> tagNameById,
                                                     Map<Long, List<Long>> tagIdsByArticleId,
                                                     boolean includeContent) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", article.getId());
        data.put("title", article.getTitle());
        data.put("slug", article.getSlug());
        if (includeContent) {
            data.put("content", article.getContent());
        }
        String excerpt = isBlank(article.getSummary()) ? deriveExcerpt(article.getContent()) : article.getSummary();
        data.put("summary", excerpt);
        data.put("excerpt", excerpt);
        data.put("cover", article.getCover());
        data.put("categoryId", article.getCategoryId());
        data.put("category", categoryNameById.get(article.getCategoryId()));
        data.put("views", article.getViews());
        data.put("status", article.getStatus());
        data.put("isTop", article.getIsTop());
        data.put("createdAt", article.getCreatedAt());
        data.put("updatedAt", article.getUpdatedAt());
        data.put("created_at", article.getCreatedAt());
        data.put("updated_at", article.getUpdatedAt());

        List<Long> tagIds = tagIdsByArticleId.getOrDefault(article.getId(), Collections.emptyList());
        List<String> tagNames = tagIds.stream()
                .map(tagNameById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        data.put("tags", tagNames);
        data.put("tagIds", tagIds);

        return data;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private String deriveExcerpt(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        String text = content;
        
        text = text.replaceAll("```[\\s\\S]*?```", "");
        text = text.replaceAll("```[^`]*```", "");
        text = text.replaceAll("`{1,3}[^`]+`{1,3}", "");
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        text = text.replaceAll("\\*(.+?)\\*", "$1");
        text = text.replaceAll("__(.+?)__", "$1");
        text = text.replaceAll("_(.+?)_", "$1");
        text = text.replaceAll("~~(.+?)~~", "$1");
        text = text.replaceAll("\\!\\[([^\\]]*)\\]\\([^)]+\\)", "");
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");
        
        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("^#{1,6}\\s.*") || 
                trimmed.matches("^[-*+]\\s.*") ||
                trimmed.matches("^\\d+\\.\\s.*") ||
                trimmed.matches("^>\\s.*") ||
                trimmed.matches("^---+$") ||
                trimmed.matches("^[*-]+$")) {
                continue;
            }
            sb.append(line).append("\n");
        }
        text = sb.toString();
        
        text = text.replaceAll("\\s+", " ").trim();

        if (text.length() <= EXCERPT_MAX_LENGTH) {
            return text;
        }
        
        int cut = text.substring(0, EXCERPT_MAX_LENGTH).lastIndexOf(' ');
        return cut > 50 ? text.substring(0, cut) + "..." : text.substring(0, EXCERPT_MAX_LENGTH) + "...";
    }
}
