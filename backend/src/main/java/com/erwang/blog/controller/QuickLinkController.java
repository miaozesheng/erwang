package com.erwang.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erwang.blog.common.Result;
import com.erwang.blog.entity.QuickLink;
import com.erwang.blog.mapper.QuickLinkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quick-links")
@RequiredArgsConstructor
public class QuickLinkController {

    private final QuickLinkMapper quickLinkMapper;

    @GetMapping
    @Cacheable(value = "quickLink", key = "'list'")
    public Result<List<QuickLink>> list() {
        LambdaQueryWrapper<QuickLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(QuickLink::getSort);
        return Result.success(quickLinkMapper.selectList(wrapper));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @CacheEvict(value = "quickLink", key = "'list'")
    public Result<QuickLink> create(@RequestBody QuickLink link) {
        quickLinkMapper.insert(link);
        return Result.success(link);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @CacheEvict(value = "quickLink", key = "'list'")
    public Result<QuickLink> update(@PathVariable Long id, @RequestBody QuickLink link) {
        link.setId(id);
        quickLinkMapper.updateById(link);
        return Result.success(link);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @CacheEvict(value = "quickLink", key = "'list'")
    public Result<Void> delete(@PathVariable Long id) {
        quickLinkMapper.deleteById(id);
        return Result.success();
    }
}
