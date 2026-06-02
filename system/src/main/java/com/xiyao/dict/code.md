## dict 模块

### 模块介绍

```yaml
dict 模块提供数据字典能力，支持键值对管理和自动映射回显。

核心功能:
  - 字典数据：维护字典类型下的键值对（value/label）
  - 自动映射：查询结果自动将值映射为显示文本
  - 缓存机制：本地缓存减少数据库查询

技术特点:
  - 插件式配置：通过 @ConditionalOnProperty 实现功能可插拔
  - 注解驱动：@DictBind 注解声明自动映射
  - MyBatis 拦截：结果集返回时自动翻译（DictInterceptor）
  - MetaObject：安全的字段访问，target 字段不存在时安全跳过
```

---

### 技术实现方案

**技术栈：**

```yaml
字典翻译: MyBatis ResultSetInterceptor（DictInterceptor）
缓存: 本地缓存（DictCache）
数据访问: MyBatis-Plus Db.lambdaQuery（跳过 Service 层）
```

**字典缓存流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        字典缓存流程                                    │
└─────────────────────────────────────────────────────────────────────┘

  应用启动
        │
        ▼
  DictAutoConfig.initDictCache()
        │
        ▼
  DictCache.loadDictAll()
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
  DictDataServiceImpl 调用 dictCache.refreshAll()
        │
        ▼
  重新加载所有字典数据
```

**自动映射流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        自动映射流程                                   │
└─────────────────────────────────────────────────────────────────────┘

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

### 组件结构

```tree
com.xiyao.dict/
├── annotation/
│   └── DictBind.java               # 字典绑定注解
│                                   # - code: 字典类型编码
│                                   # - target: 目标字段名（必须显式指定）
│
├── config/
│   ├── DictAutoConfig.java        # 自动配置
│   │                               # - @ConditionalOnProperty 控制开关
│   │                               # - initDictCache() 启动时加载缓存
│   │                               # - 注册 DictInterceptor 到 MyBatis
│   │
│   └── DictCache.java             # 字典缓存管理器（单例）
│                                   # - loadDictAll(): 全量加载
│                                   # - refreshAll(): 刷新缓存
│                                   # - getDictLabel(): 获取标签
│
├── interceptor/
│   └── DictInterceptor.java       # MyBatis 结果集拦截器
│                                   # - @Intercepts(handleResultSets)
│                                   # - 递归处理 Map/Collection/Object
│                                   # - 使用 MetaObject 安全访问字段
│
└── properties/
    └── DictProperties.java        # 配置属性
                                    # - enable: 是否启用
```

**System 模块（字典业务）：**
```tree
com.xiyao.system/
├── entity/DictData.java           # 字典数据实体
├── mapper/DictDataMapper.java     # 字典数据 Mapper
├── service/
│   ├── IDictDataService.java      # 字典数据服务接口
│   └── impl/DictDataServiceImpl.java # 字典数据服务实现
│                                   # - 增删改后调用 dictCache.refreshAll()
├── service/IDictService.java      # （空接口，暂未使用）
└── service/impl/DictServiceImpl.java # （空实现，暂未使用）
```

---

### API 接口清单

```yaml
# 字典数据管理（system 模块）
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
public class UserVO {
    @DictBind(code = "status", target = "statusText")
    private Integer status;        // 数据库存储的值（如 1）
    private String statusText;      // 自动填充为"正常"或"停用"
}
```

**@DictBind 使用要求：**
- `code`：字典类型编码，必填
- `target`：目标字段名，必填
- 字段类型支持：`Integer`、`String` 等可转换类型
- target 字段不存在时安全跳过，不抛异常

---

### 依赖文件路径

```tree
# 无外部依赖（dict 模块完全独立）

# 技术依赖（通过 Maven 引入）
# - MyBatis-Plus：Db.lambdaQuery 数据查询
# - Lombok：自动生成 getter/setter
# - Hutool：ObjectUtil 空值判断
```

---

### 关键表结构

```sql
-- 字典数据表
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

### 配置文件

```yaml
# application.yml
dict:
  enable: true                    # 是否启用字典功能（默认 true）
```

---

### 与旧版本对比（简化说明）

**已移除：**
- BaseEnum 接口和枚举转换功能
- DictEnumConverterFactory（Controller 参数枚举转换）
- 枚举预扫描机制
- IDictService/DictServiceImpl（DictCache 直接使用 Db.lambdaQuery）

**当前设计优势：**
- 更轻量：移除不必要的枚举功能，专注字典回显
- 更安全：使用 MetaObject 访问字段，target 不存在不抛异常
- 更直接：DictCache 跳过 Service 层，直接用 Db.lambdaQuery 查询