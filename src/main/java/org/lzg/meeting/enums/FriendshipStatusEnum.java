package org.lzg.meeting.enums;

import lombok.Getter;

/**
 * 好友关系状态枚举
 */
@Getter
public enum FriendshipStatusEnum {
	
	PENDING(0, "待通过"),
	FRIEND(1, "好友"),
	DELETED(2, "已删除"),
	BLOCKED(3, "已拉黑");

	private final Integer status;
	private final String desc;

	FriendshipStatusEnum(Integer status, String desc) {
		this.status = status;
		this.desc = desc;
	}

	/**
	 * 根据状态值获取枚举
	 */
	public static FriendshipStatusEnum fromStatus(Integer status) {
		if (status == null) {
			return null;
		}
		for (FriendshipStatusEnum value : FriendshipStatusEnum.values()) {
			if (value.getStatus().equals(status)) {
				return value;
			}
		}
		return null;
	}
}
