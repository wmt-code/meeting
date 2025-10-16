package org.lzg.meeting.model.vo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 聊天消息VO
 *
 * @author lzg
 * @since 2025-10-15
 */
@Data
@Schema(description = "聊天消息VO")
public class ChatMessageVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 消息id
	 */
	@Schema(description = "消息id")
	private Long id;

	/**
	 * 会议id
	 */
	@Schema(description = "会议id")
	private Long meetingId;

	/**
	 * 发送者用户id
	 */
	@Schema(description = "发送者用户id")
	private Long senderId;

	/**
	 * 发送者用户名
	 */
	@Schema(description = "发送者用户名")
	private String senderName;

	/**
	 * 接收者用户id
	 */
	@Schema(description = "接收者用户id")
	private Long receiverId;

	/**
	 * 接收者用户名
	 */
	@Schema(description = "接收者用户名")
	private String receiverName;

	/**
	 * 消息类型 1-文本 2-图片 3-视频 4-文件 5-语音
	 */
	@Schema(description = "消息类型 1-文本 2-图片 3-视频 4-文件 5-语音")
	private Integer messageType;

	/**
	 * 消息类型描述
	 */
	@Schema(description = "消息类型描述")
	private String messageTypeDesc;

	/**
	 * 消息范围 1-私聊 2-群聊
	 */
	@Schema(description = "消息范围 1-私聊 2-群聊")
	private Integer messageScope;

	/**
	 * 消息范围描述
	 */
	@Schema(description = "消息范围描述")
	private String messageScopeDesc;

	/**
	 * 消息内容
	 */
	@Schema(description = "消息内容")
	private String content;

	/**
	 * 文件URL
	 */
	@Schema(description = "文件URL")
	private String fileUrl;

	/**
	 * 缩略图URL
	 */
	@Schema(description = "缩略图URL")
	private String thumbnailUrl;

	/**
	 * 文件名
	 */
	@Schema(description = "文件名")
	private String fileName;

	/**
	 * 文件大小
	 */
	@Schema(description = "文件大小")
	private Long fileSize;

	/**
	 * 文件类型
	 */
	@Schema(description = "文件类型")
	private String fileType;

	/**
	 * 消息状态
	 */
	@Schema(description = "消息状态 1-正常 0-已删除 2-已撤回")
	private Integer status;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private LocalDateTime createTime;
}
