package org.lzg.meeting.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员更新用户请求
 *
 * @author lzg
 * @since 2025-10-16
 */
@Data
public class AdminUpdateUserDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	private Long id;

	/**
	 * 用户名称
	 */
	private String userName;

	/**
	 * 用户邮箱
	 */
	private String email;

	/**
	 * 用户状态 1启用 0禁用
	 */
	private Integer status;

	/**
	 * 用户角色
	 */
	private String userRole;

	/**
	 * 用户头像
	 */
	private String avatar;
}
