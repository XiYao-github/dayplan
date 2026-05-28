## crypto 模块

### 模块介绍

```yaml
crypto 模块提供国密加解密和数据脱敏能力，保护敏感数据安全。

核心功能:
  - 接口加解密：请求体自动解密、响应体自动加密（SM2/SM4 混合模式）
  - 字段加解密：数据库敏感字段透明加解密，业务无感知
  - 数据脱敏：JSON 序列化时自动脱敏，不影响数据库原始数据

技术特点:
  - 插件式配置：encryptor-api 和 encryptor-data 分开配置
  - 国密算法：SM2/SM4/SM3 全面支持（Hutool + BouncyCastle）
  - 透明加解密：MyBatis 拦截器实现字段自动加解密
  - 脱敏序列化：Jackson 自定义序列化器
  - 等保合规：敏感数据必须加密存储
```

---

### 技术实现方案

**技术栈：**

```yaml
加解密库: Hutool + BouncyCastle（国密算法）
接口加解密: Servlet Filter（EncryptorFilter）
字段加解密: MyBatis Interceptor（EncryptInterceptor/DecryptInterceptor）
数据脱敏: Jackson Serializer（SensitiveSerializer）
```

**接口加解密流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                      接口加解密流程                                    │
└─────────────────────────────────────────────────────────────────────┘

  请求阶段：
  前端生成随机 SM4 密钥
        │
        ▼
  用 SM2 公钥加密 SM4 密钥 → Base64 → 放入请求头 X-SM4-Key
        │
        ▼
  用 SM4 密钥加密请求体 JSON → Base64
        │
        ▼
  POST /api/data（请求体 + X-SM4-Key 头）
        │
        ▼
  EncryptorFilter 拦截
        │
        ▼
  SM2 私钥解密 X-SM4-Key → SM4 密钥
        │
        ▼
  SM4 密钥解密请求体 → 明文传递 Controller

  ──────────────────────────────────────────

  响应阶段：
  Controller 返回明文 JSON
        │
        ▼
  EncryptorFilter 拦截
        │
        ▼
  用请求时的 SM4 密钥加密响应体
        │
        ▼
  Base64 编码返回客户端
```

**数据库字段加解密流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                      字段加解密流程                                    │
└─────────────────────────────────────────────────────────────────────┘

  入库时：EncryptInterceptor（MyBatis 拦截器）
        │
        ▼
  遍历参数对象，找到 @CryptoField 注解字段
        │
        ▼
  SM4 加密 → 存储密文（带 ENC_ 前缀）

  ──────────────────────────────────────────

  出库时：DecryptInterceptor（MyBatis 拦截器）
        │
        ▼
  遍历结果对象，找到 @CryptoField 注解字段
        │
        ▼
  检测 ENC_ 前缀 → SM4 解密 → 返回明文
```

**数据脱敏流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          数据脱敏流程                                 │
└─────────────────────────────────────────────────────────────────────┘

  对象序列化（toJson）
        │
        ▼
  @SensitiveField 标注了 @JsonSerialize(using = SensitiveSerializer.class)
        │
        ▼
  Jackson 调用 SensitiveSerializer
        │
        ▼
  createContextual() 从注解获取脱敏策略
        │
        ▼
  serialize() 调用策略方法脱敏
        │
        ▼
  输出脱敏后的 JSON（原始数据不变）
