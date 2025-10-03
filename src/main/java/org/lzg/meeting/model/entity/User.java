package org.lzg.meeting.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author lzg
 * @since 2025-09-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户id
	 */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 用户账号
	 */
	@TableField("userAccount")
	private String userAccount;

	/**
	 * 用户密码
	 */
	@TableField("userPassword")
	private String userPassword;

	/**
	 * 用户名称
	 */
	@TableField("userName")
	private String userName;

	/**
	 * 用户头像
	 */
	@TableField("avatar")
	private String avatar;

	/**
	 * 用户邮箱
	 */
	@TableField("email")
	private String email;

	/**
	 * 用户状态 1启用 0禁用
	 */
	@TableField("status")
	private Integer status;
	/**
	 * 用户角色
	 */
	@TableField("role")
	private String role;

	/**
	 * 会议号
	 */
	@TableField("meetingNo")
	private Long meetingNo;
	/**
	 * 创建时间
	 */
	@TableField("createTime")
	private LocalDateTime createTime;

	/**
	 * 编辑时间
	 */
	@TableField("editTime")
	private LocalDateTime editTime;

	/**
	 * 更新时间
	 */
	@TableField("updateTime")
	private LocalDateTime updateTime;

	/**
	 * 最后登录时间
	 */
	@TableField("lastLoginTime")
	private LocalDateTime lastLoginTime;

	/**
	 * 最后登出时间
	 */
	@TableField("lastLogoutTime")
	private LocalDateTime lastLogoutTime;

	/**
	 * 是否删除 1已删除 0 未删除
	 */
	@TableField("deleted")
	private Integer deleted;


}
