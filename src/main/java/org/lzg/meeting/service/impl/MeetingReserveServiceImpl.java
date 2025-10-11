package org.lzg.meeting.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.lzg.meeting.enums.JoinTypeEnum;
import org.lzg.meeting.enums.MeetingReserveStatusEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.mapper.MeetingReserveMapper;
import org.lzg.meeting.model.dto.MeetingReserveDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.entity.MeetingReserve;
import org.lzg.meeting.model.entity.MeetingReserveMember;
import org.lzg.meeting.service.IMeetingReserveMemberService;
import org.lzg.meeting.service.IMeetingReserveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 会议预约表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-10-10
 */
@Service
public class MeetingReserveServiceImpl extends ServiceImpl<MeetingReserveMapper, MeetingReserve> implements IMeetingReserveService {
	@Resource
	private IMeetingReserveMemberService meetingReserveMemberService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long reserveMeeting(MeetingReserveDTO meetingReserveDTO, TokenUserInfo tokenUserInfo) {
		LocalDateTime startTime = meetingReserveDTO.getStartTime();
		String meetingName = meetingReserveDTO.getMeetingName();
		Integer duration = meetingReserveDTO.getDuration();
		Integer joinType = meetingReserveDTO.getJoinType();
		if (ObjUtil.hasNull(startTime, meetingName, duration, joinType)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String joinPassword = meetingReserveDTO.getJoinPassword();
		List<Long> inviteUserIdList = meetingReserveDTO.getInviteUserIdList();
		Long userId = tokenUserInfo.getUserId();
		MeetingReserve meetingReserve = new MeetingReserve();
		meetingReserve.setMeetingName(meetingName);
		meetingReserve.setStartTime(startTime);
		meetingReserve.setJoinType(joinType);
		if (JoinTypeEnum.NEED_PASSWORD.getCode().equals(joinType) &&
				StrUtil.isNotBlank(joinPassword)
		) {
			meetingReserve.setJoinPassword(joinPassword);

		}
		meetingReserve.setDuration(duration);
		meetingReserve.setCreateUserId(userId);
		meetingReserve.setCreateTime(LocalDateTime.now());
		meetingReserve.setStatus(MeetingReserveStatusEnum.RESERVED.getStatus());
		boolean save = this.save(meetingReserve);
		ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "预约会议失败");
		// 添加预约人到邀请用户列表
		boolean addCreateUser2ReserveMember = meetingReserveMemberService.inviteUsers(meetingReserve.getMeetingId(),
				List.of(userId));
		ThrowUtils.throwIf(!addCreateUser2ReserveMember, ErrorCode.OPERATION_ERROR, "添加创建人至预约会议成员失败");
		// 预约会议成功后，添加邀请用户
		if (!inviteUserIdList.isEmpty()) {
			boolean res = meetingReserveMemberService.inviteUsers(meetingReserve.getMeetingId(), inviteUserIdList);
			ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "邀请用户失败");
		}
		return meetingReserve.getMeetingId();
	}

	@Override
	public boolean deleteMeetingReserve(Long meetingId) {
		boolean res = this.removeById(meetingId);
		ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "取消预约会议失败");
		// 级联删除邀请用户
		boolean remove = meetingReserveMemberService.lambdaUpdate()
				.eq(MeetingReserveMember::getMeetingId, meetingId)
				.remove();
		ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR, "取消预约会议失败");
		return true;
	}
}
