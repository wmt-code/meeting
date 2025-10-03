package org.lzg.meeting.enums;

import lombok.Getter;

/**
 * 消息发送类型枚举
 */
@Getter
public enum MsgSendTypeEnum {
	USER(1, "用户"),
	GROUP(2, "群组");
	private final Integer value;
	private final String text;

	MsgSendTypeEnum(Integer value, String text) {
		this.value = value;
		this.text = text;
	}

	public static MsgSendTypeEnum getByValue(Integer value) {
		for (MsgSendTypeEnum typeEnum : values()) {
			if (typeEnum.getValue().equals(value)) {
				return typeEnum;
			}
		}
		return null;
	}
}
