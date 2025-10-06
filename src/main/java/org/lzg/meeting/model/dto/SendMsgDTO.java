package org.lzg.meeting.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SendMsgDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * 消息传输数据对象，包含消息的相关信息。
	 *
	 * @param <T> 消息内容的类型，支持泛型以适应不同类型的消息内容。
	 */
	private Integer msgSendType; // 消息发送类型。
	private Long meetingId; // 会议的唯一标识符，用于关联消息与会议。
	private Integer msgType; // 消息类型，例如文本消息、文件消息等。
	private Long senderId; // 发送者的用户ID，用于标识消息的发送者。
	private String senderName; // 发送者的用户名。
	private Object msgContent; // 消息的具体内容，支持泛型以适应不同类型的消息。
	private Long receiverId; // 接收者的用户ID，用于指定消息的目标用户。
	private LocalDateTime sendTime; // 消息的发送时间，记录消息的时间戳。
	private Long msgId; // 消息的唯一标识符，用于区分不同的消息。
	private Integer status; // 消息的状态，例如已发送、已接收或已读。
	private String fileName; // 文件消息的文件名，仅在消息类型为文件时使用。
	private Integer fileType; // 文件类型，例如图片、文档等，仅在消息类型为文件时使用。
	private Long fileSize; // 文件大小，仅在消息类型为文件时使用。

}
