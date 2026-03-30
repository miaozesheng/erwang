package com.erwang.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erwang.blog.common.Result;
import com.erwang.blog.entity.*;
import com.erwang.blog.mapper.ArticleFavoriteMapper;
import com.erwang.blog.mapper.ArticleLikeMapper;
import com.erwang.blog.service.ArticleService;
import com.erwang.blog.service.CategoryService;
import com.erwang.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interaction")
@RequiredArgsConstructor
public class InteractionController {

    private final ArticleLikeMapper likeMapper;
    private final ArticleFavoriteMapper favoriteMapper;
    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;

    @PostMapping("/like/{articleId}")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long articleId, Authentication auth) {
        User user = (User) auth.getPrincipal();
        LambdaQueryWrapper<ArticleLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleLike::getUserId, user.getId()).eq(ArticleLike::getArticleId, articleId);
        ArticleLike existing = likeMapper.selectOne(wrapper);
        boolean liked;
        if (existing != null) {
            likeMapper.deleteById(existing.getId());
            liked = false;
        } else {
            ArticleLike like = new ArticleLike();
            like.setUserId(user.getId());
            like.setArticleId(articleId);
            likeMapper.insert(like);
            liked = true;
        }
        long count = likeMapper.selectCount(new LambdaQueryWrapper<ArticleLike>().eq(ArticleLike::getArticleId, articleId));
        Map<String, Object> data = new HashMap<>();
        data.put("liked", liked);
        data.put("count", count);
        return Result.success(data);
    }

    @PostMapping("/favorite/{articleId}")
    public Result<Map<String, Object>> toggleFavorite(@PathVariable Long articleId, Authentication auth) {
        User user = (User) auth.getPrincipal();
        LambdaQueryWrapper<ArticleFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleFavorite::getUserId, user.getId()).eq(ArticleFavorite::getArticleId, articleId);
        ArticleFavorite existing = favoriteMapper.selectOne(wrapper);
        boolean favorited;
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            favorited = false;
        } else {
            ArticleFavorite fav = new ArticleFavorite();
            fav.setUserId(user.getId());
            fav.setArticleId(articleId);
            favoriteMapper.insert(fav);
            favorited = true;
        }
        long count = favoriteMapper.selectCount(new LambdaQueryWrapper<ArticleFavorite>().eq(ArticleFavorite::getArticleId, articleId));
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);
        data.put("count", count);
        return Result.success(data);
    }

    @GetMapping("/status/{articleId}")
    public Result<Map<String, Object>> getStatus(@PathVariable Long articleId, Authentication auth) {
        Map<String, Object> data = new HashMap<>();
        long likeCount = likeMapper.selectCount(new LambdaQueryWrapper<ArticleLike>().eq(ArticleLike::getArticleId, articleId));
        long favCount = favoriteMapper.selectCount(new LambdaQueryWrapper<ArticleFavorite>().eq(ArticleFavorite::getArticleId, articleId));
        data.put("likeCount", likeCount);
        data.put("favoriteCount", favCount);
        data.put("liked", false);
        data.put("favorited", false);
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            data.put("liked", likeMapper.selectCount(new LambdaQueryWrapper<ArticleLike>()
                    .eq(ArticleLike::getUserId, user.getId()).eq(ArticleLike::getArticleId, articleId)) > 0);
            data.put("favorited", favoriteMapper.selectCount(new LambdaQueryWrapper<ArticleFavorite>()
                    .eq(ArticleFavorite::getUserId, user.getId()).eq(ArticleFavorite::getArticleId, articleId)) > 0);
        }
        return Result.success(data);
    }

    @GetMapping("/my-favorites")
    public Result<List<Map<String, Object>>> myFavorites(Authentication auth) {
        User user = (User) auth.getPrincipal();
        List<ArticleFavorite> favs = favoriteMapper.selectList(
                new LambdaQueryWrapper<ArticleFavorite>().eq(ArticleFavorite::getUserId, user.getId()).orderByDesc(ArticleFavorite::getCreatedAt));
        List<Long> articleIds = favs.stream().map(ArticleFavorite::getArticleId).collect(Collectors.toList());
        return Result.success(buildArticleListByIds(articleIds));
    }

    @GetMapping("/my-likes")
    public Result<List<Map<String, Object>>> myLikes(Authentication auth) {
        User user = (User) auth.getPrincipal();
        List<ArticleLike> likes = likeMapper.selectList(
                new LambdaQueryWrapper<ArticleLike>().eq(ArticleLike::getUserId, user.getId()).orderByDesc(ArticleLike::getCreatedAt));
        List<Long> articleIds = likes.stream().map(ArticleLike::getArticleId).collect(Collectors.toList());
        return Result.success(buildArticleListByIds(articleIds));
    }

    private List<Map<String, Object>> buildArticleListByIds(List<Long> ids) {
        if (ids.isEmpty()) return new ArrayList<>();
        Map<Long, String> catMap = categoryService.listAll().stream().collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, String> tagMap = tagService.listAll().stream().collect(Collectors.toMap(Tag::getId, Tag::getName));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Long id : ids) {
            Article article = articleService.getById(id);
            if (article == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", article.getId());
            item.put("title", article.getTitle());
            item.put("excerpt", article.getSummary());
            item.put("category", catMap.get(article.getCategoryId()));
            item.put("created_at", article.getCreatedAt());
            item.put("views", article.getViews());
            List<Long> tagIds = articleService.getTagIdsByArticleId(article.getId());
            item.put("tags", tagIds.stream().map(tagMap::get).filter(Objects::nonNull).collect(Collectors.toList()));
            result.add(item);
        }
        return result;
    }
}
