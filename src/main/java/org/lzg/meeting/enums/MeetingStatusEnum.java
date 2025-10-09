package org.lzg.meeting.enums;

import lombok.Getter;

/**
 * 会议状态枚举
 */
@Getter
public enum MeetingStatusEnum {
	RUNNING(1, "进行中"),
	END(0, "已结束"),
	CANCEL(-1, "已取消");
	private final Integer value;
	private final String text;

	MeetingStatusEnum(int value, String text) {
		this.value = value;
		this.text = text;
	}

	public static MeetingStatusEnum valueOf(int value) {
		for (MeetingStatusEnum statusEnum : values()) {
			if (statusEnum.getValue() == value) {
				return statusEnum;
			}
		}
		return null;
	}
}
