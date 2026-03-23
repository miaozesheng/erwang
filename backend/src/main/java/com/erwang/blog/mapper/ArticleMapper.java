package com.erwang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erwang.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}