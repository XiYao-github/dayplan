# dict 模块

## 模块概述

dict 模块提供数据字典能力，支持键值对管理和自动映射回显。

**核心功能：**
- 字典数据：维护字典类型下的键值对（value/label）
- 自动映射：查询结果自动将值映射为显示文本
- 缓存机制：本地缓存减少数据库查询
- 枚举转换：Spring 参数转换和 MyBatis 数据库转换

**技术特点：**
- 插件式配置：通过 @ConditionalOnProperty 实现功能可插拔
- 注解驱动：@DictBind 注解声明自动映射
- MyBatis 拦截：结果集返回时自动翻译（DictInterceptor）
- MetaObject：安全的字段访问，target 字段不存在时安全跳过

---

## 类结构详解

### 1. annotation/DictBind.java

**类说明：**
字段数据绑定注解，标注在实体字段上，标记该字段为字典值字段。MyBatis 结果集拦截器会自动查询字典缓存，将对应的描述文本（label）填充到指定的 target 字段。

**核心属性：**
```java
// String code();        // 字典类型编码，用于从字典缓存中查询（必填）
// String target();       // 字典标签回显的目标字段名（必填）
```

**使用示例：**
```java
public class UserVO {
    // status 字段存储的是 0/1，statusText 字段自动填充为"禁用"/"正常"
    @DictBind(code = "status", target = "statusText")
    private Integer status;
    private String statusText;
}
```

---

### 2. config/DictAutoConfig.java

**类说明：**
字典模块自动配置类，负责在 Spring Boot 启动时自动装配字典相关的组件。

**核心方法：**
```java
// @PostConstruct
// void initDictCache();                      // 应用启动时全量加载字典数据到缓存

// @Bean
// DictInterceptor dictInterceptor();          // 注册字典拦截器到 MyBatis
```

**装配条件：**
- 配置项 `dict-data.enable=true`（默认值为 true，可不配置）

---

### 3. enums/BaseEnum.java

**类说明：**
枚举基础接口，所有业务枚举类必须实现此接口，统一枚举的编码、描述和序列化方式。

**接口方法：**
```java
// T getCode();                               // 获取枚举的存储编码（存入数据库的值）
// String getDesc();                          // 获取枚举的描述文本（用于前端展示）
// T getValue();                              // 获取序列化值（Jackson 序列化时调用）

// 静态方法
// static <T extends BaseEnum<?>> T fromValue(Class<T> enumClass, Object value);
// 根据值查找枚举实例，支持三种匹配方式：
//   1. code 匹配：精确匹配存储值（如 0、1、"A"）
//   2. desc 匹配：匹配描述文本（如"正常"、"禁用"）
//   3. name 匹配：匹配枚举常量名称（如 DISABLED、NORMAL）
```

---

### 4. enums/Status.java

**类说明：**
通用状态枚举，用于表示系统资源的启用/禁用状态。

**枚举值：**
```java
// DISABLED(0, "禁用"),    // 资源处于不可用状态
// NORMAL(1, "正常");      // 资源处于可用状态
```

**使用场景：**
- 用户账户状态：0-禁用（无法登录）、1-正常
- 功能开关：0-关闭、1-开启

---

### 5. enums/DataStatus.java

**类说明：**
数据状态枚举，用于表示业务数据的可用状态。

**枚举值：**
```java
// PAUSE(0, "暂停"),       // 数据暂停使用，但未被物理删除
// NORMAL(1, "正常");      // 数据正常运行，对外提供服务
```

**使用场景：**
- 字典数据状态：0-暂停使用、1-正常可用
- 业务数据状态：数据是否对外提供服务

**与 Status 的区别：**
- DataStatus 用于描述业务数据本身的可用性
- Status 用于系统资源的状态（如账户、功能开关）

---

### 6. interceptor/DictInterceptor.java

**类说明：**
MyBatis 查询结果字典回显拦截器，实现 Interceptor 接口，拦截结果集处理过程。

**核心方法：**
```java
// @Override
// Object intercept(Invocation invocation) throws Throwable;
    // 拦截器核心方法，在 MyBatis 查询结果集处理完成后执行字典回显逻辑
    // 入参：invocation - 调用信息
    // 返回：原始查询结果（字典回显在原对象上直接修改）

// private void processDict(Object result);
    // 递归字典回显处理，支持 Map/Collection/Object
    // 入参：result - 查询结果对象
    // 逻辑：
    //   - Map 类型：遍历所有 value 并递归处理
    //   - Collection 类型：遍历所有元素递归处理
    //   - 普通对象：获取类所有 @DictBind 字段并处理

// private void processDictField(Field field, Object result);
    // 处理字典字段回显
    // 入参：field - 字段对象，result - 目标对象
    // 逻辑：
    //   1. 获取 @DictBind 注解配置
    //   2. 从 DictUtils 获取字典标签
    //   3. 填充到 target 字段（MetaObject 自动处理字段可访问性）

// @Override
// Object plugin(Object target);
    // 将拦截器包装为 MyBatis 插件
    // 入参：target - 被拦截的目标对象
    // 返回：包装后的代理对象

// @Override
// void setProperties(Properties properties);
    // 设置插件配置属性（当前未使用）
```

