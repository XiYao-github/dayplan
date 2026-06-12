package com.xiyao.log.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.xiyao.framework.utils.SpringUtils;
import com.xiyao.log.annotation.Log;
import com.xiyao.log.enums.OperationStatus;
import com.xiyao.log.event.LogOperationEvent;
import com.xiyao.security.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 日志切面
 * <p>
 * 拦截标注 @Log 注解的方法，自动记录操作日志。
 *
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *     <li>环绕通知：拦截方法执行，统计耗时</li>
 *     <li>用户信息：自动从 SecurityUtils 获取当前用户</li>
 *     <li>请求信息：继承 MyBaseEvent 自动获取</li>
 *     <li>事件发布：异步发布 LogOperationEvent</li>
 * </ul>
 *
 * @author xiyao
 * @see Log
 * @see LogOperationEvent
 * @see com.xiyao.log.listener.LogListener
 */
@Slf4j
@Aspect
public class LogAspect {

    /**
     * 环绕通知：拦截并记录日志
     * <p>
     * 处理流程：
     * <ol>
     *     <li>记录开始时间</li>
     *     <li>构建日志事件（继承 MyBaseEvent 自动获取请求信息）</li>
     *     <li>执行目标方法</li>
     *     <li>成功：设置成功状态和响应结果</li>
     *     <li>失败：设置失败状态和异常信息</li>
     *     <li>finally：计算耗时、发布时间</li>
     * </ol>
     *
     * @param point 连接点（被拦截的方法）
     * @param log   日志注解实例
     * @return 方法执行结果
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("@annotation(log)")
    public Object around(ProceedingJoinPoint point, Log log) throws Throwable {
        // 记录方法开始时间
        long startTime = System.currentTimeMillis();

        // 构建日志事件（继承 MyBaseEvent，构造函数自动获取请求信息）
        LogOperationEvent event = buildEvent(point, log);

        try {
            // ========== 执行目标方法 ==========
            Object result = point.proceed();

            // ========== 处理成功结果 ==========
            handleSuccess(event, result, log.isSaveResponseData());
            return result;

        } catch (Throwable e) {
            // ========== 处理失败结果 ==========
            handleFail(event, e);
            throw e;

        } finally {
            // ========== 计算耗时并发布时间 ==========
            // 设置方法执行耗时
            event.setCost(System.currentTimeMillis() - startTime);
            // 设置操作时间
            event.setTime(LocalDateTime.now());
            // 异步发布事件，由 LogListener 处理保存逻辑
            SpringUtils.publishEvent(event);
        }
    }

    /**
     * 构建日志事件
     * <p>
     * 收集日志所需的各项信息，包括用户信息、请求信息、操作信息等。
     *
     * @param point   连接点
     * @param log     日志注解
     * @return 构建好的日志事件
     */
    private LogOperationEvent buildEvent(ProceedingJoinPoint point, Log log) {
        // 创建事件对象
        LogOperationEvent event = new LogOperationEvent();

        // ========== 用户信息 ==========
        event.setUserId(SecurityUtils.getUserId());
        event.setUsername(SecurityUtils.getUsername());

        // ========== 操作信息 ==========
        // 操作模块 - 使用注解中定义的值（如"用户管理"、"订单管理"）
        event.setModule(log.module());
        // 操作路径 - 记录具体方法位置（全类名.方法名）
        String className = point.getTarget().getClass().getName();
        String methodName = point.getSignature().getName();
        event.setPath(className + "." + methodName);
        event.setType(log.type().ordinal());

        // ========== 请求参数 ==========
        if (log.isSaveRequestData()) {
            event.setParam(getRequestParams(point));
        }

        return event;
    }

