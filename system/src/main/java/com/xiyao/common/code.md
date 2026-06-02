## common 模块

### 模块介绍

```yaml
common 模块是 DayPlan 框架的公共工具和基础组件模块，为所有业务模块提供通用能力。

核心功能:
  - 统一响应：Result 封装 code、msg、data 格式
  - 基础类：Controller、Entity、Mapper、Service 基类
  - 分页支持：PageQuery 分页参数、TableDataInfo 分页响应
  - 工具类：Redis 缓存、Spring 容器访问、类型转换
  - 常量定义：日期格式、字符集、登录操作等常量
  - 状态枚举：操作结果的简单成功/失败状态

技术特点:
  - 零依赖：工具类不依赖业务模块，纯 JDK/API 实现
  - 可继承：基础类提供默认实现，按需覆盖
  - 规范统一：所有模块遵循相同的响应格式和编码规范
```

---

### 技术实现方案

**基础类继承关系：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        基础类继承关系                                 │
└─────────────────────────────────────────────────────────────────────┘

  Controller 继承链
    MyBaseController ←── 业务 Controller
                        │
    提供：success()/error()/Result

  Service 继承链
    IService（MyBatis-Plus）
        ↑
    MyBaseService ←── 业务 Service 接口
        ↑
    MyBaseServiceImpl ←── 业务 Service 实现
        │
    提供：CRUD + 分页 + 批量操作

  Mapper 继承链
    BaseMapper（MyBatis-Plus）
        ↑
    MyBaseMapper ←── 业务 Mapper 接口

  Entity 继承链
    MyBaseEntity ←── 业务实体
                    │
    提供：id/createBy/createTime/updateBy/updateTime/deleted/version
```

**分页查询流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        分页查询流程                                   │
└─────────────────────────────────────────────────────────────────────┘

  前端请求（?pageNum=1&pageSize=10）
        │
        ▼
  PageQuery 接收参数
        │
        ▼
  IPage<T> page = query.build()
        │
        ├── pageNum → page.current
        └── pageSize → page.size
        │
        ▼
  Service.page(page, wrapper)
        │
        ▼
  MyBatis-Plus 查询总数 + 当前页数据
        │
        ▼
  TableDataInfo.build(page)
        │
        ▼
  返回 { total, rows, code, msg }
```

---

### 组件结构

```tree
com.xiyao.common/
├── base/
│   ├── controller/
│   │   └── MyBaseController.java    # Controller 基类
│   │                                   # - success()/error() 方法
│   │                                   # - 返回 Result 统一响应
│   │
│   ├── entity/
│   │   └── MyBaseEntity.java        # 实体基类
│   │                                   # - id: 自增主键
│   │                                   # - createBy/createTime: 创建人/时间
│   │                                   # - updateBy/updateTime: 更新人/时间
│   │                                   # - deleted: 逻辑删除标志
│   │                                   # - version: 乐观锁版本号
│   │                                   # - remark: 备注
│   │
│   ├── mapper/
│   │   └── MyBaseMapper.java        # Mapper 基类接口
│   │                                   # 继承 BaseMapper 提供 CRUD
│   │
│   ├── service/
│   │   ├── MyBaseService.java      # Service 接口基类
│   │   │                               # 继承 IService 提供通用方法
│   │   │
│   │   └── impl/
│   │       └── MyBaseServiceImpl.java # Service 实现基类
│   │                                       # 继承 ServiceImpl
│   │
│   └── event/
│       └── MyBaseEvent.java        # 事件基类
│                                       # 继承 ApplicationEvent
│
├── utils/
│   ├── Result.java                  # 统一响应封装
│   │                                   # - code: 状态码
│   │                                   # - msg: 消息
│   │                                   # - data: 数据
│   │                                   # - success()/error() 静态方法
│   │
│   ├── RedisUtils.java             # Redis 工具类
│   │                                   # - set(key, value)/set(key, value, timeout)
│   │                                   # - get(key)/get(key, clazz)
│   │                                   # - delete(key)/expire(key, time)
│   │
│   ├── SpringUtils.java            # Spring 容器工具
│   │                                   # - getBean()/getBean(Class)
│   │                                   # - getProperty()
│   │                                   # - publishEvent()
│   │
│   ├── ConvertUtils.java           # 类型转换工具
│   │
│   ├── WebUtils.java               # Web 工具类
│   │
│   ├── CodeGenerator.java          # 代码生成器
│   │
│   └── data/
│       └── PageResult.java            # 分页结果封装
│                                       # - records: 当前页数据
│                                       # - total: 总记录数
│                                       # - size: 每页条数
│                                       # - current: 当前页码
│                                       # - pages: 总页数
│
├── enums/
│   └── Status.java                 # 通用状态枚举
│                                   # SUCCESS/FAIL
│
└── constant/
    └── Constant.java               # 全局常量接口
                                      # - 日期格式、字符集、登录操作等
```

