package org.lzg.meeting.service.impl;

import org.lzg.meeting.enums.MeetingMemberStatusEnum;
import org.lzg.meeting.enums.MeetingStatusEnum;
import org.lzg.meeting.model.entity.MeetingMember;
import org.lzg.meeting.mapper.MeetingMemberMapper;
import org.lzg.meeting.service.IMeetingMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
		MeetingMember meetingMember = new MeetingMember();
		meetingMember.setMeetingId(meetingId);
		meetingMember.setUserId(userId);
		meetingMember.setNickName(userName);
		meetingMember.setLastJoinTime(LocalDateTime.now());
		meetingMember.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
		meetingMember.setMemberType(memberType);
		meetingMember.setMeetingStatus(MeetingStatusEnum.RUNNING.getValue());
		boolean saveOrUpdate = this.saveOrUpdate(meetingMember);
		if (!saveOrUpdate) {
			throw new RuntimeException("添加会议成员失败");
		}
	}
}
