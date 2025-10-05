package org.lzg.meeting.websocket.netty;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.enums.MsgSendTypeEnum;
import org.lzg.meeting.model.dto.SendMsgDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.utils.RedisUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChannelContextUtils {
	/**
	 * 用户ID和Channel的映射
	 */
	private static final Map<Long, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();
	/**
	 * 会议号和ChannelGroup的映射
	 */
	private static final Map<Integer, ChannelGroup> MEETING_CHANNEL_GROUP_MAP = new ConcurrentHashMap<>();
	@Resource
	private RedisUtil redisUtil;

	public void addUserChannel(Long userId, Channel channel) {
		String channelId = channel.id().toString();
		AttributeKey<String> attributeKey = null;
		if (!AttributeKey.exists(channelId)) {
			// 如果不存在则创建
			attributeKey = AttributeKey.newInstance(channelId);
		} else {
			// 如果存在则获取
			attributeKey = AttributeKey.valueOf(channelId);
		}
		channel.attr(attributeKey).set(userId.toString());
		log.info("用户{}与Channel建立关联，ChannelId: {}", userId, channelId);
		// 将用户ID和Channel存入映射
		USER_CHANNEL_MAP.put(userId, channel);
		// 获取token
		String token = redisUtil.get(UserConstant.TOKEN + userId);
		if (token == null) {
			return;
		}
		String redisToken = redisUtil.get(UserConstant.TOKEN + token);
		if (StrUtil.isBlank(redisToken)) {
			return;
		}
		TokenUserInfo tokenUserInfo = JSONUtil.toBean(redisToken, TokenUserInfo.class);
		Integer meetingNo = tokenUserInfo.getMeetingNo();
		if (meetingNo == null) {
			return;
		}
		// 将用户加入对应的会议室
		addMeetingRoom(meetingNo, userId);
	}

	public void addMeetingRoom(Integer meetingNo, Long userId) {
		// 获取用户的Channel
		Channel channel = USER_CHANNEL_MAP.get(userId);
		if (channel == null) {
			log.warn("用户{}的Channel不存在，无法加入会议室{}", userId, meetingNo);
			return;
		}
		ChannelGroup channelGroup = MEETING_CHANNEL_GROUP_MAP.get(meetingNo);
		if (channelGroup == null) {
			channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
			MEETING_CHANNEL_GROUP_MAP.put(meetingNo, channelGroup);
		}
		Channel ch = channelGroup.find(channel.id());
		if (ch == null) {
			channelGroup.add(channel);
			log.info("用户{}加入会议室{}", userId, meetingNo);
		} else {
			log.info("用户{}已经在会议室{}中", userId, meetingNo);
		}
	}

	public void sendMsg(SendMsgDTO sendMsgDTO) {
		if (sendMsgDTO == null) {
			return;
		}
		if (MsgSendTypeEnum.USER.getValue().equals(sendMsgDTO.getMsgSendType())) {
			sendMsgToUser(sendMsgDTO);
		} else if (MsgSendTypeEnum.GROUP.getValue().equals(sendMsgDTO.getMsgSendType())) {
			sendMsgToGroup(sendMsgDTO);
		} else {
			log.warn("未知的消息类型：{}", sendMsgDTO.getMsgSendType());
		}
	}

	private void sendMsgToUser(SendMsgDTO sendMsgDTO) {
		Long receiverId = sendMsgDTO.getReceiverId();
		if (receiverId == null) {
			return;
		}
		Channel channel = USER_CHANNEL_MAP.get(receiverId);
		if (channel == null) {
			return;
		}
		channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(sendMsgDTO)));
	}

	private void sendMsgToGroup(SendMsgDTO sendMsgDTO) {
		Long meetingId = sendMsgDTO.getMeetingId();
		if (meetingId == null) {
			return;
		}
		ChannelGroup channelGroup = MEETING_CHANNEL_GROUP_MAP.get(meetingId);
		if (channelGroup == null) {
			return;
		}
		channelGroup.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(sendMsgDTO)));
	}

	public void closeChannel(Long userId) {
		if (StrUtil.isEmpty(userId.toString())) {
			return;
		}
		Channel channel = USER_CHANNEL_MAP.remove(userId);
		if (channel != null) {
			channel.close();
		}
	}
}
