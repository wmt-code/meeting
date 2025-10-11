package org.lzg.meeting.enums;

import lombok.Getter;

/**
 * 会议预约状态枚举
 */
@Getter
public enum MeetingReserveStatusEnum {
	RESERVED(0, "已预约"),
	CANCELLED(1, "已取消"),
	COMPLETED(2, "已结束"),
	NO_SHOW(3, "未出席");

	private final Integer status;
	private final String description;

	MeetingReserveStatusEnum(Integer status, String description) {
		this.status = status;
		this.description = description;
	}

	public static MeetingReserveStatusEnum fromStatus(Integer status) {
		for (MeetingReserveStatusEnum value : MeetingReserveStatusEnum.values()) {
			if (value.getStatus().equals(status)) {
				return value;
			}
		}
		return null;
	}
}
