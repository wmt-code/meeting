package org.lzg.meeting.service;

import org.lzg.meeting.model.dto.ChatMessageQueryDTO;
import org.lzg.meeting.model.dto.ChatMessageSendDTO;
import org.lzg.meeting.model.entity.ChatMessage;
import org.lzg.meeting.model.vo.ChatMessagePageVO;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 聊天消息表 服务类
 * </p>
 *
 * @author lzg
 * @since 2025-10-15
 */
public interface IChatMessageService extends IService<ChatMessage> {

	/**
	 * 发送聊天消息
	 *
	 * @param sendDTO 发送消息DTO
	 * @param userId  当前用户ID
	 * @return 消息信息
	 */
	ChatMessage sendMessage(ChatMessageSendDTO sendDTO, Long userId);

	/**
	 * 查询历史聊天消息（游标分页）
	 *
	 * @param queryDTO 查询条件
	 * @param userId   当前用户ID
	 * @return 分页结果
	 */
	ChatMessagePageVO queryHistoryMessages(ChatMessageQueryDTO queryDTO, Long userId);

	/**
	 * 撤回消息
	 *
	 * @param messageId 消息ID
	 * @param userId    当前用户ID
	 * @return 是否成功
	 */
	Boolean recallMessage(Long messageId, Long userId);
}
