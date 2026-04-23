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
    private static final int GITHUB_SYNC_PAGE_SIZE = 100;
    private static final int GITHUB_SYNC_PAGES = 3;
    
    private final GithubProjectMapper githubProjectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public IPage<GithubProject> listByCategory(String category, String language, String keyword, Integer page, Integer size) {
        int pageNo = page == null ? 1 : page;
        int pageSize = size == null ? 20 : Math.min(size, 100);
        Page<GithubProject> pagination = new Page<>(pageNo, pageSize);
        String normalizedCategory = normalizeCategory(category);
        String normalizedLanguage = StringUtils.isNotBlank(language) ? language.trim() : null;
        String normalizedKeyword = StringUtils.isNotBlank(keyword) ? keyword.trim() : null;

        if ("growth_7d".equalsIgnoreCase(normalizedCategory)) {
            LocalDate latestFetchDate = getLatestFetchDate();
            LocalDate cutoffDate = latestFetchDate != null ? latestFetchDate.minusDays(7) : LocalDate.now().minusDays(7);
            return githubProjectMapper.selectGrowthLeaderboard(pagination, cutoffDate, normalizedLanguage, normalizedKeyword);
        }

        if ("growth_30d".equalsIgnoreCase(normalizedCategory)) {
            LocalDate latestFetchDate = getLatestFetchDate();
            LocalDate cutoffDate = latestFetchDate != null ? latestFetchDate.minusDays(30) : LocalDate.now().minusDays(30);
            return githubProjectMapper.selectGrowthLeaderboard(pagination, cutoffDate, normalizedLanguage, normalizedKeyword);
        }

        return githubProjectMapper.selectTotalStarLeaderboard(pagination, normalizedLanguage, normalizedKeyword);
    }
    
    @Override
    public List<String> getAvailableLanguages() {
        return githubProjectMapper.selectAvailableLanguagesFromLatestRecords().stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public LocalDate getLatestFetchDate() {
        return githubProjectMapper.selectLatestFetchDate();
    }
    
    @Override
    public void syncFromGithub() {
        log.info("Start syncing GitHub leaderboard snapshots");
        fetchAndSaveAllTime();
        log.info("GitHub leaderboard snapshot sync completed");
    }
    
    private void fetchAndSaveAllTime() {
        try {
            for (int page = 1; page <= GITHUB_SYNC_PAGES; page++) {
                String url = String.format(
                        "https://api.github.com/search/repositories?q=stars:>1&sort=stars&order=desc&per_page=%d&page=%d",
                        GITHUB_SYNC_PAGE_SIZE,
                        page
                );
                Map result = restTemplate.getForObject(url, Map.class);

                if (result != null && result.get("items") != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
                    saveProjects(items, "all_time");
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch GitHub all_time data: {}", e.getMessage());
        }
    }

    private String normalizeCategory(String category) {
        if (StringUtils.isBlank(category)) {
            return null;
        }

        String trimmedCategory = category.trim();
        return "all".equalsIgnoreCase(trimmedCategory) ? null : trimmedCategory;
    }
    
    @SuppressWarnings("unchecked")
    private void saveProjects(List<Map<String, Object>> items, String category) {
        LocalDate today = LocalDate.now();
        
        for (Map<String, Object> item : items) {
            try {
                String fullName = (String) item.get("full_name");
                
                LambdaQueryWrapper<GithubProject> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(GithubProject::getRepoName, fullName)
                    .eq(GithubProject::getPrimaryCategory, category)
                    .eq(GithubProject::getFetchDate, today);
                GithubProject existingProject = githubProjectMapper.selectOne(checkWrapper);
                
                GithubProject project = existingProject != null ? existingProject : new GithubProject();
                
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
                
                if (existingProject != null) {
                    githubProjectMapper.updateById(project);
                } else {
                    githubProjectMapper.insert(project);
                }
            } catch (Exception e) {
                log.debug("Failed to save project: {}", e.getMessage());
            }
        }
    }
}
