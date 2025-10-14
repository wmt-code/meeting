package org.lzg.meeting.enums;

import lombok.Getter;

/**
 * 会议邀请状态枚举
 */
@Getter
public enum InvitationStatusEnum {

	NO_RESPONSE(0, "未响应"),
	ACCEPTED(1, "已接受"),
	REJECTED(2, "已拒绝"),
	EXPIRED(3, "已过期");

	private final Integer status;
	private final String description;

	InvitationStatusEnum(Integer status, String description) {
		this.status = status;
		this.description = description;
	}

	/**
	 * 根据status获取枚举
	 */
	public static InvitationStatusEnum fromStatus(Integer status) {
		if (status == null) {
			return null;
		}
		for (InvitationStatusEnum statusEnum : InvitationStatusEnum.values()) {
			if (statusEnum.getStatus().equals(status)) {
				return statusEnum;
			}
		}
		return null;
	}
}
