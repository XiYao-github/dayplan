package com.xiyao.log.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.xiyao.log.annotation.Log;
import com.xiyao.log.enums.OperationStatus;
import com.xiyao.log.event.LogOperationEvent;
import com.xiyao.security.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * 操作日志切面
 * <p>
 * 功能说明：
 * <ul>
 *     <li>拦截标注 @Log 注解的方法，自动记录操作日志</li>
 *     <li>记录内容包括：操作用户、操作模块、操作类型、请求参数、返回结果、耗时等</li>
 *     <li>日志保存采用异步方式，不影响主业务性能</li>
 * </ul>
 * </p>
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * 环绕通知：拦截并记录操作日志
     *
     * @param point 切点，包含目标方法的信息
     * @param log   日志注解，包含模块、操作类型等配置
     * @return 目标方法的执行结果
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("@annotation(log)")
    public Object around(ProceedingJoinPoint point, Log log) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 构建日志事件对象（基础信息）
        LogOperationEvent event = buildBaseEvent(point, log);
        Object result = null;
        boolean success = true;
        String errorMessage = null;

        try {
            // 执行目标方法
            result = point.proceed();
            return result;

        } catch (Throwable e) {
            // 记录异常信息
            success = false;
            errorMessage = e.getMessage();
            throw e;

        } finally {
            // 统一处理日志记录
            handleLogResult(event, result, success, errorMessage);
            event.setCostTime(System.currentTimeMillis() - startTime);
            event.setTime(LocalDateTime.now());

            // 发布事件，异步保存到数据库
            SpringUtil.publishEvent(event);
        }
    }

    /**
     * 构建基础事件对象
     */
    private LogOperationEvent buildBaseEvent(ProceedingJoinPoint point, Log log) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        LogOperationEvent event = new LogOperationEvent();

        // 用户信息
        event.setUserId(SecurityUtils.getUserId());
        event.setUsername(SecurityUtils.getUsername());

        // 操作信息
        event.setModule(log.module());
        event.setType(log.operationType().ordinal());
        event.setMethod(method.getDeclaringClass().getSimpleName() + "." + method.getName());

        // 请求参数（根据配置）
        if (log.isSaveRequestData()) {
            event.setRequestParam(getRequestParams(point));
        }

        return event;
    }

    /**
     * 处理日志结果（成功/失败）
     */
    private void handleLogResult(LogOperationEvent event, Object result, boolean success, String errorMessage) {
        if (success) {
            event.setStatus(OperationStatus.SUCCESS.ordinal());
            event.setMessage("操作成功");

            // 保存响应结果（根据配置）
            if (result != null) {
                event.setReturnResult(JSONUtil.toJsonStr(result));
            }
        } else {
            event.setStatus(OperationStatus.FAIL.ordinal());
            event.setMessage(StrUtil.sub(errorMessage, 0, 500));
        }
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(ProceedingJoinPoint point) {
        try {
            Object[] args = point.getArgs();
            if (args == null || args.length == 0) {
                return "";
            }

            // 过滤掉框架对象和文件对象
            Object[] businessArgs = new Object[args.length];
            int index = 0;
            for (Object arg : args) {
                if (arg != null && !isExcludedObject(arg)) {
                    businessArgs[index++] = arg;
                }
            }

            return JSONUtil.toJsonStr(businessArgs);

        } catch (Exception e) {
            log.warn("获取请求参数失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 判断是否为需要排除的对象
     * <p>
     * 排除以下类型：
     * <ul>
     *     <li>HttpServletRequest / HttpServletResponse - 框架对象，无法序列化</li>
     *     <li>MultipartFile - 文件对象，序列化会很大且无意义</li>
     *     <li>BindingResult - Spring 校验结果，不需要记录</li>
     * </ul>
     * </p>
     */
    private boolean isExcludedObject(Object obj) {
        if (obj == null) {
            return false;
        }

        Class<?> clazz = obj.getClass();

        // 直接判断常见类型
        if (obj instanceof HttpServletRequest ||
                obj instanceof HttpServletResponse ||
                obj instanceof MultipartFile) {
            return true;
        }

        // 判断是否为 MultipartFile 数组或集合
        if (clazz.isArray()) {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        }

        if (obj instanceof Collection) {
            Collection<?> collection = (Collection<?>) obj;
            if (!collection.isEmpty()) {
                Object first = collection.iterator().next();
                return first instanceof MultipartFile;
            }
        }

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            if (!map.isEmpty()) {
                Object firstValue = map.values().iterator().next();
                return firstValue instanceof MultipartFile;
            }
        }

        return false;
    }
}