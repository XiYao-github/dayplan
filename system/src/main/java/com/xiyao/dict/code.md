## dict 模块

### 模块介绍

```yaml
dict 模块提供数据字典能力，支持键值对管理和自动映射回显。

核心功能:
  - 字典类型：管理和配置字典类型（如状态、性别、状态等）
  - 字典数据：维护字典类型下的键值对（value/label）
  - 自动映射：查询结果自动将值映射为显示文本
  - 枚举支持：实现 BaseEnum 接口，支持多种反序列化方式
  - 缓存机制：本地缓存减少数据库查询

技术特点:
  - 插件式配置：通过 @ConditionalOnProperty 实现功能可插拔
  - 注解驱动：@DictBind 注解声明自动映射
  - MyBatis 拦截：结果集返回时自动翻译
  - 枚举转换：Controller 参数自动转换为枚举
```

---

### 技术实现方案

**技术栈：**

```yaml
字典翻译: MyBatis ResultSetInterceptor
枚举转换: Spring ConverterFactory
缓存: 本地缓存（可扩展 Redis）
```

**字典加载流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          字典加载流程                                 │
└─────────────────────────────────────────────────────────────────────┘

  启动时 / 首次查询
        │
        ▼
  加载全量字典到 DictCache
        │
        ▼
  查询时直接从缓存获取

  配置变更时
        │
        ▼
  主动刷新缓存
```

**自动映射流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        自动映射流程                                   │
└─────────────────────────────────────────────────────────────────────┘

  MyBatis 查询返回结果集
        │
        ▼
  DictResultInterceptor 拦截
        │
        ▼
  扫描结果对象中的 @DictBind 字段
        │
        ▼
  根据 code 和值从缓存获取 label
        │
        ▼
  设置到 target 字段
```

**枚举转换流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        枚举转换流程                                   │
└─────────────────────────────────────────────────────────────────────┘

  Controller 接收参数（如 status=1）
        │
        ▼
  DictEnumConverterFactory 转换
        │
        ├── 按 code 匹配：1 → DataStatus.NORMAL
        ├── 按 desc 匹配："正常" → DataStatus.NORMAL
        └── 按 name 匹配："NORMAL" → DataStatus.NORMAL
        │
        ▼
  注入到方法参数
```

---

### 组件结构

```tree
com.xiyao.dict/
├── annotation/
│   └── DictBind.java               # 字典绑定注解
│                                   # - code: 字典类型编码
│                                   # - target: 目标字段名
│
├── config/
│   ├── DictAutoConfig.java        # 自动配置
│   ├── DictCache.java             # 字典缓存管理
│   │                               # - 加载字典到缓存
│   │                               # - 刷新缓存
│   ├── DictManager.java           # 字典管理器
│   │                               # - 获取字典值
│   │                               # - 翻译 label
│   ├── DictProperties.java        # 配置属性
│   │                               # - enabled: 是否启用
│   │                               # - cacheStrategy: 缓存策略
│   │                               # - loadMode: eager/lazy
│   │                               # - preloadOnStartup: 启动预加载
│   │                               # - includeCodes/excludeCodes: 加载范围
│   │
│   └── EnumScanner.java           # 枚举扫描器
│                                   # - 扫描实现 BaseEnum 的枚举
│
├── controller/
│   ├── DictTypeController.java   # 字典类型管理
│   ├── DictDataController.java   # 字典数据管理
│   └── DictTestController.java   # 测试
│
├── converter/
│   ├── DictEnumConverterFactory.java # 枚举转换器工厂
│   │                               # - 实现 ConverterFactory
│   │                               # - 将 String/Integer 转为 BaseEnum
│   │
│   └── MyEnumConverterFactory.java # 通用枚举转换（预留）
│
├── enums/
│   ├── BaseEnum.java              # 基础枚举接口
│   │                               # - getCode(): 获取存储值
│   │                               # - getName(): 获取枚举名
│   │                               # - getDesc(): 获取描述
│   │
│   └── DataStatus.java           # 数据状态枚举示例
│                                   # PAUSE(0, "暂停"), NORMAL(1, "正常")
│
├── interceptor/
│   └── DictResultInterceptor.java  # MyBatis 结果集拦截器
│                                   # - 查询结果返回时拦截
│                                   # - 扫描 @DictBind 字段
│                                   # - 翻译值并设置 target 字段
│
└── service/
    ├── IDictTypeService.java    # 字典类型服务接口
    ├── IDictDataService.java     # 字典数据服务接口
    └── impl/
        ├── DictTypeServiceImpl.java
        └── DictDataServiceImpl.java
