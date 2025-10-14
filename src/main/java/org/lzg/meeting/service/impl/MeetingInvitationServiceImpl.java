package org.lzg.meeting.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.lzg.meeting.component.RedisComponent;
import org.lzg.meeting.enums.InvitationStatusEnum;
import org.lzg.meeting.enums.InviteTypeEnum;
import org.lzg.meeting.enums.MeetingStatusEnum;
import org.lzg.meeting.enums.MemberTypeEnum;
import org.lzg.meeting.enums.MessageTypeEnum;
import org.lzg.meeting.enums.MsgSendTypeEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.mapper.MeetingInvitationMapper;
import org.lzg.meeting.model.dto.MeetingInviteDTO;
import org.lzg.meeting.model.dto.SendMsgDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.entity.Meeting;
import org.lzg.meeting.model.entity.MeetingInvitation;
import org.lzg.meeting.model.entity.MeetingMember;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.InviteResultVO;
import org.lzg.meeting.model.vo.MeetingInvitationVO;
import org.lzg.meeting.service.IMeetingInvitationService;
import org.lzg.meeting.service.IMeetingMemberService;
import org.lzg.meeting.service.IMeetingService;
import org.lzg.meeting.service.IUserService;
import org.lzg.meeting.websocket.message.MsgHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 会议邀请服务实现类
 */
