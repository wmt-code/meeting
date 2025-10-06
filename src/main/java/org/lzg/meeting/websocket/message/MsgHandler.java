package org.lzg.meeting.websocket.message;

import org.lzg.meeting.model.dto.SendMsgDTO;
import org.springframework.stereotype.Component;

/**
 * @description: 消息处理接口
 */
@Component("msgHandler")
public interface MsgHandler {
	/**
	 * 监听消息
	 */
	void listener();

	/**
	 * 发送消息
	 *
	 * @param sendMsgDTO 发送消息参数
	 */
	void sendMessage(SendMsgDTO sendMsgDTO);
}
