# Dayplan 需求

## 一、需求概述

个人作息管理与学习打卡系统（小程序端），帮助用户建立稳定的作息习惯、保持学习和输出。

---

## 二、用户体系设计

### 2.1 小程序用户与后台用户分离

- **后台用户**：`sys_user`（系统管理、权限控制等）
- **小程序用户**：`app_user`（独立用户体系，仅用于打卡业务）

### 2.2 小程序用户表 (app_user)

表结构详见 `app/src/main/resources/plan.sql`

### 2.3 认证机制

小程序用户登录后，通过 `wx.login()` 获取 code，换取 openid，生成独立的 JWT token。

**认证流程：**
1. 拦截器拦截 `/app/**` 请求
2. 从请求体/请求头获取 token
3. 解析 token 获取用户标识
4. 从缓存获取用户数据
5. 通过参数解析器注入到 Controller 方法参数

**获取当前用户ID方式：**
```java
// 通过自定义参数解析器直接获取
@GetMapping("/today")
public Result<Object> getTodayRecord(@CurrentUser Long userId) {
    // userId 直接注入，无需手动从上下文获取
}
```

**注意**：小程序 token 与后台 token 独立，不能混用。

---

## 三、代码组织

当前阶段所有代码放在 `system` 层：

```
com.xiyao.system
├── controller
│   ├── CheckinController        # 打卡接口（睡觉/健身）
│   ├── DailyRecordController   # 每日记录接口（计划/记录/总结）
│   ├── WritingController        # 写作接口
│   └── StudyController          # 学习接口
├── entity
│   ├── AppUser                  # 小程序用户
│   ├── CheckinRecord           # 打卡记录
│   ├── DailyRecord             # 每日记录
│   ├── WritingRecord           # 写作记录
│   └── StudyRecord             # 学习记录
├── mapper
│   ├── AppUserMapper
│   ├── CheckinRecordMapper
│   ├── DailyRecordMapper
│   ├── WritingRecordMapper
│   └── StudyRecordMapper
├── service
│   ├── AppUserService
│   ├── CheckinRecordService
│   ├── DailyRecordService
│   ├── WritingRecordService
│   └── StudyRecordService
├── security
│   └── AppSecurityConstants    # 小程序认证相关常量
└── vo
    ├── CheckinRecordVo
    ├── DailyRecordVo
    ├── WritingRecordVo
    ├── StudyRecordVo
    ├── CheckinStatsVo
    ├── DailyStatsVo
    └── PageResultVo
```

---

## 四、数据库表设计

**所有表结构详见 `app/src/main/resources/plan.sql`**，以下是概要说明：

| 表名 | 说明 |
|------|------|
| `app_user` | 小程序用户 |
| `checkin_record` | 打卡记录（睡觉/健身） |
| `daily_record` | 每日记录（计划/记录/总结） |
| `writing_record` | 写作记录 |
| `study_record` | 学习记录 |

### 4.1 打卡记录表 (checkin_record)

- 通过 `checkin_type` 区分睡觉(1)和健身(2)
- 字段：id、user_id、checkin_type、record_date、bed_time、start_time、end_time、duration、exercise_type、remark

### 4.2 每日记录表 (daily_record)

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

### 4.3 写作记录表 (writing_record)

- 存储写作内容，包括原文和译文
- 字段：id、user_id、record_date、title、word_count、source_text、translated_text、image_urls、remark

### 4.4 学习记录表 (study_record)

- 记录学习主题、知识点、时长
- 字段：id、user_id、record_date、subject、topic、duration、image_urls、remark

---

## 五、接口设计

### 5.1 用户接口

