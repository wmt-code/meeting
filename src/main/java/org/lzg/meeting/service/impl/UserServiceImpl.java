package org.lzg.meeting.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.lzg.meeting.component.RedisComponent;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.enums.UserStatusEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.mapper.UserMapper;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.dto.UserLoginDTO;
import org.lzg.meeting.model.dto.UserRegisterDTO;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.CaptchaVO;
import org.lzg.meeting.service.IUserService;
import org.springframework.stereotype.Service;

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
	private RedisComponent redisComponent;

	@Override
	public String login(UserLoginDTO userLoginDTO) {
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
		// 密码校验
		if (!encPassword(userPassword).equals(user.getUserPassword())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
		}
		// 生成token
		String token = SecureUtil.md5("" + user.getId() + System.currentTimeMillis());
		TokenUserInfo tokenUserInfo = new TokenUserInfo();
		tokenUserInfo.setMeetingNo(user.getMeetingNo());
		tokenUserInfo.setUserRole(user.getUserRole());
		tokenUserInfo.setUserId(user.getId());
		// redis存入 用户ID对应的token
		redisComponent.saveToken(user.getId(), token);
		// redis存入 token对应的用户信息
		redisComponent.saveTokenUserInfo(token, tokenUserInfo);
		return token;
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
		user.setUserRole(UserConstant.DEFAULT_ROLE);
		// 设置随机会议号
		user.setMeetingNo(Math.abs(RandomUtil.randomInt()));
		boolean save = this.save(user);
		ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
		return true;
	}

	@Override
	public CaptchaVO getCaptcha() {
		// 定义图形验证码的长和宽
		LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(128, 64);

		String captchaKey = "captcha:" + System.currentTimeMillis() + ":" + java.util.UUID.randomUUID();
		String code = lineCaptcha.getCode();
		String imageBase64 = lineCaptcha.getImageBase64();
		// 设置60s超时时间
		redisComponent.saveCaptcha(captchaKey, code);
		CaptchaVO captchaVO = new CaptchaVO();
		captchaVO.setCaptchaKey(captchaKey);
		captchaVO.setCaptchaBase64(imageBase64);
		return captchaVO;
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
		String code = redisComponent.get(captchaKey);
		// 验证码不存在或已过期
		if (code == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期");
		}
		// 验证码校验
		if (!code.equals(captchaCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
		}
	}
}
