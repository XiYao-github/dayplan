## governance 模块

### 模块介绍

```yaml
governance 模块提供流量治理能力，保护系统稳定性和可用性。

核心功能:
  - 限流：控制接口调用频率，防止恶意请求和流量冲击
  - 熔断：错误率达标时自动熔断，快速失败防止故障扩散
  - 隔离：限制并发数，防止资源耗尽
  - 降级：异常时提供备用响应，保障系统可用
  - 重试：临时故障自动重试，提高成功率
  - 防重提交：基于 Redis 实现请求幂等

技术特点:
  - 插件式配置：通过 @ConditionalOnProperty 实现功能可插拔
  - 注解驱动：@RateLimit 等注解声明式治理，业务代码无侵入
  - 自定义 AOP：轻量级实现，不依赖 Resilience4j
  - 注解优先：注解配置 > 全局配置（设为 -1 使用全局）
```

---

### 技术实现方案

**技术栈：**

```yaml
限流算法: RateLimiter（Guava 令牌桶）
熔断实现: 自定义状态机（CLOSED/OPEN/HALF_OPEN）
隔离实现: 信号量 + 线程池
重试实现: AOP 循环调用
防重实现: Redis SETNX
```

**限流流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          限流流程                                   │
└─────────────────────────────────────────────────────────────────────┘

  请求进入方法
        │
        ▼
  RateLimitAspect 拦截（@Around）
        │
        ▼
  RateLimiter.tryAcquire() 获取令牌
        │
        ├── 成功 → 执行目标方法
        │
        └── 失败 → 直接返回限流提示
```

**熔断器状态转换：**

```ascii
     CLOSED ───(失败率达标)───► OPEN
       ▲                          │
       │                          │
       │                     (超时后)
       │                          ↓
       └──(成功)──── HALF_OPEN ──(失败)──► OPEN
                          │
                          └──(成功)──► CLOSED
```

**重试策略：**

```ascii
固定间隔: ───●───●───●───●  (每次等待相同时间)
              │   │   │   │
指数退避: ───●───●───●───●  (间隔倍数增长)
              │ 2s 4s 8s 16s
```

**防重提交流程：**

```ascii
请求进入 ──► 生成 Key（userId + method + 参数hash）
                    │
                    ▼
              Redis EXISTS Key?
                    │
                    ├── 存在 ──► 返回"请勿重复提交"
                    │
                    └── 不存在 ──► SETNX Key（expire）──► 执行方法
```

---

### 组件结构

```tree
com.xiyao.governance/
├── annotation/
│   ├── RateLimit.java              # 限流注解
│   │                               # - permitsPerSecond: 每秒令牌数
│   │                               # - maxBurstRequests: 突发容量
│   │                               # - message: 限流提示
│   │
│   ├── CircuitBreaker.java        # 熔断注解
│   │                               # - failureRateThreshold: 失败阈值
│   │                               # - successRateThreshold: 成功阈值
│   │                               # - windowSizeMillis: 时间窗口
│   │                               # - breakDurationMillis: 熔断持续时间
│   │                               # - errorRateThreshold: 错误率阈值
│   │                               # - minRequestNumber: 最小请求数
│   │
│   ├── Bulkhead.java               # 隔离注解
│   │                               # - coreSize: 核心线程数
│   │                               # - maxSize: 最大线程数
│   │                               # - queueCapacity: 队列容量
│   │
│   ├── Retryable.java              # 重试注解
│   │                               # - maxAttempts: 最大重试次数
│   │                               # - intervalMillis: 重试间隔
│   │                               # - multiplier: 指数退避倍数
│   │                               # - includes: 可重试异常
│   │                               # - excludes: 不可重试异常
│   │
│   ├── Fallback.java               # 降级注解
│   │                               # - fallbackClass: 降级处理类
│   │                               # - fallbackMethod: 降级方法名
│   │                               # - value: 触发降级的异常类型
│   │
│   └── NoRepeatSubmit.java         # 防重提交注解
│                                   # - key: SpEL 表达式
│                                   # - expireSeconds: 锁定时间
│                                   # - message: 提示消息
│
├── config/
│   ├── GovernanceAutoConfig.java  # 自动配置
│   └── GovernanceProperties.java  # 全局配置属性
│                                   # - rate-limit.*: 限流全局配置
│                                   # - circuit-breaker.*: 熔断全局配置
│                                   # - bulkhead.*: 隔离全局配置
│                                   # - retry.*: 重试全局配置
│
├── core/
│   ├── bulkhead/
│   │   ├── BulkheadAspect.java     # 隔离切面
│   │   └── BulkheadManager.java    # 隔离管理器
│   │
│   ├── circuit/
│   │   ├── CircuitBreakerAspect.java   # 熔断切面
│   │   ├── CircuitBreakerManager.java   # 熔断管理器
│   │   └── CircuitBreakerState.java     # 熔断状态枚举
│   │
│   ├── fallback/
│   │   └── FallbackAspect.java     # 降级切面
│   │
│   ├── ratelimit/
│   │   ├── RateLimitAspect.java    # 限流切面
│   │   └── RateLimiter.java        # 令牌桶限流器
│   │
│   ├── retry/
│   │   ├── RetryAspect.java        # 重试切面
│   │   └── RetryContext.java       # 重试上下文
│   │
│   └── nosubmit/
│       └── NoRepeatSubmitAspect.java # 防重提交切面
│
└── enums/
    ├── CircuitState.java           # 熔断器状态（CLOSED=0, OPEN=1, HALF_OPEN=2）
    └── RetryStrategy.java          # 重试策略（FIXED=0, EXPONENTIAL=1）
