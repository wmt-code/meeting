package org.lzg.meeting.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天消息类型枚举
 *
 * @author lzg
 * @since 2025-10-15
 */
@Getter
@AllArgsConstructor
public enum ChatMessageTypeEnum {

	/**
	 * 文本消息
	 */
	TEXT(1, "文本"),

	/**
	 * 图片消息
	 */
	IMAGE(2, "图片"),

	/**
	 * 视频消息
	 */
	VIDEO(3, "视频"),

	/**
	 * 文件消息
	 */
	FILE(4, "文件"),

	/**
	 * 语音消息
	 */
	AUDIO(5, "语音");

	private final Integer value;
	private final String description;

	/**
	 * 根据值获取枚举
	 *
	 * @param value 值
	 * @return 枚举
	 */
	public static ChatMessageTypeEnum getByValue(Integer value) {
		if (value == null) {
			return null;
		}
		for (ChatMessageTypeEnum typeEnum : ChatMessageTypeEnum.values()) {
			if (typeEnum.value.equals(value)) {
				return typeEnum;
			}
		}
		return null;
	}
}
