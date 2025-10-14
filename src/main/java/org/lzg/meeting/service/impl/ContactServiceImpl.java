package org.lzg.meeting.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.lzg.meeting.enums.FriendshipStatusEnum;
import org.lzg.meeting.enums.UserStatusEnum;
import org.lzg.meeting.model.dto.ContactSearchDTO;
import org.lzg.meeting.model.entity.Friendship;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.ContactVO;
import org.lzg.meeting.service.IContactService;
import org.lzg.meeting.service.IFriendshipService;
import org.lzg.meeting.service.IUserService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 联系人服务实现类
 */
@Slf4j
@Service
public class ContactServiceImpl implements IContactService {

	@Resource
	private IUserService userService;

	@Resource
	private IFriendshipService friendshipService;

	@Override
	public IPage<ContactVO> searchContacts(ContactSearchDTO searchDTO, Long currentUserId) {
		log.info("搜索联系人，keyword={}, currentUserId={}", searchDTO.getKeyword(), currentUserId);

		// 构建分页对象
		Page<User> page = new Page<>(searchDTO.getCurrent(), searchDTO.getPageSize());

		// 构建查询条件
		LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();

		if (StringUtils.isNotBlank(searchDTO.getKeyword())) {
			String keyword = searchDTO.getKeyword().trim();
			query.and(wrapper -> wrapper
					.like(User::getUserName, keyword)
					.or()
					.like(User::getUserAccount, keyword)
			);
		}

		// 排除当前用户自己
		if (currentUserId != null) {
			query.ne(User::getId, currentUserId);
		}

		// 只查询正常状态的用户
		query.eq(User::getStatus, UserStatusEnum.ENABLE.getValue())
				.orderByDesc(User::getCreateTime);

		// 分页查询用户
		IPage<User> userPage = userService.page(page, query);

		// 获取用户ID列表
		List<Long> userIds = userPage.getRecords().stream()
				.map(User::getId)
				.collect(Collectors.toList());

		// 批量查询好友关系
		Map<Long, Boolean> friendMap = getFriendStatusMap(currentUserId, userIds);

		// 转换为ContactVO
		List<ContactVO> contactVOs = new ArrayList<>();
		for (User user : userPage.getRecords()) {
			ContactVO vo = new ContactVO();
			vo.setUserId(user.getId());
			vo.setUserAccount(user.getUserAccount());
			vo.setUserName(user.getUserName());
			vo.setAvatar(user.getAvatar());
			vo.setEmail(user.getEmail());
			vo.setIsFriend(friendMap.getOrDefault(user.getId(), false));

			contactVOs.add(vo);
		}

		// 构建返回的分页对象
		Page<ContactVO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
		resultPage.setRecords(contactVOs);

		return resultPage;
	}

	/**
	 * 批量获取好友状态
	 *
	 * @param currentUserId 当前用户ID
	 * @param userIds       目标用户ID列表
	 * @return userId -> isFriend 映射
	 */
	private Map<Long, Boolean> getFriendStatusMap(Long currentUserId, List<Long> userIds) {
		if (currentUserId == null || userIds == null || userIds.isEmpty()) {
			return Map.of();
		}

		// 查询好友关系
		LambdaQueryWrapper<Friendship> query = new LambdaQueryWrapper<>();
		query.eq(Friendship::getUserId, currentUserId)
				.in(Friendship::getFriendId, userIds)
				.eq(Friendship::getStatus, FriendshipStatusEnum.FRIEND.getStatus());

		List<Friendship> friendships = friendshipService.list(query);

		// 转换为Map
		return friendships.stream()
				.collect(Collectors.toMap(
						Friendship::getFriendId,
						friendship -> true,
						(v1, v2) -> v1
				));
	}
}
