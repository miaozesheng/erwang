package com.erwang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erwang.blog.entity.GithubProject;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GithubProjectMapper extends BaseMapper<GithubProject> {
}