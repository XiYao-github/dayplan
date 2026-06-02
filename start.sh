#!/bin/bash
# ============================================
# DayPlan 部署脚本
# 用法: ./start.sh
# ============================================

set -e

PROJECT_NAME="dayplan"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

log_info "========== DayPlan 部署开始 =========="

# 1. Git 拉取最新代码
log_info "1/4 拉取最新代码..."
git pull

# 2. Maven 打包
log_info "2/4 Maven 打包..."
mvn clean package -DskipTests

# 3. Docker 镜像构建
log_info "3/4 Docker 镜像构建..."
cd app/docker
mvn docker:build

# 4. Docker Compose 启动
log_info "4/4 启动容器..."
docker-compose up -d

# 返回项目根目录
cd ../..

log_info "========== 部署完成 =========="
log_info "查看日志: docker-compose -f app/docker/docker-compose.yml logs -f"
log_info "停止服务: docker-compose -f app/docker/docker-compose.yml down"