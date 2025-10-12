package org.lzg.meeting.constant;

public interface Constants {
	String MEETING_ROOM_KEY = "meetingRoom:";

	// 处理消息的渠道配置key
	String MESSAGE_HANDLE_CHANNEL_KEY = "message.handle.channel";

	String MESSAGE_HANDLE_CHANNEL_REDIS = "redis";

	String MESSAGE_HANDLE_CHANNEL_RABBITMQ = "rabbitmq";

	// 好友系统相关缓存Key
	String FRIEND_LIST_KEY = "friend:list:";
}
