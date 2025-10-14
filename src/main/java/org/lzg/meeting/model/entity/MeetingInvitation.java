package org.lzg.meeting.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会议邀请实体
 */
@Data
@TableName("meeting_invitation")
public class MeetingInvitation implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 会议ID
	 */
	private Long meetingId;

	/**
	 * 邀请人ID
	 */
	private Long inviterId;

	/**
	 * 被邀请人ID
	 */
	private Long inviteeId;

	/**
	 * 邀请方式：0站内消息 1邮件 2短信
	 */
	private Integer inviteType;

	/**
	 * 邀请状态：0未响应 1已接受 2已拒绝 3已过期
	 */
	private Integer status;

	/**
	 * 邀请附言
	 */
	private String inviteMessage;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;

	/**
	 * 响应时间
	 */
	private LocalDateTime responseTime;
}
