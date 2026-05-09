// package com.xiyao.framework.interceptor;
//
// import cn.hutool.core.exceptions.ValidateException;
// import cn.hutool.jwt.JWT;
// import cn.hutool.jwt.JWTUtil;
// import cn.hutool.jwt.JWTValidator;
// import cn.hutool.jwt.signers.JWTSignerUtil;
// import com.xiyao.tools.constant.Constant;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.apache.commons.lang3.StringUtils;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.stereotype.Component;
// import org.springframework.web.bind.annotation.RequestMethod;
// import org.springframework.web.method.HandlerMethod;
// import org.springframework.web.servlet.HandlerInterceptor;
//
//
// /**
//  * 权限(Token)验证
//  *
//  * @author steed
//  * @date 2020/8/7
//  */
// @Component
// public class AuthorizationInterceptor implements HandlerInterceptor {
//
//     private final JWTConfig jwtConfig;
//
//     @Autowired
//     public AuthorizationInterceptor(JWTConfig jwtConfig) {
//         this.jwtConfig = jwtConfig;
//     }
//
//     @Override
//     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//         if (request.getMethod().equals(RequestMethod.OPTIONS.name())) {
//             return true;
//         }
//         Login annotation;
//         if (handler instanceof HandlerMethod) {
//             annotation = ((HandlerMethod) handler).getMethodAnnotation(Login.class);
//         } else {
//             return true;
//         }
//         if (annotation == null) {
//             return true;
//         }
//         // 从header中获取accessToken
//         String accessToken = request.getHeader(Constant.USER_TOKEN);
//         // 如果header中不存在accessToken，则从参数中获取accessToken
//         if (StringUtils.isBlank(accessToken)) {
//             accessToken = request.getParameter(Constant.USER_TOKEN);
//         }
//         // token为空
//         if (StringUtils.isBlank(accessToken)) {
//             throw new MyBaseException("invalid " + Constant.USER_TOKEN, HttpStatus.UNAUTHORIZED.value());
//         }
//         JWTValidator jwtValidator;
//         try {
//             jwtValidator = JWTValidator.of(accessToken);
//         } catch (Exception e) {
//             throw new MyBaseException("invalid " + Constant.USER_TOKEN, HttpStatus.UNAUTHORIZED.value());
//         }
//         try {
//             jwtValidator.validateAlgorithm(JWTSignerUtil.hs256(jwtConfig.getSecret().getBytes()));
//         } catch (ValidateException e) {
//             throw new MyBaseException("invalid " + Constant.USER_TOKEN, HttpStatus.UNAUTHORIZED.value());
//         }
//         try {
//             jwtValidator.validateDate();
//         } catch (ValidateException e) {
//             throw new MyBaseException(Constant.USER_TOKEN + "失效，请重新登录", HttpStatus.UNAUTHORIZED.value());
//         }
//         JWT jwt = JWTUtil.parseToken(accessToken);
//         // 设置userId到request里，后续根据userId，获取用户信息
//         request.setAttribute(Constant.USER_KEY, jwt.getPayload("id"));
//         return true;
//     }
// }
