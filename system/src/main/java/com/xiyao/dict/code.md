## crypto模块

- RBAC 权限管理（含三员管理）模块方案

### 功能描述

- 字典类型维护：支持后台配置字典类型（如性别、状态）
- 字典数据维护：支持对字典类型下的键值对增删改查
- 自动映射：查询返回的 VO 字段自动将值（如 0）映射为显示文本（如“正常”）
- 枚举支持：支持使用枚举定义字典值，通过转换工厂处理请求参数
- 缓存机制：字典数据缓存至 Redis/本地，减少数据库查询


### 数据库字段加密设计

- 启动时加载全量字典到 Redis（Hash 结构：dict_type -> {value: label}）
- 配置变更时主动刷新缓存
- 本地缓存作为二级缓存，设置合理过期时间
- 查询字典时先从本地 -> Redis -> DB 的顺序加载


### 枚举规范

- 所有业务枚举需实现 BaseEnum 接口

```
public interface BaseEnum {
    String getValue();   // 数据库存储的值
    String getLabel();   // 显示文本
}

public enum DataStatus implements BaseEnum {
    NORMAL("0", "正常"),
    DISABLED("1", "停用");

    private final String value;
    private final String label;
    // 构造方法、getter...
}
```


### 自动映射机制

- 在 VO 中声明映射字段，使用 @DictBind 注解：
- DictResultInterceptor 实现 MyBatis 拦截器，查询到结果集后扫描 @DictBind 字段，根据字典类型和值从缓存中获取对应 label 并设置

```
public class UserVO {
    private Integer status;
    
    @DictBind(dictType = "sys_status", targetField = "status")
    private String statusText;   // 拦截器自动填充为 "正常" 或 "停用"
}
```

### 请求参数枚举转换

- Controller 接收参数时可使用枚举，通过 Spring Converter 自动转换：
- DictEnumConverterFactory 自动将字符串/数字转换为实现了 BaseEnum 的枚举实例

```
@GetMapping("/users")
public Result<Page<UserVo>> list(@RequestParam DataStatus status) { ... }
```


### 核心组件位置

- system/src/main/java/com/xiyao/system 实体类相关文件在这个包下

```
system/src/main/java/com/xiyao/dict/
├── annotation/
│   └── DictBind.java                  # 字典绑定注解
├── config/
│   ├── DictAutoConfig.java           # 字典自动配置
│   ├── DictCache.java                # 字典缓存（Redis/本地）
│   ├── DictManager.java              # 字典管理器（加载字典）
│   ├── DictProperties.java           # 配置属性
│   └── EnumScanner.java              # 枚举扫描器（扫描BaseEnum实现）
├── controller/
│   ├── DictTypeController.java       # 字典类型管理
│   ├── DictDataController.java       # 字典数据管理
│   └── DictTestController.java       # 测试
├── converter/
│   ├── DictEnumConverterFactory.java  # 字符串 -> BaseEnum 转换器
│   └── MyEnumConverterFactory.java   # 通用枚举转换工厂
├── enums/
│   ├── BaseEnum.java                  # 基础枚举接口
│   └── DataStatus.java               # 示例枚举
├── interceptor/
│   └── DictResultInterceptor.java     # MyBatis拦截器，结果集字段翻译
└── service/
    ├── IDictTypeService.java
    ├── IDictDataService.java
    └── impl/
        ├── DictTypeServiceImpl.java
        └── DictDataServiceImpl.java
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


### 关键表结构

- sys_oper_log：操作日志表
- sys_login_log：登录日志表

```
-- 操作日志表
CREATE TABLE sys_dict_type (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_type VARCHAR(100) NOT NULL UNIQUE COMMENT '字典类型标识',
    status    TINYINT DEFAULT 0 COMMENT '状态 0正常 1停用',
    remark    VARCHAR(500) COMMENT '备注',
    create_by VARCHAR(50),
    create_time DATETIME,
    update_by VARCHAR(50),
    update_time DATETIME
) COMMENT '字典类型表';

CREATE TABLE sys_dict_data (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型标识',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签（显示值）',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典键值',
    dict_sort INT DEFAULT 0 COMMENT '排序',
    css_class VARCHAR(100) COMMENT '样式类名（前端用）',
    status    TINYINT DEFAULT 0 COMMENT '状态',
    remark    VARCHAR(500),
    create_by VARCHAR(50),
    create_time DATETIME,
    update_by VARCHAR(50),
    update_time DATETIME
) COMMENT '字典数据表';

```