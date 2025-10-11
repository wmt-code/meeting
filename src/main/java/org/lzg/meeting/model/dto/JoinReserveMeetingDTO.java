package org.lzg.meeting.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class JoinReserveMeetingDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * 会议ID
	 */
	private Long meetingId;
	/**
	 * 加入密码
	 */
	private String joinPassword;
}
