package org.lzg.meeting.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class MeetingJoinDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private MeetingMemberDTO newMember;
	private List<MeetingMemberDTO> meetingMemberList;
}
