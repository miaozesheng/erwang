package com.erwang.blog.controller;

import com.erwang.blog.common.Result;
import com.erwang.blog.entity.Category;
import com.erwang.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }
    
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }
}