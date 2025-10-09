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
import org.lzg.meeting.component.RedisComponent;
import org.lzg.meeting.enums.MeetingMemberStatusEnum;
import org.lzg.meeting.enums.MessageTypeEnum;
import org.lzg.meeting.enums.MsgSendTypeEnum;
import org.lzg.meeting.model.dto.ExitMeetingDTO;
import org.lzg.meeting.model.dto.MeetingMemberDTO;
import org.lzg.meeting.model.dto.SendMsgDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	private static final Map<Long, ChannelGroup> MEETING_CHANNEL_GROUP_MAP = new ConcurrentHashMap<>();
	@Resource
	private RedisComponent redisComponent;

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
		String token = redisComponent.getToken(userId);
		if (StrUtil.isEmpty(token)) {
			log.warn("用户{}的token不存在，无法加入会议室", userId);
			return;
		}
		// 获取用户信息
		TokenUserInfo tokenUserInfo = redisComponent.getTokenUserInfo(token);
		Long meetingId = tokenUserInfo.getMeetingId();
		if (meetingId == null) {
			return;
		}
		// 将用户加入对应的会议室
		addMeetingRoom(meetingId, userId);
	}

	public void addMeetingRoom(Long meetingId, Long userId) {
		// 获取用户的Channel
		Channel channel = USER_CHANNEL_MAP.get(userId);
		if (channel == null) {
			log.warn("用户{}的Channel不存在，无法加入会议室{}", userId, meetingId);
			return;
		}
		ChannelGroup channelGroup = MEETING_CHANNEL_GROUP_MAP.get(meetingId);
		if (channelGroup == null) {
			channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
			MEETING_CHANNEL_GROUP_MAP.put(meetingId, channelGroup);
		}
		Channel ch = channelGroup.find(channel.id());
		if (ch == null) {
			channelGroup.add(channel);
			log.info("用户{}加入会议室{}", userId, meetingId);
		} else {
			log.info("用户{}已经在会议室{}中", userId, meetingId);
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
		// 退出会议
		if (Objects.equals(MessageTypeEnum.EXIT_MEETING_ROOM.getType(), sendMsgDTO.getMsgType())) {
			ExitMeetingDTO exitMeetingDTO = (ExitMeetingDTO) sendMsgDTO.getMsgContent();
			removeChannelFromMeetingRoom(meetingId, exitMeetingDTO.getExitUserId());
			List<MeetingMemberDTO> meetingMemberList = redisComponent.getMeetingMemberList(meetingId);
			// 获取在线人数
			List<MeetingMemberDTO> onlineMembers =
					meetingMemberList.stream().filter(item -> Objects.equals(MeetingMemberStatusEnum.NORMAL.getStatus(), item.getStatus())).toList();
			// 如果会议室没人了，移除会议室
			if (onlineMembers.isEmpty()) {
				MEETING_CHANNEL_GROUP_MAP.remove(meetingId);
				log.info("会议室{}已无人在线，移除会议室", meetingId);
			}
		}
		// 结束会议
		if (Objects.equals(MessageTypeEnum.FINIS_MEETING.getType(), sendMsgDTO.getMsgType())) {
			List<MeetingMemberDTO> meetingMemberList = redisComponent.getMeetingMemberList(meetingId);
			for (MeetingMemberDTO member : meetingMemberList) {
				removeChannelFromMeetingRoom(meetingId, member.getUserId());
			}
			MEETING_CHANNEL_GROUP_MAP.remove(meetingId);
			log.info("会议室{}已结束，移除会议室", meetingId);
		}
	}

	private void removeChannelFromMeetingRoom(Long meetingId, Long exitUserId) {
		Channel channel = USER_CHANNEL_MAP.get(exitUserId);
		if (channel == null) {
			return;
		}
		ChannelGroup channelGroup = MEETING_CHANNEL_GROUP_MAP.get(meetingId);
		if (channelGroup != null) {
			channelGroup.remove(channel);
			log.info("用户{}已从会议室{}移除", exitUserId, meetingId);
		}
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
