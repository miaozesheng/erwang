package com.erwang.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erwang.blog.entity.GithubProject;
import java.util.List;

public interface GithubProjectService {
    IPage<GithubProject> listByCategory(String category, String language, String keyword, Integer page, Integer size);
    List<String> getAvailableLanguages();
    void syncFromGithub();
}