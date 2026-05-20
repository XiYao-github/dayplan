## 项目概述
- 项目名称：dayplan（公安网高安全等级企业级全栈框架）
- 架构模式：Spring Boot 3 单体式多模块 Maven 项目
- 安全等级：等保合规，公安网标准

## 模块结构

### system 层（基础能力）
- framework：Spring Boot 3 基础配置
- security+jwt：Spring Security 6 + JWT + RBAC，扩展三员管理
- governance：Resilience4j 限流、熔断、降级、隔离、重试
- encrypt：国密 SM2/SM3/SM4 全链路加密与脱敏
- log：日志审计，等保合规，防篡改
- common：线程池、文件上传、短信、代码生成器

### service 层（业务逻辑）
- 纯业务逻辑实现，按领域划分模块
- 不直接暴露给外部

### app 层（对外接口）
- 管理后台接口
- 小程序接口
- 开放 API 接口

## 后端技术栈
- 基础框架：Spring Boot 3.x
- 安全框架：Spring Security 6 + JWT
- 加密算法：国密 SM2/SM3/SM4
- 弹性治理：Resilience4j
- 数据库：MySQL
- 缓存：Redis
- 构建工具：Maven（多模块）

## 前端技术栈

### 管理后台
- 框架：Vue 3 + Vite
- 状态管理：Pinia
- 路由：Vue Router（动态路由 + 权限守卫）
- UI 组件：Element Plus
- HTTP：Axios（JWT Token、请求签名、SM2/SM4 加密）
- 安全：CSRF Token、空闲自动登出、按钮级权限

### 小程序端
- 框架：UniApp（Vue 3 + 微信小程序/H5 多端）
- 状态管理：Pinia
- UI 组件：uni-ui
- 特性：微信静默登录、分包加载、请求签名加密

## 运维部署
- 容器化：Docker + docker-compose
- Web 服务：Nginx（反向代理、静态资源托管）
- 更新流程：SSH 脚本拉取代码 → 前端构建 → Maven 打包 → 滚动更新

## 开发规范
- 代码注释使用中文
- 遵循阿里巴巴 Java 开发手册
- 敏感数据必须加密（SM2/SM3/SM4）
- 接口响应统一封装格式
- 异常统一处理

## 对话要求
- 回答时结合本项目架构特点
- 提供代码时注明应该放在哪个模块（system/service/app）
- 涉及加密操作时优先使用国密算法
- 安全相关的修改需额外说明

