package org.lzg.meeting.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.lzg.meeting.annotation.GlobalInterceptor;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.enums.MeetingMemberStatusEnum;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.model.dto.JoinReserveMeetingDTO;
import org.lzg.meeting.model.dto.PreJoinMeetingDTO;
import org.lzg.meeting.model.dto.QuickMeetingDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.entity.Meeting;
import org.lzg.meeting.model.entity.MeetingMember;
import org.lzg.meeting.service.IMeetingMemberService;
import org.lzg.meeting.service.IMeetingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 会议表 前端控制器
 * </p>
 *
 * @author lzg
 * @since 2025-10-03
 */
@RestController
@RequestMapping("/meeting")
@Tag(name = "会议管理", description = "会议相关接口")
public class MeetingController extends BaseController {
	@Resource
	private IMeetingService meetingService;
	@Resource
	private IMeetingMemberService meetingMemberService;

	/**
	 * 快速创建会议
	 *
	 * @param quickMeetingDTO 快速创建会议参数
	 * @return 会议ID
	 */
	@PostMapping("/quickMeeting")
	@Operation(summary = "快速创建会议")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Long> quickMeeting(@RequestBody QuickMeetingDTO quickMeetingDTO) {
		ThrowUtils.throwIf(null == quickMeetingDTO, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Long meetingId = meetingService.quickMeeting(quickMeetingDTO, tokenUserInfo);
		// 更新tokenUserInfo
		tokenUserInfo.setMeetingId(meetingId);
		resetTokenUserInfo(tokenUserInfo);
		return ResultUtils.success(meetingId);
	}

	/**
	 * 预加入会议
	 *
	 * @param preJoinMeetingDTO 预加入会议参数
	 * @return 会议ID
	 */
	@PostMapping("/preJoinMeeting")
	@Operation(summary = "预加入会议")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Long> preJoinMeeting(@RequestBody PreJoinMeetingDTO preJoinMeetingDTO) {
		ThrowUtils.throwIf(null == preJoinMeetingDTO, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Long meetingId = meetingService.preJoinMeeting(preJoinMeetingDTO, tokenUserInfo);
		return ResultUtils.success(meetingId);
	}

	/**
	 * 加入会议
	 *
	 * @param videoOPen 是否打开摄像头
	 * @return 是否成功
	 */
	@PostMapping("/joinMeeting")
	@Operation(summary = "加入会议")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Boolean> joinMeeting(Boolean videoOPen) {
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		meetingService.joinMeeting(tokenUserInfo.getMeetingId(), tokenUserInfo.getUserId(),
				tokenUserInfo.getUserName(), videoOPen);
		return ResultUtils.success(true);
	}

	/**
	 * 用户退出会议
	 *
	 * @return 是否成功
	 */
	@GetMapping("/exitMeeting")
	@Operation(summary = "退出会议")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Boolean> exitMeeting() {
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Boolean res = meetingService.exitMeeting(tokenUserInfo, MeetingMemberStatusEnum.EXIT_MEETING);
		return ResultUtils.success(res);
	}

	/**
	 * 将某个用户踢出会议
	 *
	 * @param userId 用户ID
	 * @return 是否成功
	 */
	@GetMapping("/kickOutMeeting")
	@Operation(summary = "将某个用户踢出会议")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Boolean> kickOutMeeting(@RequestParam Long userId) {
		ThrowUtils.throwIf(null == userId || userId <= 0, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Boolean res = meetingService.forceExitingMeeting(userId, tokenUserInfo, MeetingMemberStatusEnum.KICK_OUT);
		return ResultUtils.success(res);
	}

	/**
	 * 将某个用户拉黑
	 *
	 * @param userId 用户ID
	 * @return 是否成功
	 */
	@GetMapping("/blackList")
	@Operation(summary = "将某个用户拉黑")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Boolean> blackList(@RequestParam Long userId) {
		ThrowUtils.throwIf(null == userId || userId <= 0, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Boolean res = meetingService.forceExitingMeeting(userId, tokenUserInfo, MeetingMemberStatusEnum.BLACKLIST);
		return ResultUtils.success(res);
	}

	/**
	 * 结束会议
	 *
	 * @return 会议信息
	 */
	@GetMapping("/finishMeeting")
	@Operation(summary = "结束会议")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Meeting> finishMeeting() {
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Long meetingId = tokenUserInfo.getMeetingId();
		ThrowUtils.throwIf(null == meetingId, ErrorCode.NOT_FOUND_ERROR);
		Meeting meeting = meetingService.getById(meetingId);
		// 只有主持人能够结束会议
		ThrowUtils.throwIf(null == meeting || (!meeting.getCreateUserId().equals(tokenUserInfo.getUserId()) && !UserConstant.ADMIN_ROLE.equals(tokenUserInfo.getUserRole())),
				ErrorCode.NO_AUTH_ERROR);
		Boolean res = meetingService.finishMeeting(meetingId);
		ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "结束会议失败");
		return ResultUtils.success(meeting);
	}

	/**
	 * 删除会议记录
	 *
	 * @param meetingId 会议ID
	 * @return 是否成功
	 */
	@GetMapping("/deleteMeetingRecord")
	@Operation(summary = "删除会议记录")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Boolean> deleteMeetingRecord(@RequestParam Long meetingId) {
		ThrowUtils.throwIf(null == meetingId || meetingId <= 0, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		MeetingMember meetingMember = new MeetingMember();
		meetingMember.setStatus(MeetingMemberStatusEnum.DEL_MEETING.getStatus());
		boolean update = meetingMemberService.lambdaUpdate()
				.eq(MeetingMember::getMeetingId, meetingId)
				.eq(MeetingMember::getUserId, tokenUserInfo.getUserId())
				.update(meetingMember);
		ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "删除会议记录失败");
		return ResultUtils.success(true);
	}

	/**
	 * 加载会议成员列表
	 *
	 * @param meetingId 会议ID
	 * @return 会议成员列表
	 */
	@GetMapping("/loadMeetingMembers")
	@Operation(summary = "加载会议成员列表")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<List<MeetingMember>> loadMeetingMembers(@RequestParam Long meetingId) {
		ThrowUtils.throwIf(null == meetingId || meetingId <= 0, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		List<MeetingMember> meetingMemberList = meetingMemberService.lambdaQuery()
				.eq(MeetingMember::getMeetingId, meetingId)
				.list();
		List<MeetingMember> list = meetingMemberList.stream().filter(item -> item
				.getUserId().equals(tokenUserInfo.getUserId())).toList();
		ThrowUtils.throwIf(list.isEmpty(), ErrorCode.NO_AUTH_ERROR);
		return ResultUtils.success(list);
	}

	/**
	 * 受邀用户加入预约会议
	 *
	 * @param joinReserveMeetingDTO 加入预约会议参数
	 * @return 会议ID
	 */
	@PostMapping("joinReserveMeeting")
	@Operation(summary = "受邀用户加入预约会议")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Long> joinReserveMeeting(@RequestBody JoinReserveMeetingDTO joinReserveMeetingDTO) {
		ThrowUtils.throwIf(null == joinReserveMeetingDTO, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Long meetingId = meetingService.joinReserveMeeting(joinReserveMeetingDTO, tokenUserInfo);
		return ResultUtils.success(meetingId);
	}
}
