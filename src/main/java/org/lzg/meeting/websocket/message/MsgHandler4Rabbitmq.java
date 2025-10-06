package org.lzg.meeting.websocket.message;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.config.RabbitMQConfig;
import org.lzg.meeting.constant.Constants;
import org.lzg.meeting.model.dto.SendMsgDTO;
import org.lzg.meeting.websocket.netty.ChannelContextUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = Constants.MESSAGE_HANDLE_CHANNEL_KEY, havingValue =
		Constants.MESSAGE_HANDLE_CHANNEL_RABBITMQ)
public class MsgHandler4Rabbitmq implements MsgHandler {
	@Resource
	private RabbitTemplate rabbitTemplate;
	@Resource
	private ChannelContextUtils channelContextUtils;

	@Override
	public void listener() {
	}

	@Override
	public void sendMessage(SendMsgDTO sendMsgDTO) {
		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "", sendMsgDTO);
	}

	@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
	public void onMessage(SendMsgDTO sendMsgDTO) {
		// 处理接收到的消息
		log.info("收到RabbitMQ消息: {}", sendMsgDTO);
		channelContextUtils.sendMsg(sendMsgDTO);
	}
}