```

---

### 组件结构

```tree
com.xiyao.crypto/
├── annotation/
│   ├── CryptoField.java              # 字段加解密注解
│   │                                   # - algorithm: 算法类型
│   │                                   # - encode: 编码类型
│   │                                   # - password/keys: 密钥配置
│   │
│   └── SensitiveField.java          # 数据脱敏注解
│                                       # - value: 脱敏策略
│
├── config/
│   ├── EncryptorApiConfig.java       # API 加解密配置（@ConditionalOnProperty）
│   │                                   # - 注册 EncryptorFilter
│   │                                   # - FilterRegistrationBean
│   │
│   └── EncryptorDataConfig.java      # 数据加解密配置（@ConditionalOnProperty）
│                                       # - 注册 EncryptorManager
│                                       # - 注册 EncryptInterceptor/DecryptInterceptor
│
├── core/
│   ├── EncryptContext.java           # 加密上下文配置
│   ├── EncryptorManager.java         # 加密管理器（缓存加密器和字段）
│   ├── IEncryptor.java             # 加密器接口
│   │                                 # - encrypt()/decrypt()
│   │                                 # - algorithm()
│   └── encryptor/
│       ├── AbstractEncryptor.java   # 抽象基类
│       ├── Sm2Encryptor.java        # SM2 加密器
│       └── Sm4Encryptor.java         # SM4 加密器
│
├── enums/
│   ├── AlgorithmType.java           # 算法类型（DEFAULT=0, SM2=1, SM4=2）
│   ├── EncodeType.java             # 编码类型（DEFAULT=0, BASE64=1, HEX=2）
│   └── SensitiveStrategy.java       # 脱敏策略（14种策略）
│
├── filter/
│   ├── EncryptorFilter.java         # 加解密过滤器（由 EncryptorApiConfig 注册）
│   │                                 # - 路径过滤
│   │                                 # - 请求解密/响应加密
│   │
│   └── wrapper/
│       ├── DecryptRequestWrapper.java   # 请求解密包装器
│       └── EncryptResponseWrapper.java # 响应加密包装器
│
├── interceptor/
│   ├── EncryptInterceptor.java      # 入库加密拦截器（@Intercepts）
│   │                                 # - @Signature(type=ParameterHandler)
│   │                                 # - 对 @CryptoField 字段加密
│   │
│   └── DecryptInterceptor.java     # 出库解密拦截器（@Intercepts）
│                                       # - @Signature(type=ResultSetHandler)
│                                       # - 对 @CryptoField 字段解密
│
├── properties/
│   ├── EncryptorApi.java           # API 加密属性
│   │                                 # - enable/publicKey/privateKey
│   │                                 # - headerFlag/includePaths/excludePaths
│   │
│   └── EncryptorData.java         # 数据加密属性
│                                       # - enable/algorithm/encode
│                                       # - password/publicKey/privateKey
│
├── serialize/
│   └── SensitiveSerializer.java    # 脱敏序列化器
│                                     # - 继承 JsonSerializer
│                                     # - 根据 @SensitiveField 策略脱敏
│
└── utils/
    └── EncryptUtils.java          # 加密工具类
```

---

### Bean 注册方式

crypto 模块采用插件式架构，两个功能独立配置：

**API 加解密（EncryptorApiConfig）：**

```java
@Configuration
@EnableConfigurationProperties(EncryptorApi.class)
@ConditionalOnProperty(value = "encryptor-api.enable", havingValue = "true")
public class EncryptorApiConfig {

