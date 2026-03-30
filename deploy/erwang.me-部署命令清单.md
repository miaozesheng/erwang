# erwang.me 部署命令清单

## 一、本地打包

### 1) 打包前端
```bash
cd /Users/miaozesheng/study/erwang/frontend
npm install
npm run build
```

### 2) 打包后端
```bash
cd /Users/miaozesheng/study/erwang/backend
mvn clean package -DskipTests
```

---

## 二、上传到服务器

把 `你的服务器IP` 替换成真实 IP。

```bash
scp -r /Users/miaozesheng/study/erwang/frontend/dist/* root@你的服务器IP:/www/wwwroot/erwang/frontend/dist/
scp /Users/miaozesheng/study/erwang/backend/target/blog-backend-1.0.0.jar root@你的服务器IP:/www/wwwroot/erwang/backend/app.jar
scp /Users/miaozesheng/study/erwang/backend/sql/init.sql root@你的服务器IP:/www/wwwroot/erwang/backend/sql/init.sql
scp /Users/miaozesheng/study/erwang/deploy/* root@你的服务器IP:/www/wwwroot/erwang/deploy/
```

---

## 三、服务器初始化目录

```bash
mkdir -p /www/wwwroot/erwang/{frontend/dist,backend,uploads,deploy}
mkdir -p /www/wwwroot/erwang/backend/sql
```

---

## 四、数据库初始化

### 1) 进入 MySQL
```bash
mysql -uroot -p
```

### 2) 执行 SQL
```sql
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'bloguser'@'127.0.0.1' IDENTIFIED BY '请改成你的数据库密码';
GRANT ALL PRIVILEGES ON blog.* TO 'bloguser'@'127.0.0.1';
FLUSH PRIVILEGES;
```

### 3) 导入表结构
```bash
mysql -uroot -p blog < /www/wwwroot/erwang/backend/sql/init.sql
```

---

## 五、配置后端环境变量

```bash
cp /www/wwwroot/erwang/deploy/erwang-backend.env.example /www/wwwroot/erwang/deploy/erwang-backend.env
vi /www/wwwroot/erwang/deploy/erwang-backend.env
```

建议写成：

```env
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=9090

DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=blog
DB_USERNAME=bloguser
DB_PASSWORD=你的数据库密码

JWT_SECRET=请替换成一串至少32位的随机密钥
JWT_EXPIRATION=86400000

CORS_ORIGINS=https://erwang.me,https://www.erwang.me
APP_UPLOAD_DIR=/www/wwwroot/erwang/uploads
```

---

## 六、安装并启动后端服务

```bash
cp /www/wwwroot/erwang/deploy/erwang.service /etc/systemd/system/erwang.service
systemctl daemon-reload
systemctl enable erwang
systemctl start erwang
systemctl status erwang
```

查看日志：

```bash
journalctl -u erwang -f
```

---

## 七、验证后端

```bash
curl http://127.0.0.1:9090/api/articles
```

---

## 八、宝塔 Nginx 站点配置

宝塔站点域名：

```text
erwang.me
www.erwang.me
```

站点根目录：

```text
/www/wwwroot/erwang/frontend/dist
```

Nginx 核心配置：

```nginx
server {
    listen 80;
    server_name erwang.me www.erwang.me;

    root /www/wwwroot/erwang/frontend/dist;
    index index.html;

    location /api/ {
        proxy_pass http://127.0.0.1:9090/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

---

## 九、最终检查

```bash
systemctl status erwang
curl http://127.0.0.1:9090/api/articles
```

浏览器访问：

```text
https://erwang.me
https://www.erwang.me
```
