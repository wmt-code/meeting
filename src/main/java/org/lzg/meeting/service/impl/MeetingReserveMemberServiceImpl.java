package org.lzg.meeting.service.impl;

import org.lzg.meeting.model.entity.MeetingReserveMember;
import org.lzg.meeting.mapper.MeetingReserveMemberMapper;
import org.lzg.meeting.service.IMeetingReserveMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 邀请用户表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-10-10
 */
@Service
public class MeetingReserveMemberServiceImpl extends ServiceImpl<MeetingReserveMemberMapper, MeetingReserveMember> implements IMeetingReserveMemberService {

	@Override
	public boolean inviteUsers(Long meetingId, List<Long> inviteUserIdList) {
		if (inviteUserIdList == null || inviteUserIdList.isEmpty()) {
			return false;
		}
		// 批量插入邀请用户
		inviteUserIdList.forEach(userId -> {
			MeetingReserveMember meetingReserveMember = new MeetingReserveMember();
			meetingReserveMember.setMeetingId(meetingId);
			meetingReserveMember.setInvitateUserId(userId);
			this.save(meetingReserveMember);
		});
		return true;
	}
}
