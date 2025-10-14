package org.lzg.meeting.controller;

import java.util.List;

import org.lzg.meeting.annotation.GlobalInterceptor;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.model.dto.ContactSearchDTO;
import org.lzg.meeting.model.dto.MeetingInviteDTO;
import org.lzg.meeting.model.vo.ContactVO;
import org.lzg.meeting.model.vo.InviteResultVO;
import org.lzg.meeting.model.vo.MeetingInvitationVO;
import org.lzg.meeting.service.IContactService;
import org.lzg.meeting.service.IMeetingInvitationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 会议邀请控制器
 */
@Tag(name = "会议邀请管理")
@Slf4j
@RestController
@RequestMapping("/meeting/invitation")
public class MeetingInvitationController extends BaseController {

	@Resource
	private IMeetingInvitationService meetingInvitationService;

	@Resource
	private IContactService contactService;

	/**
	 * 邀请联系人加入会议
	 */
	@Operation(summary = "邀请联系人加入会议")
	@PostMapping("/{meetingId}/invite")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<List<InviteResultVO>> inviteContacts(
			@PathVariable Long meetingId,
			@RequestBody MeetingInviteDTO inviteDTO) {

		if (meetingId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议ID不能为空");
		}

		if (inviteDTO == null || inviteDTO.getInviteeIds() == null || inviteDTO.getInviteeIds().isEmpty()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "被邀请人列表不能为空");
		}

		Long currentUserId = getTokenUserInfo().getUserId();

		List<InviteResultVO> results = meetingInvitationService.inviteContacts(
				meetingId, inviteDTO, currentUserId);

		return ResultUtils.success(results);
	}

	/**
	 * 查询会议邀请历史
	 */
	@Operation(summary = "查询会议邀请历史")
	@GetMapping("/{meetingId}/history")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<List<MeetingInvitationVO>> getInvitationHistory(@PathVariable Long meetingId) {
		if (meetingId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议ID不能为空");
		}

		List<MeetingInvitationVO> history = meetingInvitationService.getInvitationHistory(meetingId);

		return ResultUtils.success(history);
	}

	/**
	 * 查询我收到的邀请
	 */
	@Operation(summary = "查询我收到的邀请")
	@GetMapping("/my")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<List<MeetingInvitationVO>> getMyInvitations() {
		Long currentUserId = getTokenUserInfo().getUserId();

		List<MeetingInvitationVO> invitations = meetingInvitationService.getMyInvitations(currentUserId);

		return ResultUtils.success(invitations);
	}

	/**
	 * 接受邀请
	 */
	@Operation(summary = "接受会议邀请")
	@PostMapping("/{invitationId}/accept")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Void> acceptInvitation(@PathVariable Long invitationId) {
		if (invitationId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "邀请ID不能为空");
		}

		Long currentUserId = getTokenUserInfo().getUserId();

		meetingInvitationService.acceptInvitation(invitationId, currentUserId);

		return ResultUtils.success(null);
	}

	/**
	 * 拒绝邀请
	 */
	@Operation(summary = "拒绝会议邀请")
	@PostMapping("/{invitationId}/reject")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Void> rejectInvitation(@PathVariable Long invitationId) {
		if (invitationId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "邀请ID不能为空");
		}

		Long currentUserId = getTokenUserInfo().getUserId();

		meetingInvitationService.rejectInvitation(invitationId, currentUserId);

		return ResultUtils.success(null);
	}

	/**
	 * 搜索联系人
	 */
	@Operation(summary = "搜索联系人")
	@PostMapping("/contact/search")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<IPage<ContactVO>> searchContacts(@RequestBody ContactSearchDTO searchDTO) {
		if (searchDTO == null) {
			searchDTO = new ContactSearchDTO();
		}

		Long currentUserId = getTokenUserInfo().getUserId();

		IPage<ContactVO> contacts = contactService.searchContacts(searchDTO, currentUserId);

		return ResultUtils.success(contacts);
	}
}
