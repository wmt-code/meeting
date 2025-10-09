package org.lzg.meeting.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ExitMeetingDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	private Long exitUserId;
	private List<MeetingMemberDTO> meetingMemberDTOList;
	private Integer exitStaus;
}
