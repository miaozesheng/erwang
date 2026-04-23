package com.erwang.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erwang.blog.entity.GithubProject;

import java.time.LocalDate;
import java.util.List;

public interface GithubProjectService {
    IPage<GithubProject> listByCategory(String category, String language, String keyword, Integer page, Integer size);
    List<String> getAvailableLanguages();
    LocalDate getLatestFetchDate();
    void syncFromGithub();
}
