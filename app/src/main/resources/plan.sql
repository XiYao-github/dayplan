# Dayplan 小程序数据库设计

-- ==================== 小程序用户表 ====================

drop table if exists app_user;
create table app_user
(
    id              bigint auto_increment primary key,
    openid          varchar(100)            not null comment '微信openID',
    nickname        varchar(50)  default '' null comment '昵称',
    phone           varchar(20)  default '' null comment '电话',
    avatar          varchar(255) default '' null comment '头像',
    status          int          default 1  null comment '状态(0.停用 1.正常)',
    last_login_ip   varchar(50)  default '' null comment '最后登录IP',
    last_login_time datetime                null comment '最后登录时间',
    deleted         int          default 0  null comment '逻辑删除(0.未删除 1.已删除)',
    version         int          default 0  null comment '乐观锁'
) comment '小程序用户';

-- ==================== 打卡记录表 ====================

drop table if exists checkin_record;
create table checkin_record
(
    id            bigint auto_increment primary key,
    user_id       bigint        not null comment '用户ID',
    checkin_type  tinyint       not null comment '打卡类型(1.睡觉 2.健身)',
    record_date   date          not null comment '打卡日期',
    bed_time      time          null comment '上床时间',
    start_time    time          null comment '开始时间',
    end_time      time          null comment '结束时间',
    duration      int           null comment '时长(分钟)',
    exercise_type varchar(20)   null comment '运动类型(strength/cardio/stretch)',
    remark        varchar(500)  null comment '备注',
    deleted       int default 0 null comment '逻辑删除(0.未删除 1.已删除)',
    version       int default 0 null comment '乐观锁'
) comment '打卡记录表';

-- ==================== 每日记录表 ====================

drop table if exists daily_record;
create table daily_record
(
    id           bigint auto_increment primary key,
    user_id      bigint        not null comment '用户ID',
    record_type  tinyint       not null comment '记录类型(1.计划 2.记录 3.总结)',
    period tinyint default 1 not null comment '周期维度(1.日 2.周 3.月 4.年)',
    period_value varchar(20)   not null comment '周期值',
    content      text          not null comment '记录内容',
    highlight    varchar(500)  null comment '高光/成就',
    blocker      varchar(500)  null comment '卡点/困难',
    category     varchar(20)   null comment '分类(learn/work/life)',
    deleted      int default 0 null comment '逻辑删除(0.未删除 1.已删除)',
    version      int default 0 null comment '乐观锁'
) comment '每日记录表';

-- ==================== 写作记录表（朗读+写作合并） ====================

drop table if exists writing_record;
create table writing_record
(
    id              bigint auto_increment primary key,
    user_id         bigint        not null comment '用户ID',
    record_date     date          not null comment '记录日期',
    title           varchar(200)  not null comment '标题/简介',
    word_count      int default 0 not null comment '背单词数量',
    source_text     text          null comment '原文',
    translated_text text          null comment '译文',
    image_urls      varchar(1000) null comment '辅助图片(多个用逗号分隔)',
    remark          varchar(500)  null comment '备注',
    deleted         int default 0 null comment '逻辑删除(0.未删除 1.已删除)',
    version         int default 0 null comment '乐观锁'
) comment '写作记录表';

-- ==================== 学习记录表 ====================

drop table if exists study_record;
create table study_record
(
    id          bigint auto_increment primary key,
    user_id     bigint        not null comment '用户ID',
    record_date date          not null comment '记录日期',
    subject     varchar(50)   not null comment '学习主题',
    topic       varchar(200)  not null comment '具体知识点',
    duration    int           not null comment '学习时长(分钟)',
    image_urls  varchar(1000) null comment '辅助图片(多个用逗号分隔)',
    remark      varchar(500)  null comment '备注',
    deleted     int default 0 null comment '逻辑删除(0.未删除 1.已删除)',
    version     int default 0 null comment '乐观锁'
) comment '学习记录表';
