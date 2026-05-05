package com.xiyao;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 安全模块集成测试
 *
 * 注意：所有接口的 HTTP 状态码均为 200，实际的业务状态码在响应 JSON 的 "code" 字段中：
 * - 200：成功
 * - 401：未认证（未登录或 token 无效）
 * - 403：已认证但权限不足
 * - 500：服务器内部错误
 */
public class SecurityIntegrationTest extends BaseSecurityTest {

    // ==================== 登录相关测试 ====================

    /**
     * 测试正常登录（admin / 123456）
     * 预期：HTTP 200，响应 JSON 的 code=200，且 data 字段包含有效的 JWT token
     */
    @Test
    public void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andDo(print())  // 打印请求/响应详情
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    /**
     * 测试登录失败（密码错误）
     * 预期：HTTP 200，响应 JSON 的 code=401，msg 提示用户名或密码错误
     */
    @Test
    public void testLoginFailure() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    // ==================== 正常访问（admin token） ====================

    /**
     * 测试 admin 访问用户列表接口（需要 sys:user:list 权限）
     * 预期：成功，code=200，返回用户列表数组
     */
    @Test
    public void testAccessUserListWithAdminToken() throws Exception {
        System.out.println("=== 使用 adminToken: " + adminToken);
        mockMvc.perform(get("/system/user/list")
                        .header("Authorization", adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试 admin 新增用户（需要 sys:user:add 权限）
     * 预期：成功，code=200
     */
    @Test
    public void testAddUserWithAdminToken() throws Exception {
        String newUser = "{\"username\":\"test001\",\"password\":\"123456\",\"nickName\":\"测试\"}";
        mockMvc.perform(post("/system/user/add")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 普通用户 token 权限不足测试 ====================

    /**
     * 测试普通用户访问用户列表（普通用户拥有 sys:user:list 权限）
     * 预期：成功，code=200
     */
    @Test
    public void testAccessUserListWithUserToken() throws Exception {
        System.out.println("=== 使用 userToken: " + userToken);
        mockMvc.perform(get("/system/user/list")
                        .header("Authorization", userToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试普通用户新增用户（无 sys:user:add 权限）
     * 预期：HTTP 200，但 code=403，提示权限不足
     */
    @Test
    public void testAddUserWithUserToken_ShouldForbidden() throws Exception {
        String newUser = "{\"username\":\"test002\",\"password\":\"123456\",\"nickName\":\"测试2\"}";
        mockMvc.perform(post("/system/user/add")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    /**
     * 测试普通用户删除用户（无 sys:user:delete 权限）
     * 预期：code=403
     */
    @Test
    public void testDeleteUserWithUserToken_ShouldForbidden() throws Exception {
        mockMvc.perform(delete("/system/user/2")
                        .header("Authorization", userToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    // ==================== 未认证/无效 token 测试 ====================

    /**
     * 测试不携带任何 token 访问需认证接口
     * 预期：code=401，提示未认证
     */
    @Test
    public void testNoTokenAccess_ShouldUnauthorized() throws Exception {
        mockMvc.perform(get("/system/user/list"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 测试携带无效 token（格式错误或签名无效）
     * 预期：code=401
     */
    @Test
    public void testInvalidToken_ShouldUnauthorized() throws Exception {
        mockMvc.perform(get("/system/user/list")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}