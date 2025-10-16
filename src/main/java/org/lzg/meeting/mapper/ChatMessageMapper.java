package org.lzg.meeting.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.lzg.meeting.model.entity.ChatMessage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 聊天消息表 Mapper 接口
 * </p>
 *
 * @author lzg
 * @since 2025-10-15
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

	/**
	 * 游标分页查询群聊消息
	 *
	 * @param meetingId 会议ID
	 * @param cursor    游标（消息ID，小于该ID的消息）
	 * @param pageSize  每页大小
	 * @return 消息列表
	 */
	List<ChatMessage> selectGroupMessagesByCursor(@Param("meetingId") Long meetingId,
	                                               @Param("cursor") Long cursor,
	                                               @Param("pageSize") Integer pageSize);

	/**
	 * 游标分页查询私聊消息
	 *
	 * @param meetingId   会议ID
	 * @param currentUserId 当前用户ID
	 * @param otherUserId 对方用户ID
	 * @param cursor      游标（消息ID，小于该ID的消息）
	 * @param pageSize    每页大小
	 * @return 消息列表
	 */
	List<ChatMessage> selectPrivateMessagesByCursor(@Param("meetingId") Long meetingId,
	                                                 @Param("currentUserId") Long currentUserId,
	                                                 @Param("otherUserId") Long otherUserId,
	                                                 @Param("cursor") Long cursor,
	                                                 @Param("pageSize") Integer pageSize);
}
