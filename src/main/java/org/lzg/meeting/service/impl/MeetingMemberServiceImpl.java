package org.lzg.meeting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lzg.meeting.enums.MeetingMemberStatusEnum;
import org.lzg.meeting.enums.MeetingStatusEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.mapper.MeetingMemberMapper;
import org.lzg.meeting.model.entity.MeetingMember;
import org.lzg.meeting.service.IMeetingMemberService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 会议成员关联表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-10-03
 */
@Service
public class MeetingMemberServiceImpl extends ServiceImpl<MeetingMemberMapper, MeetingMember> implements IMeetingMemberService {
	/**
	 * 添加会议成员
	 *
	 * @param meetingId  会议ID
	 * @param userId     用户ID
	 * @param userName   用户昵称
	 * @param memberType 成员类型
	 */
	@Override
	public void addMeetingMember(Long meetingId, Long userId, String userName, Integer memberType) {
		MeetingMember exist = this.lambdaQuery()
				.eq(MeetingMember::getMeetingId, meetingId)
				.eq(MeetingMember::getUserId, userId)
				.one();
		MeetingMember meetingMember = new MeetingMember();
		meetingMember.setMeetingId(meetingId);
		meetingMember.setUserId(userId);
		meetingMember.setNickName(userName);
		meetingMember.setLastJoinTime(LocalDateTime.now());
		meetingMember.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
		meetingMember.setMemberType(memberType);
		meetingMember.setMeetingStatus(MeetingStatusEnum.RUNNING.getValue());
		if (exist != null) {
			meetingMember.setId(exist.getId());
		}
		boolean saveOrUpdate = this.saveOrUpdate(meetingMember);
		if (!saveOrUpdate) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "添加会议成员失败");
		}
	}
}
