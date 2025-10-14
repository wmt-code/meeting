package org.lzg.meeting.model.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 邀请联系人DTO
 */
@Data
public class MeetingInviteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 被邀请人ID列表
	 */
	private List<Long> inviteeIds;

	/**
	 * 邀请方式：0站内消息 1邮件 2短信
	 */
	private Integer inviteType;

	/**
	 * 邀请附言
	 */
	private String inviteMessage;
}