    /**
     * 处理成功结果
     * <p>
     * 设置成功状态，根据配置保存响应结果。
     *
     * @param event             日志事件
     * @param result            方法返回值
     * @param saveResponseData  是否保存响应数据
     */
    private void handleSuccess(LogOperationEvent event, Object result, boolean saveResponseData) {
        // 设置成功状态
        event.setStatus(OperationStatus.SUCCESS.ordinal());
        event.setMessage("操作成功");

        // 根据配置决定是否保存响应数据
        if (saveResponseData && ObjectUtil.isNotNull(result)) {
            // 将返回值序列化为 JSON 字符串
            event.setResult(JSONUtil.toJsonStr(result));
        }
    }

    /**
     * 处理失败结果
     * <p>
     * 设置失败状态，保存异常信息。
     *
     * @param event 日志事件
     * @param e     异常
     */
    private void handleFail(LogOperationEvent event, Throwable e) {
        // 设置失败状态
        event.setStatus(OperationStatus.FAIL.ordinal());
        // 保存异常消息
        event.setMessage(e.getMessage());
    }

    /**
     * 获取请求参数
     * <p>
     * 从方法参数中提取参数名和参数值，排除框架对象，序列化为 JSON。
     *
     * @param point 连接点
     * @return 请求参数字符串（JSON 格式）
     */
    private String getRequestParams(ProceedingJoinPoint point) {
        try {
            // 获取方法参数值数组
            Object[] args = point.getArgs();
            if (ArrayUtil.isEmpty(args)) {
                return "";
            }

            // 获取方法签名（包含参数名）
            MethodSignature signature = (MethodSignature) point.getSignature();
            String[] parameterNames = signature.getParameterNames();

            // 构建参数 Map
            Map<String, Object> params = new LinkedHashMap<>();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                // 跳过框架对象（Request、Response、Session 等）
                if (isExcludedObject(arg)) {
                    continue;
                }
                // 获取参数名
                String paramName;
                if (ObjectUtil.isNotNull(parameterNames) && i < parameterNames.length) {
                    paramName = parameterNames[i];
                } else {
                    paramName = "arg" + i;
                }
                params.put(paramName, arg);
            }

            // 序列化为 JSON 字符串
            return JSONUtil.toJsonStr(params);

        } catch (Exception e) {
            // 获取参数失败不影响业务，记录错误并返回空字符串
            log.error("获取请求参数失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 判断对象是否需要排除
     * <p>
     * 框架对象、输出流、文件对象等不应被记录到日志中。
     *
     * @param obj 待检查的对象
     * @return true 需要排除，false 需要记录
     */
    private boolean isExcludedObject(Object obj) {
        if (ObjectUtil.isNull(obj)) {
            return true;
        }

        // ========== 框架对象排除 ==========
        if (obj instanceof HttpServletRequest ||
                obj instanceof HttpServletResponse ||
                obj instanceof HttpSession ||
                obj instanceof Model ||               // Spring Model
                obj instanceof ModelMap ||            // Spring ModelMap
                obj instanceof Principal ||           // Spring Security
                obj instanceof BindingResult           // Spring Validation
        ) {
            return true;
        }

        // ========== 输出流排除 ==========
        if (obj instanceof OutputStream || obj instanceof PrintWriter) {
            return true;
        }

        // ========== 文件对象排除 ==========
        if (obj instanceof MultipartFile) {
            return true;
        }
        // 文件数组类型
        Class<?> clazz = obj.getClass();
        if (clazz.isArray() && MultipartFile.class.isAssignableFrom(clazz.getComponentType())) {
            return true;
        }

        // ========== 集合中的文件对象排除 ==========
        if (obj instanceof Collection) {
            return containsMultipartFile((Collection<?>) obj);
        }
        if (obj instanceof Map) {
            return containsMultipartFile(((Map<?, ?>) obj).values());
        }

        return false;
    }

    /**
     * 检查集合中是否包含文件对象
     *
     * @param collection 集合
     * @return true 包含文件对象，false 不包含
     */
    private boolean containsMultipartFile(Collection<?> collection) {
        for (Object item : collection) {
            if (item instanceof MultipartFile) {
                return true;
            }
        }
        return false;
    }
}