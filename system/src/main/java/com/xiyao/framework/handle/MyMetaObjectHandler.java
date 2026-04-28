package com.xiyao.framework.handle;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.xiyao.common.base.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");

        LocalDateTime now = LocalDateTime.now();

        this.strictInsertFill(metaObject, BaseEntity.Fields.createTime, LocalDateTime.class, now);

        this.strictInsertFill(metaObject, BaseEntity.Fields.updateTime, LocalDateTime.class, now);
    }

    /**
     * 更新时填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");

        LocalDateTime now = LocalDateTime.now();

        this.strictUpdateFill(metaObject, BaseEntity.Fields.updateTime, LocalDateTime.class, now);
    }
}
