package org.lzg.meeting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.annotation.GlobalInterceptor;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.model.dto.AdminUpdateUserDTO;
import org.lzg.meeting.model.dto.UserQueryDTO;
import org.lzg.meeting.model.vo.UserPageVO;
import org.lzg.meeting.model.vo.UserVO;
import org.lzg.meeting.service.IUserService;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户管理控制器
 *
 * @author lzg
 * @since 2025-10-16
 */
@RestController
@RequestMapping("/admin/user")
@Slf4j
@Tag(name = "管理员-用户管理", description = "管理员用户管理接口")
public class AdminUserController extends BaseController {

	@Resource
	private IUserService userService;

	/**
	 * 分页查询用户列表
	 *
	 * @param queryDTO 查询条件
	 * @return 用户列表
	 */
	@PostMapping("/list")
	@Operation(summary = "分页查询用户列表")
	@GlobalInterceptor(checkAdmin = true)
	public BaseResponse<UserPageVO> listUsers(@RequestBody UserQueryDTO queryDTO) {
		ThrowUtils.throwIf(queryDTO == null, ErrorCode.PARAMS_ERROR);

		UserPageVO pageVO = userService.listUsersByPage(queryDTO);
		return ResultUtils.success(pageVO);
	}

	/**
	 * 根据ID获取用户详情
	 *
	 * @param id 用户ID
	 * @return 用户详情
	 */
	@GetMapping("/{id}")
	@Operation(summary = "根据ID获取用户详情")
	@GlobalInterceptor(checkAdmin = true)
	public BaseResponse<UserVO> getUserById(@PathVariable Long id) {
		ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户ID无效");

		UserVO userVO = userService.getUserVOById(id);
		ThrowUtils.throwIf(userVO == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

		return ResultUtils.success(userVO);
	}

	/**
	 * 更新用户信息
	 *
	 * @param updateDTO 更新信息
	 * @return 是否成功
	 */
	@PutMapping("/update")
	@Operation(summary = "更新用户信息")
	@GlobalInterceptor(checkAdmin = true)
	public BaseResponse<Boolean> updateUser(@RequestBody AdminUpdateUserDTO updateDTO) {
		ThrowUtils.throwIf(updateDTO == null, ErrorCode.PARAMS_ERROR);
		if (updateDTO != null) {
			ThrowUtils.throwIf(updateDTO.getId() == null || updateDTO.getId() <= 0,
					ErrorCode.PARAMS_ERROR, "用户ID无效");
		}

		Boolean result = userService.updateUserByAdmin(updateDTO);
		return ResultUtils.success(result);
	}

	/**
	 * 删除用户（逻辑删除）
	 *
	 * @param id 用户ID
	 * @return 是否成功
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "删除用户")
	@GlobalInterceptor(checkAdmin = true)
	public BaseResponse<Boolean> deleteUser(@PathVariable Long id) {
		ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户ID无效");

		Boolean result = userService.deleteUserById(id);
		return ResultUtils.success(result);
	}

	/**
	 * 批量删除用户
	 *
	 * @param ids 用户ID列表
	 * @return 是否成功
	 */
	@DeleteMapping("/batch")
	@Operation(summary = "批量删除用户")
	@GlobalInterceptor(checkAdmin = true)
	public BaseResponse<Boolean> batchDeleteUsers(@RequestBody java.util.List<Long> ids) {
		ThrowUtils.throwIf(ids == null || ids.isEmpty(), ErrorCode.PARAMS_ERROR, "用户ID列表不能为空");

		Boolean result = userService.batchDeleteUsers(ids);
		return ResultUtils.success(result);
	}

	/**
	 * 启用/禁用用户
	 *
	 * @param id     用户ID
	 * @param status 状态 1启用 0禁用
	 * @return 是否成功
	 */
	@PutMapping("/{id}/status")
	@Operation(summary = "启用/禁用用户")
	@GlobalInterceptor(checkAdmin = true)
	public BaseResponse<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
		ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户ID无效");
		ThrowUtils.throwIf(status == null || (status != 0 && status != 1),
				ErrorCode.PARAMS_ERROR, "状态参数无效");

		Boolean result = userService.updateUserStatus(id, status);
		return ResultUtils.success(result);
	}

	/**
	 * 重置用户密码
	 *
	 * @param id 用户ID
	 * @return 是否成功
	 */
	@PutMapping("/{id}/reset-password")
	@Operation(summary = "重置用户密码")
	@GlobalInterceptor(checkAdmin = true)
	public BaseResponse<String> resetUserPassword(@PathVariable Long id) {
		ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户ID无效");

		String newPassword = userService.resetUserPassword(id);
		return ResultUtils.success(newPassword);
	}
}
