package org.lzg.meeting.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.lzg.meeting.component.RedisComponent;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

public class BaseController {
	@Resource
	private RedisComponent redisComponent;

	protected TokenUserInfo getTokenUserInfo() {
		HttpServletRequest request =
				((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
		String token = request.getHeader("token");
		if (Objects.isNull(token)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR,
					"token不存在,请重新登录");
		}
		return redisComponent.getTokenUserInfo(token);
	}

	protected void resetTokenUserInfo(TokenUserInfo tokenUserInfo) {
		HttpServletRequest request =
				((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
		String token = request.getHeader("token");
		Long expire = redisComponent.getExpire(UserConstant.TOKEN + token);
		redisComponent.saveTokenEX(token, tokenUserInfo, expire);
	}

}
