package com.erwang.blog.controller;

import com.erwang.blog.common.Result;
import com.erwang.blog.config.JwtUtil;
import com.erwang.blog.entity.User;
import com.erwang.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDirPath;

    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    ));
    
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String email = params.get("email");
        String phone = params.get("phone");

        if (email != null) {
            email = email.trim();
        }
        if (phone != null) {
            phone = phone.trim();
        }
        
        if (username == null || password == null) {
            return Result.error("参数不完整");
        }

        if ((email == null || email.isEmpty()) && (phone == null || phone.isEmpty())) {
            return Result.error("邮箱和手机号至少填写一项");
        }

        if (email != null && !email.isEmpty() && userService.existsByEmail(email)) {
            return Result.error("邮箱已存在");
        }

        if (phone != null && !phone.isEmpty() && userService.existsByPhone(phone)) {
            return Result.error("手机号已存在");
        }
        
        try {
            User user = userService.register(
                    username,
                    password,
                    email == null || email.isEmpty() ? null : email,
                    phone == null || phone.isEmpty() ? null : phone
            );
            user.setPassword(null);
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("role", user.getRole());
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
            user.setPassword(null);
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("role", user.getRole());
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
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(Authentication authentication, @RequestBody Map<String, Object> params) {
        if (authentication == null) {
            return Result.error(401, "未登录");
        }

        try {
            User currentUser = (User) authentication.getPrincipal();
            User user = userService.updateProfile(currentUser.getId(), params);
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<Void> changePassword(Authentication authentication, @RequestBody Map<String, String> params) {
        if (authentication == null) {
            return Result.error(401, "未登录");
        }

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }

        try {
            User currentUser = (User) authentication.getPrincipal();
            userService.changePassword(currentUser.getId(), oldPassword, newPassword);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/avatar")
    public Result<Map<String, Object>> uploadAvatar(Authentication authentication, @RequestParam("file") MultipartFile file) {
        if (authentication == null) {
            return Result.error(401, "未登录");
        }
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            Path uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
            if (Files.notExists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            if (!ALLOWED_AVATAR_EXTENSIONS.contains(extension)) {
                return Result.error("不支持的文件类型，仅允许上传图片文件");
            }

            String filename = UUID.randomUUID() + extension;
            Path target = uploadDir.resolve(filename).normalize();

            if (!target.startsWith(uploadDir)) {
                return Result.error("非法文件路径");
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = "/api/files/" + filename;
            User currentUser = (User) authentication.getPrincipal();
            Map<String, Object> updateParams = new HashMap<>();
            updateParams.put("avatar", url);
            User updatedUser = userService.updateProfile(currentUser.getId(), updateParams);

            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("updatedAt", updatedUser.getUpdatedAt().toString());
            return Result.success(data);
        } catch (IOException e) {
            return Result.error("文件上传失败");
        }
    }
}
