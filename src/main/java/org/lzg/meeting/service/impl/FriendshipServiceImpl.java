package org.lzg.meeting.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.constant.Constants;
import org.lzg.meeting.enums.FriendshipStatusEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.mapper.FriendshipMapper;
import org.lzg.meeting.model.entity.Friendship;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.FriendRelationVO;
import org.lzg.meeting.model.vo.FriendVO;
import org.lzg.meeting.service.IFriendshipService;
import org.lzg.meeting.service.IUserService;
import org.lzg.meeting.utils.RedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 好友关系表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-10-11
 */
@Slf4j
@Service
public class FriendshipServiceImpl extends ServiceImpl<FriendshipMapper, Friendship> implements IFriendshipService {

	@Resource
	private IUserService userService;

	@Resource
	private RedisUtil redisUtil;

	// 缓存过期时间（小时）
	private static final Long CACHE_EXPIRE_HOURS = 24L;

	@Override
	public List<FriendVO> getFriendList(Long currentUserId) {
		log.info("查询好友列表，userId={}", currentUserId);
		
		// 尝试从缓存获取
		String cacheKey = Constants.FRIEND_LIST_KEY + currentUserId;
		String cachedData = redisUtil.get(cacheKey);
		if (cachedData != null) {
			log.info("命中缓存，返回缓存数据: {}", cachedData);
			return JSONUtil.toList(cachedData, FriendVO.class);
		}
		
		log.info("缓存未命中，从数据库查询");

		// 查询好友关系
		LambdaQueryWrapper<Friendship> query = new LambdaQueryWrapper<>();
		query.eq(Friendship::getUserId, currentUserId)
				.eq(Friendship::getStatus, FriendshipStatusEnum.FRIEND.getStatus());
		List<Friendship> friendships = this.list(query);
		
		log.info("数据库查询结果，好友数量: {}", friendships.size());

		if (friendships.isEmpty()) {
			// 不缓存空列表，避免数据更新后仍返回旧缓存
			log.info("好友列表为空，不缓存，直接返回");
			return new ArrayList<>();
		}

		// 获取所有好友ID
		List<Long> friendIds = friendships.stream()
				.map(Friendship::getFriendId)
				.collect(Collectors.toList());

		// 批量查询好友信息
		List<User> friends = userService.listByIds(friendIds);
		Map<Long, User> userMap = friends.stream()
				.collect(Collectors.toMap(User::getId, user -> user));

		// 组装返回结果
		List<FriendVO> result = new ArrayList<>();
		for (Friendship friendship : friendships) {
			User friend = userMap.get(friendship.getFriendId());
			if (friend != null) {
				FriendVO vo = new FriendVO();
				vo.setUserId(friend.getId());
				vo.setUserAccount(friend.getUserAccount());
				vo.setUserName(friend.getUserName());
				vo.setAvatar(friend.getAvatar());
				vo.setEmail(friend.getEmail());
				vo.setStatus(friendship.getStatus());
				result.add(vo);
			}
		}

		// 缓存结果
		String cacheData = JSONUtil.toJsonStr(result);
		log.info("查询完成，缓存结果: {}", cacheData);
		redisUtil.setEx(cacheKey, cacheData, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteFriend(Long friendId, Long currentUserId) {
		// 参数校验
		if (friendId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友ID不能为空");
		}

		if (currentUserId.equals(friendId)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能删除自己");
		}

		// 查询好友关系
		LambdaQueryWrapper<Friendship> query = new LambdaQueryWrapper<>();
		query.eq(Friendship::getUserId, currentUserId)
				.eq(Friendship::getFriendId, friendId)
				.eq(Friendship::getStatus, FriendshipStatusEnum.FRIEND.getStatus());
		Friendship friendship = this.getOne(query);

		if (friendship == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "好友关系不存在");
		}

		// 单向删除：更新状态为已删除
		LambdaUpdateWrapper<Friendship> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(Friendship::getUserId, currentUserId)
				.eq(Friendship::getFriendId, friendId)
				.set(Friendship::getStatus, FriendshipStatusEnum.DELETED.getStatus());
		this.update(updateWrapper);

		// 清除缓存
		clearCache(currentUserId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void blockFriend(Long friendId, Long currentUserId) {
		// 参数校验
		if (friendId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友ID不能为空");
		}

		if (currentUserId.equals(friendId)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能拉黑自己");
		}

		// 检查用户是否存在
		User user = userService.getById(friendId);
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}

		// 查询是否存在关系
		LambdaQueryWrapper<Friendship> query = new LambdaQueryWrapper<>();
		query.eq(Friendship::getUserId, currentUserId)
				.eq(Friendship::getFriendId, friendId);
		Friendship friendship = this.getOne(query);

		if (friendship == null) {
			// 不存在关系，创建拉黑关系
			friendship = new Friendship();
			friendship.setUserId(currentUserId);
			friendship.setFriendId(friendId);
			friendship.setStatus(FriendshipStatusEnum.BLOCKED.getStatus());
			this.save(friendship);
		} else {
			// 已存在关系，更新为拉黑
			if (FriendshipStatusEnum.BLOCKED.getStatus().equals(friendship.getStatus())) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "已经拉黑该用户");
			}
			friendship.setStatus(FriendshipStatusEnum.BLOCKED.getStatus());
			this.updateById(friendship);
		}

		// 清除缓存
		clearCache(currentUserId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void unblockFriend(Long friendId, Long currentUserId) {
		// 参数校验
		if (friendId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友ID不能为空");
		}

		// 查询拉黑关系
		LambdaQueryWrapper<Friendship> query = new LambdaQueryWrapper<>();
		query.eq(Friendship::getUserId, currentUserId)
				.eq(Friendship::getFriendId, friendId)
				.eq(Friendship::getStatus, FriendshipStatusEnum.BLOCKED.getStatus());
		Friendship friendship = this.getOne(query);

		if (friendship == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未拉黑该用户");
		}

		// 删除拉黑关系（或者更新为已删除状态）
		this.removeById(friendship.getId());

		// 清除缓存
		clearCache(currentUserId);
	}

	@Override
	public FriendRelationVO getRelation(Long friendId, Long currentUserId) {
		// 参数校验
		if (friendId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友ID不能为空");
		}

		if (currentUserId.equals(friendId)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能查询与自己的关系");
		}

		// 查询我对对方的关系
		LambdaQueryWrapper<Friendship> myQuery = new LambdaQueryWrapper<>();
		myQuery.eq(Friendship::getUserId, currentUserId)
				.eq(Friendship::getFriendId, friendId);
		Friendship myRelation = this.getOne(myQuery);

		// 查询对方对我的关系
		LambdaQueryWrapper<Friendship> otherQuery = new LambdaQueryWrapper<>();
		otherQuery.eq(Friendship::getUserId, friendId)
				.eq(Friendship::getFriendId, currentUserId);
		Friendship otherRelation = this.getOne(otherQuery);

		// 组装返回结果
		FriendRelationVO vo = new FriendRelationVO();

		Integer myStatus = myRelation != null && myRelation.getStatus() != null ? myRelation.getStatus() : 0;
		Integer otherStatus = otherRelation != null && otherRelation.getStatus() != null ? otherRelation.getStatus() : 0;

		vo.setMyStatus(myStatus);
		vo.setOtherStatus(otherStatus);

		// 双方都是好友状态才算是好友
		vo.setIsFriend(FriendshipStatusEnum.FRIEND.getStatus().equals(myStatus) 
				&& FriendshipStatusEnum.FRIEND.getStatus().equals(otherStatus));

		// 我是否拉黑对方
		vo.setIsBlockedByMe(FriendshipStatusEnum.BLOCKED.getStatus().equals(myStatus));

		// 对方是否拉黑我
		vo.setIsBlockedByOther(FriendshipStatusEnum.BLOCKED.getStatus().equals(otherStatus));

		return vo;
	}

	/**
	 * 清除好友列表缓存
	 */
	private void clearCache(Long userId) {
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
		}
	}
}
