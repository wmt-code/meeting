package org.lzg.meeting.enums;

import lombok.Getter;

/**
 * 会议成员状态枚举
 */
@Getter
public enum MeetingMemberStatusEnum {

	DEL_MEETING(0, "删除会议"),
	NORMAL(1, "正常"),
	EXIT_MEETING(2, "退出会议"),
	KICK_OUT(3, "被踢出会议"),
	BLACKLIST(4, "被拉黑");

	private final int status;
	private final String desc;

	MeetingMemberStatusEnum(int status, String desc) {
		this.status = status;
		this.desc = desc;
	}

	public static MeetingMemberStatusEnum fromStatus(int status) {
		for (MeetingMemberStatusEnum value : MeetingMemberStatusEnum.values()) {
			if (value.getStatus() == status) {
				return value;
			}
		}
		return null;
	}
}
