package org.lzg.meeting.model.vo;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

/**
 * 好友关系VO
 */
@Data
public class FriendRelationVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 是否是好友
	 */
	private Boolean isFriend;

	/**
	 * 是否被我拉黑
	 */
	private Boolean isBlockedByMe;

	/**
	 * 是否被对方拉黑
	 */
	private Boolean isBlockedByOther;

	/**
	 * 我方关系状态：0无关系 1好友 2被删除 3被拉黑
	 */
	private Integer myStatus;

	/**
	 * 对方关系状态：0无关系 1好友 2被删除 3被拉黑
	 */
	private Integer otherStatus;
}
