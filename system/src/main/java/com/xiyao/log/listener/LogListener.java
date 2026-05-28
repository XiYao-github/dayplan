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
 * 监听并保存操作日志和认证日志，实现日志的异步持久化。
 *
 * <p>
 * <b>监听的事件：</b>
 * <ul>
 *     <li>LogOperationEvent：操作/审计日志事件</li>
 *     <li>LogLoginEvent：认证日志事件（登录/登出/注册）</li>
 * </ul>
 *
 * <p>
 * <b>存储策略：</b>
 * <ul>
 *     <li>OPERATION 日志：存入 log_operation 表（无哈希链）</li>
 *     <li>AUDIT 日志：存入 log_operation 表（带 SM3 哈希链）</li>
 *     <li>认证日志：存入 log_login 表（带 SM3 哈希链）</li>
 * </ul>
 *
 * <p>
 * <b>哈希链说明：</b>
 * AUDIT 类型日志和认证日志使用 SM3 国密算法计算哈希链，
 * 每条记录的 hash 值包含：数据内容 + 上一条记录的 hash，
 * 保证日志完整性，任何篡改都可以被检测到。
 *
 * @author xiyao
 * @see LogOperationEvent
 * @see LogLoginEvent
 */
@Slf4j
@RequiredArgsConstructor
public class LogListener {

    /**
     * 哈希种子
     * <p>
     * 用于哈希计算的数据混淆，增加安全性。
     */
    private static final String HASH_SEED = "Log_Listener";

