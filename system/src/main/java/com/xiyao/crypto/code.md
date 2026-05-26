## crypto 模块

### 模块介绍

```yaml
crypto 模块提供国密加解密和数据脱敏能力，保护敏感数据安全。

核心功能:
  - 接口加解密：请求体自动解密、响应体自动加密（SM2/SM4）
  - 字段加解密：数据库敏感字段透明加解密，业务无感知
  - 数据脱敏：JSON 序列化时自动脱敏，不影响数据库原始数据

技术特点:
  - 插件式配置：通过 @ConditionalOnProperty 实现功能可插拔
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
接口加解密: Servlet Filter
字段加解密: MyBatis Interceptor
数据脱敏: Jackson Serializer
```

**请求解密流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        请求解密流程                                   │
└─────────────────────────────────────────────────────────────────────┘

  前端获取 SM2 公钥
        │
        ▼
  生成随机 SM4 密钥
        │
        ▼
  SM2 公钥加密 SM4 密钥 → Base64 → 放入请求头 X-SM4-Key
        │
        ▼
  SM4 密钥加密请求体 JSON → Base64
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
```

**响应加密流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        响应加密流程                                   │
└─────────────────────────────────────────────────────────────────────┘

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
        │
        ▼
  前端用 SM4 密钥解密
```

**数据库字段加解密流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                      字段加解密流程                                    │
└─────────────────────────────────────────────────────────────────────┘

  入库时：EncryptInterceptor
        │
        ▼
  扫描 @CryptoField 注解字段
        │
        ▼
  SM4 加密 → Base64 编码
        │
        ▼
  存储密文

  ────────────────────────────────

  出库时：DecryptInterceptor
        │
        ▼
  扫描 @CryptoField 注解字段
        │
        ▼
  Base64 解码 → SM4 解密
        │
        ▼
  返回明文
```

**数据脱敏流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          数据脱敏流程                                 │
└─────────────────────────────────────────────────────────────────────┘

  对象序列化（toJson）
        │
        ▼
  Jackson 调用 SensitiveSerializer
        │
        ▼
  检测 @Sensitive 注解
        │
        ▼
  调用脱敏策略方法
        │
        ▼
  输出脱敏后的 JSON
```

---

### 组件结构

```tree
com.xiyao.crypto/
├── annotation/
│   ├── CryptoField.java           # 字段加解密注解
│   │                               # - algorithm: 算法类型（SM2/SM4/DEFAULT）
│   │                               # - encode: 编码类型（BASE64/HEX/DEFAULT）
│   │                               # - password: SM4 密钥
│   │                               # - publicKey: SM2 公钥
│   │                               # - privateKey: SM2 私钥
│   │
│   └── Sensitive.java             # 数据脱敏注解
│                                   # - value: 脱敏策略
│
├── config/
│   ├── EncryptorApiConfig.java   # API 加解密配置
│   │                               # - enabled: 是否启用
│   │                               # - publicKey/privateKey: SM2 密钥路径
│   │                               # - headerFlag: 加密标识请求头
│   │                               # - includePaths/excludePaths: 路径过滤
│   │
│   └── EncryptorDataConfig.java  # 数据加解密配置
│                                   # - enabled: 是否启用
│                                   # - sm4.key/mode/padding: SM4 配置
│
├── core/
│   ├── EncryptContext.java        # 加密上下文（当前请求 SM4 密钥）
│   ├── EncryptorManager.java     # 加密管理器（获取加密器）
│   ├── IEncryptor.java          # 加密器接口
│   └── encryptor/
│       ├── AbstractEncryptor.java # 抽象基类
│       ├── Sm2Encryptor.java     # SM2 加密器
│       └── Sm4Encryptor.java     # SM4 加密器
│
├── enums/
│   ├── AlgorithmType.java        # 算法类型（DEFAULT=0, SM2=1, SM4=2）
│   ├── EncodeType.java           # 编码类型（DEFAULT=0, BASE64=1, HEX=2）
│   └── SensitiveStrategy.java    # 脱敏策略（14种策略）
│
├── filter/
│   ├── EncryptorFilter.java      # 加解密过滤器
│   │                               # - 路径过滤
│   │                               # - 加密标识检查
│   │                               # - 请求解密/响应加密
│   │
│   └── wrapper/
│       ├── DecryptRequestWrapper.java  # 请求解密包装器
│       └── EncryptResponseWrapper.java # 响应加密包装器
│
├── interceptor/
│   ├── EncryptInterceptor.java   # 入库加密拦截器
│   └── DecryptInterceptor.java   # 出库解密拦截器
│
├── properties/
│   ├── EncryptorApi.java        # API 加密属性
│   └── EncryptorData.java        # 数据加密属性
│
├── serialize/
│   └── SensitiveSerializer.java # 脱敏序列化器
│
└── utils/
    └── EncryptUtils.java        # 加密工具类
```

