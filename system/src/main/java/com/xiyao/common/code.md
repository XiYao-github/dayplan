## common 模块

### 模块介绍

```yaml
common 模块是 DayPlan 框架的公共工具和基础组件模块，为所有业务模块提供通用能力。

核心功能:
  - 统一响应：Result 封装 code、msg、data 格式
  - 基础类：Controller、Entity、Mapper、Service 基类
  - 分页支持：PageResult 分页响应
  - 工具类：Stream 流处理、类型转换、代码生成、测试数据生成
  - 常量定义：日期格式、字符集等常量
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
  Controller 接收参数
        │
        ▼
  Page< User> page = new Page<>(pageNum, pageSize)
        │
        ▼
  Service.page(page, wrapper)
        │
        ▼
  MyBatis-Plus 查询总数 + 当前页数据
        │
        ▼
  PageResult.page(page, voList)
        │
        ▼
  返回 { records, total, size, current, pages }
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
│                                       # 自动从 HttpServletRequest 提取请求信息
│
├── constant/
│   └── Constant.java               # 全局常量接口
│                                       # - 日期格式、字符集等
│
└── utils/
    ├── CodeGenerator.java          # 代码生成器
    │                                   # - 根据数据库表生成 Entity/Mapper/Service/Controller
    │
    ├── ConvertUtils.java            # 对象转换工具
    │                                   # - sourceToTarget(): 单对象转换
    │                                   # - sourceToTarget(): 集合批量转换
    │
    ├── ExcelUtils.java             # Excel 工具类
    │                                   # - export(): 导出 Excel
    │                                   # - importExcel(): 导入 Excel
    │                                   # - fillTemplate(): 模板填充
    │
    ├── FileUtils.java               # 文件工具类
    │                                   # - upload(): 文件上传
    │                                   # - download(): 文件下载
    │                                   # - delete(): 文件删除
    │                                   # - 支持本地/云存储（OSS/COS/七牛云）
    │
    ├── StreamUtils.java             # Stream 流工具
    │                                   # - filter(): 过滤
    │                                   # - toList/toSet/toMap(): 类型转换
    │                                   # - groupByKey(): 分组
    │                                   # - merge(): Map 合并
    │
    ├── TestDataUtils.java           # 测试数据生成
    │                                   # - randomString(): 随机字符串
    │                                   # - randomPhone(): 随机手机号
    │                                   # - randomIdCard(): 随机身份证
    │                                   # - randomEmail(): 随机邮箱
    │                                   # - randomName(): 随机姓名
    │                                   # - generateUsers(): 批量生成用户
    │
    └── data/
        ├── Result.java               # 统一响应封装
        └── PageResult.java           # 分页结果封装
                                        # - records: 当前页数据
                                        # - total: 总记录数
                                        # - size: 每页条数
                                        # - current: 当前页码
                                        # - pages: 总页数
```

---

### 基础类说明

**MyBaseEntity 字段说明：**

```java
// 主键策略：自增（数据库 AUTO_INCREMENT）
// @TableId(type = IdType.AUTO)

// 自动填充（MybatisPlusConfig 配置）
// createBy     // 创建人 ID（insert 时填充）
// createTime   // 创建时间（insert 时填充）
// updateBy     // 更新人 ID（insert/update 时填充）
// updateTime   // 更新时间（insert/update 时填充）

// 逻辑删除（@TableLogic 自动过滤）
// deleted = 0  // 未删除
// deleted = 1  // 已删除

// 乐观锁（@Version 并发控制）
// version      // 每次更新 +1，WHERE version = oldVersion
```

**MyBaseServiceImpl 提供的方法：**

```java
// CRUD 继承自 MyBatis-Plus IService
// save(T entity)
// saveBatch(List<T> list)
// saveOrUpdate(T entity)
// removeById(Serializable id)
// removeByIds(Collection<Serializable> ids)
// getById(Serializable id)
// list()
// list(Wrapper<T> wrapper)
// page(IPage<T> page)
// page(IPage<T> page, Wrapper<T> wrapper)

// 分页查询
// Page<T> page = new Page<>(current, size);
```

**Result 响应格式：**

```java
// 成功响应
// Result.success()                    // { code: 200, msg: "请求成功", data: null }
// Result.success(data)                // { code: 200, msg: "请求成功", data: {...} }

// 失败响应
// Result.error("操作失败")             // { code: 500, msg: "操作失败", data: null }
```

---

### 分页使用示例

```java
// Controller
// @GetMapping("/list")
// public Result<PageResult<UserVO>> list(UserQuery query) {
//     Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
//     IPage<User> result = userService.page(page, wrapper);
//     List<UserVO> voList = convertToVoList(result.getRecords());
//     return success(PageResult.page(result, voList));
// }

// 响应
// {
//     "code": 200,
//     "msg": "请求成功",
//     "data": {
//         "records": [...],
//         "total": 100,
//         "size": 10,
//         "current": 1,
//         "pages": 10
//     }
// }
```

---

### 依赖文件路径

```tree
# 无外部依赖，common 模块是基础中的基础

# 被以下模块依赖：
src/main/java/com/xiyao/
├── system/                          # 业务实体、Mapper、Service
├── security/                        # 登录用户、权限
├── log/                             # 日志事件
├── dict/                            # 字典绑定注解
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
