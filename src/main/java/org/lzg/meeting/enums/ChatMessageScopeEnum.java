package org.lzg.meeting.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天消息范围枚举
 *
 * @author lzg
 * @since 2025-10-15
 */
@Getter
@AllArgsConstructor
public enum ChatMessageScopeEnum {

	/**
	 * 私聊（发送给指定用户）
	 */
	PRIVATE(1, "私聊"),

	/**
	 * 群聊（发送至会议，全体可见）
	 */
	GROUP(2, "群聊");

	private final Integer value;
	private final String description;

	/**
	 * 根据值获取枚举
	 *
	 * @param value 值
	 * @return 枚举
	 */
	public static ChatMessageScopeEnum getByValue(Integer value) {
		if (value == null) {
			return null;
		}
		for (ChatMessageScopeEnum scopeEnum : ChatMessageScopeEnum.values()) {
			if (scopeEnum.value.equals(value)) {
				return scopeEnum;
			}
		}
		return null;
	}
}
