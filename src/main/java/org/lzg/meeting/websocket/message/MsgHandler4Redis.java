package org.lzg.meeting.websocket.message;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.constant.Constants;
import org.lzg.meeting.model.dto.SendMsgDTO;
import org.lzg.meeting.websocket.netty.ChannelContextUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 基于redis的消息处理器
 * 只有在配置文件中配置 message.handle.channel=redis 时该类才会被加载
 */
@Slf4j
@Component
@ConditionalOnProperty(name = Constants.MESSAGE_HANDLE_CHANNEL_KEY, havingValue =
		Constants.MESSAGE_HANDLE_CHANNEL_REDIS)
public class MsgHandler4Redis implements MsgHandler {
	private static final String MESSAGE_TOPIC = "message.topic";
	@Resource
	private RedissonClient redissonClient;
	@Resource
	private ChannelContextUtils channelContextUtils;

	/**
	 * 监听 Redis 消息
	 */
	@Override
	public void listener() {
		RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
		rTopic.addListener(SendMsgDTO.class, (channel, msg) -> {
			log.info("收到Redis消息: {}", JSONUtil.toJsonStr(msg));
			// 发送消息
			channelContextUtils.sendMsg(msg);
		});
	}

	/**
	 * 发送消息
	 *
	 * @param sendMsgDTO 发送消息参数
	 */
	@Override
	public void sendMessage(SendMsgDTO sendMsgDTO) {
		redissonClient.getTopic(MESSAGE_TOPIC).publish(sendMsgDTO);
	}

	@PreDestroy
	public void preDestroy() {
		redissonClient.shutdown();
	}
}
