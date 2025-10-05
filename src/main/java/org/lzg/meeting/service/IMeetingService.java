package org.lzg.meeting.service;

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
}
