package com.erwang.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("github_project")
public class GithubProject {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String repoName;
    private String fullName;
    private String description;
    private String url;
    private String language;
    private Integer stars;
    private Integer forks;
    private Integer openIssues;
    private Integer watchers;
    private String license;
    private String topics;
    private String ownerAvatar;
    private String ownerName;
    private String primaryCategory;
    private LocalDate fetchDate;

    @TableField(exist = false)
    private Integer starGrowth;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
