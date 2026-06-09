package com.xiyao.log.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SmUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.log.event.LogLoginEvent;
import com.xiyao.log.event.LogOperationEvent;
import com.xiyao.system.entity.LogLogin;
import com.xiyao.system.entity.LogOperation;
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
 *     <li>LogOperationEvent：操作日志事件</li>
 *     <li>LogLoginEvent：认证日志事件（登录/登出/注册）</li>
 * </ul>
 *
 * <p>
 * <b>存储策略：</b>
 * <ul>
 *     <li>操作日志：存入 log_operation 表（带 SM3 哈希链）</li>
 *     <li>认证日志：存入 log_login 表（带 SM3 哈希链）</li>
 * </ul>
 *
 * <p>
 * <b>哈希链说明：</b>
 * 操作日志和认证日志都使用 SM3 哈希链防篡改。
 * 哈希算法：hash = SM3(上条记录id + "|" + 上条记录hash)
 * 任意一条日志被篡改会导致后续所有日志哈希验证失败。
 *
 * @author xiyao
 * @see LogOperationEvent
 * @see LogLoginEvent
 */
@Slf4j
public class LogListener {

    /**
     * 哈希分隔符
     */
    private static final String HASH_SEPARATOR = "|";

    /**
     * 监听操作日志事件并保存
     * <p>
     * 操作日志记录业务操作，带 SM3 哈希链防篡改。
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
            // ========== 获取上一条记录的信息 ==========
            LogOperation lastLog = getLastOperation();

            // ========== 构建日志实体 ==========
            LogOperation logOperation = new LogOperation();
            // 用户信息
            logOperation.setUserId(event.getUserId());
            logOperation.setUsername(event.getUsername());
            // 操作信息
            logOperation.setModule(event.getModule());
            logOperation.setType(event.getType());
            logOperation.setStatus(event.getStatus());
            logOperation.setTime(event.getTime());
            logOperation.setMessage(event.getMessage());
            // 请求响应信息
            logOperation.setMethod(event.getRequestMethod());
            logOperation.setUrl(event.getRequestUrl());
            logOperation.setParam(event.getParam());
            logOperation.setResult(event.getResult());
            logOperation.setCost(event.getCost());
            // 客户端信息
            logOperation.setIp(event.getClientIp());
            logOperation.setOs(event.getOs());
            logOperation.setBrowser(event.getBrowser());
            logOperation.setPlatform(event.getPlatform());
            logOperation.setTraceId(event.getTraceId());

            // ========== 保存到数据库 ==========
            Db.save(logOperation);

            // ========== 计算哈希链 ==========
            // hash = SM3(上条记录id + "|" + 上条记录hash)
            if (ObjectUtil.isNotNull(lastLog)) {
                String hash = SmUtil.sm3(lastLog.getId() + HASH_SEPARATOR + lastLog.getHash());
                logOperation.setPrevHash(lastLog.getHash());
                logOperation.setHash(hash);
                Db.updateById(logOperation);
            } else {
                // 第一条记录，hash 为空
                logOperation.setPrevHash(null);
                logOperation.setHash(null);
                Db.updateById(logOperation);
            }

        } catch (Exception e) {
            // 保存失败记录错误日志，不影响业务
            log.error("保存操作日志失败", e);
        }
    }


    /**
     * 监听认证日志事件并保存
     * <p>
     * 认证日志（登录/登出/注册）使用 SM3 哈希链防篡改。
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
            // ========== 获取上一条记录的信息 ==========
            LogLogin lastLog = getLastLogin();

            // ========== 构建日志实体 ==========
            LogLogin logLogin = new LogLogin();
            // 用户信息
            logLogin.setUserId(event.getUserId());
            logLogin.setUsername(event.getUsername());
            // 认证信息
            logLogin.setType(event.getType());
            logLogin.setStatus(event.getStatus());
            logLogin.setMessage(event.getMessage());
            logLogin.setTime(event.getTime());
            // 客户端信息
            logLogin.setIp(event.getClientIp());
            logLogin.setOs(event.getOs());
            logLogin.setBrowser(event.getBrowser());
            logLogin.setPlatform(event.getPlatform());
            logLogin.setTraceId(event.getTraceId());

            // ========== 保存到数据库 ==========
            Db.save(logLogin);

            // ========== 计算哈希链 ==========
            // hash = SM3(上条记录id + "|" + 上条记录hash)
            if (ObjectUtil.isNotNull(lastLog)) {
                String hash = SmUtil.sm3(lastLog.getId() + HASH_SEPARATOR + lastLog.getHash());
                logLogin.setPrevHash(lastLog.getHash());
                logLogin.setHash(hash);
                Db.updateById(logLogin);
            } else {
                // 第一条记录，hash 为空
                logLogin.setPrevHash(null);
                logLogin.setHash(null);
                Db.updateById(logLogin);
            }

        } catch (Exception e) {
            // 保存失败记录错误日志，不影响业务
            log.error("保存认证日志失败", e);
        }
    }

    // ==================== 哈希链计算 ====================

    /**
     * 获取上一条认证日志
     *
     * @return 上一条认证日志，无则返回 null
     */
    private LogLogin getLastLogin() {
        try {
            return Db.lambdaQuery(LogLogin.class)
                    .orderByDesc(LogLogin::getId)
                    .last("LIMIT 1").one();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取上一条操作日志
     *
     * @return 上一条操作日志，无则返回 null
     */
    private LogOperation getLastOperation() {
        try {
            return Db.lambdaQuery(LogOperation.class)
                    .orderByDesc(LogOperation::getId)
                    .last("LIMIT 1").one();
        } catch (Exception e) {
            return null;
        }
    }
}