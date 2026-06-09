# Docker 部署与 CI/CD 集成

---

## 服务器初始化（全新服务器）

### 1. 安装 Docker

```bash
# 安装 Docker
curl -fsSL https://get.docker.com | sh

# 启用 Docker
systemctl enable docker

# 验证安装
docker --version
```

### 2. 安装 Docker Compose（可选）

```bash
# 下载 Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 添加执行权限
chmod +x /usr/local/bin/docker-compose

# 验证安装
docker-compose --version
```

### 3. 开放防火墙端口

```bash
# 开放 9999 端口（Dayplan 后端）
firewall-cmd --permanent --add-port=9999/tcp

# 开放 80 端口（Nginx 前端）
firewall-cmd --permanent --add-port=80/tcp

# 重载防火墙
firewall-cmd --reload

# 查看已开放端口
firewall-cmd --list-ports
```

### 4. 创建 Docker 网络（让容器间可以通信）

```bash
docker network create networks
```

### 5. 创建部署目录

```bash
mkdir -p /opt/dayplan
```

### 6. 生成 SSH 部署密钥

```bash
# 生成密钥（一路回车）
ssh-keygen -t rsa -b 4096 -C "deploy@server"

# 将公钥加入授权
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys

# 查看私钥内容（复制到 GitHub Secrets）
cat ~/.ssh/id_rsa
```

---

## MySQL 和 Redis 部署（Docker 方式）

### MySQL 部署

```bash
docker run -d \
  --name mysql \
  --network networks \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=你的密码 \
  -e MYSQL_DATABASE=dayplan \
  -v /opt/mysql/data:/var/lib/mysql \
  --restart unless-stopped \
  mysql:8.0
```

### Redis 部署

```bash
docker run -d \
  --name redis \
  --network networks \
  -p 6379:6379 \
  -v /opt/redis/data:/data \
  --restart unless-stopped \
  redis:7-alpine
```

### Nginx 部署（可选，前端）

```bash
docker run -d \
  --name nginx \
  --network networks \
  -p 80:80 \
  -v /opt/nginx/html:/usr/share/nginx/html \
  --restart unless-stopped \
  nginx:alpine
```

---

## GitHub CI/CD 配置

### 1. GitHub 仓库添加 Secrets

进入仓库 → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

| Secret 名称 | 值 |
|-------------|-----|
| SERVER_IP | 你的服务器公网 IP |
| SERVER_PORT | 22 |
| SERVER_USER | root |
| SERVER_SSH_KEY | 第6步复制的私钥内容 |

### 2. 创建 Workflow 文件

创建文件：`.github/workflows/deploy.yml`

```yaml
name: Deploy to Server

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build with Maven
        run: mvn clean package -DskipTests

      - name: Build Docker image
        run: docker build -t dayplan:latest .

      - name: Save JAR
        run: docker save -o dayplan.tar dayplan:latest

      - name: Transfer to server
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.SERVER_IP }}
          port: ${{ secrets.SERVER_PORT }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          source: "docker-compose.yml,Dockerfile,.env,start.sh,dayplan.tar"
          target: /opt/dayplan

      - name: Deploy to server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_IP }}
          port: ${{ secrets.SERVER_PORT }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          script: |
            cd /opt/dayplan
            docker load -i dayplan.tar
            docker-compose up -d
```

### 3. 提交到 GitHub

```bash
git add .github/workflows/deploy.yml
git commit -m "add ci/cd workflow"
git push
```

---

## 手动部署（不通过 CI/CD）

如果想手动部署，先把文件传到服务器：

```bash
scp docker-compose.yml Dockerfile .env start.sh root@服务器IP:/opt/dayplan/
scp target/dayplan.jar root@服务器IP:/opt/dayplan/
```

然后在服务器上执行：

```bash
cd /opt/dayplan
docker build -t dayplan:latest .
docker-compose up -d
```

---

## 部署后验证

```bash
# 查看容器状态
docker ps

# 查看日志
docker logs -f dayplan

# 访问服务
curl http://localhost:9999
```

---

## 完整流程图

```
新建服务器
    │
    ├── 安装 Docker
    ├── 开放端口（9999）
    ├── 创建 Docker 网络
    ├── 生成 SSH 密钥
    │
    └── 部署 MySQL/Redis（Nginx）
            │
            ▼
    GitHub 配置 Secrets
            │
            ▼
    push 代码自动触发 CI/CD
            │
            ├── GitHub 构建（JDK 17 + Maven）
            ├── Docker 打包镜像
            └── 传到服务器部署
            │
            ▼
    访问 http://IP:9999
```

---

## 常用服务器命令

```bash
# 进入容器
docker exec -it mysql bash

# 查看日志
docker logs -f dayplan

# 重启容器
docker-compose restart dayplan

# 停止容器
docker-compose down

# 删除旧镜像
docker image prune -f

# 查看容器资源使用
docker stats
```

---

## 文件清单

| 文件 | 说明 |
|------|------|
| `.github/workflows/deploy.yml` | CI/CD 工作流配置 |
| `docker-compose.yml` | 服务编排配置 |
| `Dockerfile` | Docker 镜像构建 |
| `.env` | 环境变量 |
| `start.sh` | 部署脚本 |
