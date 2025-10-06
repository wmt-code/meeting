package org.lzg.meeting.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MeetingMemberDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	private Long userId;
	private Long meetingId;
	private String userName;
	private Integer memberType; // 0-普通成员 1-主持人
	private Integer status; // 成员状态 0-正常 1-被踢出 2-拉入黑名单
	private String avatar;
	private Boolean videoOpen;
	private LocalDateTime lastJoinTime;
}
