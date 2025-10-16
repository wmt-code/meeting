package org.lzg.meeting.controller;

import org.lzg.meeting.annotation.GlobalInterceptor;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.model.dto.ChatMessageQueryDTO;
import org.lzg.meeting.model.dto.ChatMessageSendDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.entity.ChatMessage;
import org.lzg.meeting.model.vo.ChatMessagePageVO;
import org.lzg.meeting.service.IChatMessageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 聊天消息管理 前端控制器
 * </p>
 *
 * @author lzg
 * @since 2025-10-15
 */
@RestController
@RequestMapping("/chat")
@Slf4j
@Tag(name = "聊天消息管理", description = "聊天消息发送、查询等接口")
public class ChatMessageController extends BaseController {

	@Resource
	private IChatMessageService chatMessageService;

	/**
	 * 发送文本消息
	 *
	 * @param sendDTO 发送消息DTO
	 * @return 消息信息
	 */
	@PostMapping("/send/text")
	@Operation(summary = "发送文本消息")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<ChatMessage> sendTextMessage(@RequestBody ChatMessageSendDTO sendDTO) {
		ThrowUtils.throwIf(sendDTO == null, ErrorCode.PARAMS_ERROR);
		sendDTO.setMessageType(1); // 文本
		
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		ChatMessage message = chatMessageService.sendMessage(sendDTO, tokenUserInfo.getUserId());
		return ResultUtils.success(message);
	}

	/**
	 * 发送图片消息
	 *
	 * @param meetingId    会议ID
	 * @param messageScope 消息范围 1-私聊 2-群聊
	 * @param receiverId   接收者ID（私聊时必填）
	 * @param file         图片文件
	 * @return 消息信息
	 */
	@PostMapping("/send/image")
	@Operation(summary = "发送图片消息")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<ChatMessage> sendImageMessage(
			@RequestParam Long meetingId,
			@RequestParam Integer messageScope,
			@RequestParam(required = false) Long receiverId,
			@RequestParam("file") MultipartFile file) {
		ThrowUtils.throwIf(meetingId == null, ErrorCode.PARAMS_ERROR, "会议ID不能为空");
		ThrowUtils.throwIf(messageScope == null, ErrorCode.PARAMS_ERROR, "消息范围不能为空");
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "图片文件不能为空");

		ChatMessageSendDTO sendDTO = new ChatMessageSendDTO();
		sendDTO.setMeetingId(meetingId);
		sendDTO.setMessageScope(messageScope);
		sendDTO.setReceiverId(receiverId);
		sendDTO.setMessageType(2); // 图片
		sendDTO.setFile(file);

		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		ChatMessage message = chatMessageService.sendMessage(sendDTO, tokenUserInfo.getUserId());
		return ResultUtils.success(message);
	}

	/**
	 * 发送视频消息
	 *
	 * @param meetingId    会议ID
	 * @param messageScope 消息范围 1-私聊 2-群聊
	 * @param receiverId   接收者ID（私聊时必填）
	 * @param file         视频文件
	 * @return 消息信息
	 */
	@PostMapping("/send/video")
	@Operation(summary = "发送视频消息")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<ChatMessage> sendVideoMessage(
			@RequestParam Long meetingId,
			@RequestParam Integer messageScope,
			@RequestParam(required = false) Long receiverId,
			@RequestParam("file") MultipartFile file) {
		ThrowUtils.throwIf(meetingId == null, ErrorCode.PARAMS_ERROR, "会议ID不能为空");
		ThrowUtils.throwIf(messageScope == null, ErrorCode.PARAMS_ERROR, "消息范围不能为空");
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "视频文件不能为空");

		ChatMessageSendDTO sendDTO = new ChatMessageSendDTO();
		sendDTO.setMeetingId(meetingId);
		sendDTO.setMessageScope(messageScope);
		sendDTO.setReceiverId(receiverId);
		sendDTO.setMessageType(3); // 视频
		sendDTO.setFile(file);

		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		ChatMessage message = chatMessageService.sendMessage(sendDTO, tokenUserInfo.getUserId());
		return ResultUtils.success(message);
	}

	/**
	 * 发送文件消息
	 *
	 * @param meetingId    会议ID
	 * @param messageScope 消息范围 1-私聊 2-群聊
	 * @param receiverId   接收者ID（私聊时必填）
	 * @param file         文件
	 * @return 消息信息
	 */
	@PostMapping("/send/file")
	@Operation(summary = "发送文件消息")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<ChatMessage> sendFileMessage(
			@RequestParam Long meetingId,
			@RequestParam Integer messageScope,
			@RequestParam(required = false) Long receiverId,
			@RequestParam("file") MultipartFile file) {
		ThrowUtils.throwIf(meetingId == null, ErrorCode.PARAMS_ERROR, "会议ID不能为空");
		ThrowUtils.throwIf(messageScope == null, ErrorCode.PARAMS_ERROR, "消息范围不能为空");
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");

		ChatMessageSendDTO sendDTO = new ChatMessageSendDTO();
		sendDTO.setMeetingId(meetingId);
		sendDTO.setMessageScope(messageScope);
		sendDTO.setReceiverId(receiverId);
		sendDTO.setMessageType(4); // 文件
		sendDTO.setFile(file);

		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		ChatMessage message = chatMessageService.sendMessage(sendDTO, tokenUserInfo.getUserId());
		return ResultUtils.success(message);
	}

	/**
	 * 查询历史聊天消息（游标分页）
	 *
	 * @param queryDTO 查询条件
	 * @return 分页结果
	 */
	@PostMapping("/history")
	@Operation(summary = "查询历史聊天消息")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<ChatMessagePageVO> queryHistory(@RequestBody ChatMessageQueryDTO queryDTO) {
		ThrowUtils.throwIf(queryDTO == null, ErrorCode.PARAMS_ERROR);
		
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		ChatMessagePageVO result = chatMessageService.queryHistoryMessages(queryDTO, tokenUserInfo.getUserId());
		return ResultUtils.success(result);
	}

	/**
	 * 撤回消息
	 *
	 * @param messageId 消息ID
	 * @return 是否成功
	 */
	@DeleteMapping("/recall/{messageId}")
	@Operation(summary = "撤回消息")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Boolean> recallMessage(@PathVariable Long messageId) {
		ThrowUtils.throwIf(messageId == null, ErrorCode.PARAMS_ERROR, "消息ID不能为空");
		
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Boolean result = chatMessageService.recallMessage(messageId, tokenUserInfo.getUserId());
		return ResultUtils.success(result);
	}
}
