package org.lzg.meeting.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PreJoinMeetingDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	// 会议号
	private Integer meetingNo;
	// 会议密码
	private String password;
	// 加入会议后的昵称
	private String nickName;
}
