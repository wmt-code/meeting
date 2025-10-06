package org.lzg.meeting.enums;

import lombok.Getter;

/**
 * 成员类型枚举
 */
@Getter
public enum MemberTypeEnum {
	COMMON(0, "普通成员"),
	ADMIN(1, "主持人");
	private final Integer value;
	private final String text;

	MemberTypeEnum(Integer value, String text) {
		this.value = value;
		this.text = text;
	}
}
