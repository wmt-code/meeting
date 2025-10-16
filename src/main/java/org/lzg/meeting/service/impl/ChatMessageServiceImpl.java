package org.lzg.meeting.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.lzg.meeting.component.CosComponent;
import org.lzg.meeting.component.FFmpegComponent;
import org.lzg.meeting.enums.ChatMessageScopeEnum;
import org.lzg.meeting.enums.ChatMessageTypeEnum;
import org.lzg.meeting.enums.MessageTypeEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.mapper.ChatMessageMapper;
import org.lzg.meeting.model.dto.ChatMessageQueryDTO;
import org.lzg.meeting.model.dto.ChatMessageSendDTO;
import org.lzg.meeting.model.dto.SendMsgDTO;
import org.lzg.meeting.model.entity.ChatMessage;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.ChatMessagePageVO;
import org.lzg.meeting.model.vo.ChatMessageVO;
import org.lzg.meeting.service.IChatMessageService;
import org.lzg.meeting.service.IUserService;
import org.lzg.meeting.websocket.message.MsgHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 聊天消息表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-10-15
 */
@Service
@Slf4j
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

	@Resource
	private CosComponent cosComponent;

	@Resource
	private FFmpegComponent fFmpegComponent;

	@Resource
	private MsgHandler msgHandler;

	@Resource
	private IUserService userService;

	@Override
	public ChatMessage sendMessage(ChatMessageSendDTO sendDTO, Long userId) {
		// 参数校验
		validateSendDTO(sendDTO, userId);

		// 获取发送者信息
		User sender = userService.getById(userId);
		if (sender == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}

		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setMeetingId(sendDTO.getMeetingId());
		chatMessage.setSenderId(userId);
		chatMessage.setSenderName(sender.getUserName());
		chatMessage.setMessageType(sendDTO.getMessageType());
		chatMessage.setMessageScope(sendDTO.getMessageScope());
		chatMessage.setStatus(1);
		chatMessage.setCreateTime(LocalDateTime.now());
		chatMessage.setUpdateTime(LocalDateTime.now());

		// 处理私聊接收者
		if (ChatMessageScopeEnum.PRIVATE.getValue().equals(sendDTO.getMessageScope())) {
			if (sendDTO.getReceiverId() == null) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "私聊消息必须指定接收者");
			}
			User receiver = userService.getById(sendDTO.getReceiverId());
			if (receiver == null) {
				throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接收者不存在");
			}
			chatMessage.setReceiverId(sendDTO.getReceiverId());
			chatMessage.setReceiverName(receiver.getUserName());
		}

		// 根据消息类型处理内容
		ChatMessageTypeEnum messageTypeEnum = ChatMessageTypeEnum.getByValue(sendDTO.getMessageType());
		if (messageTypeEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的消息类型");
		}

		switch (messageTypeEnum) {
			case TEXT:
				handleTextMessage(chatMessage, sendDTO);
				break;
			case IMAGE:
				handleImageMessage(chatMessage, sendDTO);
				break;
			case VIDEO:
				handleVideoMessage(chatMessage, sendDTO);
				break;
			case FILE:
			case AUDIO:
				handleFileMessage(chatMessage, sendDTO);
				break;
			default:
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的消息类型");
		}

		// 保存消息
		boolean saved = this.save(chatMessage);
		if (!saved) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送消息失败");
		}

		// 实时消息推送
		try {
			boolean isGroup = ChatMessageScopeEnum.GROUP.getValue().equals(chatMessage.getMessageScope());
			if (isGroup) {
				// 群聊：推送到会议房间
				SendMsgDTO dto = new SendMsgDTO();
				dto.setMeetingId(chatMessage.getMeetingId());
				dto.setMsgSendType(org.lzg.meeting.enums.MsgSendTypeEnum.GROUP.getValue());
				dto.setMsgType(MessageTypeEnum.CHAT_MEDIA_MESSAGE.getType());
				dto.setSenderId(chatMessage.getSenderId());
				dto.setSenderName(chatMessage.getSenderName());
				dto.setReceiverId(null);
				dto.setMsgContent(chatMessage);
				msgHandler.sendMessage(dto);
			} else {
				// 私聊：给对方 + 自己各推送一条
				// 1) 发给接收者
				SendMsgDTO toReceiver = new SendMsgDTO();
				toReceiver.setMeetingId(chatMessage.getMeetingId());
				toReceiver.setMsgSendType(org.lzg.meeting.enums.MsgSendTypeEnum.USER.getValue());
				toReceiver.setMsgType(MessageTypeEnum.CHAT_MEDIA_MESSAGE.getType());
				toReceiver.setSenderId(chatMessage.getSenderId());
				toReceiver.setSenderName(chatMessage.getSenderName());
				toReceiver.setReceiverId(chatMessage.getReceiverId());
				toReceiver.setMsgContent(chatMessage);
				msgHandler.sendMessage(toReceiver);

				// 2) 回显给自己
				SendMsgDTO toSelf = new SendMsgDTO();
				toSelf.setMeetingId(chatMessage.getMeetingId());
				toSelf.setMsgSendType(org.lzg.meeting.enums.MsgSendTypeEnum.USER.getValue());
				toSelf.setMsgType(MessageTypeEnum.CHAT_MEDIA_MESSAGE.getType());
				toSelf.setSenderId(chatMessage.getSenderId());
				toSelf.setSenderName(chatMessage.getSenderName());
				toSelf.setReceiverId(chatMessage.getSenderId());
				toSelf.setMsgContent(chatMessage);
				msgHandler.sendMessage(toSelf);
			}
		} catch (Exception ex) {
			log.warn("发送聊天实时消息失败：{}", ex.getMessage());
		}

		return chatMessage;
	}

	@Override
	public ChatMessagePageVO queryHistoryMessages(ChatMessageQueryDTO queryDTO, Long userId) {
		if (queryDTO == null || queryDTO.getMeetingId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议ID不能为空");
		}

		// 限制每页大小
		Integer pageSize = queryDTO.getPageSize();
		if (pageSize == null || pageSize <= 0) {
			pageSize = 20;
		}
		if (pageSize > 100) {
			pageSize = 100;
		}

		List<ChatMessage> messages;

		// 根据消息范围查询
		if (ChatMessageScopeEnum.PRIVATE.getValue().equals(queryDTO.getMessageScope())) {
			// 查询私聊消息
			if (queryDTO.getOtherUserId() == null) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询私聊消息时必须指定对方用户ID");
			}
			messages = baseMapper.selectPrivateMessagesByCursor(
					queryDTO.getMeetingId(),
					userId,
					queryDTO.getOtherUserId(),
					queryDTO.getCursor(),
					pageSize + 1 // 多查一条用于判断是否还有更多
			);
		} else if (ChatMessageScopeEnum.GROUP.getValue().equals(queryDTO.getMessageScope())) {
			// 查询群聊消息
			messages = baseMapper.selectGroupMessagesByCursor(
					queryDTO.getMeetingId(),
					queryDTO.getCursor(),
					pageSize + 1);
		} else {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息范围参数错误");
		}

		// 构建返回结果
		ChatMessagePageVO pageVO = new ChatMessagePageVO();
		boolean hasMore = messages.size() > pageSize;
		if (hasMore) {
			messages = messages.subList(0, pageSize);
		}

		List<ChatMessageVO> voList = new ArrayList<>();
		for (ChatMessage message : messages) {
			ChatMessageVO vo = convertToVO(message);
			voList.add(vo);
		}

		pageVO.setMessages(voList);
		pageVO.setHasMore(hasMore);
		if (hasMore && !messages.isEmpty()) {
			pageVO.setNextCursor(messages.get(messages.size() - 1).getId());
		}

		return pageVO;
	}

	@Override
	public Boolean recallMessage(Long messageId, Long userId) {
		if (messageId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息ID不能为空");
		}

		ChatMessage message = this.getById(messageId);
		if (message == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "消息不存在");
		}

		// 只能撤回自己的消息
		if (!message.getSenderId().equals(userId)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能撤回自己的消息");
		}

		// 更新消息状态为已撤回
		ChatMessage updateMessage = new ChatMessage();
		updateMessage.setId(messageId);
		updateMessage.setStatus(2); // 2-已撤回
		updateMessage.setUpdateTime(LocalDateTime.now());

		return this.updateById(updateMessage);
	}

	/**
	 * 校验发送消息参数
	 */
	private void validateSendDTO(ChatMessageSendDTO sendDTO, Long userId) {
		if (sendDTO == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		if (sendDTO.getMeetingId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议ID不能为空");
		}
		if (sendDTO.getMessageType() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息类型不能为空");
		}
		if (sendDTO.getMessageScope() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息范围不能为空");
		}
	}

	/**
	 * 处理文本消息
	 */
	private void handleTextMessage(ChatMessage chatMessage, ChatMessageSendDTO sendDTO) {
		if (StrUtil.isBlank(sendDTO.getContent())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文本消息内容不能为空");
		}
		if (sendDTO.getContent().length() > 5000) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文本消息内容过长");
		}
		chatMessage.setContent(sendDTO.getContent());
	}

	/**
	 * 处理图片消息
	 */
	private void handleImageMessage(ChatMessage chatMessage, ChatMessageSendDTO sendDTO) {
		MultipartFile file = sendDTO.getFile();
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片文件不能为空");
		}

		// 验证文件类型
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能上传图片文件");
		}

		// 上传原图
		String imageUrl = cosComponent.uploadFile(file, "chat/image");
		chatMessage.setFileUrl(imageUrl);
		chatMessage.setFileName(file.getOriginalFilename());
		chatMessage.setFileSize(file.getSize());
		chatMessage.setFileType(contentType);

		// 生成缩略图（改用 ffmpeg）
		try {
			byte[] thumbBytes = fFmpegComponent.createImageThumbnail(file);
			String thumbUrl = cosComponent.uploadBytes(thumbBytes, "image/jpeg", "chat/thumbnail", ".jpg");
			chatMessage.setThumbnailUrl(thumbUrl);
		} catch (Exception e) {
			log.warn("生成图片缩略图失败，回退原图", e);
			chatMessage.setThumbnailUrl(imageUrl);
		}
	}

	/**
	 * 处理视频消息
	 */
	private void handleVideoMessage(ChatMessage chatMessage, ChatMessageSendDTO sendDTO) {
		MultipartFile file = sendDTO.getFile();
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "视频文件不能为空");
		}

		// 验证文件类型
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("video/")) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能上传视频文件");
		}

		// 限制文件大小（100MB）
		if (file.getSize() > 100 * 1024 * 1024) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "视频文件大小不能超过100MB");
		}

		// 上传视频
		String videoUrl = cosComponent.uploadFile(file, "chat/video");
		chatMessage.setFileUrl(videoUrl);
		chatMessage.setFileName(file.getOriginalFilename());
		chatMessage.setFileSize(file.getSize());
		chatMessage.setFileType(contentType);

		// 生成视频封面（首帧）作为缩略图
		try {
			byte[] coverBytes = fFmpegComponent.createVideoCover(file);
			String coverUrl = cosComponent.uploadBytes(coverBytes, "image/jpeg", "chat/video-cover", ".jpg");
			chatMessage.setThumbnailUrl(coverUrl);
		} catch (Exception e) {
			log.warn("生成视频封面失败，忽略", e);
		}
	}

	/**
	 * 处理文件消息
	 */
	private void handleFileMessage(ChatMessage chatMessage, ChatMessageSendDTO sendDTO) {
		MultipartFile file = sendDTO.getFile();
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
		}

		// 限制文件大小（50MB）
		if (file.getSize() > 50 * 1024 * 1024) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过50MB");
		}

		// 上传文件
		String fileUrl = cosComponent.uploadFile(file, "chat/file");
		chatMessage.setFileUrl(fileUrl);
		chatMessage.setFileName(file.getOriginalFilename());
		chatMessage.setFileSize(file.getSize());
		chatMessage.setFileType(file.getContentType());
	}

	/**
	 * 转换为VO对象
	 */
	private ChatMessageVO convertToVO(ChatMessage message) {
		ChatMessageVO vo = new ChatMessageVO();
		BeanUtil.copyProperties(message, vo);

		// 设置枚举描述
		ChatMessageTypeEnum typeEnum = ChatMessageTypeEnum.getByValue(message.getMessageType());
		if (typeEnum != null) {
			vo.setMessageTypeDesc(typeEnum.getDescription());
		}

		ChatMessageScopeEnum scopeEnum = ChatMessageScopeEnum.getByValue(message.getMessageScope());
		if (scopeEnum != null) {
			vo.setMessageScopeDesc(scopeEnum.getDescription());
		}

		return vo;
	}
}
