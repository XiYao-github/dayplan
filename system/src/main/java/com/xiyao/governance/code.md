## crypto模块

- 流量治理（限流熔断降级）

### 功能描述

- 限流：控制接口每秒/每分钟访问次数，防止恶意请求
- 熔断：当错误率达到阈值时自动熔断，快速失败
- 降级：提供备用响应逻辑或兜底数据
- 隔离：通过信号量限制最大并发数
- 重试：遇到特定异常自动重试
- 防重提交：基于 Redis 实现幂等性，防止重复提交

### 技术实现方案

- 基于 AOP 的自定义轻量级治理组件
- 限流：基于 RateLimiter（Guava）令牌桶算法
- 熔断：基于状态机实现（CLOSED -> OPEN -> HALF_OPEN）
- 隔离：Semaphore 信号量控制
- 重试：AOP 切面循环调用
- 防重提交：SpEL 表达式生成唯一 key，Redis SETNX 实现


### 核心组件位置

- system/src/main/java/com/xiyao/system 实体类相关文件在这个包下

```
system/src/main/java/com/xiyao/governance/
├── annotation/
│   ├── RateLimit.java                # 限流注解
│   ├── CircuitBreaker.java           # 熔断注解
│   ├── Bulkhead.java                 # 隔离注解
│   ├── Retryable.java                # 重试注解
│   ├── Fallback.java                 # 降级方法指定
│   └── NoRepeatSubmit.java           # 防重注解
├── config/
│   ├── GovernanceAutoConfig.java     # 治理自动配置（启用条件）
│   └── GovernanceProperties.java     # 配置属性绑定
├── core/
│   ├── bulkhead/
│   │   ├── BulkheadAspect.java
│   │   └── BulkheadManager.java
│   ├── circuit/
│   │   ├── CircuitBreakerAspect.java
│   │   ├── CircuitBreakerManager.java
│   │   └── CircuitBreakerState.java  # 熔断状态枚举（CLOSE, OPEN, HALF_OPEN）
│   ├── ratelimit/
│   │   ├── RateLimitAspect.java
│   │   └── RateLimiter.java          # 令牌桶限流器
│   ├── retry/
│   │   ├── RetryAspect.java
│   │   └── RetryContext.java
│   └── nosubmit/
│       └── NoRepeatSubmitAspect.java
├── enums/
│   ├── CircuitState.java
│   └── RetryStrategy.java
└── fallback/
    └── DefaultFallbackHandler.java   # 默认降级处理器
```

### 配置示例

- application.yml

```
dayplan:
  governance:
    enabled: true
    rate-limit:
      default-qps: 100              # 默认QPS
    circuit-breaker:
      failure-threshold: 50         # 错误百分比阈值
      slow-call-threshold: 5000    # 慢调用阈值(ms)
      open-timeout: 30000          # 熔断打开持续时间(ms)
      half-open-max-calls: 10      # 半开状态最大试探请求数
    bulkhead:
      max-concurrent-calls: 50     # 最大并发数
    retry:
      max-attempts: 3               # 最大重试次数
      wait-duration: 1000          # 重试间隔(ms)
```