**拦截点：**
- 拦截 `ResultSetHandler.handleResultSets()` 方法
- 在所有查询结果返回前执行字典回显

**处理流程：**
```
MyBatis 查询返回结果集
        │
        ▼
DictInterceptor 拦截
        │
        ▼
遍历对象所有字段，找到 @DictBind 注解的字段
        │
        ▼
从 DictCache 获取 label（根据 code + value）
        │
        ▼
MetaObject.setValue() 设置到 target 字段
        │
        ▼
返回给前端（target 字段已是 label）
```

---

### 7. properties/DictProperties.java

**类说明：**
字典模块配置属性，通过 @ConfigurationProperties 绑定前缀为 dict-data 的配置项。

**配置属性：**
```java
// boolean enable = true;    // 是否启用字典功能（默认 true）
```

**配置示例：**
```yaml
dict-data:
  enable: true  # 是否启用字典功能（默认 true）
```

---

### 8. utils/DictUtils.java

**类说明：**
数据字典缓存管理器，使用单例模式实现，确保全局唯一的缓存实例。

**核心方法：**
```java
// 单例
// static DictUtils getInstance();                           // 获取单例实例（饿汉模式）

// 缓存查询
// Map<String, String> getDictMap(String dictCode);        // 获取指定字典编码的字典映射
// String getDictLabel(String dictCode, String dictValue);  // 获取字典标签文本
// Set<String> getDictMapKey();                             // 获取所有已缓存的字典编码

// 缓存加载
// void loadDictMap(String dictCode);                        // 加载指定字典编码的字典数据到缓存
// void loadDictAll();                                       // 加载所有字典数据到缓存
// void refreshAll();                                        // 刷新所有字典缓存
```

**缓存结构：**
```java
// dictCache: Map<String, Map<String, String>>
// 外层 Key：dictCode（字典编码）
// 内层 Map：dictValue -> dictLabel（字典值到标签的映射）
```

**线程安全：**
- 使用 `ConcurrentHashMap` 保证线程安全
- 使用 `ReentrantReadWriteLock` 读写锁保证并发安全

---

### 9. handler/EnumTypeHandler.java

**类说明：**
MyBatis 枚举类型转换器，负责 MyBatis 与数据库之间的枚举类型与 JDBC 类型的双向转换。

**核心方法：**
```java
// 写入数据库
// @Override
// void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException;
    // 将枚举的 code 值写入数据库
    // 入参：ps - PreparedStatement，i - 参数位置，parameter - 枚举参数，jdbcType - JDBC 类型
    // 逻辑：根据 code 类型调用对应的 PreparedStatement 方法（setInt/setString/setObject）

// 从数据库读取
// @Override
// E getNullableResult(ResultSet rs, String columnName) throws SQLException;        // 通过列名获取
// @Override
// E getNullableResult(ResultSet rs, int columnIndex) throws SQLException;          // 通过列索引获取
// @Override
// E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException;  // 从 CallableStatement 获取
    // 入参：rs/cs - 结果集，columnName/columnIndex - 列信息
    // 返回：对应的枚举实例，未找到返回 null
    // 逻辑：调用 BaseEnum.fromValue() 进行转换

// private E convert(Object value);
    // 将数据库值转换为枚举实例
    // 入参：value - 数据库中的值
    // 返回：对应的枚举实例，value 为 null 或未找到返回 null
```

**使用方式：**
```java
// public class User {
//     @TableField(typeHandler = EnumTypeHandler.class)
//     private Status status;
// }
```

---

### 10. converter/EnumConverterFactory.java

**类说明：**
Spring 枚举转换器工厂，实现 ConverterFactory 接口，将 String 类型请求参数转换为 BaseEnum 子类。

