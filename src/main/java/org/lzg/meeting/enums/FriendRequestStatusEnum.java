package org.lzg.meeting.enums;

import lombok.Getter;

/**
 * 好友申请状态枚举
 */
@Getter
public enum FriendRequestStatusEnum {
	
	PENDING(0, "待处理"),
	AGREED(1, "已同意"),
	REJECTED(2, "已拒绝");

	private final Integer status;
	private final String desc;

	FriendRequestStatusEnum(Integer status, String desc) {
		this.status = status;
		this.desc = desc;
	}

	/**
	 * 根据状态值获取枚举
	 */
	public static FriendRequestStatusEnum fromStatus(Integer status) {
		if (status == null) {
			return null;
		}
		for (FriendRequestStatusEnum value : FriendRequestStatusEnum.values()) {
			if (value.getStatus().equals(status)) {
				return value;
			}
		}
		return null;
	}
}
