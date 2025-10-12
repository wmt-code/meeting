package org.lzg.meeting.service;

import java.util.List;

import org.lzg.meeting.model.entity.Friendship;
import org.lzg.meeting.model.vo.FriendRelationVO;
import org.lzg.meeting.model.vo.FriendVO;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 好友关系表 服务类
 * </p>
 *
 * @author lzg
 * @since 2025-10-11
 */
public interface IFriendshipService extends IService<Friendship> {

	/**
	 * 获取我的好友列表（带Redis缓存）
	 * @param currentUserId 当前用户ID
	 * @return 好友列表
	 */
	List<FriendVO> getFriendList(Long currentUserId);

	/**
	 * 删除好友（单向删除）
	 * @param friendId 好友ID
	 * @param currentUserId 当前用户ID
	 */
	void deleteFriend(Long friendId, Long currentUserId);

	/**
	 * 拉黑好友
	 * @param friendId 好友ID
	 * @param currentUserId 当前用户ID
	 */
	void blockFriend(Long friendId, Long currentUserId);

	/**
	 * 取消拉黑
	 * @param friendId 好友ID
	 * @param currentUserId 当前用户ID
	 */
	void unblockFriend(Long friendId, Long currentUserId);

	/**
	 * 查询双方关系
	 * @param friendId 好友ID
	 * @param currentUserId 当前用户ID
	 * @return 关系信息
	 */
	FriendRelationVO getRelation(Long friendId, Long currentUserId);
}