**核心方法：**
```java
// @Override
// <T extends BaseEnum<?>> Converter<String, T> getConverter(Class<T> targetType);
    // 获取指定枚举类型的转换器
    // 入参：targetType - 目标枚举类型
    // 返回：String 到 T 的转换器

// 内部类 StringToEnumConverter
// private static final class StringToEnumConverter<T extends BaseEnum<?>> implements Converter<String, T> {
//     T convert(String source);
        // 将 String 值转换为对应的枚举实例
        // 入参：source - String 类型的值（如 "1"、"DISABLED"、"正常"）
        // 返回：对应的枚举实例，未找到返回 null
// }
```

**工作原理：**
1. 当 Spring MVC 接收到 String 类型的参数但需要转换为枚举时调用
2. 根据目标枚举类型获取对应的 Converter
3. Converter 调用 BaseEnum.fromValue() 完成转换

**使用示例：**
```java
// @GetMapping("/users")
// public Result<?> queryUsers(Status status) {
    // Spring 会自动将请求参数 "1" 转换为 Status.NORMAL
// }
```

---

## 技术实现方案

### 技术栈

```
字典翻译: MyBatis ResultSetInterceptor（DictInterceptor）
枚举转换: MyBatis TypeHandler（EnumTypeHandler）+ Spring ConverterFactory
缓存: 本地缓存（DictUtils）
数据访问: MyBatis-Plus Db.lambdaQuery（跳过 Service 层）
```

### 字典缓存流程

```
应用启动
    │
    ▼
DictAutoConfig.initDictCache()
    │
    ▼
DictUtils.loadDictAll()
    │
    ▼
Db.lambdaQuery() 查询所有正常状态的字典数据
    │
    ▼
按 dictCode 分组缓存 (dictValue -> dictLabel)

──────────────────────────────────────────

配置变更时
    │
    ▼
DictDataServiceImpl 调用 dictUtils.refreshAll()
    │
    ▼
重新加载所有字典数据
```

---

## @DictBind 注解使用说明

### 基本使用

```java
// public class UserVO {
//     status 字段存储的是 0/1，statusText 字段自动填充为"禁用"/"正常"
    // @DictBind(code = "status", target = "statusText")
    // private Integer status;
    // private String statusText;
}
```

### 使用要求

- `code`：字典类型编码，必填
- `target`：目标字段名，必填
- 字段类型支持：`Integer`、`String` 等可转换类型
- target 字段不存在时安全跳过，不抛异常

---

## 关键表结构

### 字典数据表

```sql
CREATE TABLE sys_dict_data (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_type   VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_code   VARCHAR(100) NOT NULL COMMENT '字典编码（用于 @DictBind）',
    dict_label  VARCHAR(100) NOT NULL COMMENT '字典标签（显示值）',
    dict_value  VARCHAR(100) NOT NULL COMMENT '字典键值',
    status      TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-停用',
    is_default  TINYINT DEFAULT 0 COMMENT '是否默认 0-否 1-是',
    remark      VARCHAR(500) COMMENT '备注',
    create_by   VARCHAR(50),
    create_time DATETIME,
    update_by   VARCHAR(50),
    update_time DATETIME
);
```

---

## 配置文件

```yaml
# application.yml

# 字典模块配置
dict-data:
  enable: true                    # 是否启用字典功能（默认 true）
```

---

## 组件结构图

```tree
com.xiyao.dict/
├── annotation/
│   └── DictBind.java             # 字典绑定注解
│                                   # - code: 字典类型编码
│                                   # - target: 目标字段名
│
├── config/
│   └── DictAutoConfig.java      # 字典自动配置
│                                   # - initDictCache() 启动时加载缓存
│                                   # - dictInterceptor() 注册拦截器
│
├── enums/                          # 通用枚举
│   ├── BaseEnum.java              # 枚举基础接口
│   ├── DataStatus.java            # 数据状态枚举
│   └── Status.java                # 通用状态枚举
│
├── interceptor/
│   └── DictInterceptor.java       # MyBatis 结果集拦截器
│                                   # - @Intercepts(handleResultSets)
│                                   # - 递归处理 Map/Collection/Object
│                                   # - 使用 MetaObject 安全访问字段
│
├── properties/
│   └── DictProperties.java        # 字典配置属性
│
├── handler/
│   └── EnumTypeHandler.java      # MyBatis 枚举类型转换器
│                                   # - 枚举 -> 数据库：setNonNullParameter
│                                   # - 数据库 -> 枚举：getNullableResult
│
├── converter/
│   └── EnumConverterFactory.java  # Spring 枚举转换器工厂
│                                   # - String -> BaseEnum 参数转换
│
└── utils/
    └── DictUtils.java             # 字典工具类
                                    # - loadDictAll(): 全量加载
                                    # - refreshAll(): 刷新缓存
                                    # - getDictLabel(): 获取标签
```
