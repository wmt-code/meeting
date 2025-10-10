package org.lzg.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lzg.meeting.enums.MeetingMemberStatusEnum;
import org.lzg.meeting.model.dto.PreJoinMeetingDTO;
import org.lzg.meeting.model.dto.QuickMeetingDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.entity.Meeting;

/**
 * <p>
 * 会议表 服务类
 * </p>
 *
 * @author lzg
 * @since 2025-10-03
 */
public interface IMeetingService extends IService<Meeting> {
	/**
	 * 快速创建会议
	 *
	 * @param quickMeetingDTO 快速创建会议参数
	 * @param tokenUserInfo   用户信息
	 * @return 会议id
	 */
	Long quickMeeting(QuickMeetingDTO quickMeetingDTO, TokenUserInfo tokenUserInfo);

	/**
	 * 加入会议
	 *
	 * @param meetingId 会议ID
	 * @param userId    用户ID
	 * @param userName  用户昵称
	 * @param videoOpen 是否开启视频
	 */
	void joinMeeting(Long meetingId, Long userId, String userName, Boolean videoOpen);

	/**
	 * 预加入会议
	 *
	 * @param preJoinMeetingDTO 预加入会议参数
	 * @param tokenUserInfo     用户信息
	 * @return 会议id
	 */
	Long preJoinMeeting(PreJoinMeetingDTO preJoinMeetingDTO, TokenUserInfo tokenUserInfo);

	/**
	 * 退出会议
	 *
	 * @param tokenUserInfo           用户信息
	 * @param meetingMemberStatusEnum 退出会议的状态
	 * @return 是否成功
	 */
	Boolean exitMeeting(TokenUserInfo tokenUserInfo, MeetingMemberStatusEnum meetingMemberStatusEnum);

	/**
	 * 结束会议
	 * @param meetingId 会议ID
	 * @return 是否成功
	 */
	Boolean finishMeeting(Long meetingId);

	/**
	 * 强制某人退出会议
	 * @param userId 	 用户ID
	 * @param tokenUserInfo 操作者信息
	 * @param meetingMemberStatusEnum 退出会议的状态
	 * @return 是否成功
	 */
	Boolean forceExitingMeeting(Long userId, TokenUserInfo tokenUserInfo, MeetingMemberStatusEnum meetingMemberStatusEnum);
}
