package com.xiyao.log.listener;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.log.event.LogLoginEvent;
import com.xiyao.log.event.LogOperationEvent;
import com.xiyao.system.entity.LogLogin;
import com.xiyao.system.entity.LogOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogListener {

    @Async("logExecutor")
    @EventListener
    public void saveLoginLog(LogLoginEvent event) {
        try {
            LogLogin logLogin = new LogLogin();
            logLogin.setUserId(event.getUserId());
            logLogin.setUsername(event.getUsername());
            logLogin.setLoginTime(event.getLoginTime());
            logLogin.setStatus(event.getStatus());
            logLogin.setMessage(event.getMessage());

            logLogin.setIp(event.getClientIp());
            logLogin.setOs(event.getOs());
            logLogin.setBrowser(event.getBrowser());
            logLogin.setPlatform(event.getPlatform());

            Db.save(logLogin);
        } catch (Exception e) {
            log.error("保存登录日志失败", e);
        }
    }

    @Async("logExecutor")
    @EventListener
    public void saveOperationLog(LogOperationEvent event) {
        try {
            LogOperation logOperation = new LogOperation();
            logOperation.setUserId(event.getUserId());
            logOperation.setUsername(event.getUsername());
            logOperation.setOperationModule(event.getModule());
            logOperation.setOperationMethod(event.getMethod());
            logOperation.setOperationType(event.getType());
            logOperation.setOperationTime(event.getTime());
            logOperation.setStatus(event.getStatus());
            logOperation.setMessage(event.getMessage());

            logOperation.setRequestMethod(event.getRequestMethod());
            logOperation.setRequestUrl(event.getRequestUrl());
            logOperation.setRequestParam(event.getRequestParam());
            logOperation.setReturnResult(event.getReturnResult());
            logOperation.setCostTime(event.getCostTime());

            logOperation.setIp(event.getClientIp());
            logOperation.setOs(event.getOs());
            logOperation.setBrowser(event.getBrowser());
            logOperation.setPlatform(event.getPlatform());

            Db.save(logOperation);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}