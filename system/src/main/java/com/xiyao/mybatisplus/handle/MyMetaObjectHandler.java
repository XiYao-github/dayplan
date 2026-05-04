package com.xiyao.mybatisplus.handle;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.xiyao.mybatisplus.base.entity.MyBaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");

        Long userId = 0L;
        LocalDateTime now = LocalDateTime.now();

        this.strictInsertFill(metaObject, MyBaseEntity.Fields.createBy, Long.class, userId);
        this.strictInsertFill(metaObject, MyBaseEntity.Fields.createTime, LocalDateTime.class, now);

        this.strictInsertFill(metaObject, MyBaseEntity.Fields.updateBy, Long.class, userId);
        this.strictInsertFill(metaObject, MyBaseEntity.Fields.updateTime, LocalDateTime.class, now);
    }

    /**
     * 更新时填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");

        Long userId = 0L;
        LocalDateTime now = LocalDateTime.now();

        this.strictUpdateFill(metaObject, MyBaseEntity.Fields.updateBy, Long.class, userId);
        this.strictUpdateFill(metaObject, MyBaseEntity.Fields.updateTime, LocalDateTime.class, now);
    }
}
