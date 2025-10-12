package org.lzg.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.constant.Constants;
import org.lzg.meeting.enums.FriendRequestStatusEnum;
import org.lzg.meeting.enums.FriendshipStatusEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.mapper.FriendRequestMapper;
import org.lzg.meeting.model.dto.FriendApplyDTO;
import org.lzg.meeting.model.entity.FriendRequest;
import org.lzg.meeting.model.entity.Friendship;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.FriendRequestVO;
import org.lzg.meeting.service.IFriendRequestService;
import org.lzg.meeting.service.IFriendshipService;
import org.lzg.meeting.service.IUserService;
import org.lzg.meeting.utils.RedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 好友申请表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-10-11
 */
@Slf4j
@Service
public class FriendRequestServiceImpl extends ServiceImpl<FriendRequestMapper, FriendRequest> implements IFriendRequestService {

	@Resource
	private IFriendshipService friendshipService;

	@Resource
	private IUserService userService;

	@Resource
	private RedisUtil redisUtil;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sendApply(FriendApplyDTO friendApplyDTO, Long currentUserId) {
		Long toUserId = friendApplyDTO.getToUserId();

		// 参数校验
		if (toUserId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "接收人ID不能为空");
		}

		// 不能向自己发送申请
		if (currentUserId.equals(toUserId)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能向自己发送申请");
		}

		// 检查接收人是否存在
		User toUser = userService.getById(toUserId);
		if (toUser == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接收人不存在");
		}

