package org.lzg.meeting.service;

import java.util.List;

import org.lzg.meeting.model.dto.FriendApplyDTO;
import org.lzg.meeting.model.entity.FriendRequest;
import org.lzg.meeting.model.vo.FriendRequestVO;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 好友申请表 服务类
 * </p>
 *
 * @author lzg
 * @since 2025-10-11
 */
public interface IFriendRequestService extends IService<FriendRequest> {

	/**
	 * 发送好友申请
	 * @param friendApplyDTO 申请信息
	 * @param currentUserId 当前用户ID
	 */
	void sendApply(FriendApplyDTO friendApplyDTO, Long currentUserId);

	/**
	 * 同意好友申请
	 * @param requestId 申请记录ID
	 * @param currentUserId 当前用户ID
	 */
	void agreeApply(Long requestId, Long currentUserId);

	/**
	 * 拒绝好友申请
	 * @param requestId 申请记录ID
	 * @param currentUserId 当前用户ID
	 */
	void rejectApply(Long requestId, Long currentUserId);

	/**
	 * 查询我收到的好友申请
	 * @param currentUserId 当前用户ID
	 * @return 好友申请列表
	 */
	List<FriendRequestVO> getReceivedRequests(Long currentUserId);
}
