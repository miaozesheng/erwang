package com.erwang.blog.controller;

import com.erwang.blog.common.Result;
import com.erwang.blog.entity.Category;
import com.erwang.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    public Result<List<String>> list() {
        List<String> names = categoryService.listAll().stream()
                .map(Category::getName)
                .collect(Collectors.toList());
        return Result.success(names);
    }
    
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Long> create(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        if (name == null || name.trim().isEmpty()) {
            return Result.error("分类名称不能为空");
        }
        return Result.success(categoryService.createByName(name.trim()));
    }
}
