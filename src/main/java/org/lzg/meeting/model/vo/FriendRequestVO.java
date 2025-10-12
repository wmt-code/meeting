package org.lzg.meeting.model.vo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 好友申请VO
 */
@Data
public class FriendRequestVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 申请记录ID
	 */
	private Long id;

	/**
	 * 申请人ID
	 */
	private Long fromUserId;

	/**
	 * 申请人账号
	 */
	private String fromUserAccount;

	/**
	 * 申请人名称
	 */
	private String fromUserName;

	/**
	 * 申请人头像
	 */
	private String fromUserAvatar;

	/**
	 * 申请附言
	 */
	private String message;

	/**
	 * 状态：0待处理 1同意 2拒绝
	 */
	private Integer status;

	/**
	 * 申请时间
	 */
	private LocalDateTime createTime;
}
