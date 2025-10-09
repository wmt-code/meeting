package org.lzg.meeting.controller;


import jakarta.annotation.Resource;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.enums.MeetingMemberStatusEnum;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.model.dto.PreJoinMeetingDTO;
import org.lzg.meeting.model.dto.QuickMeetingDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.entity.Meeting;
import org.lzg.meeting.service.IMeetingService;
import org.springframework.web.bind.annotation.*;

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
public class MeetingController extends BaseController {
	@Resource
	private IMeetingService meetingService;

	/**
	 * 快速创建会议
	 *
	 * @param quickMeetingDTO 快速创建会议参数
	 * @return 会议ID
	 */
	@PostMapping("/quickMeeting")
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
	public BaseResponse<Boolean> blackList(@RequestParam Long userId) {
		ThrowUtils.throwIf(null == userId || userId <= 0, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Boolean res = meetingService.forceExitingMeeting(userId, tokenUserInfo, MeetingMemberStatusEnum.BLACKLIST);
		return ResultUtils.success(res);
	}

	@GetMapping("/finishMeeting")
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
}
