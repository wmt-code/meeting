package org.lzg.meeting.model.dto;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

/**
 * 好友申请请求DTO
 */
@Data
public class FriendApplyDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 接收人ID
	 */
	private Long toUserId;

	/**
	 * 申请附言
	 */
	private String message;
}
