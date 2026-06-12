# dict 模块

## 模块概述

dict 模块提供数据字典和地区信息管理能力，支持键值对管理和自动映射回显。

**核心功能：**
- 字典数据：维护字典类型下的键值对（value/label）
- 自动映射：查询结果自动将值映射为显示文本
- 缓存机制：本地缓存减少数据库查询
- 枚举转换：Spring 参数转换和 MyBatis 数据库转换
- 地区管理：地区信息缓存，支持层级结构和批量填充

**技术特点：**
- 插件式配置：通过 @ConditionalOnProperty 实现功能可插拔
- 注解驱动：@DictBind 注解声明自动映射
- MyBatis 拦截：结果集返回时自动翻译（DictInterceptor）
- MetaObject：安全的字段访问，target 字段不存在时安全跳过

---

## 技术实现方案

### 技术栈

```
字典翻译: MyBatis ResultSetInterceptor（DictInterceptor）
枚举转换: MyBatis TypeHandler（EnumTypeHandler）+ Spring ConverterFactory
缓存: 本地缓存（DictUtils）
数据访问: MyBatis-Plus Db.lambdaQuery（跳过 Service 层）
```

### 模块结构

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          dict 模块结构                                │
└─────────────────────────────────────────────────────────────────────┘

  DictAutoConfig（模块自动配置）
        │
        ├── @PostConstruct initDictCache() 启动加载字典缓存
        ├── @PostConstruct initAddressCache() 启动加载地区缓存
        └── @Bean DictInterceptor 注册字典拦截器

  字典数据流程
        │
        ├── @DictBind 标注字段
        ├── MyBatis 查询返回结果集
        ├── DictInterceptor 拦截
        └── 自动填充 target 字段为 label

  地区数据流程
        │
        ├── AddressUtils.getName() 获取地区名称
        ├── AddressUtils.getTree() 获取地区树
        └── AddressUtils.fillName() 批量填充
```

### 字典回显流程

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        字典回显流程                                  │
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
  从 DictUtils.getDictLabel(code, value) 获取标签
        │
        ▼
  MetaObject.setValue() 设置到 target 字段
        │
        ▼
  返回给前端（target 字段已是 label）
```

### 枚举转换流程

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        枚举转换流程                                  │
└─────────────────────────────────────────────────────────────────────┘

  Spring MVC 接收请求参数（String 类型）
        │
        ▼
  EnumConverterFactory.getConverter() 获取转换器
        │
        ▼
  StringToEnumConverter.convert() 调用 BaseEnum.fromValue()
        │
        ▼
  匹配枚举实例（支持 code/desc/name 三种匹配方式）
        │
        ▼
  返回对应枚举类型

  ──────────────────────────────────────────

  MyBatis 查询结果（数据库存储的是 code 值）
        │
        ▼
  EnumTypeHandler.getNullableResult()
        │
        ▼
  调用 BaseEnum.fromValue() 转换为枚举
        │
        ▼
  返回枚举类型字段
```

### 字典缓存流程

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        字典缓存流程                                  │
└─────────────────────────────────────────────────────────────────────┘

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

## 组件结构

```tree
com.xiyao.dict/
├── annotation/
│   └── DictBind.java              # 字典绑定注解
│                                   # - code: 字典类型编码
│                                   # - target: 目标字段名
│
├── config/
│   └── DictAutoConfig.java       # 字典自动配置
│                                   # - initDictCache() 启动加载字典缓存
│                                   # - initAddressCache() 启动加载地区缓存
│                                   # - dictInterceptor() 注册拦截器
│
├── converter/
│   └── EnumConverterFactory.java # Spring 枚举转换器工厂
│                                   # - String -> BaseEnum 参数转换
│
├── enums/                          # 通用枚举
│   ├── BaseEnum.java              # 枚举基础接口
│   ├── DataStatus.java            # 数据状态枚举
│   └── Status.java                # 通用状态枚举
│
├── handler/
│   └── EnumTypeHandler.java       # MyBatis 枚举类型转换器
│                                   # - 枚举 -> 数据库：setNonNullParameter
│                                   # - 数据库 -> 枚举：getNullableResult
│
├── interceptor/
│   └── DictInterceptor.java       # MyBatis 结果集拦截器
│                                   # - @Intercepts(handleResultSets)
│                                   # - 递归处理 Map/Collection/Object
│                                   # - 使用 MetaObject 安全访问字段
│
├── properties/
│   └── DictProperties.java       # 字典配置属性
│
└── utils/
    ├── AddressUtils.java         # 地区信息缓存管理器
    │                               # - getName(): 获取地区名称
    │                               # - getNameMap(): 批量获取名称映射
    │                               # - fillName(): 批量填充地址名称
    │                               # - getChildren(): 获取子地区列表
    │                               # - getTree(): 获取地区树形结构
    │
    └── DictUtils.java             # 字典工具类
                                    # - loadDictAll(): 全量加载
                                    # - refreshAll(): 刷新缓存
                                    # - getDictLabel(): 获取标签
```

---

## 配置文件

```yaml
# application.yml

# 字典模块配置
system:
  dict:
    enable: true                    # 是否启用字典功能（默认 true）
```

---

## 依赖文件路径

```tree
# Common 基础类
src/main/java/com/xiyao/common/
├── base/
│   ├── controller/MyBaseController.java
│   ├── entity/MyBaseEntity.java
│   ├── event/MyBaseEvent.java
│   ├── mapper/MyBaseMapper.java
│   ├── service/MyBaseService.java
│   └── service/impl/MyBaseServiceImpl.java
├── constant/Constant.java
└── utils/
    ├── Result.java
    ├── RedisUtils.java
    ├── SpringUtils.java
    └── WebUtils.java

# System 模块（实体）
src/main/java/com/xiyao/system/
├── entity/
│   ├── SysDictData.java          # 字典数据实体
│   └── SysAddress.java           # 地区信息实体
└── mapper/
    ├── SysDictDataMapper.java
    └── SysAddressMapper.java
```
