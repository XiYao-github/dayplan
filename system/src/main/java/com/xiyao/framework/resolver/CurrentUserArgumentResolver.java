package com.xiyao.framework.resolver;

import com.xiyao.framework.annotation.CurrentUser;
import com.xiyao.security.details.LoginUser;
import com.xiyao.security.utils.SecurityUtils;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 当前登录用户参数解析器
 * <p>
 * 实现 HandlerMethodArgumentResolver 接口，
 * 将 @CurrentUser 注解的参数自动解析为当前登录用户。
 *
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *     <li>判断参数是否标注了 @CurrentUser 注解</li>
 *     <li>判断参数类型是否为 LoginUser 或其子类</li>
 *     <li>满足条件则从 SecurityUtils 获取当前用户并注入</li>
 * </ol>
 *
 * @author xiyao
 * @see CurrentUser
 * @see com.xiyao.security.utils.SecurityUtils
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否支持此参数解析
     * <p>
     * 必须满足：
     * <ul>
     *     <li>参数标注了 @CurrentUser 注解</li>
     *     <li>参数类型可赋值给 LoginUser</li>
     * </ul>
     *
     * @param parameter 方法参数
     * @return true 支持解析，false 不支持
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().isAssignableFrom(LoginUser.class);
    }

    /**
     * 解析参数值
     * <p>
     * 从 SecurityContext 获取当前登录用户并返回。
     *
     * @param parameter      方法参数
     * @param mavContainer   模型视图容器
     * @param webRequest    Web 请求
     * @param binderFactory  数据绑定工厂
     * @return 当前登录用户，未登录返回 null
     */
    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) {
        return SecurityUtils.getLoginUser();
    }
}