    @Bean
    public FilterRegistrationBean<EncryptorFilter> filterFilterRegistrationBean(EncryptorApi properties) {
        FilterRegistrationBean<EncryptorFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new EncryptorFilter(properties));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
```

**数据加解密（EncryptorDataConfig）：**

```java
@Configuration
@AutoConfigureAfter(MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(EncryptorData.class)
@ConditionalOnProperty(value = "encryptor-data.enable", havingValue = "true")
public class EncryptorDataConfig {

    @Bean
    public EncryptorManager encryptorManager() {
        return new EncryptorManager();
    }

    @Bean
    public EncryptInterceptor encryptInterceptor(EncryptorManager manager, EncryptorData properties) {
        return new EncryptInterceptor(manager, properties);
    }

    @Bean
    public DecryptInterceptor decryptInterceptor(EncryptorManager manager, EncryptorData properties) {
        return new DecryptInterceptor(manager, properties);
    }
}
```

---

### 关键注解说明

**字段加解密（@CryptoField）：**

```java
// 入库自动加密、出库自动解密
public class User {
    // 使用全局默认配置
    @CryptoField
    private String idCard;

    // 指定 SM4 算法和 HEX 编码
    @CryptoField(algorithm = AlgorithmType.SM4, encode = EncodeType.HEX, password = "16位密钥")
    private String bankCard;

    // 指定 SM2 非对称加密
    @CryptoField(algorithm = AlgorithmType.SM2, publicKey = "公钥", privateKey = "私钥")
    private String phone;
}
```

**数据脱敏（@SensitiveField）：**

```java
public class UserVo {
    @SensitiveField(SensitiveStrategy.MOBILE_PHONE)    // 138****8000
    private String phone;

    @SensitiveField(SensitiveStrategy.ID_CARD)          // 110***********1234
    private String idCard;

    @SensitiveField(SensitiveStrategy.CHINESE_NAME)     // 张*
    private String name;

    @SensitiveField(SensitiveStrategy.BANK_CARD)        // 6222*****7890
    private String bankCard;

    @SensitiveField(SensitiveStrategy.EMAIL)             // t**@example.com
    private String email;
}
```

---

### 脱敏策略枚举

```java
// SensitiveStrategy {
//     USER_ID,       // 用户ID → 10001
//     CHINESE_NAME,  // 中文姓名 → 张三 → 张*
//     ID_CARD,       // 身份证号 → 110101199001011234 → 110***********1234
//     FIXED_PHONE,   // 座机号 → 010-12345678 → 010****5678
//     MOBILE_PHONE,  // 手机号 → 13800138000 → 138****8000
//     ADDRESS,       // 地址 → 北京市朝阳区建国路... → 北京市朝阳***
//     EMAIL,         // 邮箱 → test@example.com → t**@example.com
//     PASSWORD,      // 密码 → secret123 → *********
//     CAR_LICENSE,   // 车牌号 → 京A12345 → 京A****5
//     BANK_CARD,     // 银行卡 → 6222021234567890 → 6222*****7890
//     IPV4,          // IPv4地址 → 192.168.1.100 → 192.***.***.***
//     IPV6,          // IPv6地址 → 2001:db8::1 → 2001:***::1
//     FIRST_MASK     // 首字符遮蔽 → 张三 → 张*
// }
```

---

### 配置文件

```yaml
# API 加解密（请求/响应体）
encryptor-api:
  enable: true
  headerFlag: X-SM4-Key                    # 请求头中传递 SM4 密钥的标识
  publicKey: "SM2公钥（用于加密响应）"
  privateKey: "SM2私钥（用于解密请求）"
  includePaths:
    - /api/**
  excludePaths:
    - /api/public/**

# 数据加解密（数据库字段）
encryptor-data:
  enable: true
  algorithm: SM4                            # 默认算法
  encode: HEX                               # 默认编码
  password: "16位SM4密钥"                   # SM4 密钥
  publicKey: "SM2公钥"                     # SM2 公钥
  privateKey: "SM2私钥"                    # SM2 私钥
```

---

### 依赖文件路径

```tree
# crypto 模块是完全独立的模块，不依赖项目内部其他模块
# 仅依赖外部技术栈（通过 Maven 引入）

# 技术依赖
# - Hutool：国密算法实现（SM2/SM3/SM4）
# - BouncyCastle：额外加密提供者
# - MyBatis-Plus：拦截器机制（字段加解密）
# - Jackson：JSON 序列化/脱敏

# 配置读取（通过 @ConfigurationProperties 绑定）
# encryptor-api：接口加解密配置
# encryptor-data：数据加解密配置
```

---

### 安全合规说明

```java
// 等保合规要求
// 1. 敏感数据必须经过 SM4 加密后存储
// 2. 数据传输使用 SM2+SM4 混合加密
// 3. SM4 密钥使用 SM2 公钥保护
// 4. 响应数据脱敏处理，防止泄露

// 敏感字段示例
// - 手机号、身份证号、银行卡号
// - 密码、密钥
// - 详细地址
```