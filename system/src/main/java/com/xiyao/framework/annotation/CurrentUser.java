package com.xiyao.framework.annotation;

import java.lang.annotation.*;

/**
 * 当前登录用户注解
 * <p>
 * 标注在 Controller 方法参数上，自动注入当前登录用户信息。
 * 与 CurrentUserArgumentResolver 配合使用。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @GetMapping("/info")
 * public Result&lt;User&gt; getUserInfo(@CurrentUser LoginUser user) {
 *     // 直接获取当前登录用户，无需从 SecurityContext 手动获取
 *     return Result.ok(user);
 * }
 * }</pre>
 *
 * @author xiyao
 * @see com.xiyao.framework.resolver.CurrentUserArgumentResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}