package org.lzg.meeting.enums;

import lombok.Getter;

/**
 * 邀请方式枚举
 */
@Getter
public enum InviteTypeEnum {

	INTERNAL_MESSAGE(0, "站内消息"),
	EMAIL(1, "邮件"),
	SMS(2, "短信");

	private final Integer type;
	private final String description;

	InviteTypeEnum(Integer type, String description) {
		this.type = type;
		this.description = description;
	}

	/**
	 * 根据type获取枚举
	 */
	public static InviteTypeEnum fromType(Integer type) {
		if (type == null) {
			return null;
		}
		for (InviteTypeEnum typeEnum : InviteTypeEnum.values()) {
			if (typeEnum.getType().equals(type)) {
				return typeEnum;
			}
		}
		return null;
	}
}