@Slf4j
@Service
public class MeetingInvitationServiceImpl extends ServiceImpl<MeetingInvitationMapper, MeetingInvitation>
		implements IMeetingInvitationService {

	@Resource
	private IMeetingService meetingService;

	@Resource
	private IMeetingMemberService meetingMemberService;

	@Resource
	private IUserService userService;

	@Resource
	private MsgHandler msgHandler;

	@Resource
	private RedisComponent redisComponent;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public List<InviteResultVO> inviteContacts(Long meetingId, MeetingInviteDTO inviteDTO, Long inviterId) {
		log.info("开始邀请联系人加入会议，meetingId={}, inviterId={}, inviteeIds={}",
				meetingId, inviterId, inviteDTO.getInviteeIds());

		// 参数校验
		if (inviteDTO.getInviteeIds() == null || inviteDTO.getInviteeIds().isEmpty()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "被邀请人列表不能为空");
		}

		if (inviteDTO.getInviteType() == null) {
			inviteDTO.setInviteType(InviteTypeEnum.INTERNAL_MESSAGE.getType());
		}

		// 验证会议是否存在
		Meeting meeting = meetingService.getById(meetingId);
		if (meeting == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议不存在");
		}

		// 验证会议状态（只有进行中的会议才能邀请）
		if (!MeetingStatusEnum.RUNNING.getValue().equals(meeting.getStatus())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "只有进行中的会议才能邀请联系人");
		}

		// 验证邀请人权限（必须是主持人或创建者）
		if (!meeting.getCreateUserId().equals(inviterId)) {
			LambdaQueryWrapper<MeetingMember> memberQuery = new LambdaQueryWrapper<>();
			memberQuery.eq(MeetingMember::getMeetingId, meetingId)
					.eq(MeetingMember::getUserId, inviterId)
					.eq(MeetingMember::getMemberType, MemberTypeEnum.ADMIN.getValue());
			MeetingMember member = meetingMemberService.getOne(memberQuery);

			if (member == null) {
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有会议创建者或主持人可以邀请联系人");
			}
		}

		List<InviteResultVO> results = new ArrayList<>();

		// 获取邀请人信息
		User inviter = userService.getById(inviterId);
		if (inviter == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "邀请人不存在");
		}

		// 批量查询被邀请人信息
		List<User> invitees = userService.listByIds(inviteDTO.getInviteeIds());
		Map<Long, User> userMap = invitees.stream()
				.collect(Collectors.toMap(User::getId, user -> user));

		// 查询已存在的邀请记录
		LambdaQueryWrapper<MeetingInvitation> existQuery = new LambdaQueryWrapper<>();
		existQuery.eq(MeetingInvitation::getMeetingId, meetingId)
				.in(MeetingInvitation::getInviteeId, inviteDTO.getInviteeIds());
		List<MeetingInvitation> existingInvitations = this.list(existQuery);
		Map<Long, MeetingInvitation> existingMap = existingInvitations.stream()
				.collect(Collectors.toMap(MeetingInvitation::getInviteeId, inv -> inv));

		// 查询已在会议中的成员
		LambdaQueryWrapper<MeetingMember> memberQuery = new LambdaQueryWrapper<>();
		memberQuery.eq(MeetingMember::getMeetingId, meetingId)
				.in(MeetingMember::getUserId, inviteDTO.getInviteeIds());
		List<MeetingMember> existingMembers = meetingMemberService.list(memberQuery);
		Map<Long, MeetingMember> memberMap = existingMembers.stream()
				.collect(Collectors.toMap(MeetingMember::getUserId, member -> member));

		// 逐个处理邀请
		for (Long inviteeId : inviteDTO.getInviteeIds()) {
			InviteResultVO result = new InviteResultVO();
			result.setInviteeId(inviteeId);

			User invitee = userMap.get(inviteeId);
			if (invitee != null) {
				result.setInviteeName(invitee.getUserName());
			}

			try {
				// 验证被邀请人是否存在
				if (invitee == null) {
					result.setSuccess(false);
					result.setFailReason("用户不存在");
					results.add(result);
					continue;
				}

				// 不能邀请自己
				if (inviteeId.equals(inviterId)) {
					result.setSuccess(false);
					result.setFailReason("不能邀请自己");
					results.add(result);
					continue;
				}

				// 检查是否已在会议中
				if (memberMap.containsKey(inviteeId)) {
					result.setSuccess(false);
					result.setFailReason("该用户已在会议中");
					results.add(result);
					continue;
				}

				// 检查是否已有邀请记录
				MeetingInvitation existingInvitation = existingMap.get(inviteeId);
				if (existingInvitation != null) {
					// 如果已有未响应的邀请，更新邀请信息
					if (InvitationStatusEnum.NO_RESPONSE.getStatus().equals(existingInvitation.getStatus())) {
						existingInvitation.setInviteType(inviteDTO.getInviteType());
						existingInvitation.setInviteMessage(inviteDTO.getInviteMessage());
						existingInvitation.setUpdateTime(LocalDateTime.now());
						this.updateById(existingInvitation);

						result.setSuccess(true);
						log.info("更新邀请记录成功，inviteeId={}", inviteeId);

						// 更新邀请也发送通知
						sendInvitationNotification(meeting, invitee, existingInvitation, inviter);
					} else {
						result.setSuccess(false);
						result.setFailReason("该用户已有处理过的邀请记录");
					}
					results.add(result);
					continue;
				}

				// 创建新邀请记录
				MeetingInvitation invitation = new MeetingInvitation();
				invitation.setMeetingId(meetingId);
				invitation.setInviterId(inviterId);
				invitation.setInviteeId(inviteeId);
				invitation.setInviteType(inviteDTO.getInviteType());
				invitation.setStatus(InvitationStatusEnum.NO_RESPONSE.getStatus());
				invitation.setInviteMessage(inviteDTO.getInviteMessage());

				this.save(invitation);

				result.setSuccess(true);
				log.info("创建邀请记录成功，inviteeId={}", inviteeId);

				// 发送邀请通知
				sendInvitationNotification(meeting, invitee, invitation, inviter);

			} catch (Exception e) {
				log.error("邀请用户失败，inviteeId={}", inviteeId, e);
				result.setSuccess(false);
				result.setFailReason("系统错误：" + e.getMessage());
			}

			results.add(result);
		}

		log.info("邀请联系人完成，成功: {}, 失败: {}",
				results.stream().filter(InviteResultVO::getSuccess).count(),
				results.stream().filter(r -> !r.getSuccess()).count());

		return results;
	}

	@Override
	public List<MeetingInvitationVO> getInvitationHistory(Long meetingId) {
		log.info("查询会议邀请历史，meetingId={}", meetingId);

		// 查询邀请记录
		LambdaQueryWrapper<MeetingInvitation> query = new LambdaQueryWrapper<>();
		query.eq(MeetingInvitation::getMeetingId, meetingId)
				.orderByDesc(MeetingInvitation::getCreateTime);
		List<MeetingInvitation> invitations = this.list(query);

		if (invitations.isEmpty()) {
			return new ArrayList<>();
		}

		// 获取会议信息
		Meeting meeting = meetingService.getById(meetingId);

		// 获取所有相关用户ID
		List<Long> userIds = new ArrayList<>();
		invitations.forEach(inv -> {
			userIds.add(inv.getInviterId());
			userIds.add(inv.getInviteeId());
		});

		// 批量查询用户信息
		List<User> users = userService.listByIds(userIds.stream().distinct().collect(Collectors.toList()));
		Map<Long, User> userMap = users.stream()
				.collect(Collectors.toMap(User::getId, user -> user));

		// 组装返回结果
		List<MeetingInvitationVO> result = new ArrayList<>();
		for (MeetingInvitation invitation : invitations) {
			MeetingInvitationVO vo = new MeetingInvitationVO();
			vo.setId(invitation.getId());
			vo.setMeetingId(invitation.getMeetingId());
			if (meeting != null) {
				vo.setMeetingName(meeting.getMeetingName());
			}

			vo.setInviterId(invitation.getInviterId());
			User inviter = userMap.get(invitation.getInviterId());
			if (inviter != null) {
				vo.setInviterName(inviter.getUserName());
			}

			vo.setInviteeId(invitation.getInviteeId());
			User invitee = userMap.get(invitation.getInviteeId());
			if (invitee != null) {
				vo.setInviteeName(invitee.getUserName());
				vo.setInviteeAccount(invitee.getUserAccount());
				vo.setInviteeAvatar(invitee.getAvatar());
			}

			vo.setInviteType(invitation.getInviteType());
			InviteTypeEnum typeEnum = InviteTypeEnum.fromType(invitation.getInviteType());
			if (typeEnum != null) {
				vo.setInviteTypeDesc(typeEnum.getDescription());
			}

			vo.setStatus(invitation.getStatus());
			InvitationStatusEnum statusEnum = InvitationStatusEnum.fromStatus(invitation.getStatus());
			if (statusEnum != null) {
				vo.setStatusDesc(statusEnum.getDescription());
			}

			vo.setInviteMessage(invitation.getInviteMessage());
			vo.setCreateTime(invitation.getCreateTime());
			vo.setResponseTime(invitation.getResponseTime());

			result.add(vo);
		}

		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void acceptInvitation(Long invitationId, Long userId) {
		log.info("接受邀请，invitationId={}, userId={}", invitationId, userId);

		// 查询邀请记录
		MeetingInvitation invitation = this.getById(invitationId);
		if (invitation == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "邀请记录不存在");
		}

		// 验证是否是被邀请人
		if (!invitation.getInviteeId().equals(userId)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
		}

		// 验证邀请状态
		if (!InvitationStatusEnum.NO_RESPONSE.getStatus().equals(invitation.getStatus())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邀请已处理");
		}

		// 验证会议是否存在且进行中
		Meeting meeting = meetingService.getById(invitation.getMeetingId());
		if (meeting == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议不存在");
		}

		if (!MeetingStatusEnum.RUNNING.getValue().equals(meeting.getStatus())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议已结束或已取消");
		}

		// 更新邀请状态
		invitation.setStatus(InvitationStatusEnum.ACCEPTED.getStatus());
		invitation.setResponseTime(LocalDateTime.now());
		this.updateById(invitation);

		log.info("接受邀请成功，开始自动加入会议");

		// 获取被邀请人信息
		User invitee = userService.getById(userId);
		if (invitee == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}

		// 检查用户是否已在其他会议中
		String token = redisComponent.getToken(userId);
		if (token != null) {
			TokenUserInfo tokenUserInfo = redisComponent.getTokenUserInfo(token);
			if (tokenUserInfo.getMeetingId() != null && !tokenUserInfo.getMeetingId().equals(meeting.getId())) {
				throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已在其他会议中，无法加入");
			}
		}

		// 自动加入会议
		try {
			meetingService.joinMeeting(meeting.getId(), userId, invitee.getUserName(), false);
			log.info("用户自动加入会议成功，userId={}, meetingId={}", userId, meeting.getId());

			// 发送接受邀请通知给邀请人
			sendAcceptNotification(meeting, invitee, invitation);
		} catch (Exception e) {
			log.error("自动加入会议失败，userId={}, meetingId={}", userId, meeting.getId(), e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "加入会议失败：" + e.getMessage());
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void rejectInvitation(Long invitationId, Long userId) {
		log.info("拒绝邀请，invitationId={}, userId={}", invitationId, userId);

		// 查询邀请记录
		MeetingInvitation invitation = this.getById(invitationId);
		if (invitation == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "邀请记录不存在");
		}

		// 验证是否是被邀请人
		if (!invitation.getInviteeId().equals(userId)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
		}

		// 验证邀请状态
		if (!InvitationStatusEnum.NO_RESPONSE.getStatus().equals(invitation.getStatus())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邀请已处理");
		}

		// 更新邀请状态
		invitation.setStatus(InvitationStatusEnum.REJECTED.getStatus());
		invitation.setResponseTime(LocalDateTime.now());
		this.updateById(invitation);

		log.info("拒绝邀请成功");

		// 获取被邀请人信息
		User invitee = userService.getById(userId);
		// 获取会议信息
		Meeting meeting = meetingService.getById(invitation.getMeetingId());

		// 发送拒绝邀请通知给邀请人
		if (invitee != null && meeting != null) {
			sendRejectNotification(meeting, invitee, invitation);
		}
	}

	@Override
	public List<MeetingInvitationVO> getMyInvitations(Long userId) {
		log.info("查询用户收到的邀请，userId={}", userId);

		// 查询未响应的邀请
		LambdaQueryWrapper<MeetingInvitation> query = new LambdaQueryWrapper<>();
		query.eq(MeetingInvitation::getInviteeId, userId)
				.eq(MeetingInvitation::getStatus, InvitationStatusEnum.NO_RESPONSE.getStatus())
				.orderByDesc(MeetingInvitation::getCreateTime);
		List<MeetingInvitation> invitations = this.list(query);

		if (invitations.isEmpty()) {
			return new ArrayList<>();
		}

		// 获取所有会议ID和邀请人ID
		List<Long> meetingIds = invitations.stream()
				.map(MeetingInvitation::getMeetingId)
				.distinct()
				.collect(Collectors.toList());

		List<Long> inviterIds = invitations.stream()
				.map(MeetingInvitation::getInviterId)
				.distinct()
				.collect(Collectors.toList());

		// 批量查询会议信息
		List<Meeting> meetings = meetingService.listByIds(meetingIds);
		Map<Long, Meeting> meetingMap = meetings.stream()
				.collect(Collectors.toMap(Meeting::getId, meeting -> meeting));

		// 批量查询邀请人信息
		List<User> inviters = userService.listByIds(inviterIds);
		Map<Long, User> inviterMap = inviters.stream()
				.collect(Collectors.toMap(User::getId, user -> user));

		// 组装返回结果
		List<MeetingInvitationVO> result = new ArrayList<>();
		for (MeetingInvitation invitation : invitations) {
			// 过滤已结束的会议
			Meeting meeting = meetingMap.get(invitation.getMeetingId());
			if (meeting == null || !MeetingStatusEnum.RUNNING.getValue().equals(meeting.getStatus())) {
				continue;
			}

			MeetingInvitationVO vo = new MeetingInvitationVO();
			vo.setId(invitation.getId());
			vo.setMeetingId(invitation.getMeetingId());
			vo.setMeetingName(meeting.getMeetingName());

			vo.setInviterId(invitation.getInviterId());
			User inviter = inviterMap.get(invitation.getInviterId());
			if (inviter != null) {
				vo.setInviterName(inviter.getUserName());
			}

			vo.setInviteeId(invitation.getInviteeId());

			vo.setInviteType(invitation.getInviteType());
			InviteTypeEnum typeEnum = InviteTypeEnum.fromType(invitation.getInviteType());
			if (typeEnum != null) {
				vo.setInviteTypeDesc(typeEnum.getDescription());
			}

			vo.setStatus(invitation.getStatus());
			InvitationStatusEnum statusEnum = InvitationStatusEnum.fromStatus(invitation.getStatus());
			if (statusEnum != null) {
				vo.setStatusDesc(statusEnum.getDescription());
			}

			vo.setInviteMessage(invitation.getInviteMessage());
			vo.setCreateTime(invitation.getCreateTime());

			result.add(vo);
		}

		return result;
	}

	/**
	 * 发送邀请通知
	 *
	 * @param meeting    会议信息
	 * @param invitee    被邀请人信息
	 * @param invitation 邀请记录
	 * @param inviter    邀请人信息
	 */
	private void sendInvitationNotification(Meeting meeting, User invitee,
											MeetingInvitation invitation, User inviter) {
		try {
			// 构建邀请通知消息内容
			Map<String, Object> msgContent = new HashMap<>();
			msgContent.put("invitationId", invitation.getId());
			msgContent.put("meetingId", meeting.getId());
			msgContent.put("meetingNo", meeting.getMeetingNo());
			msgContent.put("meetingName", meeting.getMeetingName());
			msgContent.put("inviterId", inviter.getId());
			msgContent.put("inviterName", inviter.getUserName());
			msgContent.put("inviteeId", invitee.getId());
			msgContent.put("inviteeName", invitee.getUserName());
			msgContent.put("inviteMessage", invitation.getInviteMessage());
			msgContent.put("inviteType", invitation.getInviteType());
			msgContent.put("createTime", invitation.getCreateTime());

			// 构建发送消息DTO
			SendMsgDTO sendMsgDTO = new SendMsgDTO();
			sendMsgDTO.setMsgType(MessageTypeEnum.INVITE_MEMBER_MEETING.getType());
			sendMsgDTO.setMsgSendType(MsgSendTypeEnum.USER.getValue());
			sendMsgDTO.setMeetingId(meeting.getId());
			sendMsgDTO.setSenderId(inviter.getId());
			sendMsgDTO.setSenderName(inviter.getUserName());
			sendMsgDTO.setReceiverId(invitee.getId());
			sendMsgDTO.setMsgContent(msgContent);
			sendMsgDTO.setSendTime(LocalDateTime.now());

			// 发送消息
			msgHandler.sendMessage(sendMsgDTO);

			log.info("发送邀请通知成功，inviterId={}, inviteeId={}, meetingId={}",
					inviter.getId(), invitee.getId(), meeting.getId());
		} catch (Exception e) {
			log.error("发送邀请通知失败，inviterId={}, inviteeId={}, meetingId={}",
					inviter.getId(), invitee.getId(), meeting.getId(), e);
			// 不抛出异常，避免影响邀请流程
		}
	}

	/**
	 * 发送接受邀请通知给邀请人
	 *
	 * @param meeting    会议信息
	 * @param invitee    被邀请人信息
	 * @param invitation 邀请记录
	 */
	private void sendAcceptNotification(Meeting meeting, User invitee, MeetingInvitation invitation) {
		try {
			// 获取邀请人信息
			User inviter = userService.getById(invitation.getInviterId());
			if (inviter == null) {
				log.warn("邀请人不存在，无法发送接受通知，inviterId={}", invitation.getInviterId());
				return;
			}

			// 构建接受邀请通知消息内容
			Map<String, Object> msgContent = new HashMap<>();
			msgContent.put("invitationId", invitation.getId());
			msgContent.put("meetingId", meeting.getId());
			msgContent.put("meetingNo", meeting.getMeetingNo());
			msgContent.put("meetingName", meeting.getMeetingName());
			msgContent.put("inviteeId", invitee.getId());
			msgContent.put("inviteeName", invitee.getUserName());
			msgContent.put("inviteeAccount", invitee.getUserAccount());
			msgContent.put("inviteeAvatar", invitee.getAvatar());
			msgContent.put("action", "accepted");
			msgContent.put("message", invitee.getUserName() + " 已接受您的邀请并加入会议");
			msgContent.put("responseTime", invitation.getResponseTime());

			// 构建发送消息DTO - 发送给邀请人
			SendMsgDTO sendMsgDTO = new SendMsgDTO();
			sendMsgDTO.setMsgType(MessageTypeEnum.INVITE_MEMBER_MEETING.getType());
			sendMsgDTO.setMsgSendType(MsgSendTypeEnum.USER.getValue());
			sendMsgDTO.setMeetingId(meeting.getId());
			sendMsgDTO.setSenderId(invitee.getId());
			sendMsgDTO.setSenderName(invitee.getUserName());
			sendMsgDTO.setReceiverId(inviter.getId()); // 发送给邀请人
			sendMsgDTO.setMsgContent(msgContent);
			sendMsgDTO.setSendTime(LocalDateTime.now());

			// 发送消息
			msgHandler.sendMessage(sendMsgDTO);

			log.info("发送接受邀请通知成功，inviteeId={}, inviterId={}, meetingId={}",
					invitee.getId(), inviter.getId(), meeting.getId());
		} catch (Exception e) {
			log.error("发送接受邀请通知失败，inviteeId={}, inviterId={}, meetingId={}",
					invitee.getId(), invitation.getInviterId(), meeting.getId(), e);
			// 不抛出异常，避免影响主流程
		}
	}

	/**
	 * 发送拒绝邀请通知给邀请人
	 *
	 * @param meeting    会议信息
	 * @param invitee    被邀请人信息
	 * @param invitation 邀请记录
	 */
	private void sendRejectNotification(Meeting meeting, User invitee, MeetingInvitation invitation) {
		try {
			// 获取邀请人信息
			User inviter = userService.getById(invitation.getInviterId());
			if (inviter == null) {
				log.warn("邀请人不存在，无法发送拒绝通知，inviterId={}", invitation.getInviterId());
				return;
			}

			// 构建拒绝邀请通知消息内容
			Map<String, Object> msgContent = new HashMap<>();
			msgContent.put("invitationId", invitation.getId());
			msgContent.put("meetingId", meeting.getId());
			msgContent.put("meetingNo", meeting.getMeetingNo());
			msgContent.put("meetingName", meeting.getMeetingName());
			msgContent.put("inviteeId", invitee.getId());
			msgContent.put("inviteeName", invitee.getUserName());
			msgContent.put("inviteeAccount", invitee.getUserAccount());
			msgContent.put("inviteeAvatar", invitee.getAvatar());
			msgContent.put("action", "rejected");
			msgContent.put("message", invitee.getUserName() + " 已拒绝您的会议邀请");
			msgContent.put("responseTime", invitation.getResponseTime());

			// 构建发送消息DTO - 发送给邀请人
			SendMsgDTO sendMsgDTO = new SendMsgDTO();
			sendMsgDTO.setMsgType(MessageTypeEnum.INVITE_MEMBER_MEETING.getType());
			sendMsgDTO.setMsgSendType(MsgSendTypeEnum.USER.getValue());
			sendMsgDTO.setMeetingId(meeting.getId());
			sendMsgDTO.setSenderId(invitee.getId());
			sendMsgDTO.setSenderName(invitee.getUserName());
			sendMsgDTO.setReceiverId(inviter.getId()); // 发送给邀请人
			sendMsgDTO.setMsgContent(msgContent);
			sendMsgDTO.setSendTime(LocalDateTime.now());

			// 发送消息
			msgHandler.sendMessage(sendMsgDTO);

			log.info("发送拒绝邀请通知成功，inviteeId={}, inviterId={}, meetingId={}",
					invitee.getId(), inviter.getId(), meeting.getId());
		} catch (Exception e) {
			log.error("发送拒绝邀请通知失败，inviteeId={}, inviterId={}, meetingId={}",
					invitee.getId(), invitation.getInviterId(), meeting.getId(), e);
			// 不抛出异常，避免影响主流程
		}
	}
}
