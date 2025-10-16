package org.lzg.meeting.model.dto;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 查询聊天历史消息DTO（游标分页）
 *
 * @author lzg
 * @since 2025-10-15
 */
@Data
@Schema(description = "查询聊天历史消息DTO")
public class ChatMessageQueryDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 会议id
	 */
	@Schema(description = "会议id", required = true)
	private Long meetingId;

	/**
	 * 消息范围 1-私聊 2-群聊（可选，不填查全部）
	 */
	@Schema(description = "消息范围 1-私聊 2-群聊")
	private Integer messageScope;

	/**
	 * 对方用户id（查询私聊消息时使用）
	 */
	@Schema(description = "对方用户id（查询私聊消息时使用）")
	private Long otherUserId;

	/**
	 * 游标（上次查询的最后一条消息ID，不填表示查询最新）
	 */
	@Schema(description = "游标（上次查询的最后一条消息ID）")
	private Long cursor;

	/**
	 * 每页大小
	 */
	@Schema(description = "每页大小，默认20，最大100")
	private Integer pageSize = 20;
}