    /**
     * 监听操作日志事件并保存
     * <p>
     * 根据 logType 决定是否计算 SM3 哈希链：
     * <ul>
     *     <li>OPERATION：无哈希链，普通业务追踪</li>
     *     <li>AUDIT：有 SM3 哈希链，用于等保合规审计</li>
     * </ul>
     *
     * <p>
     * <b>异步执行：</b>
     * 使用 @Async 注解在线程池中异步执行，不阻塞业务线程。
     *
     * @param event 操作日志事件
     */
    @Async("logExecutor")
    @EventListener
    public void saveOperationLog(LogOperationEvent event) {
        try {
            // ========== 构建日志实体 ==========
            LogOperation logOperation = new LogOperation();
            // 用户信息
            logOperation.setUserId(event.getUserId());
            logOperation.setUsername(event.getUsername());
            logOperation.setAdminType(event.getAdminType());
            // 操作信息
            logOperation.setOperationModule(event.getModule());
            logOperation.setOperationMethod(event.getMethod());
            logOperation.setOperationType(event.getType());
            logOperation.setOperationTime(event.getTime());
            logOperation.setStatus(event.getStatus());
            logOperation.setMessage(event.getMessage());
            // 请求响应信息
            logOperation.setRequestParam(event.getRequestParam());
            logOperation.setReturnResult(event.getReturnResult());
            logOperation.setCostTime(event.getCostTime());
            // 客户端信息
            logOperation.setIp(event.getClientIp());
            logOperation.setOs(event.getOs());
            logOperation.setBrowser(event.getBrowser());
            logOperation.setPlatform(event.getPlatform());
            logOperation.setTraceId(event.getTraceId());
            logOperation.setRequestMethod(event.getRequestMethod());
            logOperation.setRequestUrl(event.getRequestUrl());
            logOperation.setLogType(event.getLogType());

            // ========== AUDIT 类型计算哈希链 ==========
            // OPERATION 类型日志不做哈希链，AUDIT 类型需要防篡改
            if (event.getLogType() != null && event.getLogType() == LogType.AUDIT.ordinal()) {
                // 计算当前记录的哈希值
                String hash = computeOperationHash(logOperation);
                logOperation.setHash(hash);
                // 获取上一条记录的哈希值，形成链式结构
                logOperation.setPrevHash(getLastOperationHash());
            }

            // ========== 保存到数据库 ==========
            Db.save(logOperation);

        } catch (Exception e) {
            // 保存失败记录错误日志，不影响业务
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 监听认证日志事件并保存
     * <p>
     * 认证日志（登录/登出/注册）强制使用 SM3 哈希链防篡改。
     *
     * <p>
     * <b>异步执行：</b>
     * 使用 @Async 注解在线程池中异步执行，不阻塞业务线程。
     *
     * @param event 认证日志事件
     */
    @Async("logExecutor")
    @EventListener
    public void saveLoginLog(LogLoginEvent event) {
        try {
            // ========== 构建日志实体 ==========
            LogLogin logLogin = new LogLogin();
            // 用户信息
            logLogin.setUserId(event.getUserId());
            logLogin.setUsername(event.getUsername());
            // 认证信息
            logLogin.setAuthType(event.getAuthType());
            logLogin.setStatus(event.getStatus());
            logLogin.setMessage(event.getMessage());
            logLogin.setLoginTime(event.getLoginTime());
            // 客户端信息
            logLogin.setIp(event.getClientIp());
            logLogin.setOs(event.getOs());
            logLogin.setBrowser(event.getBrowser());
            logLogin.setPlatform(event.getPlatform());
            logLogin.setTraceId(event.getTraceId());

            // ========== 计算 SM3 哈希链 ==========
            // 认证日志强制使用哈希链，保证登录操作可追溯和防篡改
            String hash = computeLoginHash(logLogin);
            logLogin.setHash(hash);
            logLogin.setPrevHash(getLastLoginHash());

            // ========== 保存到数据库 ==========
            Db.save(logLogin);

        } catch (Exception e) {
            // 保存失败记录错误日志，不影响业务
            log.error("保存认证日志失败", e);
        }
    }

    // ==================== 哈希链计算 ====================

    /**
     * 获取上一条认证日志的哈希值
     * <p>
     * 用于构建哈希链，将上一条记录的哈希值存入当前记录的 prevHash 字段。
     *
     * @return 上一条记录的哈希值，无记录则返回 null
     */
    private String getLastLoginHash() {
        try {
            // 查询最新一条认证日志的哈希值
            LogLogin last = Db.lambdaQuery(LogLogin.class)
                    .orderByDesc(LogLogin::getId)                 // 按 ID 降序
                    .select(LogLogin::getHash)                     // 只查询 hash 字段
                    .last("LIMIT 1").one();                        // 取最新一条
            return last != null ? last.getHash() : null;
        } catch (Exception e) {
            // 查询失败返回 null，第一条记录没有 prevHash
            return null;
        }
    }

    /**
     * 获取上一条操作日志的哈希值
     * <p>
     * 用于构建哈希链，将上一条记录的哈希值存入当前记录的 prevHash 字段。
     * 仅 AUDIT 类型日志需要计算哈希链。
     *
     * @return 上一条记录的哈希值，无记录则返回 null
     */
    private String getLastOperationHash() {
        try {
            // 查询最新一条操作日志的哈希值
            LogOperation last = Db.lambdaQuery(LogOperation.class)
                    .orderByDesc(LogOperation::getId)              // 按 ID 降序
                    .select(LogOperation::getHash)                // 只查询 hash 字段
                    .last("LIMIT 1").one();                        // 取最新一条
            return last != null ? last.getHash() : null;
        } catch (Exception e) {
            // 查询失败返回 null，第一条记录没有 prevHash
            return null;
        }
    }

    /**
     * 计算认证日志 SM3 哈希值
     * <p>
     * 哈希数据包含：
     * <ul>
     *     <li>哈希种子（HASH_SEED）</li>
     *     <li>用户 ID</li>
     *     <li>用户名</li>
     *     <li>认证类型</li>
     *     <li>认证状态</li>
     *     <li>消息</li>
     *     <li>IP</li>
     *     <li>traceId</li>
     *     <li>认证时间</li>
     *     <li>上一条记录的哈希值（形成链式结构）</li>
     * </ul>
     *
     * @param logLogin 认证日志实体
     * @return SM3 哈希值（64位十六进制字符串）
     */
    private String computeLoginHash(LogLogin logLogin) {
        // 使用 | 分隔符拼接各项数据
        String data = String.join("|",
                HASH_SEED,                                              // 哈希种子
                String.valueOf(logLogin.getUserId()),                  // 用户 ID
                logLogin.getUsername() != null ? logLogin.getUsername() : "",  // 用户名
                String.valueOf(logLogin.getAuthType()),               // 认证类型
                String.valueOf(logLogin.getStatus()),                 // 认证状态
                logLogin.getMessage() != null ? logLogin.getMessage() : "",    // 消息
                logLogin.getIp() != null ? logLogin.getIp() : "",      // IP
                logLogin.getTraceId() != null ? logLogin.getTraceId() : "",    // traceId
                String.valueOf(logLogin.getLoginTime() != null ? logLogin.getLoginTime().toString() : ""),  // 认证时间
                getLastLoginHash() != null ? getLastLoginHash() : ""   // 上一条哈希
        );
        // 使用国密 SM3 算法计算哈希
        return SmUtil.sm3(data);
    }

    /**
     * 计算操作日志 SM3 哈希值（用于 AUDIT 类型）
     * <p>
     * 哈希数据包含：
     * <ul>
     *     <li>哈希种子（HASH_SEED）</li>
     *     <li>用户 ID</li>
     *     <li>用户名</li>
     *     <li>三员类型</li>
     *     <li>操作模块</li>
     *     <li>操作方法</li>
     *     <li>操作类型</li>
     *     <li>状态</li>
     *     <li>消息</li>
     *     <li>消耗时间</li>
     *     <li>traceId</li>
     *     <li>操作时间</li>
     *     <li>上一条记录的哈希值（形成链式结构）</li>
     * </ul>
     *
     * @param logOperation 操作日志实体
     * @return SM3 哈希值（64位十六进制字符串）
     */
    private String computeOperationHash(LogOperation logOperation) {
        // 使用 | 分隔符拼接各项数据
        String data = String.join("|",
                HASH_SEED,                                              // 哈希种子
                String.valueOf(logOperation.getUserId()),             // 用户 ID
                logOperation.getUsername() != null ? logOperation.getUsername() : "",  // 用户名
                String.valueOf(logOperation.getAdminType()),          // 三员类型
                logOperation.getOperationModule() != null ? logOperation.getOperationModule() : "",  // 操作模块
                logOperation.getOperationMethod() != null ? logOperation.getOperationMethod() : "",  // 操作方法
                String.valueOf(logOperation.getOperationType()),      // 操作类型
                String.valueOf(logOperation.getStatus()),            // 状态
                logOperation.getMessage() != null ? logOperation.getMessage() : "",  // 消息
                String.valueOf(logOperation.getCostTime()),           // 消耗时间
                logOperation.getTraceId() != null ? logOperation.getTraceId() : "",  // traceId
                String.valueOf(logOperation.getOperationTime() != null ? logOperation.getOperationTime().toString() : ""),  // 操作时间
                getLastOperationHash() != null ? getLastOperationHash() : ""  // 上一条哈希
        );
        // 使用国密 SM3 算法计算哈希
        return SmUtil.sm3(data);
    }
}