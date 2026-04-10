package com.erwang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erwang.blog.entity.GithubProject;
import com.erwang.blog.mapper.GithubProjectMapper;
import com.erwang.blog.service.GithubProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubProjectServiceImpl implements GithubProjectService {
    
    private final GithubProjectMapper githubProjectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public IPage<GithubProject> listByCategory(String category, String language, String keyword, Integer page, Integer size) {
        int pageNo = page == null ? 1 : page;
        int pageSize = size == null ? 20 : Math.min(size, 100);
        
        LocalDate fetchDate = LocalDate.now();
        Page<GithubProject> pagination = new Page<>(pageNo, pageSize);
        
        LambdaQueryWrapper<GithubProject> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(category) && !"all".equalsIgnoreCase(category)) {
            wrapper.eq(GithubProject::getPrimaryCategory, category);
        }
        
        if (StringUtils.isNotBlank(language)) {
            wrapper.eq(GithubProject::getLanguage, language);
        }
        
        if (StringUtils.isNotBlank(keyword)) {
            String pattern = "%" + keyword + "%";
            wrapper.and(w -> w
                .like(GithubProject::getRepoName, pattern)
                .or()
                .like(GithubProject::getDescription, pattern)
                .or()
                .like(GithubProject::getOwnerName, pattern)
            );
        }
        
        wrapper.eq(GithubProject::getFetchDate, fetchDate)
            .orderByDesc(GithubProject::getStars);
        
        return githubProjectMapper.selectPage(pagination, wrapper);
    }
    
    @Override
    public List<String> getAvailableLanguages() {
        List<GithubProject> projects = githubProjectMapper.selectList(
            new LambdaQueryWrapper<GithubProject>()
                .isNotNull(GithubProject::getLanguage)
                .eq(GithubProject::getFetchDate, LocalDate.now())
                .groupBy(GithubProject::getLanguage)
        );
        return projects.stream()
            .map(GithubProject::getLanguage)
            .filter(StringUtils::isNotBlank)
            .sorted()
            .collect(Collectors.toList());
    }
    
    @Override
    public void syncFromGithub() {
        log.info("Start syncing GitHub trending data");
        LocalDate today = LocalDate.now();
        
        fetchAndSaveCategory("trending", "since=daily");
        fetchAndSaveCategory("weekly", "since=weekly");
        fetchAndSaveCategory("monthly", "since=monthly");
        fetchAndSaveAllTime();
        
        log.info("GitHub trending data sync completed");
    }
    
    private void fetchAndSaveCategory(String category, String sinceParam) {
        try {
            String url = "https://api.github.com/search/repositories?q=stars:>1&sort=stars&order=desc&per_page=30";
            Map result = restTemplate.getForObject(url, Map.class);
            
            if (result != null && result.get("items") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
                saveProjects(items, category);
            }
        } catch (Exception e) {
            log.error("Failed to fetch GitHub {} data: {}", category, e.getMessage());
        }
    }
    
    private void fetchAndSaveAllTime() {
        try {
            String url = "https://api.github.com/search/repositories?q=stars:>10000&sort=stars&order=desc&per_page=30";
            Map result = restTemplate.getForObject(url, Map.class);
            
            if (result != null && result.get("items") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
                saveProjects(items, "all_time");
            }
        } catch (Exception e) {
            log.error("Failed to fetch GitHub all_time data: {}", e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void saveProjects(List<Map<String, Object>> items, String category) {
        LocalDate today = LocalDate.now();
        
        for (Map<String, Object> item : items) {
            try {
                GithubProject project = new GithubProject();
                
                String fullName = (String) item.get("full_name");
                project.setRepoName(fullName);
                project.setFullName(fullName);
                project.setDescription((String) item.get("description"));
                project.setUrl((String) item.get("html_url"));
                project.setLanguage((String) item.get("language"));
                project.setStars((Integer) item.get("stargazers_count"));
                project.setForks((Integer) item.get("forks_count"));
                project.setOpenIssues((Integer) item.get("open_issues_count"));
                project.setWatchers((Integer) item.get("watchers_count"));
                
                Map<String, Object> licenseObj = (Map<String, Object>) item.get("license");
                project.setLicense(licenseObj != null ? (String) licenseObj.get("name") : null);
                
                List<String> topicsList = (List<String>) item.get("topics");
                if (topicsList != null && !topicsList.isEmpty()) {
                    project.setTopics(String.join(",", topicsList));
                }
                
                Map<String, Object> owner = (Map<String, Object>) item.get("owner");
                if (owner != null) {
                    project.setOwnerAvatar((String) owner.get("avatar_url"));
                    project.setOwnerName((String) owner.get("login"));
                }
                
                project.setPrimaryCategory(category);
                project.setFetchDate(today);
                
                githubProjectMapper.insert(project);
            } catch (Exception e) {
                log.debug("Failed to save project: {}", e.getMessage());
            }
        }
    }
}