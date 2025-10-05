package org.lzg.meeting.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TokenUserInfo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	private Integer meetingNo; // 会议号
	private String userRole; // 用户角色
	private Long userId; // 用户ID
	private String userName; // 用户名称
	private Long meetingId; // 会议ID
}
