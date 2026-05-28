package com.xiyao.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * 数据状态枚举
 * <p>
 * 定义系统中常用的数据状态，用于表示业务记录的启用/禁用状态。
 * 实现 BaseEnum 接口，支持字典转换和 JSON 序列化。
 *
 * <p>
 * <b>状态说明：</b>
 * <ul>
 *     <li>PAUSE（暂停）：表示数据暂时禁用，不可用于业务操作</li>
 *     <li>NORMAL（正常）：表示数据正常启用，可以正常使用</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 在实体类中使用
 * public class User {
 *     @EnumValue
 *     private Integer status;
 *
 *     private DataStatus statusEnum;  // MyBatis 拦截器会自动填充
 * }
 *
 * // 在 Controller 中接收参数
 * @GetMapping("/user")
 * public Result&lt;List&lt;User&gt;&gt; getUsers(@RequestParam DataStatus status) {
 *     // 请求 /api/user?status=1 或 /api/user?status=正常 都能匹配
 *     return Result.ok(userService.listByStatus(status));
 * }
 * }</pre>
 *
 * @author xiyao
 * @see BaseEnum
 */
@Getter
@AllArgsConstructor
public enum DataStatus implements BaseEnum<Integer> {

    /**
     * 暂停
     */
    PAUSE(0, "暂停"),

    /**
     * 正常
     */
    NORMAL(1, "正常");

    /**
     * 存储到数据库的值
     */
    private final Integer code;

    /**
     * 描述文本
     * <p>
     * 用于前端展示，如"正常"、"暂停"
     */
    private final String desc;

    /**
     * JSON 反序列化方法
     * <p>
     * 支持从 value、desc、name 三种方式反序列化。
     *
     * @param value 输入值
     * @return 对应的枚举常量，找不到返回 null
     */
    @JsonCreator
    public static DataStatus fromValue(String value) {
        // 空值判断
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String input = value.trim();
        for (DataStatus status : values()) {
            // 1. 按 code 值匹配
            if (status.getCode().toString().equals(input)) {
                return status;
            }
            // 2. 按描述匹配
            if (status.getDesc().equals(input)) {
                return status;
            }
            // 3. 按枚举名匹配
            if (status.name().equals(input)) {
                return status;
            }
        }
        // 都匹配不上，返回 null
        return null;
    }

    /**
     * JSON 序列化方法
     * <p>
     * 序列化时返回 code 值
     *
     * @return code 值
     */
    @JsonValue
    public Integer getValue() {
        return code;
    }

    /**
     * 获取枚举名称
     * <p>
     * 返回枚举常量的名字，如 NORMAL、PAUSE
     *
     * @return 枚举名称
     */
    @Override
    public String getName() {
        return this.name();
    }
}