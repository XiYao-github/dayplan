package com.xiyao.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 * <p>
 * 存储系统用户信息，包含登录凭证、联系方式、状态等。
 * 继承自 MyBaseEntity 获取审计字段（创建人、创建时间、更新人、更新时间）。
 *
 * <p>
 * <b>字段说明：</b>
 * <ul>
 *     <li>id：自增主键</li>
 *     <li>username：用户账号，唯一，用于登录</li>
 *     <li>password：BCrypt 加密后的密码</li>
 *     <li>salt：密码盐值（当前版本未使用，预留）</li>
 *     <li>mobile/email：联系方式</li>
 *     <li>status：状态（0=停用，1=正常）</li>
 *     <li>loginIp/loginDate：最后登录信息</li>
 * </ul>
 *
 * <p>
 * <b>关联关系：</b>
 * <ul>
 *     <li>用户-角色：N:N 关系，通过 sys_user_role 表关联</li>
 *     <li>角色-菜单：N:N 关系，通过 sys_role_menu 表关联</li>
 * </ul>
 *
 * @author xiyao
 */
@Data
@TableName("sys_user")
@Accessors(chain = true)
public class SysUser {

    /**
     * 主键 ID
     * <p>
     * 数据库自增策略，AUTO_INCREMENT。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户账号
     * <p>
     * 唯一标识，用于登录认证。
     */
    @TableField("username")
    private String username;

    /**
     * 密码（BCrypt 加密）
     * <p>
     * 使用 BCrypt 算法加密存储，每次加密结果不同。
     * 验证时通过 BCryptPasswordEncoder.matches() 比对。
     */
    @TableField("password")
    private String password;

    /**
     * 密码盐值
     * <p>
     * 当前版本未使用，预留用于未来扩展。
     */
    @TableField("salt")
    private String salt;

    /**
     * 手机号码
     * <p>
     * 可用于登录、找回密码等场景。
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 昵称
     * <p>
     * 用于前端展示，非登录标识。
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 邮箱
     * <p>
     * 用于邮件通知等场景。
     */
    @TableField("email")
    private String email;

    /**
     * 性别
     * <p>
     * 取值：0=未知，1=男，2=女
     */
    @TableField("sex")
    private Integer sex;

    /**
     * 头像 URL
     * <p>
     * 存储用户头像的图片地址。
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 最后登录 IP
     * <p>
     * 记录用户最近一次登录的 IP 地址。
     */
    @TableField("login_ip")
    private String loginIp;

    /**
     * 最后登录时间
     * <p>
     * 记录用户最近一次登录的时间戳。
     */
    @TableField("login_date")
    private LocalDateTime loginDate;

    /**
     * 状态
     * <p>
     * 控制用户是否可以登录：0=停用，1=正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     * <p>
     * 用于记录用户的额外说明信息。
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     * <p>
     * MyBatis-Plus 自动填充，插入时自动设置。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>
     * MyBatis-Plus 自动填充，插入和更新时自动设置。
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除时间
     * <p>
     * 软删除时记录删除时间，与 deleted 字段配合实现逻辑删除。
     */
    @TableField("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 逻辑删除标志
     * <p>
     * 0=未删除（正常），1=已删除
     * MyBatis-Plus 查询时自动拼接 deleted=0 条件。
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 乐观锁版本号
     * <p>
     * 并发更新时防止数据覆盖。
     * 更新时 version+1，并校验 WHERE version=oldVersion。
     */
    @Version
    @TableField("version")
    private Integer version;
}
