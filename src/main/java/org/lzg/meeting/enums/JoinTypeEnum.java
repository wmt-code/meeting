package org.lzg.meeting.enums;

import lombok.Getter;

/**
 * 加入类型枚举
 */
@Getter
public enum JoinTypeEnum {
	NO_PASSWORD(0, "无需密码"),
	NEED_PASSWORD(1, "需要密码");

	private final int code;
	private final String description;

	JoinTypeEnum(int code, String description) {
		this.code = code;
		this.description = description;
	}

}
