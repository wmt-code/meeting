package org.lzg.meeting.model.vo;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 聊天消息分页结果VO（游标分页）
 *
 * @author lzg
 * @since 2025-10-15
 */
@Data
@Schema(description = "聊天消息分页结果VO")
public class ChatMessagePageVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 消息列表
	 */
	@Schema(description = "消息列表")
	private List<ChatMessageVO> messages;

	/**
	 * 下一页游标（最后一条消息的ID，没有更多数据时为null）
	 */
	@Schema(description = "下一页游标")
	private Long nextCursor;

	/**
	 * 是否还有更多数据
	 */
	@Schema(description = "是否还有更多数据")
	private Boolean hasMore;
}