**路径前缀：** `/app/user`

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST /login` | 小程序登录 | 微信 code 换 openid |
| `GET /info` | 获取个人信息 | |
| `PUT /info` | 更新个人信息 | 修改昵称、头像等 |

**登录请求体：**
```json
{
    "code": "微信code"
}
```

**登录响应：**
```json
{
    "token": "jwt-token",
    "userId": 1
}
```

---

### 5.2 打卡接口 (CheckinController)

**路径前缀：** `/app/checkin`

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST /sleep` | 创建/更新睡觉打卡 | 同一日期重复调用会更新 |
| `POST /workout` | 创建/更新健身打卡 | 同一日期重复调用会更新 |
| `GET /sleep/today` | 获取今日睡觉打卡 | 无记录返回 null |
| `GET /workout/today` | 获取今日健身打卡 | 无记录返回 null |
| `GET /sleep/stats` | 睡觉打卡统计 | 连续天数、本周数据 |
| `GET /workout/stats` | 健身打卡统计 | 连续天数、本周数据 |

**睡觉打卡请求：**
```json
{
    "bedTime": "23:40",
    "remark": "今天比较早"
}
```

**健身打卡请求：**
```json
{
    "startTime": "17:30",
    "endTime": "18:40",
    "exerciseType": "strength",
    "remark": "练了胸"
}
```

**exercise_type 可选值：**
| 值 | 说明 |
|----|------|
| strength | 力量训练 |
| cardio | 有氧运动 |
| stretch | 拉伸 |

**统计响应 (CheckinStatsVo)：**
```json
{
    "continuousDays": 7,
    "totalDays": 30,
    "thisWeek": 5,
    "thisMonth": 20,
    "last7Days": [
        {"date": "2026-06-19", "done": true, "bedTime": "23:00"},
        {"date": "2026-06-20", "done": false}
    ]
}
```

---

### 5.3 每日记录接口 (DailyRecordController)

**路径前缀：** `/app/daily`

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST /plan` | 创建/更新计划 | |
| `GET /plan/today` | 获取今日计划 | |
| `GET /plan/week` | 获取本周计划 | |
| `GET /plan/month` | 获取本月计划 | |
| `POST /record` | 创建/更新记录 | |
| `GET /record/today` | 获取今日记录列表 | |
| `POST /summary` | 创建/更新总结 | |
| `GET /summary/today` | 获取今日总结 | |
| `GET /summary/week` | 获取本周总结 | |
| `GET /summary/month` | 获取本月总结 | |
| `GET /today` | 获取今日三记（计划+记录+总结） | |
| `GET /week` | 获取本周三记 | |
| `GET /month` | 获取本月三记 | |
| `GET /stats` | 获取统计数据 | 各周期完成率 |

**创建计划请求：**
```json
{
    "period": 1,
    "content": "今天Java学集合的List接口，朗读《白杨礼赞》片段",
    "category": "learn"
}
```

**period 取值说明：**
| period | 说明 | period_value 示例 |
|--------|------|-------------------|
| 1 | 日 | 2026-06-25 |
| 2 | 周 | 2026-W26 |
| 3 | 月 | 2026-06 |
| 4 | 年 | 2026 |

**创建记录请求：**
```json
{
    "period": 1,
    "content": "List和Set的区别搞混了，明天查文档",
    "category": "learn"
}
```

**创建总结请求：**
```json
{
    "period": 1,
    "content": "今天效率不错",
    "highlight": "完成了HashMap学习",
    "blocker": "晚上有点犯困"
}
```

**获取统计数据响应：**
```json
{
    "today": {
        "plan": true,
        "record": true,
        "summary": false
    },
    "thisWeek": {
        "plan": 5,
        "record": 7,
        "summary": 4
    },
    "thisMonth": {
        "plan": 20,
        "record": 28,
        "summary": 18
    }
}
```

---

### 5.4 写作接口 (WritingController)

**路径前缀：** `/app/writing`

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST` | 创建/更新写作记录 | |
| `GET /today` | 获取今日写作记录 | |
| `GET /stats` | 获取统计数据 | 连续打卡天数 |

**请求体：**
```json
{
    "title": "白杨礼赞翻译练习",
    "wordCount": 100,
    "sourceText": "白杨树实在是不平凡的，我赞美白杨树！",
    "translatedText": "The white poplar is truly extraordinary, I praise the white poplar!",
    "imageUrls": "/uploads/xxx.jpg,/uploads/yyy.jpg",
    "remark": "第一次翻译，语法可能有错误"
}
```

