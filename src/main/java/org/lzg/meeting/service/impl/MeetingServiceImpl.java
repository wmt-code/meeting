package org.lzg.meeting.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lzg.meeting.enums.JoinTypeEnum;
import org.lzg.meeting.enums.MeetingNoTypeEnum;
import org.lzg.meeting.enums.MeetingStatusEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.mapper.MeetingMapper;
import org.lzg.meeting.model.dto.QuickMeetingDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.entity.Meeting;
import org.lzg.meeting.service.IMeetingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 会议表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-10-03
 */
@Service
public class MeetingServiceImpl extends ServiceImpl<MeetingMapper, Meeting> implements IMeetingService {

	@Override
	public Long quickMeeting(QuickMeetingDTO quickMeetingDTO, TokenUserInfo tokenUserInfo) {
		Long meetingId = tokenUserInfo.getMeetingId();
		if (meetingId != null) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已在会议中");
		}
		Integer meetingNoType = quickMeetingDTO.getMeetingNoType();
		String meetingName = quickMeetingDTO.getMeetingName();
		Integer joinType = quickMeetingDTO.getJoinType();
		if (StrUtil.hasBlank(meetingNoType.toString(), meetingName, joinType.toString())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String meetingPassword = quickMeetingDTO.getMeetingPassword();
		Meeting meeting = new Meeting();
		meeting.setMeetingName(meetingName);
		meeting.setCreateTime(LocalDateTime.now());
		meeting.setCreateUserId(tokenUserInfo.getUserId());
		meeting.setJoinType(joinType);
		if (JoinTypeEnum.NEED_PASSWORD.getCode() == joinType) {
			// 需要密码
			if (StrUtil.isBlank(meetingPassword) || meetingPassword.length() < 4) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议密码不能为空且不能少于4位");
			}
			meeting.setJoinPassword(meetingPassword);
		} else {
			meeting.setJoinPassword("");
		}
		// 会议号类型 0 使用个人会议号 1 使用随机会议号
		if (Objects.equals(MeetingNoTypeEnum.PERSONAL_MEETING_NO.getValue(), meetingNoType)) {
			// 使用个人会议号
			Integer meetingNo = tokenUserInfo.getMeetingNo();
			if (meetingNo == null) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "个人会议号不存在，请先设置个人会议号");
			}
			meeting.setMeetingNo(meetingNo);
		} else {
			// 随机会议号
			int randomInt = RandomUtil.randomInt(100000000, 999999999);
			meeting.setMeetingNo(randomInt);
		}
		meeting.setStartTime(LocalDateTime.now());
		meeting.setStatus(MeetingStatusEnum.RUNNING.getValue());
		boolean save = this.save(meeting);
		ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建会议失败");
		return meeting.getId();
	}
}
