package org.lzg.meeting.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.lzg.meeting.component.RedisComponent;
import org.lzg.meeting.enums.*;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.mapper.MeetingMapper;
import org.lzg.meeting.model.dto.*;
import org.lzg.meeting.model.entity.Meeting;
import org.lzg.meeting.service.IMeetingMemberService;
import org.lzg.meeting.service.IMeetingService;
import org.lzg.meeting.websocket.netty.ChannelContextUtils;
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
	@Resource
	private ChannelContextUtils channelContextUtils;
	@Resource
	private IMeetingMemberService meetingMemberService;
	@Resource
	private RedisComponent redisComponent;

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

	@Override
	public void joinMeeting(Long meetingId, Long userId, String userName, Boolean videoOpen) {
		if (null == meetingId) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议ID不能为空");
		}
		Meeting meeting = this.getById(meetingId);
		if (null == meeting || meeting.getStatus() != MeetingStatusEnum.RUNNING.getValue()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议不存在或已结束");
		}
		// 校验用户
		this.checkMeetingJoin(meetingId, userId);
		// 加入成员
		MemberTypeEnum memberTypeEnum = Objects.equals(meeting.getCreateUserId(), userId) ? MemberTypeEnum.ADMIN :
				MemberTypeEnum.COMMON;
		meetingMemberService.addMeetingMember(meetingId, userId, userName, memberTypeEnum.getValue());
		// 加入会议
		this.add2Meeting(meetingId, userId, userName, memberTypeEnum.getValue(), videoOpen);
		// 加入ws
		channelContextUtils.addMeetingRoom(meetingId, userId);
		// 发送ws消息
		// 成员加入消息
		MeetingJoinDTO meetingJoinDTO = new MeetingJoinDTO();
		meetingJoinDTO.setNewMember(redisComponent.getMeetingMemberDTO(meetingId, userId));
		meetingJoinDTO.setMeetingMemberList(redisComponent.getMeetingMemberList(meetingId));

		SendMsgDTO sendMsgDTO = new SendMsgDTO();
		sendMsgDTO.setMsgContent(null);
		sendMsgDTO.setMeetingId(meetingId);
		sendMsgDTO.setMsgType(MessageTypeEnum.ADD_MEETING_ROOM.getType());
		sendMsgDTO.setMsgSendType(MsgSendTypeEnum.GROUP.getValue());
		channelContextUtils.sendMsg(sendMsgDTO);

	}

	@Override
	public Long preJoinMeeting(PreJoinMeetingDTO preJoinMeetingDTO, TokenUserInfo tokenUserInfo) {
		Integer meetingNo = preJoinMeetingDTO.getMeetingNo();
		String nickName = preJoinMeetingDTO.getNickName();
		String password = preJoinMeetingDTO.getPassword();
		if (null == meetingNo || StrUtil.isBlank(nickName)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Long meetingId = tokenUserInfo.getMeetingId();
		if (meetingId != null) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已在会议中,无法加入其他会议");
		}
		Meeting meeting = this.getOne(new QueryWrapper<Meeting>()
				.eq("meetingNo", meetingNo)
				.eq("status", MeetingStatusEnum.RUNNING.getValue()));
		if (null == meeting) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议不存在或已结束");
		}
		// 校验用户类型是否被拉黑
		checkMeetingJoin(meeting.getId(), tokenUserInfo.getUserId());

		if (JoinTypeEnum.NEED_PASSWORD.getCode() == meeting.getJoinType()
				&& !StrUtil.equals(meeting.getJoinPassword(), password)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议密码错误");
		}
		// 更新tokenUserInfo
		tokenUserInfo.setMeetingId(meeting.getId());
		tokenUserInfo.setUserName(nickName);
		redisComponent.updateTokenUserInfo(tokenUserInfo);
		return meeting.getId();
	}

	private void checkMeetingJoin(Long meetingId, Long userId) {
		MeetingMemberDTO meetingMemberDTO = redisComponent.getMeetingMemberDTO(meetingId, userId);
		if (meetingMemberDTO != null && MeetingMemberStatusEnum.BLACKLIST.getStatus() == meetingMemberDTO.getStatus()) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您已被拉黑，无法再次加入");
		}
	}


	private void add2Meeting(Long meetingId, Long userId, String userName, Integer memberType, Boolean videoOpen) {
		MeetingMemberDTO meetingMemberDTO = new MeetingMemberDTO();
		meetingMemberDTO.setUserId(userId);
		meetingMemberDTO.setMeetingId(meetingId);
		meetingMemberDTO.setUserName(userName);
		meetingMemberDTO.setMemberType(memberType);
		meetingMemberDTO.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
		meetingMemberDTO.setVideoOpen(videoOpen);
		meetingMemberDTO.setLastJoinTime(LocalDateTime.now());
		redisComponent.add2Meeting(meetingId, meetingMemberDTO);
	}
}