---

### 基础类说明

**MyBaseEntity 字段说明：**

```java
// 主键策略：自增（数据库 AUTO_INCREMENT）
@TableId(type = IdType.AUTO)

// 自动填充（MybatisPlusConfig 配置）
createBy     // 创建人 ID（insert 时填充）
createTime   // 创建时间（insert 时填充）
updateBy     // 更新人 ID（insert/update 时填充）
updateTime   // 更新时间（insert/update 时填充）

// 逻辑删除（@TableLogic 自动过滤）
deleted = 0  // 未删除
deleted = 1  // 已删除

// 乐观锁（@Version 并发控制）
version      // 每次更新 +1，WHERE version = oldVersion
```

**MyBaseServiceImpl 提供的方法：**

```java
// CRUD 继承自 MyBatis-Plus IService
save(T entity)
saveBatch(List<T> list)
saveOrUpdate(T entity)
removeById(Serializable id)
removeByIds(Collection<Serializable> ids)
getById(Serializable id)
list()
list(Wrapper<T> wrapper)
page(IPage<T> page)
page(IPage<T> page, Wrapper<T> wrapper)

// 分页查询
Page<T> page = new Page<>(current, size);
```

**Result 响应格式：**

```java
// 成功响应
Result.success()                    // { code: 200, msg: "请求成功", data: null }
Result.success(data)                // { code: 200, msg: "请求成功", data: {...} }
Result.success("操作成功", data)     // { code: 200, msg: "操作成功", data: {...} }

// 失败响应
Result.error("操作失败")             // { code: 500, msg: "操作失败", data: null }
Result.error(400, "参数错误")         // { code: 400, msg: "参数错误", data: null }
```

---

### 分页使用示例

```java
// Controller
@GetMapping("/list")
public Result<TableDataInfo<User>> list(UserQuery query) {
    IPage<User> page = query.build();
    userService.page(page, wrapper);
    return success(TableDataInfo.build(page));
}

// 分页参数类
@Data
public class UserQuery extends PageQuery {
    private String username;
    private Integer status;
}

// 前端请求
GET /user/list?pageNum=1&pageSize=10&orderByColumn=createTime&isAsc=desc

// 响应
{
    "code": 200,
    "msg": "查询成功",
    "total": 100,
    "rows": [...]
}
```

---

### 依赖文件路径

```tree
# 无外部依赖，common 模块是基础中的基础

# 被以下模块依赖：
system/src/main/java/com/xiyao/
├── system/                          # 业务实体、Mapper、Service
├── security/                        # 登录用户、权限
├── log/                               # 日志事件
├── governance/                      # 降级处理
├── dict/                            # 枚举转换
└── crypto/                          # 加密工具

# 依赖关系：
common 是所有其他模块的基础，不依赖任何其他业务模块
```

---

### 配置说明

```yaml
# common 模块无需配置，基础类直接继承使用

# 使用 MyBaseEntity 只需在实体类中
@Data
public class User extends MyBaseEntity {
    private String username;
    private String password;
    // 无需定义基类字段，父类已提供
}

# 使用 MyBaseController 只需继承
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController extends MyBaseController {
    // 直接使用 success()/error() 方法
}

# 使用 MyBaseServiceImpl 只需泛型指定
@Service
public class UserServiceImpl extends MyBaseServiceImpl<UserMapper, User> implements UserService {
    // 直接使用父类 CRUD 方法
}
```