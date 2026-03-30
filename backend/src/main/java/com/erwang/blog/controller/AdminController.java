package com.erwang.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.erwang.blog.common.Result;
import com.erwang.blog.entity.Article;
import com.erwang.blog.entity.Category;
import com.erwang.blog.entity.Tag;
import com.erwang.blog.mapper.ArticleMapper;
import com.erwang.blog.mapper.CategoryMapper;
import com.erwang.blog.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Long articleCount = articleMapper.selectCount(new QueryWrapper<Article>().eq("status", 1));
        Long totalViews = articleMapper.selectObjs(new QueryWrapper<Article>()
                        .select("COALESCE(SUM(views), 0)")
                        .eq("status", 1))
                .stream()
                .findFirst()
                .map(v -> ((Number) v).longValue())
                .orElse(0L);
        Long categoryCount = categoryMapper.selectCount(new QueryWrapper<Category>());
        Long tagCount = tagMapper.selectCount(new QueryWrapper<Tag>());

        Map<String, Object> data = new HashMap<>();
        data.put("articleCount", articleCount);
        data.put("totalViews", totalViews);
        data.put("categoryCount", categoryCount);
        data.put("tagCount", tagCount);
        return Result.success(data);
    }
}