		// 检查是否已经是好友
		LambdaQueryWrapper<Friendship> friendshipQuery = new LambdaQueryWrapper<>();
		friendshipQuery.eq(Friendship::getUserId, currentUserId)
				.eq(Friendship::getFriendId, toUserId)
				.eq(Friendship::getStatus, FriendshipStatusEnum.FRIEND.getStatus());
		Friendship existingFriendship = friendshipService.getOne(friendshipQuery);
		if (existingFriendship != null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "对方已经是您的好友");
		}

		// 检查是否被对方拉黑
		LambdaQueryWrapper<Friendship> blockedQuery = new LambdaQueryWrapper<>();
		blockedQuery.eq(Friendship::getUserId, toUserId)
				.eq(Friendship::getFriendId, currentUserId)
				.eq(Friendship::getStatus, FriendshipStatusEnum.BLOCKED.getStatus());
		Friendship blockedRelation = friendshipService.getOne(blockedQuery);
		if (blockedRelation != null) {
			throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无法发送好友申请");
		}

		// 检查双方关系状态：如果我删除了对方，但对方仍保留我为好友，则直接恢复好友关系
		LambdaQueryWrapper<Friendship> myRelationQuery = new LambdaQueryWrapper<>();
		myRelationQuery.eq(Friendship::getUserId, currentUserId)
				.eq(Friendship::getFriendId, toUserId);
		Friendship myRelation = friendshipService.getOne(myRelationQuery);

		LambdaQueryWrapper<Friendship> otherRelationQuery = new LambdaQueryWrapper<>();
		otherRelationQuery.eq(Friendship::getUserId, toUserId)
				.eq(Friendship::getFriendId, currentUserId);
		Friendship otherRelation = friendshipService.getOne(otherRelationQuery);

		Integer myStatus = myRelation != null ? myRelation.getStatus() : null;
		Integer otherStatus = otherRelation != null ? otherRelation.getStatus() : null;

		log.info("发送申请检查 - 我方状态: {}, 对方状态: {}", myStatus, otherStatus);

		// 场景：我删除了对方（myStatus=DELETED），但对方仍保留我为好友（otherStatus=FRIEND）
		// 此时直接恢复好友关系，无需对方再次同意
		if (myRelation != null && otherRelation != null
				&& FriendshipStatusEnum.DELETED.getStatus().equals(myStatus)
				&& FriendshipStatusEnum.FRIEND.getStatus().equals(otherStatus)) {
			log.info("检测到对方仍保留好友关系，直接恢复为好友");

			// 恢复我方关系为FRIEND
			myRelation.setStatus(FriendshipStatusEnum.FRIEND.getStatus());
			friendshipService.updateById(myRelation);

			// 清除双方缓存
			clearFriendListCache(currentUserId);
			clearFriendListCache(toUserId);

			log.info("好友关系已自动恢复");
			return;  // 直接返回，不需要创建申请
		}

		// 检查是否已存在待处理的申请
		LambdaQueryWrapper<FriendRequest> requestQuery = new LambdaQueryWrapper<>();
		requestQuery.eq(FriendRequest::getFromUserId, currentUserId)
				.eq(FriendRequest::getToUserId, toUserId)
				.eq(FriendRequest::getStatus, FriendRequestStatusEnum.PENDING.getStatus());
		FriendRequest existingRequest = this.getOne(requestQuery);

		if (existingRequest != null) {
			// 更新申请时间
			existingRequest.setMessage(friendApplyDTO.getMessage());
			existingRequest.setUpdateTime(LocalDateTime.now());
			this.updateById(existingRequest);
		} else {
			// 创建新申请
			FriendRequest newRequest = new FriendRequest();
			newRequest.setFromUserId(currentUserId);
			newRequest.setToUserId(toUserId);
			newRequest.setMessage(friendApplyDTO.getMessage());
			newRequest.setStatus(FriendRequestStatusEnum.PENDING.getStatus());
			this.save(newRequest);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void agreeApply(Long requestId, Long currentUserId) {
		log.info("开始处理好友申请同意，requestId={}, currentUserId={}", requestId, currentUserId);

		// 查询申请记录
		FriendRequest request = this.getById(requestId);
		if (request == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申请记录不存在");
		}

		// 验证是否是接收人
		if (!request.getToUserId().equals(currentUserId)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
		}

		// 验证申请状态
		if (!FriendRequestStatusEnum.PENDING.getStatus().equals(request.getStatus())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "该申请已处理");
		}

		Long fromUserId = request.getFromUserId();
		log.info("申请人ID={}", fromUserId);

		// 检查双方的好友关系状态
		// 1. 查询当前用户 -> 申请人的关系
		LambdaQueryWrapper<Friendship> myRelationQuery = new LambdaQueryWrapper<>();
		myRelationQuery.eq(Friendship::getUserId, currentUserId)
				.eq(Friendship::getFriendId, fromUserId);
		Friendship myRelation = friendshipService.getOne(myRelationQuery);

		// 2. 查询申请人 -> 当前用户的关系
		LambdaQueryWrapper<Friendship> otherRelationQuery = new LambdaQueryWrapper<>();
		otherRelationQuery.eq(Friendship::getUserId, fromUserId)
				.eq(Friendship::getFriendId, currentUserId);
		Friendship otherRelation = friendshipService.getOne(otherRelationQuery);

		// 业务逻辑判断
		Integer myStatus = myRelation != null ? myRelation.getStatus() : null;
		Integer otherStatus = otherRelation != null ? otherRelation.getStatus() : null;

		log.info("关系状态检查 - 我方状态: {}, 对方状态: {}", myStatus, otherStatus);

		// 情况1：双方都已是好友状态，无需处理（幂等）
		if (FriendshipStatusEnum.FRIEND.getStatus().equals(myStatus)
				&& FriendshipStatusEnum.FRIEND.getStatus().equals(otherStatus)) {
			log.info("双方已是好友关系，直接返回");
			request.setStatus(FriendRequestStatusEnum.AGREED.getStatus());
			request.setUpdateTime(LocalDateTime.now());
			this.updateById(request);
			return;
		}

		// 情况2：对方拉黑了我，不能成为好友
		if (FriendshipStatusEnum.BLOCKED.getStatus().equals(otherStatus)) {
			throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无法添加该好友");
		}

		// 情况3：我拉黑了对方，不能成为好友
		if (FriendshipStatusEnum.BLOCKED.getStatus().equals(myStatus)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "您已拉黑该用户，无法成为好友");
		}

		// 处理我方关系
		if (myRelation == null) {
			// 不存在记录，插入新关系
			log.info("我方无记录，插入新关系");
			Friendship newMyRelation = new Friendship();
			newMyRelation.setUserId(currentUserId);
			newMyRelation.setFriendId(fromUserId);
			newMyRelation.setStatus(FriendshipStatusEnum.FRIEND.getStatus());
			friendshipService.save(newMyRelation);
		} else if (FriendshipStatusEnum.DELETED.getStatus().equals(myStatus)) {
			// 已删除状态，恢复为好友
			log.info("我方状态为DELETED，恢复为好友");
			myRelation.setStatus(FriendshipStatusEnum.FRIEND.getStatus());
			friendshipService.updateById(myRelation);
		} else if (FriendshipStatusEnum.FRIEND.getStatus().equals(myStatus)) {
			// 我方已经是好友，保持不变
			log.info("我方状态已是FRIEND，保持不变");
		}
		// 如果是其他状态（PENDING），也更新为FRIEND
		else {
			log.info("我方状态为其他({}），更新为好友", myStatus);
			myRelation.setStatus(FriendshipStatusEnum.FRIEND.getStatus());
			friendshipService.updateById(myRelation);
		}

		// 处理对方关系
		if (otherRelation == null) {
			// 不存在记录，插入新关系
			log.info("对方无记录，插入新关系");
			Friendship newOtherRelation = new Friendship();
			newOtherRelation.setUserId(fromUserId);
			newOtherRelation.setFriendId(currentUserId);
			newOtherRelation.setStatus(FriendshipStatusEnum.FRIEND.getStatus());
			friendshipService.save(newOtherRelation);
		} else if (FriendshipStatusEnum.DELETED.getStatus().equals(otherStatus)) {
			// 对方之前删除了我，现在恢复为好友
			log.info("对方状态为DELETED，恢复为好友");
			otherRelation.setStatus(FriendshipStatusEnum.FRIEND.getStatus());
			friendshipService.updateById(otherRelation);
		} else if (FriendshipStatusEnum.FRIEND.getStatus().equals(otherStatus)) {
			// 对方已经是好友，保持不变
			log.info("对方状态已是FRIEND，保持不变");
		}
		// 如果是其他状态（PENDING），也更新为FRIEND
		else {
			log.info("对方状态为其他({})，更新为好友", otherStatus);
			otherRelation.setStatus(FriendshipStatusEnum.FRIEND.getStatus());
			friendshipService.updateById(otherRelation);
		}

		// 更新申请状态为已同意
		request.setStatus(FriendRequestStatusEnum.AGREED.getStatus());
		request.setUpdateTime(LocalDateTime.now());
		this.updateById(request);

		log.info("清理缓存: userId={}, friendId={}", currentUserId, fromUserId);
		// 清理Redis缓存
		clearFriendListCache(currentUserId);
		clearFriendListCache(fromUserId);

		log.info("好友申请处理完成");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void rejectApply(Long requestId, Long currentUserId) {
		// 查询申请记录
		FriendRequest request = this.getById(requestId);
		if (request == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申请记录不存在");
		}

		// 验证是否是接收人
		if (!request.getToUserId().equals(currentUserId)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
		}

		// 验证申请状态
		if (!FriendRequestStatusEnum.PENDING.getStatus().equals(request.getStatus())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "该申请已处理");
		}

		// 更新申请状态为已拒绝
		request.setStatus(FriendRequestStatusEnum.REJECTED.getStatus());
		request.setUpdateTime(LocalDateTime.now());
		this.updateById(request);
	}

	@Override
	public List<FriendRequestVO> getReceivedRequests(Long currentUserId) {
		// 查询收到的好友申请
		LambdaQueryWrapper<FriendRequest> query = new LambdaQueryWrapper<>();
		query.eq(FriendRequest::getToUserId, currentUserId)
				.orderByDesc(FriendRequest::getCreateTime);
		List<FriendRequest> requests = this.list(query);

		if (requests.isEmpty()) {
			return new ArrayList<>();
		}

		// 获取所有申请人ID
		List<Long> fromUserIds = requests.stream()
				.map(FriendRequest::getFromUserId)
				.distinct()
				.collect(Collectors.toList());

		// 批量查询申请人信息
		List<User> users = userService.listByIds(fromUserIds);
		Map<Long, User> userMap = users.stream()
				.collect(Collectors.toMap(User::getId, user -> user));

		// 组装返回结果
		List<FriendRequestVO> result = new ArrayList<>();
		for (FriendRequest request : requests) {
			FriendRequestVO vo = new FriendRequestVO();
			vo.setId(request.getId());
			vo.setFromUserId(request.getFromUserId());
			vo.setMessage(request.getMessage());
			vo.setStatus(request.getStatus());
			vo.setCreateTime(request.getCreateTime());

			// 填充申请人信息
			User fromUser = userMap.get(request.getFromUserId());
			if (fromUser != null) {
				vo.setFromUserAccount(fromUser.getUserAccount());
				vo.setFromUserName(fromUser.getUserName());
				vo.setFromUserAvatar(fromUser.getAvatar());
			}

			result.add(vo);
		}

		return result;
	}

	/**
	 * 获取当前用户的待处理申请数量
	 *
	 * @param currentUserId 当前用户ID
	 * @return 待处理申请数量
	 */
	@Override
	public long getPendingApplyCount(Long currentUserId) {
		LambdaQueryWrapper<FriendRequest> query = new LambdaQueryWrapper<>();
		query.eq(FriendRequest::getToUserId, currentUserId)
				.eq(FriendRequest::getStatus, FriendRequestStatusEnum.PENDING.getStatus());
		return this.count(query);
	}

	/**
	 * 清理好友列表缓存
	 */
	private void clearFriendListCache(Long userId) {
		String cacheKey = Constants.FRIEND_LIST_KEY + userId;
		log.info("准备清除缓存，key={}", cacheKey);

		// 先检查缓存是否存在
		String oldCache = redisUtil.get(cacheKey);
		if (oldCache != null) {
			log.info("清除前缓存内容: {}", oldCache);
		} else {
			log.info("缓存不存在，无需清除");
		}

		// 删除缓存
		redisUtil.delete(cacheKey);

		// 验证删除是否成功
		String checkCache = redisUtil.get(cacheKey);
		if (checkCache == null) {
			log.info("缓存清除成功");
		} else {
			log.warn("缓存清除失败，仍然存在: {}", checkCache);
			// 再次尝试删除
			redisUtil.delete(cacheKey);
		}
	}
}
