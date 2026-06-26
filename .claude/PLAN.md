# Dayplan 需求

## 一、需求概述

个人作息管理与学习打卡系统（小程序端），帮助用户建立稳定的作息习惯、保持学习和输出。

---

## 二、RESTful 接口设计规则

### 2.1 核心原则

| 原则 | 说明 |
|------|------|
| URL定位资源 | URL使用名词复数，定位资源而不是动作 |
| HTTP方法语义 | GET查询、POST新增、PUT更新、DELETE删除 |
| 资源唯一标识 | 每个资源有唯一标识，URL体现资源层级 |
| 前后端解耦 | Controller接收Request DTO，不直接传Entity给Service |

### 2.2 URL规范

```
# 正确：资源+层级
GET    /app/checkin/sleep          # 睡觉打卡资源
POST   /app/checkin/sleep          # 新增睡觉打卡
PUT    /app/checkin/sleep          # 更新睡觉打卡
DELETE /app/checkin/sleep          # 删除睡觉打卡

GET    /app/checkin/sleep/stats    # 睡觉打卡统计

# 错误：动词+名词
GET    /app/getTodaySleepCheckin
POST   /app/saveSleepCheckin
```

### 2.3 PUT vs POST 区分

```
POST   /app/checkin/sleep   → 新增今日睡觉打卡（资源不存在时）
PUT    /app/checkin/sleep   → 更新今日睡觉打卡（资源已存在时）
```

**业务规则：**
- 每日每种类型打卡只有一条记录，日期隐含为今天
- PUT调用时若资源不存在应报错，不自动新增
- POST调用时若资源已存在应报错，不自动更新

### 2.4 三层职责

```
Controller层:
  - 接收HTTP请求参数（@RequestParam、@RequestBody）
  - 调用Service业务方法
  - 封装Result响应
  - 【禁止】在Controller中构造Entity

Service层:
  - 纯业务逻辑
  - 接收业务参数（非Entity）
  - 构造Entity并调用Mapper
  - 【禁止】直接暴露HTTP相关注解

Mapper层:
  - 数据访问
  - MyBatis-Plus CRUD
```

### 2.5 Request/Response DTO规范

```
包结构:
  com.xiyao.service.controller.request   # 请求DTO
  com.xiyao.service.controller.response  # 响应DTO（备用）
  com.xiyao.service.vo                  # VO对象

原则:
  - Request DTO: 接收前端参数，与Entity解耦
  - Response DTO: 封装返回数据
  - VO: View Object，专门给前端返回的对象
```

### 2.6 统一响应格式

```java
@RestController
@RequestMapping("/app/checkin")
@RequiredArgsConstructor
public class CheckinController extends MyBaseController {

    private final CheckinRecordService checkinRecordService;

    @PostMapping("/sleep")
    public Result<Void> createSleep(
            @RequestParam Long userId,
            @RequestBody SleepCheckinRequest request
    ) {
        checkinRecordService.createSleep(userId, request);
        return Result.ok();
    }

    @GetMapping("/sleep")
    public Result<SleepCheckinVo> getTodaySleep(@RequestParam Long userId) {
        SleepCheckinVo vo = checkinRecordService.getTodaySleep(userId);
        return Result.ok(vo);
    }
}
```

### 2.7 错误处理

```java
// Service层抛出业务异常
@Override
public void createSleep(Long userId, SleepCheckinRequest request) {
    CheckinRecord exist = getByUserAndDate(userId, 1, LocalDate.now());
    if (ObjectUtil.isNotNull(exist)) {
        throw new BusinessException("今日睡觉打卡已存在");
    }
    // ...
}

// Controller层不需try-catch，全局异常处理器统一处理
```

---

## 三、数据库表设计

**所有表结构详见 `app/src/main/resources/plan.sql`**，以下是概要说明：

| 表名 | 说明 |
|------|------|
| `app_user` | 小程序用户 |
| `checkin_record` | 打卡记录（睡觉/健身） |
| `daily_record` | 每日记录（计划/记录/总结） |
| `writing_record` | 写作记录 |
| `study_record` | 学习记录 |

### 3.1 打卡记录表 (checkin_record)

- 通过 `checkin_type` 区分睡觉(1)和健身(2)
- 字段：id、user_id、checkin_type、record_date、bed_time、start_time、end_time、duration、exercise_type、remark

### 3.2 每日记录表 (daily_record)

- 通过 `record_type` 区分计划(1)、记录(2)、总结(3)
- 通过 `period` + `period_value` 支持日/周/月/年四种维度
- 字段：id、user_id、record_type、period、period_value、content、highlight、blocker、category

**period_value 示例：**
| period | period_value | 说明 |
|--------|--------------|------|
| 1 (日) | 2026-06-25 | 2026年6月25日 |
| 2 (周) | 2026-W26 | 2026年第26周 |
| 3 (月) | 2026-06 | 2026年6月 |
| 4 (年) | 2026 | 2026年 |

### 3.3 写作记录表 (writing_record)

