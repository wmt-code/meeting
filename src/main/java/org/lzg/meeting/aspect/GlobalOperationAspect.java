package org.lzg.meeting.aspect;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.lzg.meeting.annotation.GlobalInterceptor;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.utils.RedisUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Objects;

@Component
@Aspect
@Slf4j
public class GlobalOperationAspect {
	@Resource
	private RedisUtil redisUtil;

	@Before("@annotation(org.lzg.meeting.annotation.GlobalInterceptor)")
	public void doInterceptor(JoinPoint joinPoint) {
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		GlobalInterceptor globalInterceptor = method.getAnnotation(GlobalInterceptor.class);
		if (globalInterceptor == null) {
			return;
		}
		if (globalInterceptor.checkAdmin() || globalInterceptor.checkLogin()) {
			checkLogin(globalInterceptor.checkAdmin());
		}
		log.info("全局操作拦截器执行，方法：{}", method.getName());
	}

	private void checkLogin(boolean checkAdmin) {
		HttpServletRequest request = getHttpServletRequest();
		String token = request.getHeader("token");
		if (token == null || token.isEmpty()) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录,请先登录");
		}
		String redisToken = redisUtil.get(UserConstant.TOKEN + token);
		if (redisToken == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "token无效,请重新登录");
		}
		TokenUserInfo tokenUserInfo = JSONUtil.toBean(redisToken, TokenUserInfo.class);
		if (checkAdmin) {
			String userRole = tokenUserInfo.getUserRole();
			if (!"admin".equals(userRole)) {
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
			}
		}
	}

	private static HttpServletRequest getHttpServletRequest() {
		return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
	}
}
