package com.xiyao.dict.controller;

import com.xiyao.common.utils.Result;
import com.xiyao.dict.annotation.DictBind;
import com.xiyao.dict.config.DictCache;
import com.xiyao.dict.enums.DataStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 字典功能测试控制器
 * <p>
 * 提供字典缓存和枚举转换功能的测试接口。
 * 用于开发和测试阶段验证字典功能是否正常工作。
 *
 * <p>
 * <b>接口分类：</b>
 * <ol>
 *     <li>字典缓存操作：查询字典Map、字典标签、刷新缓存</li>
 *     <li>枚举缓存操作：按 code/desc/name 查询枚举、刷新缓存</li>
 *     <li>模拟查询测试：测试 @DictBind 注解和枚举转换功能</li>
 * </ol>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 查询字典
 * GET /dict/test/cache/dict/status
 *
 * // 按code查询枚举
 * GET /dict/test/cache/enum/byCode?enumType=DataStatus&code=1
 *
 * // 刷新字典缓存
 * POST /dict/test/cache/refresh/dict?code=status
 * }</pre>
 *
 * @author xiyao
 * @see DictCache
 * @see DictBind
 * @see DataStatus
 */
@RestController
@RequestMapping("/dict/test")
public class DictTestController {

    // ==================== 字典缓存操作 ====================

    /**
     * 查询字典Map
     * <p>
     * 根据字典编码查询字典的值到标签映射。
     *
     * @param code 字典编码
     * @return 字典值到标签的映射
     */
    @GetMapping("/cache/dict/{code}")
    public Result getDictMap(@PathVariable String code) {
        Map<String, String> map = DictCache.getInstance().getDictMap(code);
        return Result.ok(map);
    }

    /**
     * 查询字典标签
     * <p>
     * 根据字典编码和字典值查询对应的标签描述文本。
     *
     * @param code  字典编码
     * @param value 字典值
     * @return 字典标签，找不到返回空字符串
     */
    @GetMapping("/cache/dict/label")
    public Result getDictLabel(@RequestParam String code, @RequestParam String value) {
        String label = DictCache.getInstance().getDictLabel(code, value);
        return Result.ok(label);
    }

    // ==================== 枚举缓存操作 ====================

    /**
     * 按 code 值查询枚举
     * <p>
     * 通过枚举的 getCode() 返回值查询对应的枚举常量。
     *
     * @param enumType 枚举类型
     * @param code     存储值
     * @return 对应的枚举常量
     */
    @GetMapping("/cache/enum/byCode")
    public Result getEnumByCode(@RequestParam Class<DataStatus> enumType, @RequestParam String code) {
        DataStatus status = DictCache.getInstance().getEnumByCode(enumType, code);
        return Result.ok(status);
    }

    /**
     * 按描述查询枚举
     * <p>
     * 通过枚举的 getDesc() 返回值查询对应的枚举常量。
     *
     * @param enumType 枚举类型
     * @param desc     描述文本
     * @return 对应的枚举常量
     */
    @GetMapping("/cache/enum/byDesc")
    public Result getEnumByDesc(@RequestParam Class<DataStatus> enumType, @RequestParam String desc) {
        DataStatus status = DictCache.getInstance().getEnumByDesc(enumType, desc);
        return Result.ok(status);
    }

    /**
     * 按枚举名字查询枚举
     * <p>
     * 通过枚举常量的名称（如 NORMAL）查询对应的枚举常量。
     *
     * @param enumType 枚举类型
     * @param name     枚举常量名
     * @return 对应的枚举常量
     */
    @GetMapping("/cache/enum/byName")
    public Result getEnumByName(@RequestParam Class<DataStatus> enumType, @RequestParam String name) {
        DataStatus status = DictCache.getInstance().getEnumByName(enumType, name);
        return Result.ok(status);
    }

    /**
     * 获取枚举的所有 code 值列表
     *
     * @param enumType 枚举类型
     * @return 该枚举类型所有 code 值的列表
     */
    @GetMapping("/cache/enum/codes")
    public Result getEnumCodes(@RequestParam Class<DataStatus> enumType) {
        List<String> codes = DictCache.getInstance().getEnumCodes(enumType);
        return Result.ok(codes);
    }

    // ==================== 缓存刷新操作 ====================

