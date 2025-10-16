package org.lzg.meeting.model.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 聊天消息表
 * </p>
 *
 * @author lzg
 * @since 2025-10-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_message")
public class ChatMessage implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 消息id
	 */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 会议id
	 */
	@TableField("meetingId")
	private Long meetingId;

	/**
	 * 发送者用户id
	 */
	@TableField("senderId")
	private Long senderId;

	/**
	 * 发送者用户名
	 */
	@TableField("senderName")
	private String senderName;

	/**
	 * 接收者用户id（私聊时使用，群聊时为null）
	 */
	@TableField("receiverId")
	private Long receiverId;

	/**
	 * 接收者用户名（私聊时使用）
	 */
	@TableField("receiverName")
	private String receiverName;

	/**
	 * 消息类型 1-文本 2-图片 3-视频 4-文件 5-语音
	 */
	@TableField("messageType")
	private Integer messageType;

	/**
	 * 消息范围 1-私聊 2-群聊
	 */
	@TableField("messageScope")
	private Integer messageScope;

	/**
	 * 消息内容（文本消息）
	 */
	@TableField("content")
	private String content;

	/**
	 * 文件URL（图片、视频、文件消息）
	 */
	@TableField("fileUrl")
	private String fileUrl;

	/**
	 * 缩略图URL（图片、视频消息）
	 */
	@TableField("thumbnailUrl")
	private String thumbnailUrl;

	/**
	 * 文件名（文件消息）
	 */
	@TableField("fileName")
	private String fileName;

	/**
	 * 文件大小（字节）
	 */
	@TableField("fileSize")
	private Long fileSize;

	/**
	 * 文件类型/MIME类型
	 */
	@TableField("fileType")
	private String fileType;

	/**
	 * 消息状态 1-正常 0-已删除 2-已撤回
	 */
	@TableField("status")
	private Integer status;

	/**
	 * 创建时间
	 */
	@TableField("createTime")
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	@TableField("updateTime")
	private LocalDateTime updateTime;
}
