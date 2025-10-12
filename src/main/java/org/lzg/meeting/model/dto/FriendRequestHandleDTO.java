package org.lzg.meeting.model.dto;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

/**
 * 处理好友申请请求DTO（同意/拒绝）
 */
@Data
public class FriendRequestHandleDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 申请记录ID
	 */
	private Long requestId;
}
