# 2026-04-12 博客自动化部署实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现一个自动化的部署脚本，完成本地构建产物到阿里云服务器的同步与服务重启。

**Architecture:** 
使用 Bash 脚本封装本地检查、SSH 远程备份、SCP 上传、SSH 远程重启和 curl 健康检查。脚本将配置信息（如 IP、路径）作为变量提取，便于维护。

**Tech Stack:** Bash, SSH, SCP, systemd.

---

### Task 1: 创建部署脚本骨架

**Files:**
- Create: `scripts/deploy.sh`

- [ ] **Step 1: 编写脚本初始代码**

```bash
#!/bin/bash
# Erwang Blog Automation Deployment Script

set -e

# --- Configuration ---
SERVER_IP="47.102.109.115"
SERVER_USER="root"
REMOTE_FRONTEND_PATH="/www/wwwroot/erwang"
REMOTE_BACKEND_PATH="/www/server/erwang/backend"
REMOTE_BACKUP_PATH="/www/server/erwang/backups"
LOCAL_FRONTEND_DIST="frontend/dist"
LOCAL_BACKEND_JAR="backend/target/blog-backend-1.0.0.jar"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo ">>> Starting deployment at $TIMESTAMP"

# 1. Local Check
if [ ! -d "$LOCAL_FRONTEND_DIST" ] || [ ! -f "$LOCAL_BACKEND_JAR" ]; then
    echo "Error: Build artifacts not found. Please run build first."
    exit 1
fi
```

- [ ] **Step 2: 赋予执行权限并测试逻辑**

Run: `chmod +x scripts/deploy.sh && ./scripts/deploy.sh`
Expected: 如果本地没有构建产物则报错退出，否则继续（目前仅有检查逻辑）。

- [ ] **Step 3: Commit**

```bash
git add scripts/deploy.sh
git commit -m "deploy: init deployment script skeleton"
```

---

### Task 2: 实现远程备份与上传逻辑

**Files:**
- Modify: `scripts/deploy.sh`

- [ ] **Step 1: 添加备份与上传代码**

```bash
# 2. Remote Backup
echo ">>> Backing up remote files..."
ssh $SERVER_USER@$SERVER_IP "mkdir -p $REMOTE_BACKUP_PATH/$TIMESTAMP && \
    [ -f $REMOTE_BACKEND_PATH/app.jar ] && cp $REMOTE_BACKEND_PATH/app.jar $REMOTE_BACKUP_PATH/$TIMESTAMP/app.jar.bak; \
    [ -d $REMOTE_FRONTEND_PATH ] && cp -r $REMOTE_FRONTEND_PATH $REMOTE_BACKUP_PATH/$TIMESTAMP/frontend_bak"

# 3. Upload
echo ">>> Uploading artifacts..."
scp -r $LOCAL_FRONTEND_DIST/* $SERVER_USER@$SERVER_IP:$REMOTE_FRONTEND_PATH/
scp $LOCAL_BACKEND_JAR $SERVER_USER@$SERVER_IP:$REMOTE_BACKEND_PATH/app.jar
```

- [ ] **Step 2: 验证脚本结构**

Run: `bash -n scripts/deploy.sh`
Expected: 无语法错误。

- [ ] **Step 3: Commit**

```bash
git add scripts/deploy.sh
git commit -m "deploy: add backup and upload logic to script"
```

---

### Task 3: 实现重启与健康检查逻辑

**Files:**
- Modify: `scripts/deploy.sh`

- [ ] **Step 1: 添加重启与检查代码**

```bash
# 4. Restart Service
echo ">>> Restarting backend service..."
ssh $SERVER_USER@$SERVER_IP "systemctl restart erwang"

# 5. Health Check
echo ">>> Running health check..."
sleep 5
HTTP_STATUS=$(ssh $SERVER_USER@$SERVER_IP "curl -s -o /dev/null -w \"%{http_code}\" http://127.0.0.1:9090/api/articles")

if [ "$HTTP_STATUS" == "200" ]; then
    echo ">>> Deployment Successful! (Status: $HTTP_STATUS)"
else
    echo ">>> Health check failed (Status: $HTTP_STATUS). Please check server logs."
    exit 1
fi
```

- [ ] **Step 2: 最终脚本验证**

Run: `cat scripts/deploy.sh`
Expected: 包含完整的五个阶段。

- [ ] **Step 3: Commit**

```bash
git add scripts/deploy.sh
git commit -m "deploy: complete deployment script with restart and health check"
```

---

### Task 4: 执行部署

- [ ] **Step 1: 执行脚本**

Run: `./scripts/deploy.sh`
Expected: 依次显示备份、上传、重启、成功信息。

- [ ] **Step 2: 手动验证线上页面**

访问 `https://erwang.me` 确认页面已更新。
