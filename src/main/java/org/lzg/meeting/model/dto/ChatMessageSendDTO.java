package org.lzg.meeting.model.dto;

import java.io.Serial;
import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发送聊天消息DTO
 *
 * @author lzg
 * @since 2025-10-15
 */
@Data
@Schema(description = "发送聊天消息DTO")
public class ChatMessageSendDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 会议id
	 */
	@Schema(description = "会议id", required = true)
	private Long meetingId;

	/**
	 * 接收者用户id（私聊时必填，群聊时为null）
	 */
	@Schema(description = "接收者用户id（私聊时必填）")
	private Long receiverId;

	/**
	 * 消息类型 1-文本 2-图片 3-视频 4-文件 5-语音
	 */
	@Schema(description = "消息类型 1-文本 2-图片 3-视频 4-文件 5-语音", required = true)
	private Integer messageType;

	/**
	 * 消息范围 1-私聊 2-群聊
	 */
	@Schema(description = "消息范围 1-私聊 2-群聊", required = true)
	private Integer messageScope;

	/**
	 * 消息内容（文本消息）
	 */
	@Schema(description = "消息内容（文本消息时必填）")
	private String content;

	/**
	 * 文件（图片、视频、文件消息）
	 */
	@Schema(description = "文件（图片、视频、文件消息时必填）")
	private MultipartFile file;
}