    /**
     * 刷新指定字典的缓存
     * <p>
     * 删除指定字典的缓存后重新从数据库加载。
     * 用于管理员在后台修改字典数据后手动刷新。
     *
     * @param code 字典编码
     * @return 操作结果
     */
    @PostMapping("/cache/refresh/dict")
    public Result refreshDict(@RequestParam String code) {
        DictCache.getInstance().refreshDict(code);
        return Result.ok("字典刷新成功: " + code);
    }

    /**
     * 刷新指定枚举类型的缓存
     * <p>
     * 移除该枚举的缓存后重新加载。
     *
     * @param enumType 枚举类型
     * @return 操作结果
     */
    // @PostMapping("/cache/refresh/enum")
    // public Result refreshEnum(@RequestParam Class<? extends BaseEnum> enumType) {
    //     DictCache.getInstance().refreshEnum(enumType);
    //     return Result.ok("枚举刷新成功: " + enumType.getSimpleName());
    // }

    /**
     * 刷新所有缓存
     * <p>
     * 同时刷新字典和枚举缓存。
     *
     * @return 操作结果
     */
    @PostMapping("/cache/refresh/all")
    public Result refreshAll() {
        DictCache.getInstance().refreshAll();
        return Result.ok("全量刷新成功");
    }

    // ==================== 模拟查询结果（用于测试拦截器） ====================

    /**
     * 模拟查询用户列表（带 @DictBind 字典回显）
     * <p>
     * 模拟从数据库查询的用户数据，演示 @DictBind 注解的自动回显功能。
     * status 字段标注了 @DictBind(code = "status")，
     * 拦截器会自动将 status 的值（1、0）转换为对应的描述文本填充到 statusText 字段。
     */
    @GetMapping("/query/withDictBind")
    public Result queryWithDictBind() {
        // 模拟从数据库查询的用户数据
        UserVO user1 = new UserVO();
        user1.setId(1L);
        user1.setName("张三");
        user1.setStatus(1);  // 数据库存的值为1
        // statusText 应该被拦截器填充为"正常"

        UserVO user2 = new UserVO();
        user2.setId(2L);
        user2.setName("李四");
        user2.setStatus(0);  // 数据库存的值为0
        // statusText 应该被拦截器填充为"暂停"

        return Result.ok(List.of(user1, user2));
    }

    /**
     * 模拟查询用户列表（带枚举类型）
     * <p>
     * 模拟从数据库查询的用户数据，演示查询结果自动转换为枚举的功能。
     * status 字段是 Integer 类型，拦截器会根据字段类型自动将数据库值转换为对应的 DataStatus 枚举。
     */
    @GetMapping("/query/withEnum")
    public Result queryWithEnum() {
        UserEnumVO vo1 = new UserEnumVO();
        vo1.setId(1L);
        vo1.setName("张三");
        vo1.setStatus(1);  // 数据库存的值

        UserEnumVO vo2 = new UserEnumVO();
        vo2.setId(2L);
        vo2.setName("李四");
        vo2.setStatus(0);

        return Result.ok(List.of(vo1, vo2));
    }

    // ==================== 测试 VO ====================

    /**
     * 用于测试 @DictBind 字典回显的 VO
     * <p>
     * status 字段标注了 @DictBind(code = "status") 注解，
     * MyBatis 拦截器会自动将 status 的值填充到 statusText 字段。
     */
    public static class UserVO {
        private Long id;
        private String name;

        // 字典绑定注解：标注该字段需要字典回显
        @DictBind(code = "status")
        private Integer status;
        // 回显目标字段：拦截器会自动填充对应的描述文本
        private String statusText;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public String getStatusText() { return statusText; }
        public void setStatusText(String statusText) { this.statusText = statusText; }
    }

    /**
     * 用于测试查询结果转枚举的 VO
     * <p>
     * status 是 Integer 类型（数据库值），statusEnum 是 DataStatus 枚举类型。
     * 拦截器会自动将 status 的值转换为对应的 DataStatus 枚举填充到 statusEnum 字段。
     */
    public static class UserEnumVO {
        private Long id;
        private String name;
        // 数据库值：拦截器会将其转换为 DataStatus 枚举
        private Integer status;
        // 枚举字段：MyBatis 查询结果字段名需要匹配
        private DataStatus statusEnum;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public DataStatus getStatusEnum() { return statusEnum; }
        public void setStatusEnum(DataStatus statusEnum) { this.statusEnum = statusEnum; }
    }
}