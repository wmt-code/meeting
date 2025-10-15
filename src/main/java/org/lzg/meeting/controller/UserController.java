package org.lzg.meeting.controller;


import org.lzg.meeting.annotation.GlobalInterceptor;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.dto.UserLoginDTO;
import org.lzg.meeting.model.dto.UserPasswordDTO;
import org.lzg.meeting.model.dto.UserRegisterDTO;
import org.lzg.meeting.model.dto.UserUpdateDTO;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.CaptchaVO;
import org.lzg.meeting.model.vo.UserVO;
import org.lzg.meeting.service.IUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author lzg
 * @since 2025-09-29
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController extends BaseController {
	@Resource
	private IUserService userService;

	/**
	 * 获取验证码
	 *
	 * @return 返回验证码的key和验证码图片base64
	 */
	@GetMapping("/captcha")
	@Operation(summary = "获取验证码")
	public BaseResponse<CaptchaVO> getCaptcha() {
		CaptchaVO captchaVO = userService.getCaptcha();
		return ResultUtils.success(captchaVO);
	}

	/**
	 * 用户登录
	 *
	 * @param userLoginDTO 登录参数
	 * @return 用户token
	 */
	@PostMapping("/login")
	@Operation(summary = "用户登录")
	public BaseResponse<String> login(@RequestBody UserLoginDTO userLoginDTO) {
		ThrowUtils.throwIf(userLoginDTO == null, ErrorCode.PARAMS_ERROR);
		String token = userService.login(userLoginDTO);
		return ResultUtils.success(token);
	}

	@PostMapping("/register")
	@Operation(summary = "用户注册")
	public BaseResponse<Boolean> register(@RequestBody UserRegisterDTO userRegisterDTO) {
		ThrowUtils.throwIf(userRegisterDTO == null, ErrorCode.PARAMS_ERROR);
		Boolean result = userService.register(userRegisterDTO);
		return ResultUtils.success(result);
	}

	/**
	 * 获取当前登录用户的信息
	 *
	 * @return 当前登录用户的信息
	 */
	@GetMapping("/current")
	@Operation(summary = "获取当前登录用户的信息")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<UserVO> getCurrentUser() {
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		User byId = userService.getById(tokenUserInfo.getUserId());
		ThrowUtils.throwIf(byId == null, ErrorCode.NOT_LOGIN_ERROR);
		UserVO userVO = new UserVO();
		BeanUtil.copyProperties(byId, userVO);
		return ResultUtils.success(userVO);
	}

	/**
	 * 更新当前用户信息
	 *
	 * @param userUpdateDTO 用户信息更新DTO
	 * @return 是否更新成功
	 */
	@PutMapping("/update")
	@Operation(summary = "更新当前用户信息")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Boolean> updateUserInfo(@RequestBody UserUpdateDTO userUpdateDTO) {
		ThrowUtils.throwIf(userUpdateDTO == null, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Boolean result = userService.updateUserInfo(tokenUserInfo.getUserId(), userUpdateDTO);
		return ResultUtils.success(result);
	}

	/**
	 * 修改密码
	 *
	 * @param userPasswordDTO 密码修改DTO
	 * @return 是否修改成功
	 */
	@PutMapping("/password")
	@Operation(summary = "修改密码")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Boolean> updatePassword(@RequestBody UserPasswordDTO userPasswordDTO) {
		ThrowUtils.throwIf(userPasswordDTO == null, ErrorCode.PARAMS_ERROR);
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Boolean result = userService.updatePassword(tokenUserInfo.getUserId(), userPasswordDTO);
		return ResultUtils.success(result);
	}

	/**
	 * 上传头像
	 *
	 * @param file 头像文件
	 * @return 头像URL
	 */
	@PostMapping("/avatar")
	@Operation(summary = "上传头像")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		String avatarUrl = userService.uploadAvatar(tokenUserInfo.getUserId(), file);
		return ResultUtils.success(avatarUrl);
	}
}
