package org.lzg.meeting.model.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 邀请结果VO
 */
@Data
public class InviteResultVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 被邀请人ID
	 */
	private Long inviteeId;

	/**
	 * 被邀请人姓名
	 */
	private String inviteeName;

	/**
	 * 是否成功
	 */
	private Boolean success;

	/**
	 * 失败原因
	 */
	private String failReason;
}
