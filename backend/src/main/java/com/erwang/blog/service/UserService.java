package com.erwang.blog.service;

import com.erwang.blog.entity.User;

public interface UserService {
    User register(String username, String password);
    User login(String username, String password);
    User getUserById(Long id);
    User getUserByUsername(String username);
}