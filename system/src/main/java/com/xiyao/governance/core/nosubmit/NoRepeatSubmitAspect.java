package com.xiyao.governance.core.nosubmit;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.xiyao.common.utils.RedisUtils;
import com.xiyao.framework.exception.BusinessException;
import com.xiyao.governance.annotation.NoRepeatSubmit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 防止重复提交切面
 * <p>
 * 配合 @NoRepeatSubmit 注解使用，
 * 通过 Redis 缓存实现请求去重，防止重复提交。
 *
 * <p>
 * <b>生成请求标识的规则：</b>
 * <pre>{@code
 * "nosubmit:" + userId + ":" + methodName + ":" + paramHash
 * }</pre>
 *
 * @author xiyao
 * @see NoRepeatSubmit
 */
@Slf4j
@Aspect
@Component
public class NoRepeatSubmitAspect {

    private  RedisUtils redisUtils;

    /** 缓存键前缀 */
    private static final String CACHE_KEY_PREFIX = "nosubmit:";

    /** SpEL 表达式解析器 */
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    /** 参数名发现器 */
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 环绕通知：拦截带 @NoRepeatSubmit 注解的方法
     *
     * @param point 切点
     * @param noRepeatSubmit 注解
     * @return 方法返回值
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint point, NoRepeatSubmit noRepeatSubmit) throws Throwable {
        // 构建请求标识
        String requestMark = buildRequestMark(point, noRepeatSubmit);

        // 检查 Redis 中是否存在
        if (redisUtils.get(requestMark) != null) {
            log.warn("检测到重复提交: {}", requestMark);
            throw new BusinessException(noRepeatSubmit.message());
        }

        // 存入 Redis，设置过期时间
        int expireSeconds = noRepeatSubmit.expireSeconds();
        redisUtils.set(requestMark, "1", expireSeconds);

        try {
            // 执行目标方法
            return point.proceed();
        } finally {
            // 可选：方法成功后删除缓存（如果需要更严格的去重，可保留到过期）
            // RedisUtils.delete(requestMark);
        }
    }

    /**
     * 构建请求唯一标识
     * <p>
     * 格式：nosubmit:{userId}:{methodName}:{paramHash}
     *
     * @param point            切点
     * @param noRepeatSubmit    注解
     * @return 请求标识
     */
    private String buildRequestMark(ProceedingJoinPoint point, NoRepeatSubmit noRepeatSubmit) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();

        // 获取用户标识（优先使用 SpEL 表达式从参数中获取 userId）
        String userId = resolveUserId(point, noRepeatSubmit);

        // 获取方法名
        String methodName = method.getDeclaringClass().getName() + "." + method.getName();

        // 计算参数 hash
        String paramHash = calculateParamHash(args);

        return CACHE_KEY_PREFIX + userId + ":" + methodName + ":" + paramHash;
    }

    /**
     * 解析用户 ID
     * <p>
     * 优先从注解的 SpEL 表达式中获取，
     * 如果注解未指定 key，则使用 "anonymous"。
     *
     * @param point            切点
     * @param noRepeatSubmit    注解
     * @return 用户标识
     */
    private String resolveUserId(ProceedingJoinPoint point, NoRepeatSubmit noRepeatSubmit) {
        String keyExpression = noRepeatSubmit.key();

        if (StrUtil.isBlank(keyExpression)) {
            return "anonymous";
        }

        try {
            // 解析 SpEL 表达式
            Expression expression = PARSER.parseExpression(keyExpression);
            EvaluationContext context = buildEvaluationContext(point);

            Object result = expression.getValue(context);
            return result != null ? result.toString() : "anonymous";
        } catch (Exception e) {
            log.warn("解析NoRepeatSubmit key表达式失败: {}, 使用默认值", keyExpression, e);
            return "anonymous";
        }
    }

    /**
     * 构建 SpEL 评估上下文
     *
     * @param point 切点
     * @return 评估上下文
     */
    private EvaluationContext buildEvaluationContext(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();
        String[] paramNames = NAME_DISCOVERER.getParameterNames(method);

        StandardEvaluationContext context = new StandardEvaluationContext();

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        return context;
    }

    /**
     * 计算参数哈希值
     * <p>
     * 对方法参数进行 SHA256 哈希，生成短字符串用于标识此次请求。
     * 参数为空时返回 "empty"。
     *
     * @param args 方法参数
     * @return 参数哈希
     */
    private String calculateParamHash(Object[] args) {
        if (args == null || args.length == 0) {
            return "empty";
        }

        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                sb.append(arg.toString());
            }
        }

        return DigestUtil.sha256Hex(sb.toString()).substring(0, 16);
    }
}