- 存储写作内容，包括原文和译文
- 字段：id、user_id、record_date、title、word_count、source_text、translated_text、image_urls、remark

### 3.4 学习记录表 (study_record)

- 记录学习主题、知识点、时长
- 字段：id、user_id、record_date、subject、topic、duration、image_urls、remark

---

## 四、接口设计

### 4.1 打卡接口

**路径前缀：** `/app/checkin`

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST /sleep` | 新增睡觉打卡 | |
| `PUT /sleep` | 更新睡觉打卡 | |
| `DELETE /sleep` | 删除睡觉打卡 | |
| `GET /sleep` | 获取今日睡觉打卡 | |
| `GET /sleep/stats` | 睡觉打卡统计 | |
| `POST /workout` | 新增健身打卡 | |
| `PUT /workout` | 更新健身打卡 | |
| `DELETE /workout` | 删除健身打卡 | |
| `GET /workout` | 获取今日健身打卡 | |
| `GET /workout/stats` | 健身打卡统计 | |

**睡觉打卡请求(SleepCheckinRequest)：**
```json
{
    "bedTime": "23:40",
    "remark": "今天比较早"
}
```

**健身打卡请求(WorkoutCheckinRequest)：**
```json
{
    "startTime": "17:30",
    "endTime": "18:40",
    "exerciseType": "strength",
    "remark": "练了胸"
}
```

### 4.2 每日记录接口

**路径前缀：** `/app/daily`

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST /plan` | 新增计划 | |
| `PUT /plan` | 更新计划 | |
| `GET /plan` | 获取计划 | 参数: period=1/2/3/4 |
| `POST /record` | 新增记录 | |
| `PUT /record` | 更新记录 | |
| `GET /record` | 获取记录列表 | |
| `POST /summary` | 新增总结 | |
| `PUT /summary` | 更新总结 | |
| `GET /summary` | 获取总结 | |
| `GET /today` | 获取今日三记 | |
| `GET /stats` | 获取统计数据 | |

### 4.3 写作接口

**路径前缀：** `/app/writing`

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST` | 新增写作记录 | |
| `PUT` | 更新写作记录 | |
| `DELETE` | 删除写作记录 | |
| `GET` | 获取今日写作记录 | |
| `GET /stats` | 获取统计数据 | |

### 4.4 学习接口

**路径前缀：** `/app/study`

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST` | 新增学习记录 | |
| `PUT` | 更新学习记录 | |
| `DELETE` | 删除学习记录 | |
| `GET` | 获取今日学习记录 | |
| `GET /list` | 分页查询 | |
| `GET /stats` | 获取统计数据 | |

---

## 五、周期工具类

提供 `PeriodUtils` 工具类，用于生成 period_value：

```java
public class PeriodUtils {

    public static final int PERIOD_DAY = 1;
    public static final int PERIOD_WEEK = 2;
    public static final int PERIOD_MONTH = 3;
    public static final int PERIOD_YEAR = 4;

    public static String today() {
        return LocalDate.now().toString();
    }

    public static String thisWeek() {
        WeekFields weekFields = WeekFields.ISO;
        int weekOfYear = LocalDate.now().get(weekFields.weekOfYear());
        return LocalDate.now().getYear() + "-W" + String.format("%02d", weekOfYear);
    }

    public static String thisMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public static String thisYear() {
        return String.valueOf(LocalDate.now().getYear());
    }

    public static String getCurrentPeriodValue(Integer period) {
        if (period == null) {
            return today();
        }
        return switch (period) {
            case PERIOD_DAY -> today();
            case PERIOD_WEEK -> thisWeek();
            case PERIOD_MONTH -> thisMonth();
            case PERIOD_YEAR -> thisYear();
            default -> today();
        };
    }
}
```

---

## 六、原有需求描述（保留参考）

### 睡觉打卡
每天只记录一个数字——上床时间（例如23:40）。不纠结几点睡着，只记录躺到床上的时刻。目的是给自己一个"收工"信号，逐步把作息稳定下来。建议设一个22:50的闹钟，提醒自己准备结束一切活动。

### 健身打卡
记录开始时间和结束时间（例如17:30-18:40），可以顺手备注运动类型（力量/有氧/拉伸）。时长不限，但必须记下起止点，这是对自己"动起来"的确认。如果当天实在没时间，哪怕做10分钟拉伸也要打卡，保持连续性比强度更重要。

### 每日记录/计划/总结
每天至少完成一项，但鼓励三项都做。计划写在做事之前；记录写在做事之中；总结写在一天结束。支持按日/周/月/年四个维度进行记录和统计。

### 每日写作
当前阶段每天背100个单词，同时把朗读的中文文章片段尝试翻译成英文。等词汇量上来后，逐步过渡到直接写英文小短文。记录标题、原文、译文、图片、备注等信息。

### 学习技能
每天固定学习Java，按基础语法→面向对象→集合框架→IO流→多线程→JVM→框架的顺序推进。每天只学一个具体知识点，记录学了什么内容以及用时。
