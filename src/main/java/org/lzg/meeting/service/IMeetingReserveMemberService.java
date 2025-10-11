package org.lzg.meeting.service;

import org.lzg.meeting.model.entity.MeetingReserveMember;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 邀请用户表 服务类
 * </p>
 *
 * @author lzg
 * @since 2025-10-10
 */
public interface IMeetingReserveMemberService extends IService<MeetingReserveMember> {
	/**
	 * 邀请用户参加会议
	 *
	 * @param meetingId        会议ID
	 * @param inviteUserIdList 邀请用户ID列表
	 * @return 是否邀请成功
	 */
	boolean inviteUsers(Long meetingId, List<Long> inviteUserIdList);
}
