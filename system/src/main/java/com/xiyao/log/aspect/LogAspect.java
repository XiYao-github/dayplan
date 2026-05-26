package com.xiyao.log.aspect;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.xiyao.log.annotation.AuditLog;
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志切面
 * <p>
 * 功能：
 * <ul>
 *     <li>拦截标注 @AuditLog 注解的方法，自动记录操作日志</li>
 *     <li>记录内容：操作用户、模块、类型、请求参数、返回结果、耗时等</li>
 *     <li>日志保存采用异步方式（通过 Spring Event），不影响主业务性能</li>
 * </ul>
 *
 * <p>
 * <b>日志记录流程：</b>
 * <ol>
 *     <li>方法执行前：构建基础事件，记录请求参数</li>
 *     <li>方法执行后：记录返回结果或异常信息</li>
 *     <li>finally 块：记录耗时，发布事件（异步保存）</li>
 * </ol>
 *
 * @author xiyao
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * 环绕通知：拦截并记录操作日志
     * <p>
     * 在方法执行前后进行拦截，记录完整的操作日志。
     *
     * @param point 切点信息
     * @param auditLog   日志注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();  // 记录开始时间

        // 构建基础事件（用户、模块、方法等）
        LogOperationEvent event = buildBaseEvent(point, auditLog);

        try {
            // 执行目标方法
            Object result = point.proceed();

            // 记录成功日志
            handleSuccess(event, result, auditLog.isSaveResponseData());

            return result;
        } catch (Throwable e) {
            // 记录失败日志
            handleFail(event, e);
            throw e;
        } finally {
            // 计算耗时
            long endTime = System.currentTimeMillis();
            event.setCostTime(endTime - startTime);
            event.setTime(LocalDateTime.now());

            // 异步发布事件（由监听器异步保存日志）
            SpringUtil.publishEvent(event);
        }
    }

    /**
     * 处理成功结果
     *
     * @param event            日志事件
     * @param result           方法返回值
     * @param saveResponseData 是否保存响应数据
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
     *
     * @param event 日志事件
     * @param e      异常信息
     */
    private void handleFail(LogOperationEvent event, Throwable e) {
        event.setStatus(OperationStatus.FAIL.ordinal());
        event.setMessage(e.getMessage());
    }

    /**
     * 构建基础日志事件
     *
     * @param point 切点信息
     * @param auditLog   日志注解
     * @return 日志事件对象
     */
    private LogOperationEvent buildBaseEvent(ProceedingJoinPoint point, AuditLog auditLog) {
        LogOperationEvent event = new LogOperationEvent();

        // 设置操作用户信息
        event.setUserId(SecurityUtils.getUserId());
        event.setUsername(SecurityUtils.getUsername());
        event.setAdminType(SecurityUtils.getAdminType());
        event.setModule(auditLog.module());
        event.setType(auditLog.operationType().ordinal());
        // 设置方法名称
        String className = point.getTarget().getClass().getName();
        String methodName = point.getSignature().getName();
        event.setMethod(className + "." + methodName);
        // MethodSignature signature = (MethodSignature) point.getSignature();
        // Method method = signature.getMethod();
        // event.setMethod(method.getDeclaringClass().getSimpleName() + "." + method.getName());

        if (auditLog.isSaveRequestData()) {
            event.setRequestParam(getRequestParams(point));
        }

        return event;
    }

    /**
     * 获取请求参数
     * <p>
     * 将方法参数转换为 JSON 字符串，方便日志存储和查看。
     * 会排除 HttpServletRequest/Response、MultipartFile 等框架对象。
     *
     * @param point 切点信息
     * @return 请求参数的 JSON 字符串
     */
    private String getRequestParams(ProceedingJoinPoint point) {
        try {
            Object[] args = point.getArgs();
            if (args == null || args.length == 0) {
                return "";
            }

            // 使用 LinkedHashMap 保持参数顺序
            Map<String, Object> params = new LinkedHashMap<>();
            String[] paramNames = ((MethodSignature) point.getSignature()).getParameterNames();

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];

                // 跳过排除的对象
                if (arg == null || isExcludedObject(arg)) {
                    continue;
                }

                // 参数名：优先使用实际参数名，否则用 arg0、arg1...
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
     * 排除的对象类型：
     * <ul>
     *     <li>HttpServletRequest/Response：框架对象，无法序列化</li>
     *     <li>MultipartFile：文件对象，太大且无意义</li>
     *     <li>BindingResult：Spring 校验结果，无需记录</li>
     * </ul>
     *
     * @param obj 待检查的对象
     * @return true 需要排除，false 需要记录
     */
    private boolean isExcludedObject(Object obj) {
        if (obj == null) {
            return true;
        }

        Class<?> clazz = obj.getClass();

        // ========== 框架对象 ==========
        if (obj instanceof HttpServletRequest || obj instanceof HttpServletResponse) {
            return true;
        }

        // ========== 文件对象 ==========
        if (obj instanceof MultipartFile) {
            return true;
        }
        // MultipartFile 数组
        if (clazz.isArray() && MultipartFile.class.isAssignableFrom(clazz.getComponentType())) {
            return true;
        }

        // ========== Spring 校验结果 ==========
        if (obj instanceof BindingResult) {
            return true;
        }

        // ========== 集合中的文件对象 ==========
        if (obj instanceof Collection) {
            return containsMultipartFile((Collection<?>) obj);
        }
        if (obj instanceof Map) {
            return containsMultipartFile(((Map<?, ?>) obj).values());
        }

        return false;
    }

    /**
     * 判断集合中是否包含 MultipartFile
     *
     * @param collection 待检查的集合
     * @return true 包含文件对象，false 不包含
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