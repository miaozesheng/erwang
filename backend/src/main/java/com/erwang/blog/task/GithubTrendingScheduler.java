package com.erwang.blog.task;

import com.erwang.blog.service.GithubProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubTrendingScheduler {
    
    private final GithubProjectService githubProjectService;
    
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000)
    public void fetchTrending() {
        log.info("Start scheduled GitHub trending sync");
        githubProjectService.syncFromGithub();
        log.info("Scheduled GitHub trending sync completed");
    }
}