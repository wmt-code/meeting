package org.lzg.meeting.service;

import org.lzg.meeting.model.dto.MeetingReserveDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.entity.MeetingReserve;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 会议预约表 服务类
 * </p>
 *
 * @author lzg
 * @since 2025-10-10
 */
public interface IMeetingReserveService extends IService<MeetingReserve> {
	/**
	 * 预约会议
	 *
	 * @param meetingReserveDTO 预约会议信息
	 * @param tokenUserInfo     预约人信息
	 * @return 预约成功的会议ID
	 */
	Long reserveMeeting(MeetingReserveDTO meetingReserveDTO, TokenUserInfo tokenUserInfo);

	/**
	 * 取消预约会议
	 *
	 * @param meetingId 会议ID
	 * @return 是否取消成功
	 */
	boolean deleteMeetingReserve(Long meetingId);
}
