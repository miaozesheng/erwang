# 2026-04-12 博客自动化部署方案设计

## 1. 背景与目标
当前博客已完成功能增强（登录动画、快速链接编辑等），需要将本地构建产物同步到阿里云 ECS 服务器（47.102.109.115）。为了提高效率并降低操作风险，设计一套自动化部署流程。

## 2. 部署架构
- **本地环境**: macOS, JDK 1.8, Node.js 18+, Maven.
- **目标服务器**: 阿里云 ECS (CentOS/Debian-like via 宝塔), Nginx, MySQL 5.7, systemd.
- **同步方式**: SSH/SCP/rsync.

## 3. 详细设计

### 3.1 核心逻辑 (deploy.sh)
设计一个 `scripts/deploy.sh` 脚本，包含以下阶段：

1. **Build Phase (Optional)**:
   - `cd frontend && npm run build`
   - `cd backend && mvn clean package -DskipTests`

2. **Backup Phase**:
   - 在服务器端创建目录 `/www/server/erwang/backups/YYYYMMDD_HHMMSS`。
   - 移动当前的 `app.jar` 和 `erwang/` 静态目录到备份目录。

3. **Upload Phase**:
   - 上传 `frontend/dist/*` 到 `/www/wwwroot/erwang/`。
   - 上传 `backend/target/blog-backend-1.0.0.jar` 到 `/www/server/erwang/backend/app.jar`。

4. **Activation Phase**:
   - 执行 `ssh root@47.102.109.115 "systemctl restart erwang"`。

5. **Validation Phase**:
   - `curl -I http://127.0.0.1:9090/api/articles`。

### 3.2 安全与回滚
- **环境变量**: 敏感信息（数据库密码等）已在服务器 `/www/server/erwang/deploy/erwang-backend.env` 中持久化，部署脚本仅替换运行产物。
- **回滚**: 若健康检查失败，脚本支持通过 SSH 将备份目录的文件移回原位并重启。

## 4. 实施计划
1. 创建 `docs/superpowers/specs/2026-04-12-deployment-design.md`。
2. 编写 `scripts/deploy.sh` 并赋予执行权限。
3. 执行部署并验证。

## 5. 验收标准
- [ ] 前端页面展示最新功能（登录页遮眼动画）。
- [ ] 后端接口 `/api/articles` 返回正常数据。
- [ ] 服务器端 `systemctl status erwang` 处于 `active (running)` 状态。
