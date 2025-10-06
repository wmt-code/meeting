package org.lzg.meeting.service;

import org.lzg.meeting.model.dto.PreJoinMeetingDTO;
import org.lzg.meeting.model.dto.QuickMeetingDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.entity.Meeting;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
