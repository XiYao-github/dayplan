package com.xiyao.log.listener;

import cn.hutool.crypto.SmUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.log.enums.LogType;
import com.xiyao.log.event.LogLoginEvent;
import com.xiyao.log.event.LogOperationEvent;
import com.xiyao.system.entity.LogLogin;
import com.xiyao.system.entity.LogOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * 日志事件监听器
 * <p>
 * 监听并保存操作日志和认证日志：
 * <ul>
 *     <li>操作日志事件：存入 log_operation 表（OPERATION 无哈希链，AUDIT 带哈希链）</li>
 *     <li>认证日志事件（LOGIN/LOGOUT/REGISTER）：存入 log_login 表（带哈希链）</li>
 * </ul>
 *
 * @author xiyao
 */
@Slf4j
@RequiredArgsConstructor
public class LogListener {

    private static final String HASH_SEED = "Log_Listener";

    /**
     * 监听操作日志事件并保存
     * <p>
     * 存入 log_operation 表：
     * <ul>
     *     <li>OPERATION 类型：无哈希链</li>
     *     <li>AUDIT 类型：带 SM3 哈希链防篡改</li>
     * </ul>
     */
    @Async("logExecutor")
    @EventListener
    public void saveOperationLog(LogOperationEvent event) {
        try {
            LogOperation logOperation = new LogOperation();
            logOperation.setUserId(event.getUserId());
            logOperation.setUsername(event.getUsername());
            logOperation.setAdminType(event.getAdminType());
            logOperation.setOperationModule(event.getModule());
            logOperation.setOperationMethod(event.getMethod());
            logOperation.setOperationType(event.getType());
            logOperation.setOperationTime(event.getTime());
            logOperation.setStatus(event.getStatus());
            logOperation.setMessage(event.getMessage());
            logOperation.setRequestParam(event.getRequestParam());
            logOperation.setReturnResult(event.getReturnResult());
            logOperation.setCostTime(event.getCostTime());
            logOperation.setIp(event.getClientIp());
            logOperation.setOs(event.getOs());
            logOperation.setBrowser(event.getBrowser());
            logOperation.setPlatform(event.getPlatform());
            logOperation.setTraceId(event.getTraceId());
            logOperation.setRequestMethod(event.getRequestMethod());
            logOperation.setRequestUrl(event.getRequestUrl());
            logOperation.setLogType(event.getLogType());

            // AUDIT 类型计算 SM3 哈希链
            if (event.getLogType() != null && event.getLogType() == LogType.AUDIT.ordinal()) {
                String hash = computeOperationHash(logOperation);
                logOperation.setHash(hash);
                logOperation.setPrevHash(getLastOperationHash());
            }

            Db.save(logOperation);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 监听认证日志事件并保存
     * <p>
     * 存入 log_login 表（登录/登出/注册），带 SM3 哈希链防篡改。
     */
    @Async("logExecutor")
    @EventListener
    public void saveLoginLog(LogLoginEvent event) {
        try {
            LogLogin logLogin = new LogLogin();
            logLogin.setUserId(event.getUserId());
            logLogin.setUsername(event.getUsername());
            logLogin.setAuthType(event.getAuthType());
            logLogin.setStatus(event.getStatus());
            logLogin.setMessage(event.getMessage());
            logLogin.setLoginTime(event.getLoginTime());
            logLogin.setIp(event.getClientIp());
            logLogin.setOs(event.getOs());
            logLogin.setBrowser(event.getBrowser());
            logLogin.setPlatform(event.getPlatform());
            logLogin.setTraceId(event.getTraceId());

            // 计算 SM3 哈希链
            String hash = computeLoginHash(logLogin);
            logLogin.setHash(hash);
            logLogin.setPrevHash(getLastLoginHash());

            Db.save(logLogin);
        } catch (Exception e) {
            log.error("保存认证日志失败", e);
        }
    }

    /**
     * 计算认证日志 SM3 哈希
     */
    private String computeLoginHash(LogLogin logLogin) {
        String data = String.join("|",
                HASH_SEED,
                String.valueOf(logLogin.getUserId()),
                logLogin.getUsername() != null ? logLogin.getUsername() : "",
                String.valueOf(logLogin.getAuthType()),
                String.valueOf(logLogin.getStatus()),
                logLogin.getMessage() != null ? logLogin.getMessage() : "",
                logLogin.getIp() != null ? logLogin.getIp() : "",
                logLogin.getTraceId() != null ? logLogin.getTraceId() : "",
                String.valueOf(logLogin.getLoginTime() != null ? logLogin.getLoginTime().toString() : ""),
                getLastLoginHash() != null ? getLastLoginHash() : ""
        );
        return SmUtil.sm3(data);
    }

    /**
     * 获取上一条认证日志的哈希
     */
    private String getLastLoginHash() {
        try {
            LogLogin last = Db.lambdaQuery(LogLogin.class)
                    .orderByDesc(LogLogin::getId)
                    .select(LogLogin::getHash)
                    .last("LIMIT 1").one();
            return last != null ? last.getHash() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取上一条操作日志的哈希（用于哈希链）
     */
    private String getLastOperationHash() {
        try {
            LogOperation last = Db.lambdaQuery(LogOperation.class)
                    .orderByDesc(LogOperation::getId)
                    .select(LogOperation::getHash)
                    .last("LIMIT 1").one();
            return last != null ? last.getHash() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算操作日志 SM3 哈希（用于 AUDIT 类型）
     */
    private String computeOperationHash(LogOperation logOperation) {
        String data = String.join("|",
                HASH_SEED,
                String.valueOf(logOperation.getUserId()),
                logOperation.getUsername() != null ? logOperation.getUsername() : "",
                String.valueOf(logOperation.getAdminType()),
                logOperation.getOperationModule() != null ? logOperation.getOperationModule() : "",
                logOperation.getOperationMethod() != null ? logOperation.getOperationMethod() : "",
                String.valueOf(logOperation.getOperationType()),
                String.valueOf(logOperation.getStatus()),
                logOperation.getMessage() != null ? logOperation.getMessage() : "",
                String.valueOf(logOperation.getCostTime()),
                logOperation.getTraceId() != null ? logOperation.getTraceId() : "",
                String.valueOf(logOperation.getOperationTime() != null ? logOperation.getOperationTime().toString() : ""),
                getLastOperationHash() != null ? getLastOperationHash() : ""
        );
        return SmUtil.sm3(data);
    }
}