package org.lzg.meeting.controller;


import jakarta.annotation.Resource;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.model.dto.PreJoinMeetingDTO;
import org.lzg.meeting.model.dto.QuickMeetingDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.service.IMeetingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
