package com.erwang.blog.controller;

import com.erwang.blog.common.Result;
import com.erwang.blog.entity.Tag;
import com.erwang.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    @GetMapping
    public Result<List<String>> list() {
        List<String> names = tagService.listAll().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
        return Result.success(names);
    }
    
    @GetMapping("/{id}")
    public Result<Tag> getById(@PathVariable Long id) {
        return Result.success(tagService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Long> create(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        if (name == null || name.trim().isEmpty()) {
            return Result.error("标签名称不能为空");
        }
        return Result.success(tagService.createByName(name.trim()));
    }
}
