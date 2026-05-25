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

/**
 * 日志事件监听器
 * <p>
 * 负责监听并异步保存登录日志和操作日志。
 * <p>
 * <b>设计说明：</b>
 * <ul>
 *     <li>使用 Spring Event 机制实现日志的异步保存，避免阻塞主业务</li>
 *     <li>每个监听方法都使用 @Async("logExecutor") 注解，在独立的线程池中执行</li>
 *     <li>日志保存异常仅记录错误日志，不影响主业务逻辑</li>
 * </ul>
 *
 * <p>
 * <b>线程池配置：</b>
 * 使用自定义的 logExecutor 线程池，可在配置文件中调整线程池参数。
 *
 * @author xiyao
 * @see com.xiyao.log.event.LogLoginEvent
 * @see com.xiyao.log.event.LogOperationEvent
 */
@Slf4j
@Component
public class LogListener {

    /**
     * 监听登录日志事件并保存
     * <p>
     * 当用户登录时发布 LogLoginEvent 事件，此方法异步接收并保存登录日志。
     * <p>
     * <b>保存内容：</b>
     * <ul>
     *     <li>用户信息：userId、username</li>
     *     <li>登录状态：status、message</li>
     *     <li>客户端信息：ip、os、browser、platform</li>
     *     <li>登录时间：loginTime</li>
     * </ul>
     *
     * @param event 登录日志事件
     */
    @Async("logExecutor")
    @EventListener
    public void saveLoginLog(LogLoginEvent event) {
        try {
            // 构建登录日志实体
            LogLogin logLogin = new LogLogin();
            logLogin.setUserId(event.getUserId());
            logLogin.setUsername(event.getUsername());
            logLogin.setLoginTime(event.getLoginTime());
            logLogin.setStatus(event.getStatus());
            logLogin.setMessage(event.getMessage());

            // 设置客户端信息
            logLogin.setIp(event.getClientIp());
            logLogin.setOs(event.getOs());
            logLogin.setBrowser(event.getBrowser());
            logLogin.setPlatform(event.getPlatform());

            // 保存到数据库
            Db.save(logLogin);
        } catch (Exception e) {
            // 仅记录日志，不抛出异常，避免影响主业务
            log.error("保存登录日志失败", e);
        }
    }

    /**
     * 监听操作日志事件并保存
     * <p>
     * 当标注 @Log 注解的方法执行后发布 LogOperationEvent 事件，此方法异步接收并保存操作日志。
     * <p>
     * <b>保存内容：</b>
     * <ul>
     *     <li>用户信息：userId、username</li>
     *     <li>操作信息：module、method、type</li>
     *     <li>请求信息：requestMethod、requestUrl、requestParam</li>
     *     <li>响应信息：returnResult、status、message</li>
     *     <li>性能信息：costTime、operationTime</li>
     *     <li>客户端信息：ip、os、browser、platform</li>
     * </ul>
     *
     * @param event 操作日志事件
     */
    @Async("logExecutor")
    @EventListener
    public void saveOperationLog(LogOperationEvent event) {
        try {
            // 构建操作日志实体
            LogOperation logOperation = new LogOperation();
            logOperation.setUserId(event.getUserId());
            logOperation.setUsername(event.getUsername());
            logOperation.setOperationModule(event.getModule());
            logOperation.setOperationMethod(event.getMethod());
            logOperation.setOperationType(event.getType());
            logOperation.setOperationTime(event.getTime());
            logOperation.setStatus(event.getStatus());
            logOperation.setMessage(event.getMessage());

            // 设置请求信息
            logOperation.setRequestMethod(event.getRequestMethod());
            logOperation.setRequestUrl(event.getRequestUrl());
            logOperation.setRequestParam(event.getRequestParam());
            logOperation.setReturnResult(event.getReturnResult());
            logOperation.setCostTime(event.getCostTime());

            // 设置客户端信息
            logOperation.setIp(event.getClientIp());
            logOperation.setOs(event.getOs());
            logOperation.setBrowser(event.getBrowser());
            logOperation.setPlatform(event.getPlatform());

            // 保存到数据库
            Db.save(logOperation);
        } catch (Exception e) {
            // 仅记录日志，不抛出异常，避免影响主业务
            log.error("保存操作日志失败", e);
        }
    }
}