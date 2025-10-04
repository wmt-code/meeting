package org.lzg.meeting.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户id
	 */
	private Long id;

	/**
	 * 用户账号
	 */
	private String userAccount;

	/**
	 * 用户token
	 */
	private String token;

	/**
	 * 用户名称
	 */
	private String userName;

	/**
	 * 用户头像
	 */
	private String avatar;

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
	 * 个人会议ID
	 */
	private Long meetingId;
}
