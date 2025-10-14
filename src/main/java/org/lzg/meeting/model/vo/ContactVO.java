package org.lzg.meeting.model.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 联系人VO
 */
@Data
public class ContactVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 用户账号
	 */
	private String userAccount;

	/**
	 * 用户名称
	 */
	private String userName;

	/**
	 * 用户头像
	 */
	private String avatar;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 是否为好友
	 */
	private Boolean isFriend;
}
