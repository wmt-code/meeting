package org.lzg.meeting.controller;


import java.util.List;

import org.lzg.meeting.annotation.GlobalInterceptor;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.model.vo.FriendRelationVO;
import org.lzg.meeting.model.vo.FriendVO;
import org.lzg.meeting.service.IFriendshipService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;

/**
 * <p>
 * 好友关系表 前端控制器
 * </p>
 *
 * @author lzg
 * @since 2025-10-11
 */
@RestController
@RequestMapping("/friend")
@Tag(name = "好友关系管理", description = "好友关系相关接口")
public class FriendshipController extends BaseController {

	@Resource
	private IFriendshipService friendshipService;

	/**
	 * 获取我的好友列表（带Redis缓存）
	 */
	@GetMapping("/list")
	@GlobalInterceptor(checkLogin = true)
	@Operation(summary = "获取好友列表")
	public BaseResponse<List<FriendVO>> list() {
		Long currentUserId = getTokenUserInfo().getUserId();
		List<FriendVO> friendList = friendshipService.getFriendList(currentUserId);
		return ResultUtils.success(friendList);
	}

	/**
	 * 删除好友（单向删除）
	 */
	@DeleteMapping("/{friendId}")
	@GlobalInterceptor(checkLogin = true)
	@Operation(summary = "删除好友")
	public BaseResponse<Boolean> delete(@PathVariable("friendId") Long friendId) {
		if (friendId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友ID不能为空");
		}

		Long currentUserId = getTokenUserInfo().getUserId();
		friendshipService.deleteFriend(friendId, currentUserId);
		return ResultUtils.success(true);
	}

	/**
	 * 拉黑好友
	 */
	@PostMapping("/block/{friendId}")
	@GlobalInterceptor(checkLogin = true)
	@Operation(summary = "拉黑好友")
	public BaseResponse<Boolean> block(@PathVariable("friendId") Long friendId) {
		if (friendId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友ID不能为空");
		}

		Long currentUserId = getTokenUserInfo().getUserId();
		friendshipService.blockFriend(friendId, currentUserId);
		return ResultUtils.success(true);
	}

	/**
	 * 取消拉黑
	 */
	@PostMapping("/unblock/{friendId}")
	@GlobalInterceptor(checkLogin = true)
	@Operation(summary = "取消拉黑")
	public BaseResponse<Boolean> unblock(@PathVariable("friendId") Long friendId) {
		if (friendId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友ID不能为空");
		}

		Long currentUserId = getTokenUserInfo().getUserId();
		friendshipService.unblockFriend(friendId, currentUserId);
		return ResultUtils.success(true);
	}

	/**
	 * 查询双方关系（是否好友、是否拉黑）
	 */
	@GetMapping("/relation/{friendId}")
	@GlobalInterceptor(checkLogin = true)
	@Operation(summary = "查询好友关系")
	public BaseResponse<FriendRelationVO> relation(@PathVariable("friendId") Long friendId) {
		if (friendId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友ID不能为空");
		}

		Long currentUserId = getTokenUserInfo().getUserId();
		FriendRelationVO relation = friendshipService.getRelation(friendId, currentUserId);
		return ResultUtils.success(relation);
	}
}
