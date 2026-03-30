package com.erwang.blog.service;

import com.erwang.blog.entity.Tag;
import java.util.List;

public interface TagService {
    List<Tag> listAll();
    Tag getById(Long id);
    Long createByName(String name);
}