package com.erwang.blog.controller;

import com.erwang.blog.common.Result;
import com.erwang.blog.entity.Tag;
import com.erwang.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    @GetMapping
    public Result<List<Tag>> list() {
        return Result.success(tagService.listAll());
    }
    
    @GetMapping("/{id}")
    public Result<Tag> getById(@PathVariable Long id) {
        return Result.success(tagService.getById(id));
    }
}