```

---

### API 接口清单

```yaml
# 字典类型管理
GET    /dict/type/list              # 字典类型列表
GET    /dict/type/{id}             # 字典类型详情
POST   /dict/type                  # 创建字典类型
PUT    /dict/type                  # 更新字典类型
DELETE /dict/type/{id}            # 删除字典类型

# 字典数据管理
GET    /dict/data/list             # 字典数据列表
GET    /dict/data/{id}            # 字典数据详情
POST   /dict/data                 # 创建字典数据
PUT    /dict/data                 # 更新字典数据
DELETE /dict/data/{id}            # 删除字典数据
GET    /dict/data/options         # 字典数据下拉选项
```

---

### 依赖文件路径

```tree
# Base 基础类
src/main/java/com/xiyao/common/
├── utils/Result.java               # 统一响应
└── utils/page/PageQuery.java      # 分页查询

# System 模块（字典实体和 Mapper）
src/main/java/com/xiyao/system/
├── entity/
│   ├── DictType.java             # 字典类型实体
│   └── DictData.java             # 字典数据实体
└── mapper/
    ├── DictTypeMapper.java
    └── DictDataMapper.java
```

---

### 关键表结构

```sql
-- 字典类型表
CREATE TABLE sys_dict_type (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_name   VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_type   VARCHAR(100) NOT NULL UNIQUE COMMENT '字典类型标识',
    status      TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-停用',
    remark      VARCHAR(500) COMMENT '备注',
    create_by   VARCHAR(50),
    create_time DATETIME,
    update_by   VARCHAR(50),
    update_time DATETIME
);

-- 字典数据表
CREATE TABLE sys_dict_data (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_type   VARCHAR(100) NOT NULL COMMENT '字典类型标识',
    dict_label  VARCHAR(100) NOT NULL COMMENT '字典标签（显示值）',
    dict_value  VARCHAR(100) NOT NULL COMMENT '字典键值',
    dict_sort   INT DEFAULT 0 COMMENT '排序',
    css_class   VARCHAR(100) COMMENT '样式类名',
    status      TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-停用',
    remark      VARCHAR(500) COMMENT '备注',
    create_by   VARCHAR(50),
    create_time DATETIME,
    update_by   VARCHAR(50),
    update_time DATETIME
);
```

---

### 配置文件

```yaml
# application.yml
dict:
  enabled: true                    # 是否启用字典功能
  preload-on-startup: false        # 启动时预加载所有字典
  cache-strategy: local           # 缓存策略（预留 Redis）
  load-mode: eager                # 加载模式 eager/lazy
  include-codes:                  # 指定加载的字典编码
    - status
    - gender
  exclude-codes:                  # 排除的字典编码
```

---

### 枚举规范

```java
// BaseEnum 接口
public interface BaseEnum<T> {
    T getCode();       // 数据库存储的值
    String getName();  // 枚举名称
    String getDesc();  // 显示文本
}

// 实现示例
public enum DataStatus implements BaseEnum<Integer> {
    PAUSE(0, "暂停"),
    NORMAL(1, "正常");

    @EnumValue
    private final Integer code;
    private final String desc;

    @Override
    public Integer getCode() { return code; }

    @Override
    public String getName() { return name(); }

    @Override
    public String getDesc() { return desc; }
}

// 使用 @JsonCreator 支持多种反序列化方式
// 1=Normal, 正常=Normal, NORMAL=Normal 都能匹配
```