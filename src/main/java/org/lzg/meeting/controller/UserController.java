package org.lzg.meeting.controller;


import cn.hutool.core.bean.BeanUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.dto.UserLoginDTO;
import org.lzg.meeting.model.dto.UserRegisterDTO;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.CaptchaVO;
import org.lzg.meeting.model.vo.UserVO;
import org.lzg.meeting.service.IUserService;
import org.springframework.web.bind.annotation.*;

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
public class UserController extends BaseController {
	@Resource
	private IUserService userService;

	/**
	 * 获取验证码
	 *
	 * @return 返回验证码的key和验证码图片base64
	 */
	@GetMapping("/captcha")
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
	public BaseResponse<String> login(@RequestBody UserLoginDTO userLoginDTO) {
		ThrowUtils.throwIf(userLoginDTO == null, ErrorCode.PARAMS_ERROR);
		String token = userService.login(userLoginDTO);
		return ResultUtils.success(token);
	}

	@PostMapping("/register")
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
	public BaseResponse<UserVO> getCurrentUser() {
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		User byId = userService.getById(tokenUserInfo.getUserId());
		ThrowUtils.throwIf(byId == null, ErrorCode.NOT_LOGIN_ERROR);
		UserVO userVO = new UserVO();
		BeanUtil.copyProperties(byId, userVO);
		return ResultUtils.success(userVO);
	}
}
