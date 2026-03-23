package com.erwang.blog.controller;

import com.erwang.blog.common.Result;
import com.erwang.blog.config.JwtUtil;
import com.erwang.blog.entity.User;
import com.erwang.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        
        if (username == null || password == null) {
            return Result.error("参数不完整");
        }
        
        try {
            User user = userService.register(username, password);
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        
        if (username == null || password == null) {
            return Result.error("参数不完整");
        }
        
        try {
            User user = userService.login(username, password);
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/info")
    public Result<User> getUserInfo(Authentication authentication) {
        if (authentication == null) {
            return Result.error(401, "未登录");
        }
        
        User user = (User) authentication.getPrincipal();
        return Result.success(user);
    }
}