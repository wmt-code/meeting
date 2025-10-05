package org.lzg.meeting.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class QuickMeetingDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * 会议号类型 0 使用个人会议号 1 使用随机会议号
	 */
	private Integer meetingNoType;
	/**
	 * 会议名称
	 */
	private String meetingName;

	/**
	 * 加入类型 0 无需密码 1 需要密码
	 */
	private Integer joinType;

	/**
	 * 会议密码
	 */
	private String meetingPassword;
}
