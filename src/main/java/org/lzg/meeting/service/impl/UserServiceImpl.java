package org.lzg.meeting.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.enums.UserStatusEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.mapper.UserMapper;
import org.lzg.meeting.model.dto.UserLoginDTO;
import org.lzg.meeting.model.dto.UserRegisterDTO;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.UserVO;
import org.lzg.meeting.service.IUserService;
import org.lzg.meeting.utils.JwtUtils;
import org.lzg.meeting.utils.RedisUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-09-29
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
	@Resource
	private RedisUtil redisUtil;

	@Override
	public UserVO login(UserLoginDTO userLoginDTO) {
		String userAccount = userLoginDTO.getUserAccount();
		String userPassword = userLoginDTO.getUserPassword();
		String captchaKey = userLoginDTO.getCaptchaKey();
		String captchaCode = userLoginDTO.getCaptchaCode();
		if (StrUtil.hasBlank(userAccount, userPassword, captchaKey, captchaCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 验证码校验
		checkCaptcha(captchaKey, captchaCode);
		checkAccAndPwd(userAccount, userPassword);
		// 判断用户是否存在
		User user = this.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
		if (user == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
		}
		// 判断用户是否被禁用
		if (user.getStatus().equals(UserStatusEnum.DISABLE.getValue())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已被禁用");
		}
		// 判断用户是否重复登录
		if (user.getLastLoginTime().isAfter(user.getLastLogoutTime())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已登录，请勿重复登录");
		}
		// 密码校验
		if (!encPassword(userPassword).equals(user.getUserPassword())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
		}
		// 更新最后登录时间
		user.setLastLoginTime(LocalDateTime.now());
		// 生成token
		Map<String, Object> map = new HashMap<>();
		map.put("userId", user.getId());
		if (user.getMeetingNo() != null) {
			map.put("meetingNo", user.getMeetingNo());
		}
		String token = JwtUtils.generateToken(user.getId().toString(), map);
		redisUtil.setEx(UserConstant.TOKEN + user.getId(), token, JwtUtils.EXPIRE_TIME / 1000, TimeUnit.SECONDS);
		UserVO userVO = new UserVO();
		BeanUtil.copyProperties(user, userVO);
		userVO.setToken(token);
		return userVO;
	}

	@Override
	public Boolean register(UserRegisterDTO userRegisterDTO) {
		String userAccount = userRegisterDTO.getUserAccount();
		String userPassword = userRegisterDTO.getUserPassword();
		String checkPassword = userRegisterDTO.getCheckPassword();
		String captchaKey = userRegisterDTO.getCaptchaKey();
		String captchaCode = userRegisterDTO.getCaptchaCode();
		if (StrUtil.hasBlank(userAccount, userPassword, checkPassword, captchaKey, captchaCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		checkAccAndPwd(userAccount, userPassword);
		if (!userPassword.equals(checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
		}
		if (this.getOne(new QueryWrapper<User>().eq("userAccount", userAccount)) != null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
		}
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encPassword(userPassword));
		user.setUserName(userAccount);
		user.setRole(UserConstant.DEFAULT_ROLE);
		boolean save = this.save(user);
		ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
		return true;
	}

	private static void checkAccAndPwd(String userAccount, String userPassword) {
		// 账号密码校验
		if (userAccount.length() < 4 || userAccount.length() > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度必须在4~20之间");
		}
		if (userPassword.length() < 8 || userPassword.length() > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度必须在8~20之间");
		}
	}

	private String encPassword(String password) {
		return SecureUtil.md5(password + UserConstant.SALT);
	}

	private void checkCaptcha(String captchaKey, String captchaCode) {
		// 验证码校验
		if (!redisUtil.get(captchaKey).equals(captchaCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
		}
	}
}
