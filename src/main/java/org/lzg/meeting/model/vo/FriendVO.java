package org.lzg.meeting.model.vo;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

/**
 * 好友信息VO
 */
@Data
public class FriendVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 好友用户ID
	 */
	private Long userId;

	/**
	 * 好友账号
	 */
	private String userAccount;

	/**
	 * 好友名称
	 */
	private String userName;

	/**
	 * 好友头像
	 */
	private String avatar;

	/**
	 * 好友邮箱
	 */
	private String email;

	/**
	 * 关系状态：1好友 2被删除 3被拉黑
	 */
	private Integer status;
}
