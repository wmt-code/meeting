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

	private final Integer status;
	private final String desc;

	MeetingMemberStatusEnum(Integer status, String desc) {
		this.status = status;
		this.desc = desc;
	}

	public static MeetingMemberStatusEnum fromStatus(Integer status) {
		for (MeetingMemberStatusEnum value : MeetingMemberStatusEnum.values()) {
			if (value.getStatus() == status) {
				return value;
			}
		}
		return null;
	}
}
