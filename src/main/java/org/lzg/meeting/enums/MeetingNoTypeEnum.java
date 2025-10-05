package org.lzg.meeting.enums;

import lombok.Getter;

/**
 * @description: 会议号类型枚举
 */
@Getter
public enum MeetingNoTypeEnum {
	PERSONAL_MEETING_NO(0, "使用个人会议号"),
	RANDOM_MEETING_NO(1, "使用随机会议号"),
	;

	private final Integer value;
	private final String text;

	MeetingNoTypeEnum(Integer value, String text) {
		this.value = value;
		this.text = text;
	}
}
