## common 模块

### 模块介绍

```yaml
common 模块是 DayPlan 框架的公共工具和基础组件模块，为所有业务模块提供通用能力。

核心功能:
  - 统一响应：Result 封装 code、msg、data 格式
  - 基础类：Controller、Entity、Mapper、Service 基类
  - 分页支持：PageResult 分页响应
  - 工具类：类型转换、代码生成、测试数据生成
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
    提供：ok()/error()/Result

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

  前端请求（?pageNum=1&pageSize=10&orderByColumn=createTime&isAsc=desc）
        │
        ▼
  PageQuery 接收参数
        │
        ▼
  query.build(Entity.class) 构建分页对象 +排序项
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
│   │                                   # - ok()/error() 方法
│   │                                   # - getLoginUser()/getUserId() 获取当前用户
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
│   │                                   # - 继承 BaseMapper 提供 CRUD
│   │                                   # - queryCount(): 自定义条件统计
│   │
│   ├── service/
│   │   ├── MyBaseService.java      # Service 接口基类
│   │   │                               # - 继承 IService 提供通用方法
│   │   │                               # - queryByField(): 按字段查询单条/批量
│   │   │                               # - queryPage(): 分页查询
│   │   │
│   │   └── impl/
│   │       └── MyBaseServiceImpl.java # Service 实现基类
│   │                                       # - 继承 ServiceImpl
│   │                                       # - getNumber(): 生成业务编号
│   │
│   └── event/
│       └── MyBaseEvent.java        # 事件基类
│                                       # - 继承 ApplicationEvent
│                                       # - 自动从 HttpServletRequest 提取请求信息
│
├── constant/
│   └── Constant.java               # 全局常量接口
│                                       # - 日期格式、字符集等
│                                       # - ASC/DESC 排序方向常量
│
├── enums/
│   └── FormatType.java # 日期格式枚举
│                                       # - 提供常用的日期时间格式常量
│                                       # - getFormatsType() 按字符串匹配格式
│
├── properties/
│   ├── OssProperties.java        # 对象存储配置（阿里云 OSS）
│   │                                 # - accessKeyId/accessKeySecret
│   │                                 # - endpoint/bucketName
│   │
│   ├── SmsProperties.java        # 短信服务配置（阿里云）
│   │                                 # - accessKeyId/accessKeySecret
│   │                                 # - signName/templateCode
│   │
│   └── WeChatProperties.java # 微信小程序配置
│                                     # - appId/appSecret
│
├── validate/
│   └── groups/
│       ├── Add.java              # 新增校验分组
│       ├── Edit.java             # 编辑校验分组
│       └── Query.java            # 查询校验分组
│                                     # 用于 @Validated 注解分组校验
│
└── utils/
    ├── CodeGenerator.java          # 代码生成器
    │                                   # - 根据数据库表生成 Entity/Mapper/Service/Controller
    │
    ├── ConvertUtils.java            # 对象转换工具
    │                                   # - sourceToTarget(): 单对象转换
    │                                   # - sourceToTarget(): 集合批量转换
    │
    ├── DateUtils.java              # 日期工具类
    │                                   # - format(): 格式化日期时间
    │                                   # - parse(): 解析日期字符串
    │                                   # - getStartOfDay/getEndOfDay(): 当天起止时间
    │                                   # - plusDays/plusMonths(): 日期加减
    │                                   # - daysBetween(): 计算天数差
    │                                   # - getAge(): 计算年龄
    │                                   # - isToday/isBetween(): 日期范围判断
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
        ├── PageResult.java           # 分页结果封装
        │                                 # - records: 当前页数据
        │                                 # - total: 总记录数
        │                                 # - size: 每页条数
        │                                 # - current: 当前页码
        │                                 # - pages: 总页数
        └── PageQuery.java            # 分页查询实体
                                          # - pageSize/pageNum: 分页参数
                                          # - orderByColumn/isAsc: 排序参数
                                          # - build(): 构建 MyBatis-Plus Page 对象
                                          # - 支持驼峰和数据库列名两种排序字段格式
                                          # - 白名单校验防止 SQL 注入
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

**MyBaseService 接口提供的方法：**

```java
// CRUD 继承自 MyBatis-Plus IService
// save(T entity) // 单条插入
// saveBatch(List<T> list)           // 批量插入
// saveOrUpdate(T entity)            // 插入或更新
// removeById(Serializable id)       // 根据 ID 删除
// removeByIds(Collection<ids>)       // 批量删除
// getById(Serializable id)          // 根据 ID 查询
// list()                             // 查询所有
// list(Wrapper<T> wrapper)           // 条件查询列表
// page(IPage<T> page)                // 分页查询
// page(IPage<T> page, Wrapper<T>) // 条件分页查询

