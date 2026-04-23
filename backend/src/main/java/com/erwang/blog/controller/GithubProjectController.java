package com.erwang.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erwang.blog.common.Result;
import com.erwang.blog.entity.GithubProject;
import com.erwang.blog.service.GithubProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GithubProjectController {
    
    private final GithubProjectService githubProjectService;
    
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        IPage<GithubProject> pageResult = githubProjectService.listByCategory(category, language, keyword, page, size);
        
        List<String> languages = githubProjectService.getAvailableLanguages();
        LocalDate latestFetchDate = githubProjectService.getLatestFetchDate();
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        data.put("languages", languages);
        data.put("latestFetchDate", latestFetchDate);
        
        return Result.success(data);
    }
    
    @PostMapping("/sync")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> sync() {
        githubProjectService.syncFromGithub();
        return Result.success(null);
    }
}
