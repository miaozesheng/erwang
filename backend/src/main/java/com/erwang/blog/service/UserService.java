package com.erwang.blog.service;

import com.erwang.blog.entity.User;

import java.util.Map;

public interface UserService {
    User register(String username, String password, String email, String phone);
    User login(String username, String password);
    User getUserById(Long id);
    User getUserByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    User updateProfile(Long userId, Map<String, Object> params);
    void changePassword(Long userId, String oldPassword, String newPassword);
}