// 便捷查询方法
// queryByField(SFunction, Object)    // 按字段查询单条
// queryByField(SFunction, Collection) // 按字段批量查询
// queryPage(PageQuery)              // 分页查询（使用 PageQuery 构建）
// queryPage(PageQuery, QueryWrapper) //条件分页查询
```

**MyBaseServiceImpl 提供的方法：**

```java
// CRUD 继承自 MyBatis-Plus ServiceImpl
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

// 业务编号生成
// String code = getNumber("TX", User::getCreateTime);
// 生成格式：TX202606100001（前缀+年月日+4位序号）
// 说明：根据日期字段统计当日数据条数，生成连续编号
```

**PageQuery 排序功能：**

```java
// 支持多种排序用法：
// {isAsc:"asc", orderByColumn:"id"}         → order by id asc
// {isAsc:"asc", orderByColumn:"createTime"} → order by create_time asc
// {isAsc:"desc", orderByColumn:"id,name"}   → order by id desc, name desc
// {isAsc:"asc,desc", orderByColumn:"id,name"} → order by id asc, name desc

// 字段格式说明：
// - 输入支持驼峰格式（createTime）或数据库列名格式（create_time）
// - 内部自动转换为数据库列名进行白名单校验
// - 不在白名单中的列名将被拒绝，防止 SQL 注入
```

**Result 响应格式：**

```java
// 成功响应
// Result.ok()                         // { code: 200, msg: "请求成功", data: null }
// Result.ok(data)                     // { code: 200, msg: "请求成功", data: {...} }
// Result.ok(msg, data)                 // { code: 200, msg: "自定义消息", data: {...} }

// 失败响应
// Result.error()                      // { code: 500, msg: "请求失败", data: null }
// Result.error("操作失败")             // { code: 500, msg: "操作失败", data: null }
```

---

### 分页使用示例

```java
// 前端请求示例（带排序）
// GET /api/user/list?pageNum=1&pageSize=10&orderByColumn=createTime&isAsc=desc
// Controller
// @GetMapping("/list")
// public Result<PageResult<UserVO>> list(PageQuery query) {
//     // 使用 PageQuery 构建分页对象（自动处理排序参数）
//     Page<User> page = query.build(User.class);
//     IPage<User> result = userService.page(page, wrapper);
//     List<UserVO> voList = convertToVoList(result.getRecords());
//     return ok(PageResult.page(result, voList));
// }

// Service 中使用分页查询（推荐方式）
// public PageResult<UserVO> queryUsers(PageQuery query) {
//     return queryPage(query);
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
# common 模块基础类无需配置，直接继承使用

#第三方服务配置（system.common.xxx 前缀）
system:
  common:
    oss:                    # 对象存储配置
      accessKeyId: "your-access-key-id"
      accessKeySecret: "your-access-key-secret"
      endpoint: "oss-cn-hangzhou.aliyuncs.com"
      bucketName: "your-bucket-name"
    sms:                    # 短信服务配置
      accessKeyId: "your-access-key-id"
      accessKeySecret: "your-access-key-secret"
      endpoint: "dysmsapi.aliyuncs.com"
      signName: "签名名称"
      templateCode: "SMS_xxx"
    wechat:                 # 微信小程序配置
      appId: "your-app-id"
      secret: "your-secret"

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
    // 直接使用 ok()/error() 方法
}

# 使用 MyBaseServiceImpl 只需泛型指定
@Service
public class UserServiceImpl extends MyBaseServiceImpl<UserMapper, User> implements UserService {
    // 直接使用父类 CRUD 方法
}
```