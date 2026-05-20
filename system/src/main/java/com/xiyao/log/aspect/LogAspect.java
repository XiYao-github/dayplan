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
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
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
     */
    @Around("@annotation(log)")
    public Object around(ProceedingJoinPoint point, Log log) throws Throwable {
        long startTime = System.currentTimeMillis();
        LogOperationEvent event = buildBaseEvent(point, log);

        try {
            Object result = point.proceed();
            handleSuccess(event, result, log.isSaveResponseData());
            return result;
        } catch (Throwable e) {
            handleFail(event, e);
            throw e;
        } finally {
            event.setCostTime(System.currentTimeMillis() - startTime);
            event.setTime(LocalDateTime.now());
            SpringUtil.publishEvent(event);
        }
    }

    /**
     * 构建基础事件
     */
    private LogOperationEvent buildBaseEvent(ProceedingJoinPoint point, Log log) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        LogOperationEvent event = new LogOperationEvent();
        event.setUserId(SecurityUtils.getUserId());
        event.setUsername(SecurityUtils.getUsername());
        event.setAdminType(SecurityUtils.getAdminType());
        event.setModule(log.module());
        event.setType(log.operationType().ordinal());
        event.setMethod(method.getDeclaringClass().getSimpleName() + "." + method.getName());

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
        if (saveResponseData && result != null) {
            event.setReturnResult(JSONUtil.toJsonStr(result));
        }
    }

    /**
     * 处理失败结果
     */
    private void handleFail(LogOperationEvent event, Throwable e) {
        event.setStatus(OperationStatus.FAIL.ordinal());
        event.setMessage(StrUtil.sub(e.getMessage(), 0, 500));
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

            // 使用 LinkedHashMap 保持参数顺序，便于排查问题
            Map<String, Object> params = new LinkedHashMap<>();
            String[] paramNames = ((MethodSignature) point.getSignature()).getParameterNames();

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null || isExcludedObject(arg)) {
                    continue;
                }
                // 参数名优先使用参数列表中的名称，否则用下标
                String name = (paramNames != null && i < paramNames.length) ? paramNames[i] : "arg" + i;
                params.put(name, arg);
            }

            return JSONUtil.toJsonStr(params);
        } catch (Exception e) {
            log.warn("获取请求参数失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 判断是否为需要排除的对象
     * <p>
     * 排除：框架对象、文件对象、校验结果对象
     * </p>
     */
    private boolean isExcludedObject(Object obj) {
        if (obj == null) {
            return true;
        }

        Class<?> clazz = obj.getClass();

        // 1. 框架对象（无法序列化）
        if (obj instanceof HttpServletRequest || obj instanceof HttpServletResponse) {
            return true;
        }

        // 2. 文件对象（太大且无意义）
        if (obj instanceof MultipartFile) {
            return true;
        }
        // MultipartFile 数组
        if (clazz.isArray() && MultipartFile.class.isAssignableFrom(clazz.getComponentType())) {
            return true;
        }

        // 3. Spring 校验结果
        if (obj instanceof BindingResult) {
            return true;
        }

        // 4. 集合/Map 中的文件对象
        if (obj instanceof Collection) {
            return containsMultipartFile((Collection<?>) obj);
        }
        if (obj instanceof Map) {
            return containsMultipartFile(((Map<?, ?>) obj).values());
        }

        return false;
    }

    /**
     * 判断集合/Collection 是否包含 MultipartFile
     */
    private boolean containsMultipartFile(Collection<?> collection) {
        if (collection.isEmpty()) {
            return false;
        }
        // 检查任意一个元素是否为 MultipartFile
        for (Object item : collection) {
            if (item instanceof MultipartFile) {
                return true;
            }
        }
        return false;
    }
}