---

### API 接口清单

```yaml
# 加密功能通过 Filter 自动处理，无需直接 API

# 获取公钥（前端加密需要）
GET /api/public-key              # 获取 SM2 公钥

# 加密标识
X-SM4-Key: <SM2加密后的SM4密钥>

# 使用示例
POST /api/user
Header: X-Encrypt: true
Header: X-SM4-Key: <加密后的SM4密钥>
Body: <SM4加密后的JSON>
```

---

### 依赖文件路径

```tree
# Base 基础类
src/main/java/com/xiyao/common/
└── utils/Result.java             # 统一响应

# System 模块（实体字段加密）
src/main/java/com/xiyao/system/
└── entity/SysUser.java          # 手机号、身份证等敏感字段
```

---

### 关键注解说明

```java
// 字段加密（入库自动加密、出库自动解密）
@CryptoField
@CryptoField(algorithm = AlgorithmType.SM4, password = "key")
@CryptoField(algorithm = AlgorithmType.SM2, publicKey = "pubkey", privateKey = "prikey")

// 字段脱敏（序列化时自动脱敏）
@Sensitive(SensitiveStrategy.MOBILE_PHONE)    // 138****8000
@Sensitive(SensitiveStrategy.ID_CARD)         // 110***********1234
@Sensitive(SensitiveStrategy.CHINESE_NAME)    // 张*
@Sensitive(SensitiveStrategy.BANK_CARD)     // 6222*****7890
@Sensitive(SensitiveStrategy.EMAIL)          // t**@example.com
```

---

### 脱敏策略枚举

```java
SensitiveStrategy {
    USER_ID,       // 用户ID
    CHINESE_NAME,  // 中文姓名：张三 → 张*
    ID_CARD,       // 身份证：110101199001011234 → 110***********1234
    FIXED_PHONE,   // 座机号
    MOBILE_PHONE,  // 手机号：13800138000 → 138****8000
    ADDRESS,       // 地址
    EMAIL,         // 邮箱：test@example.com → t**@example.com
    PASSWORD,      // 密码：全部替换为 *
    CAR_LICENSE,   // 车牌号
    BANK_CARD,     // 银行卡：6222021234567890 → 6222*****7890
    IPV4,          // IPv4
    IPV6,          // IPv6
    FIRST_MASK     // 只显示第一个字符
}
```

---

### 配置文件

```yaml
# application.yml
crypto:
  enabled: true

# API 加解密（请求/响应体）
encryptor-api:
  enabled: true
  public-key: classpath:keys/sm2_public.key
  private-key: classpath:keys/sm2_private.key
  header-flag: X-Encrypt
  include-paths:
    - /api/**
  exclude-paths:
    - /api/public/**

# 数据加解密（数据库字段）
encryptor-data:
  enabled: true
  sm4:
    key: classpath:keys/sm4.key
    mode: CBC
    padding: PKCS5Padding
```

---

### 安全合规说明

```java
// 等保合规要求
// 1. 敏感数据必须经过 SM4 加密后存储
// 2. 数据传输使用 SM4 对称加密
// 3. SM4 密钥使用 SM2 公钥保护
// 4. 响应数据脱敏处理，防止泄露

// 敏感字段示例
// - 手机号、身份证号、银行卡号
// - 密码、密钥
// - 详细地址
```