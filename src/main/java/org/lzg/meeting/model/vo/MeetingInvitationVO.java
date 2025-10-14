package org.lzg.meeting.model.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 会议邀请VO
 */
@Data
public class MeetingInvitationVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 邀请ID
	 */
	private Long id;

	/**
	 * 会议ID
	 */
	private Long meetingId;

	/**
	 * 会议名称
	 */
	private String meetingName;

	/**
	 * 邀请人ID
	 */
	private Long inviterId;

	/**
	 * 邀请人姓名
	 */
	private String inviterName;

	/**
	 * 被邀请人ID
	 */
	private Long inviteeId;

	/**
	 * 被邀请人姓名
	 */
	private String inviteeName;

	/**
	 * 被邀请人账号
	 */
	private String inviteeAccount;

	/**
	 * 被邀请人头像
	 */
	private String inviteeAvatar;

	/**
	 * 邀请方式：0站内消息 1邮件 2短信
	 */
	private Integer inviteType;

	/**
	 * 邀请方式描述
	 */
	private String inviteTypeDesc;

	/**
	 * 邀请状态：0未响应 1已接受 2已拒绝 3已过期
	 */
	private Integer status;

	/**
	 * 邀请状态描述
	 */
	private String statusDesc;

	/**
	 * 邀请附言
	 */
	private String inviteMessage;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 响应时间
	 */
	private LocalDateTime responseTime;
}
