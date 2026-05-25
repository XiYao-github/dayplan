## crypto模块

- RBAC 权限管理（含三员管理）模块方案

### 功能描述

- 接口加解密：请求体自动解密、响应体自动加密
- 字段加解密：数据库敏感字段（手机号、身份证号）透明加解密
- 数据脱敏：返回前端时自动脱敏（掩码处理）
- 密钥安全：SM2保护SM4密钥传输，SM4对数据加密

### 技术实现方案

- 加密库：Hutool + Bouncy Castle 提供者
- 请求解密：EncryptorFilter + DecryptInterceptor
- 响应加密：EncryptorFilter + EncryptResponseWrapper
- 字段加密：EncryptInterceptor（MyBatis拦截器）
- 脱敏序列化：Jackson自定义序列化器 + @Sensitive注解

### 前后端加密交互流程

- 前端调用 GET /api/public-key 获取 SM2 公钥（Base64编码）
- 每次请求随机生成 16 字节 SM4 密钥（KeyGenerator）
- 请求体 JSON 字符串使用 SM4 CBC 模式加密，Base64编码后作为请求体发送
- SM4 密钥使用 SM2 公钥加密，Base64编码后放入请求头 X-SM4-Key
- 后端 EncryptorFilter 从请求头获取密文，SM2私钥解密得到SM4密钥，再解密请求体
- Controller 收到明文 JSON
- 响应时，EncryptorFilter 用同一个 SM4 密钥加密响应体，Base64编码返回客户端
- 前端用相同 SM4 密钥解密响应

### 数据库字段加密设计

- 实体字段添加 @EncryptField(algorithm = AlgorithmType.SM4)
- 自定义 MyBatis 拦截器 intercept 方法：
- 写入时扫描参数对象中的注解字段，调用 EncryptorManager 加密后替换值
- 查询结果集时扫描注解字段，调用 EncryptorManager 解密后设置值
- 加密值存储为 Base64 编码字符串，字段长度需适当放大

### 核心组件位置

- system/src/main/java/com/xiyao/system 实体类相关文件在这个包下

```
system/src/main/java/com/xiyao/encrypt/
├── annotation/
│   ├── EncryptField.java              # 字段加密注解
│   └── Sensitive.java                 # 字段脱敏注解
├── config/
│   ├── EncryptorApiConfig.java        # API加解密配置（密钥路径、开关）
│   └── EncryptorDataConfig.java       # 数据加解密配置
├── core/
│   ├── EncryptContext.java            # 加密上下文（存放当前请求SM4密钥）
│   ├── EncryptorManager.java         # 加密管理器（根据算法获取加密器）
│   ├── IEncryptor.java               # 加密器接口
│   └── encryptor/
│       ├── AbstractEncryptor.java     # 抽象基类
│       ├── Sm2Encryptor.java          # SM2加密器
│       └── Sm4Encryptor.java          # SM4加密器
├── enums/
│   ├── AlgorithmType.java            # 算法类型（SM2, SM4）
│   ├── EncodeType.java               # 编码类型（BASE64, HEX）
│   └── SensitiveStrategy.java        # 脱敏策略枚举
├── filter/
│   ├── EncryptorFilter.java          # 加解密过滤器
│   └── wrapper/
│       ├── DecryptRequestWrapper.java  # 请求解密包装器
│       └── EncryptResponseWrapper.java # 响应加密包装器
├── interceptor/
│   ├── DecryptInterceptor.java        # 请求参数解密拦截器（备选方案）
│   └── EncryptInterceptor.java        # 数据库字段加解密拦截器
├── properties/
│   ├── EncryptorApi.java              # API加密属性配置
│   └── EncryptorData.java             # 数据加密属性配置
├── serialize/
│   └── SensitiveSerializer.java       # 脱敏序列化器
└── utils/
    └── EncryptUtils.java              # 加密工具类
```

### 配置示例

- application.yml

```
dayplan:
  crypto:
    enabled: true
    sm2:
      public-key: classpath:keys/sm2_public.key
      private-key: classpath:keys/sm2_private.key
    sm4:
      key: classpath:keys/sm4.key
      mode: CBC
      padding: PKCS5Padding
```
