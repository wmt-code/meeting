package org.lzg.meeting.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议预约dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MeetingReserveDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * 会议开始时间
	 */
	private LocalDateTime startTime;
	/**
	 * 会议名称
	 */
	private String meetingName;

	/**
	 * 加入会议的类型，例如直接加入或者要密码加入
	 */
	private Integer joinType;

	/**
	 * 加入密码
	 */
	private String joinPassword;


	/**
	 * 会议持续时间 (分钟)
	 */
	private Integer duration;

	/**
	 * 邀请用户ID列表
	 */
	private List<Long> inviteUserIdList;
}
