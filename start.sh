#!/bin/bash
# ============================================
# 用途：一键部署 Spring Boot 项目到 Docker
# 用法：在项目根目录执行 ./start.sh
# 前提：已安装 Docker、Docker Compose、Maven
# ============================================

# set -e：任何命令执行失败时立即退出脚本，避免错误继续执行
set -e

# 自动创建 networks 网络（如果已存在则跳过）
# 原因：MySQL、Redis 等基础服务部署在此网络，应用容器需要加入
# || true：网络已存在时 docker network create 会报错，加此参数忽略错误
docker network create networks 2>/dev/null || true

# ============================================
# 颜色输出配置（美化控制台显示）
# ============================================
GREEN='\033[0;32m'   # 绿色，用于正常信息
RED='\033[0;31m'     # 红色，用于错误信息
NC='\033[0m'         # 重置颜色，恢复默认样式

# 日志输出函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ============================================
# 获取项目配置
# ============================================

# 获取当前目录的文件夹名作为项目名
# basename：取路径的最后一部分
# $(pwd)：获取当前工作目录路径
# 例如：/home/user/my-project → my-project
PROJECT_NAME=$(basename $(pwd))
log_info "项目名称: ${PROJECT_NAME}"

# 检查 .env 文件是否存在
if [ ! -f ".env" ]; then
    log_error ".env 文件不存在，请创建 .env 文件"
    exit 1
fi

# 读取 .env 文件中的配置
# source：在当前 shell 中执行 .env 文件，使变量生效
source .env

# 检查端口是否已配置
if [ -z "$PORT" ]; then
    log_error ".env 中未配置 PORT"
    exit 1
fi
log_info "服务端口: ${PORT}"

# 导出环境变量，供 docker-compose.yml 使用
export PROJECT_NAME=${PROJECT_NAME}
export PORT=${PORT}

# ============================================
# 部署流程（5 步）
# ============================================
log_info "========== 开始部署 =========="

# ---- 第1步：拉取最新代码 ----
log_info "[1/5] 拉取最新代码..."
git pull
log_info "[1/5] 代码拉取完成"

# ---- 第2步：Maven 打包 ----
log_info "[2/5] Maven 打包中..."
# mvn clean package：clean（清理旧编译文件）+ package（编译、测试、打包）
# -DskipTests：跳过单元测试，加快打包速度
mvn clean package -DskipTests

# 检查 JAR 文件是否生成成功
if [ ! -f "target/${PROJECT_NAME}.jar" ]; then
    log_error "JAR 文件生成失败: target/${PROJECT_NAME}.jar"
    exit 1
fi
log_info "[2/5] 打包完成"

# ---- 第3步：构建 Docker 镜像 ----
log_info "[3/5] 构建 Docker 镜像..."
# docker build：构建镜像
# -t ${PROJECT_NAME}：给镜像打标签（tag），名称为项目名
# -f Dockerfile：指定使用的 Dockerfile
# . ：构建上下文为当前目录，Docker 可以访问 target/${PROJECT_NAME}.jar
docker build -t ${PROJECT_NAME} -f Dockerfile .
log_info "[3/5] 镜像构建完成"

# ---- 第4步：启动容器 ----
log_info "[4/5] 启动容器..."
# docker-compose up：创建并启动容器
# -d：后台运行（detached mode）
# --build：启动前重新构建镜像，确保使用最新代码
docker-compose up -d --build
log_info "[4/5] 容器启动完成"

# ---- 第5步：清理旧镜像 ----
log_info "[5/5] 清理旧镜像..."
# docker image prune：删除所有悬挂（dangling）镜像
# -f：强制删除，不询问确认
# 悬挂镜像：没有标签的镜像，通常是构建过程中产生的中间产物
docker image prune -f

# ============================================
# 部署完成
# ============================================
log_info "========== 部署完成 =========="
echo ""
log_info "常用命令："
log_info "  查看日志: docker logs -f ${PROJECT_NAME}"
log_info "  查看本地日志: tail -f logs/app.log"
log_info "  停止服务: docker-compose down"
log_info "  重启服务: docker-compose restart"
log_info "  查看状态: docker-compose ps"