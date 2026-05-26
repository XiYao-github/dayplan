package com.xiyao.log.aspect;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.xiyao.log.annotation.Log;
import com.xiyao.log.enums.OperationStatus;
import com.xiyao.log.event.LogOperationEvent;
import com.xiyao.log.filter.TraceFilter;
import com.xiyao.security.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 日志切面
 * <p>
 * 拦截标注 @Log 注解的方法，根据 logType 自动分流：
 * <ul>
 *     <li>OPERATION：操作日志，简单存储</li>
 *     <li>AUDIT：审计日志，需要哈希链防篡改</li>
 * </ul>
 *
 * @author xiyao
 * @see Log
 */
@Slf4j
@Aspect
public class LogAspect {

    /**
     * 环绕通知：拦截并记录日志
     */
    @Around("@annotation(log)")
    public Object around(ProceedingJoinPoint point, Log log) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取 traceId
        String traceId = MDC.get(TraceFilter.TRACE_ID_KEY);

        // 构建事件（继承 MyBaseEvent，自动获取请求信息）
        LogOperationEvent event = buildEvent(point, log, traceId);

        try {
            // 执行目标方法
            Object result = point.proceed();

            // 处理成功
            handleSuccess(event, result, log.isSaveResponseData());
            return result;
        } catch (Throwable e) {
            // 处理失败
            handleFail(event, e);
            throw e;
        } finally {
            // 计算耗时
            event.setCostTime(System.currentTimeMillis() - startTime);
            event.setTime(LocalDateTime.now());

            // 异步发布事件
            SpringUtil.publishEvent(event);
        }
    }

    /**
     * 构建日志事件
     * <p>
     * LogOperationEvent 继承 MyBaseEvent，构造函数自动获取请求信息。
     */
    private LogOperationEvent buildEvent(ProceedingJoinPoint point, Log log, String traceId) {
        LogOperationEvent event = new LogOperationEvent();

        event.setUserId(SecurityUtils.getUserId());
        event.setUsername(SecurityUtils.getUsername());
        event.setAdminType(SecurityUtils.getAdminType());

        event.setModule(log.module());
        event.setType(log.type().ordinal());
        event.setLogType(log.logType().ordinal());
        event.setTraceId(traceId);

        // 方法名：类名.方法名
        String className = point.getTarget().getClass().getSimpleName();
        String methodName = point.getSignature().getName();
        event.setMethod(className + "." + methodName);

        // 请求参数
        if (log.isSaveRequestData()) {
            event.setRequestParam(getRequestParams(point));
        }

        return event;
    }

    /**
     * 处理成功结果
     */
    private void handleSuccess(LogOperationEvent event, Object result, boolean saveResponseData) {
        event.setStatus(OperationStatus.SUCCESS.ordinal());
        event.setMessage("操作成功");
        if (saveResponseData && ObjectUtil.isNotNull(result)) {
            event.setReturnResult(JSONUtil.toJsonStr(result));
        }
    }

    /**
     * 处理失败结果
     */
    private void handleFail(LogOperationEvent event, Throwable e) {
        event.setStatus(OperationStatus.FAIL.ordinal());
        event.setMessage(e.getMessage());
    }

    /**
     * 获取请求参数（排除框架对象）
     */
    private String getRequestParams(ProceedingJoinPoint point) {
        try {
            Object[] args = point.getArgs();
            if (args == null || args.length == 0) {
                return "";
            }

            Map<String, Object> params = new LinkedHashMap<>();
            String[] paramNames = ((MethodSignature) point.getSignature()).getParameterNames();

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null || isExcludedObject(arg)) {
                    continue;
                }
                String name = (paramNames != null && i < paramNames.length) ? paramNames[i] : "arg" + i;
                params.put(name, arg);
            }
            return JSONUtil.toJsonStr(params);
        } catch (Exception e) {
            log.error("获取请求参数失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 判断是否需要排除
     */
    private boolean isExcludedObject(Object obj) {
        if (obj == null) {
            return true;
        }
        Class<?> clazz = obj.getClass();
        // 框架对象
        if (obj instanceof jakarta.servlet.http.HttpServletRequest ||
            obj instanceof jakarta.servlet.http.HttpServletResponse) {
            return true;
        }
        // 文件对象
        if (obj instanceof MultipartFile) {
            return true;
        }
        if (clazz.isArray() && MultipartFile.class.isAssignableFrom(clazz.getComponentType())) {
            return true;
        }
        // Spring 校验结果
        if (obj instanceof BindingResult) {
            return true;
        }
        // 集合中的文件对象
        if (obj instanceof Collection) {
            return containsMultipartFile((Collection<?>) obj);
        }
        if (obj instanceof Map) {
            return containsMultipartFile(((Map<?, ?>) obj).values());
        }
        return false;
    }

    private boolean containsMultipartFile(Collection<?> collection) {
        for (Object item : collection) {
            if (item instanceof MultipartFile) {
                return true;
            }
        }
        return false;
    }
}