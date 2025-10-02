package org.lzg.meeting.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
	DISABLE(0, "禁用"),
	ENABLE(1, "启用");
	private final Integer value;
	private final String text;

	UserStatusEnum(Integer value, String text) {
		this.value = value;
		this.text = text;
	}

	public static UserStatusEnum getByValue(Integer value) {
		for (UserStatusEnum statusEnum : values()) {
			if (statusEnum.getValue().equals(value)) {
				return statusEnum;
			}
		}
		return null;
	}
}
