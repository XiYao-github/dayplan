## dict 模块

### 模块介绍

```yaml
dict 模块提供数据字典能力，支持键值对管理和自动映射回显。

核心功能:
  - 字典类型：管理和配置字典类型（如状态、性别等）
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
缓存: 本地缓存（DictCache）
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
│   ├── DictAutoConfig.java        # 自动配置（@ConditionalOnProperty）
│   ├── DictCache.java             # 字典缓存管理
│   │                               # - 加载字典到缓存
│   │                               # - 刷新缓存
│   └── DictProperties.java        # 配置属性
│                                   # - enable: 是否启用
│                                   # - preloadOnStartup: 启动预加载
│
├── converter/
│   └── DictEnumConverterFactory.java # 枚举转换器工厂
│                                   # - 实现 ConverterFactory
│                                   # - 将 String/Integer 转为 BaseEnum
│
├── enums/
│   └── BaseEnum.java              # 基础枚举接口
│                                   # - getCode(): 获取存储值
│                                   # - getName(): 获取枚举名
│                                   # - getDesc(): 获取描述
│
└── interceptor/
    └── DictResultInterceptor.java  # MyBatis 结果集拦截器
                                    # - 查询结果返回时拦截
                                    # - 扫描 @DictBind 字段
                                    # - 翻译值并设置 target 字段
```

**字典服务在 system 模块实现：**
```tree
com.xiyao.system.service/
├── IDictService.java             # 字典服务接口
└── impl/
    └── DictServiceImpl.java     # 字典服务实现
```

---

### API 接口清单

```yaml
# 字典类型管理
GET    /dict/type/list              # 字典类型列表
GET    /dict/type/{id}             # 字典类型详情
GET    /dict/type/options          # 字典类型下拉选项
POST   /dict/type                  # 创建字典类型
PUT    /dict/type                  # 更新字典类型
DELETE /dict/type/{id}            # 删除字典类型

# 字典数据管理
GET    /dict/data/list             # 字典数据列表
GET    /dict/data/{id}            # 字典数据详情
GET    /dict/data/options/{dictType} # 字典数据下拉选项
POST   /dict/data                 # 创建字典数据
PUT    /dict/data                 # 更新字典数据
DELETE /dict/data/{id}            # 删除字典数据
POST   /dict/data/refresh         # 刷新字典缓存
```

**@DictBind 注解使用示例：**

```java
// 在 VO 字段上标注，自动翻译为 label
@DictBind(code = "status", target = "statusDesc")
private Integer status;
private String statusDesc;  // 自动填充为"正常"/"停用"
```

**字典权限说明：**

```java
// 所有管理员都可以查看字典类型和字典数据
@PreAuthorize("@ss.hasAnyAdmin()")

// 只有系统管理员可以增删改字典
@PreAuthorize("@ss.isSystemAdmin()")
```

---

### 依赖文件路径

```tree
# Base 基础类
src/main/java/com/xiyao/common/
├── utils/Result.java               # 统一响应
└── utils/page/PageQuery.java      # 分页查询

# System 模块（字典实体、Mapper、Service）
src/main/java/com/xiyao/system/
├── entity/
│   ├── DictType.java             # 字典类型实体
│   └── DictData.java             # 字典数据实体
├── mapper/
│   ├── DictTypeMapper.java
│   └── DictDataMapper.java
└── service/
    ├── IDictService.java         # 字典服务接口
    └── impl/
        └── DictServiceImpl.java # 字典服务实现
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
  enable: true                    # 是否启用字典功能（默认true）
  preload-on-startup: false        # 启动时预加载所有字典
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
```
