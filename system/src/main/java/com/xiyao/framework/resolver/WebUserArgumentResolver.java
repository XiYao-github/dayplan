package com.xiyao.framework.resolver;

import com.xiyao.framework.annotation.WebUser;
import com.xiyao.security.details.LoginUser;
import com.xiyao.security.utils.SecurityUtils;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 后台用户参数解析器
 */
public class WebUserArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(WebUser.class) && parameter.getParameterType().isAssignableFrom(LoginUser.class);
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        return SecurityUtils.getLoginUser();
    }

}