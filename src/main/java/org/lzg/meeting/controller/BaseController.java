package org.lzg.meeting.controller;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.utils.RedisUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BaseController {
	@Resource
	private RedisUtil redisUtil;

	protected TokenUserInfo getTokenUserInfo() {
		HttpServletRequest request =
				((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
		String token = request.getHeader("token");
		if (Objects.isNull(token)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR,
					"token不存在,请重新登录");
		}
		String redisToken = redisUtil.get(UserConstant.TOKEN + token);
		if (Objects.isNull(redisToken)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR,
					"token无效,请重新登录");
		}
		return JSONUtil.toBean(redisToken, TokenUserInfo.class);
	}

	protected void resetToken(TokenUserInfo tokenUserInfo) {
		HttpServletRequest request =
				((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
		String token = request.getHeader("token");
		Long expire = redisUtil.getExpire(UserConstant.TOKEN + token);
		redisUtil.setEx(UserConstant.TOKEN + token, JSONUtil.toJsonStr(tokenUserInfo), expire, TimeUnit.SECONDS);
	}

}
