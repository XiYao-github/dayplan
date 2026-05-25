package com.xiyao.trace.aspect;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xiyao.trace.annotation.Trace;
import com.xiyao.trace.context.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 链路追踪切面
 * <p>
 * 配合 @Trace 注解使用，自动记录方法调用的链路信息。
 *
 * <p>
 * <b>记录内容：</b>
 * <ul>
 *     <li>traceId：追踪链 ID</li>
 *     <li>spanId：当前操作的 ID</li>
 *     <li>module：操作模块</li>
 *     <li>operation：操作描述</li>
 *     <li>方法参数、返回值、耗时、状态</li>
 * </ul>
 *
 * @author xiyao
 * @see Trace
 */
@Slf4j
@Aspect
@Component
public class TraceAspect {

    /**
     * 环绕通知：拦截带 @Trace 注解的方法
     *
     * @param point  切点
     * @param trace 注解
     * @return 方法返回值
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(trace)")
    public Object around(ProceedingJoinPoint point, Trace trace) throws Throwable {
        // 获取追踪上下文
        TraceContext context = TraceContext.get();
        String traceId = context != null ? context.getTraceId() : "unknown";
        String spanId = TraceContext.generateSpanId();

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 获取方法信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        // 构建操作描述
        String operationDesc = buildOperationDesc(trace, className, methodName);

        // 记录方法进入
        log.info("▶ {} - traceId={}, spanId={}", operationDesc, traceId, spanId);

        // 记录参数
        if (trace.isLogParams()) {
            String params = getParams(point);
            if (StrUtil.isNotBlank(params)) {
                log.debug("  参数: {}", params);
            }
        }

        Object result = null;
        boolean success = true;
        String errorMsg = null;

        try {
            // 执行目标方法
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            success = false;
            errorMsg = StrUtil.sub(e.getMessage(), 0, 200);
            throw e;
        } finally {
            // 计算耗时
            long costTime = System.currentTimeMillis() - startTime;

            // 记录方法返回
            if (success) {
                log.info("◀ {} - traceId={}, spanId={},耗时={}ms",
                        operationDesc, traceId, spanId, costTime);
            } else {
                log.warn("✗ {} - traceId={}, spanId={},耗时={}ms,错误={}",
                        operationDesc, traceId, spanId, costTime, errorMsg);
            }

            // 记录返回值（如果启用）
            if (trace.isLogResult() && result != null) {
                try {
                    String resultStr = JSONUtil.toJsonStr(result);
                    if (resultStr.length() > 500) {
                        resultStr = resultStr.substring(0, 500) + "...";
                    }
                    log.debug("  返回: {}", resultStr);
                } catch (Exception e) {
                    log.debug("  返回: [序列化失败]");
                }
            }
        }
    }

    /**
     * 构建操作描述
     */
    private String buildOperationDesc(Trace trace, String className, String methodName) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(trace.module())) {
            sb.append("[").append(trace.module()).append("] ");
        }
        if (StrUtil.isNotBlank(trace.operation())) {
            sb.append(trace.operation());
        } else {
            sb.append(className).append(".").append(methodName);
        }
        return sb.toString();
    }

    /**
     * 获取方法参数
     */
    private String getParams(ProceedingJoinPoint point) {
        try {
            Object[] args = point.getArgs();
            if (args == null || args.length == 0) {
                return "";
            }

            MethodSignature signature = (MethodSignature) point.getSignature();
            String[] paramNames = signature.getParameterNames();

            Map<String, Object> params = new LinkedHashMap<>();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                // 跳过 HttpServletRequest/Response 等框架对象
                if (arg == null) {
                    continue;
                }
                String className = arg.getClass().getName();
                if (className.startsWith("jakarta.servlet") ||
                        className.startsWith("org.springframework")) {
                    continue;
                }
                String name = (paramNames != null && i < paramNames.length)
                        ? paramNames[i] : "arg" + i;
                params.put(name, arg);
            }

            return JSONUtil.toJsonStr(params);
        } catch (Exception e) {
            return "获取参数失败";
        }
    }
}