```

---

### API 接口清单

```yaml
# Governance 模块主要通过注解使用，无直接 API 接口

# 限流
@RateLimit(permitsPerSecond = 100)

# 熔断
@CircuitBreaker(failureRateThreshold = 5)

# 隔离
@Bulkhead(coreSize = 10, maxSize = 20)

# 重试
@Retryable(maxAttempts = 3, intervalMillis = 1000)

# 降级
@Fallback(fallbackClass = UserFallback.class, value = {Exception.class})

# 防重提交
@NoRepeatSubmit(key = "#user.id", expireSeconds = 5)
```

---

### 依赖文件路径

```tree
# Base 基础类
src/main/java/com/xiyao/common/
└── utils/Result.java               # 统一响应封装

# Redis 工具（防重提交）
src/main/java/com/xiyao/common/
└── utils/RedisUtils.java          # Redis 操作工具
```

---

### 全局配置

```yaml
# application.yml
governance:
  enabled: true
  rate-limit:
    permits-per-second: 100        # 每秒令牌数
    max-burst-requests: 50         # 突发容量
    message: "请求过于频繁，请稍后再试"
  circuit-breaker:
    failure-rate-threshold: 5      # 失败阈值次数
    success-rate-threshold: 3      # 成功阈值次数
    window-size-millis: 60000      # 时间窗口（毫秒）
    break-duration-millis: 30000  # 熔断持续时间（毫秒）
    error-rate-threshold: 50       # 错误率阈值（百分比）
    min-request-number: 10         # 最小请求数
  bulkhead:
    core-size: 10                  # 核心线程数
    max-size: 20                   # 最大线程数
    queue-capacity: 100           # 队列容量
  retry:
    max-attempts: 3                # 最大重试次数
    interval-millis: 1000          # 重试间隔（毫秒）
    multiplier: 2.0                 # 指数退避倍数

# 配置优先级
# 注解属性值 > 全局配置
# 注解属性设为 -1 时自动使用全局配置
```

---

### 枚举说明

```java
CircuitState {           // 熔断器状态
    CLOSED = 0,           // 关闭状态，正常工作，统计失败率
    OPEN = 1,             // 打开状态，熔断中，拒绝所有请求
    HALF_OPEN = 2         // 半开状态，试探性恢复，允许部分请求
}

RetryStrategy {           // 重试策略
    FIXED = 0,            // 固定间隔重试
    EXPONENTIAL = 1       // 指数退避重试
}
```