**字段说明：**
| 字段 | 说明 |
|------|------|
| title | 标题/简介 |
| wordCount | 背单词数量 |
| sourceText | 原文 |
| translatedText | 译文 |
| imageUrls | 辅助图片（多个用逗号分隔） |
| remark | 备注 |

---

### 5.5 学习接口 (StudyController)

**路径前缀：** `/app/study`

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST` | 创建/更新学习记录 | |
| `GET /today` | 获取今日学习记录 | |
| `GET /list` | 分页查询学习记录 | 支持按 subject 筛选 |
| `GET /stats` | 获取统计数据 | 本周学习时长 |

**请求体：**
```json
{
    "subject": "Java",
    "topic": "HashMap扩容机制",
    "duration": 90,
    "imageUrls": "/uploads/study/xxx.jpg",
    "remark": "终于理解了扩容因子0.75的意义"
}
```

**字段说明：**
| 字段 | 说明 |
|------|------|
| subject | 学习主题 |
| topic | 具体知识点 |
| duration | 学习时长（分钟） |
| imageUrls | 辅助图片（多个用逗号分隔） |
| remark | 备注 |

---

## 六、VO 设计

### 6.1 统一分页响应

```java
@Data
@Accessors(chain = true)
public class PageResultVo<T> {
    private Long total;
    private Long page;
    private Long pageSize;
    private List<T> records;
}
```

### 6.2 打卡统计响应

```java
@Data
@Accessors(chain = true)
public class CheckinStatsVo {
    private Integer continuousDays;     // 连续打卡天数
    private Integer totalDays;          // 累计打卡天数
    private Integer thisWeek;           // 本周打卡次数
    private Integer thisMonth;          // 本月打卡次数
    private List<DayStatus> last7Days;  // 最近7天打卡状态

    @Data
    public static class DayStatus {
        private LocalDate date;
        private Boolean done;
        private String bedTime;        // 睡觉打卡特有
        private String exerciseType;   // 健身打卡特有
    }
}
```

### 6.3 每日统计响应

```java
@Data
@Accessors(chain = true)
public class DailyStatsVo {
    private PeriodStatus today;
    private PeriodStatus thisWeek;
    private PeriodStatus thisMonth;

    @Data
    public static class PeriodStatus {
        private Boolean plan;         // 计划是否完成（当日为布尔，本周/月为数字）
        private Boolean record;
        private Boolean summary;
    }
}
```

---

## 七、周期工具类

提供 `PeriodUtils` 工具类，用于生成 period_value：

```java
public class PeriodUtils {

    // 获取当前日 period_value: "2026-06-25"
    public static String today() {
        return LocalDate.now().toString();
    }

    // 获取当前周 period_value: "2026-W26"
    public static String thisWeek() {
        WeekFields weekFields = WeekFields.ISO;
        int weekOfYear = LocalDate.now().get(weekFields.weekOfYear());
        return LocalDate.now().getYear() + "-W" + String.format("%02d", weekOfYear);
    }

    // 获取当前月 period_value: "2026-06"
    public static String thisMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    // 获取当前年 period_value: "2026"
    public static String thisYear() {
        return String.valueOf(LocalDate.now().getYear());
    }
}
```

---

## 八、开发优先级

### 第一阶段：用户体系 + 打卡模块
1. 数据库表 `app_user`
2. AppUser Entity + Mapper + Service
3. 小程序登录接口
4. 认证拦截器 + 参数解析器
5. 数据库表 `checkin_record`
6. CheckinRecord Entity + Mapper + Service
7. 睡觉打卡 + 健身打卡接口
8. 统计接口（连续天数算法）

### 第二阶段：每日记录模块
1. 数据库表 `daily_record`
2. DailyRecord Entity + Mapper + Service
3. 计划 + 记录 + 总结 接口
4. 周期统计接口

### 第三阶段：写作 + 学习模块
1. 写作记录 `writing_record`
2. 学习记录 `study_record`

---

## 九、原有需求描述（保留参考）

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
