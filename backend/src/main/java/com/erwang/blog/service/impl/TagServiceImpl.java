package com.erwang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erwang.blog.entity.Tag;
import com.erwang.blog.mapper.TagMapper;
import com.erwang.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    
    private final TagMapper tagMapper;
    
    @Override
    @Cacheable(value = "tag", key = "'listAll'")
    public List<Tag> listAll() {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Tag::getName);
        return tagMapper.selectList(wrapper);
    }
    
    @Override
    public Tag getById(Long id) {
        return tagMapper.selectById(id);
    }

    @Override
    @CacheEvict(value = "tag", key = "'listAll'")
    public Long createByName(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tagMapper.insert(tag);
        return tag.getId();
    }
}