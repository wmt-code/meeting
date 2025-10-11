package org.lzg.meeting.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.PageRequest;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.enums.MeetingReserveStatusEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.model.dto.MeetingReserveDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.entity.MeetingReserve;
import org.lzg.meeting.model.entity.MeetingReserveMember;
import org.lzg.meeting.model.vo.MeetingReserveVO;
import org.lzg.meeting.service.IMeetingReserveMemberService;
import org.lzg.meeting.service.IMeetingReserveService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 会议预约表 前端控制器
 * </p>
 *
 * @author lzg
 * @since 2025-10-10
 */
@RestController
@RequestMapping("/meeting-reserve")
public class MeetingReserveController extends BaseController {
	@Resource
	private IMeetingReserveService meetingReserveService;

	@Resource
	private IMeetingReserveMemberService meetingReserveMemberService;

	/**
	 * 分页获取当前用户的会议预约列表
	 *
	 * @return BaseResponse<Page<MeetingReserve>>
	 */
	@PostMapping("/loadMeetingReserveList")
	public BaseResponse<Page<MeetingReserveVO>> loadMeetingReserveList(@RequestBody PageRequest pageRequest) {
		int current = pageRequest.getCurrent();
		int pageSize = pageRequest.getPageSize();
		String sortField = pageRequest.getSortField();
		String sortOrder = pageRequest.getSortOrder();
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		// 只有被邀请的用户能看到会议预约
		// 查出所有邀请了当前用户的会议预约ID
		LambdaQueryWrapper<MeetingReserveMember> queryWrapper = new LambdaQueryWrapper<MeetingReserveMember>()
				.eq(MeetingReserveMember::getInvitateUserId, tokenUserInfo.getUserId());
		List<Long> metingReserveIdList = meetingReserveMemberService.list(queryWrapper).stream()
				.map(MeetingReserveMember::getMeetingId)
				.distinct()
				.toList();
		if (metingReserveIdList.isEmpty()) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "没有会议预约");
		}
		Page<MeetingReserve> meetingReservePage = meetingReserveService.page(new Page<>(current, pageSize),
				new LambdaQueryWrapper<MeetingReserve>()
						.in(MeetingReserve::getMeetingId, metingReserveIdList)
						.eq(MeetingReserve::getStatus, MeetingReserveStatusEnum.RESERVED.getStatus())
						.orderBy(sortOrder.equals("asc"), sortField != null, MeetingReserve::getStartTime));
		Page<MeetingReserveVO> meetingReserveVOPage = new Page<>();
		meetingReserveVOPage.setCurrent(current);
		meetingReserveVOPage.setSize(pageSize);
		meetingReserveVOPage.setTotal(meetingReservePage.getTotal());
		List<MeetingReserveVO> meetingReserveVOList = meetingReservePage.getRecords().stream()
				.map(meetingReserve -> {
					MeetingReserveVO meetingReserveVO = new MeetingReserveVO();
					BeanUtil.copyProperties(meetingReserve, meetingReserveVO);
					return meetingReserveVO;
				}).toList();
		meetingReserveVOPage.setRecords(meetingReserveVOList);
		return ResultUtils.success(meetingReserveVOPage);
	}

	/**
	 * 预约会议
	 *
	 * @param meetingReserveDTO 预约会议信息
	 * @return BaseResponse<Long> 会议ID
	 */
	@PostMapping("/reserveMeeting")
	public BaseResponse<Long> reserveMeeting(@RequestBody MeetingReserveDTO meetingReserveDTO) {
		ThrowUtils.throwIf(meetingReserveDTO == null, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Long meetingId = meetingReserveService.reserveMeeting(meetingReserveDTO, tokenUserInfo);
		return ResultUtils.success(meetingId);
	}

	/**
	 * 创建人删除会议预约
	 *
	 * @param meetingId 会议ID
	 * @return BaseResponse<Boolean> 是否删除成功
	 */
	@GetMapping("/deleteMeetingReserve")
	public BaseResponse<Boolean> deleteMeetingReserve(@RequestParam Long meetingId) {
		ThrowUtils.throwIf(meetingId == null || meetingId <= 0, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		MeetingReserve meetingReserve = meetingReserveService.getById(meetingId);
		ThrowUtils.throwIf(meetingReserve == null, ErrorCode.NOT_FOUND_ERROR, "会议预约不存在");
		ThrowUtils.throwIf(!meetingReserve.getCreateUserId().equals(tokenUserInfo.getUserId()),
				ErrorCode.NO_AUTH_ERROR, "无权限删除该会议预约");
		boolean remove = meetingReserveService.deleteMeetingReserve(meetingId);
		return ResultUtils.success(remove);
	}

	/**
	 * 受邀用户删除会议预约
	 *
	 * @param meetingId 会议ID
	 * @return BaseResponse<Boolean> 是否删除成功
	 */
	@GetMapping("/deleteReserveByUser")
	public BaseResponse<Boolean> deleteReserveByUser(@RequestParam Long meetingId) {
		ThrowUtils.throwIf(meetingId == null || meetingId <= 0, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		MeetingReserve meetingReserve = meetingReserveService.getById(meetingId);
		ThrowUtils.throwIf(meetingReserve == null, ErrorCode.NOT_FOUND_ERROR, "会议预约不存在");
		// 删除邀请用户表中的记录
		boolean res = meetingReserveMemberService.lambdaUpdate()
				.eq(MeetingReserveMember::getMeetingId, meetingId)
				.eq(MeetingReserveMember::getInvitateUserId, tokenUserInfo.getUserId())
				.remove();
		ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "删除会议预约失败");
		return ResultUtils.success(true);
	}

	/**
	 * 根据ID获取会议预约详情
	 *
	 * @param meetingId 会议ID
	 * @return BaseResponse<MeetingReserve> 会议预约详情
	 */
	@GetMapping("/getMeetingReserveById")
	public BaseResponse<MeetingReserveVO> getMeetingReserveById(@RequestParam Long meetingId) {
		ThrowUtils.throwIf(meetingId == null || meetingId <= 0, ErrorCode.PARAMS_ERROR);
		MeetingReserve meetingReserve = meetingReserveService.getById(meetingId);
		ThrowUtils.throwIf(meetingReserve == null, ErrorCode.NOT_FOUND_ERROR, "会议预约不存在");
		MeetingReserveVO meetingReserveVO = new MeetingReserveVO();
		BeanUtil.copyProperties(meetingReserve, meetingReserveVO);
		return ResultUtils.success(meetingReserveVO);
	}
}