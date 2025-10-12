package org.lzg.meeting.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.lzg.meeting.annotation.GlobalInterceptor;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.model.dto.FriendApplyDTO;
import org.lzg.meeting.model.dto.FriendRequestHandleDTO;
import org.lzg.meeting.model.entity.FriendRequest;
import org.lzg.meeting.model.vo.FriendRequestVO;
import org.lzg.meeting.service.IFriendRequestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 好友申请表 前端控制器
 * </p>
 *
 * @author lzg
 * @since 2025-10-11
 */
@RestController
@RequestMapping("/friend")
@Tag(name = "好友申请管理", description = "好友申请相关接口")
public class FriendRequestController extends BaseController {

	@Resource
	private IFriendRequestService friendRequestService;

	/**
	 * 发送好友申请
	 */
	@PostMapping("/apply")
	@GlobalInterceptor(checkLogin = true)
	@Operation(summary = "发送好友申请")
	public BaseResponse<Boolean> apply(@RequestBody FriendApplyDTO friendApplyDTO) {
		if (friendApplyDTO == null || friendApplyDTO.getToUserId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
		}

		Long currentUserId = getTokenUserInfo().getUserId();
		friendRequestService.sendApply(friendApplyDTO, currentUserId);
		return ResultUtils.success(true);
	}

	/**
	 * 同意好友申请
	 */
	@PostMapping("/agree")
	@GlobalInterceptor(checkLogin = true)
	@Operation(summary = "同意好友申请")
	public BaseResponse<Boolean> agree(@RequestBody FriendRequestHandleDTO handleDTO) {
		if (handleDTO == null || handleDTO.getRequestId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
		}

		Long currentUserId = getTokenUserInfo().getUserId();
		friendRequestService.agreeApply(handleDTO.getRequestId(), currentUserId);
		return ResultUtils.success(true);
	}

	/**
	 * 拒绝好友申请
	 */
	@PostMapping("/reject")
	@GlobalInterceptor(checkLogin = true)
	@Operation(summary = "拒绝好友申请")
	public BaseResponse<Boolean> reject(@RequestBody FriendRequestHandleDTO handleDTO) {
		if (handleDTO == null || handleDTO.getRequestId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
		}

		Long currentUserId = getTokenUserInfo().getUserId();
		friendRequestService.rejectApply(handleDTO.getRequestId(), currentUserId);
		return ResultUtils.success(true);
	}

	/**
	 * 查询我收到的好友申请
	 */
	@GetMapping("/requests")
	@GlobalInterceptor(checkLogin = true)
	@Operation(summary = "查询收到的好友申请")
	public BaseResponse<List<FriendRequestVO>> requests() {
		Long currentUserId = getTokenUserInfo().getUserId();
		List<FriendRequestVO> requests = friendRequestService.getReceivedRequests(currentUserId);
		return ResultUtils.success(requests);
	}

	/**
	 * 获取我收到的未处理的好友申请数量
	 *
	 * @return 数量
	 */
	@GetMapping("/pendingCount")
	@GlobalInterceptor(checkLogin = true)
	@Operation(summary = "获取未处理的好友申请数量")
	public BaseResponse<Long> pendingCount() {
		Long currentUserId = getTokenUserInfo().getUserId();
		long count = friendRequestService.getPendingApplyCount(currentUserId);
		return ResultUtils.success(count);
	}

	/**
	 * 删除好友申请
	 *
	 * @param id 申请ID
	 * @return 是否成功
	 */
	@DeleteMapping("/{id}")
	@GlobalInterceptor(checkLogin = true)
	@Operation(summary = "删除好友申请")
	public BaseResponse<Boolean> delete(@PathVariable("id") Long id) {
		if (id == null || id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
		}
		// 只能删除自己的好友申请记录
		Long currentUserId = getTokenUserInfo().getUserId();
		FriendRequest friendRequest = friendRequestService.getById(id);
		if (friendRequest == null || !friendRequest.getToUserId().equals(currentUserId)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除");
		}
		boolean result = friendRequestService.removeById(id);
		return ResultUtils.success(result);
	}
}
