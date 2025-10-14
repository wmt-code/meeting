package org.lzg.meeting.service;

import java.util.List;

import org.lzg.meeting.model.dto.MeetingInviteDTO;
import org.lzg.meeting.model.entity.MeetingInvitation;
import org.lzg.meeting.model.vo.InviteResultVO;
import org.lzg.meeting.model.vo.MeetingInvitationVO;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 会议邀请服务接口
 */
public interface IMeetingInvitationService extends IService<MeetingInvitation> {

	/**
	 * 邀请联系人加入会议
	 *
	 * @param meetingId 会议ID
	 * @param inviteDTO 邀请信息
	 * @param inviterId 邀请人ID
	 * @return 邀请结果列表
	 */
	List<InviteResultVO> inviteContacts(Long meetingId, MeetingInviteDTO inviteDTO, Long inviterId);

	/**
	 * 查询会议邀请历史记录
	 *
	 * @param meetingId 会议ID
	 * @return 邀请记录列表
	 */
	List<MeetingInvitationVO> getInvitationHistory(Long meetingId);

	/**
	 * 接受邀请
	 *
	 * @param invitationId 邀请ID
	 * @param userId       用户ID
	 */
	void acceptInvitation(Long invitationId, Long userId);

	/**
	 * 拒绝邀请
	 *
	 * @param invitationId 邀请ID
	 * @param userId       用户ID
	 */
	void rejectInvitation(Long invitationId, Long userId);

	/**
	 * 获取用户收到的邀请列表
	 *
	 * @param userId 用户ID
	 * @return 邀请列表
	 */
	List<MeetingInvitationVO> getMyInvitations(Long userId);
}
