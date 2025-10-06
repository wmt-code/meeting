package org.lzg.meeting.component;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.lzg.meeting.constant.Constants;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.model.dto.MeetingMemberDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.utils.RedisUtil;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RedisComponent {
	@Resource
	private RedisUtil redisUtil;

	public TokenUserInfo getTokenUserInfo(String token) {
		String redisToken = redisUtil.get(UserConstant.TOKEN + token);
		if (redisToken == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "token无效,请重新登录");
		}
		return JSONUtil.toBean(redisToken, TokenUserInfo.class);
	}

	public String getToken(Long userId) {
		return redisUtil.get(UserConstant.TOKEN + userId);
	}

	public void saveToken(Long userId, String token) {
		redisUtil.setEx(UserConstant.TOKEN + userId, token, UserConstant.TOKEN_EXPIRE_TIME
				, TimeUnit.DAYS);
	}

	public void saveTokenUserInfo(String token, TokenUserInfo tokenUserInfo) {
		redisUtil.setEx(UserConstant.TOKEN + token, JSONUtil.toJsonStr(tokenUserInfo), UserConstant.TOKEN_EXPIRE_TIME
				, TimeUnit.DAYS);
	}

	public Long getExpire(String key) {
		return redisUtil.getExpire(key);
	}

	public void saveTokenEX(String token, TokenUserInfo tokenUserInfo, Long expire) {
		redisUtil.setEx(UserConstant.TOKEN + token, JSONUtil.toJsonStr(tokenUserInfo), expire
				, TimeUnit.SECONDS);
	}

	public void saveCaptcha(String captchaKey, String code) {
		redisUtil.setEx(captchaKey, code, 1, TimeUnit.MINUTES);
	}

	public String get(String key) {
		return redisUtil.get(key);
	}

	public void add2Meeting(Long meetingId, MeetingMemberDTO meetingMemberDTO) {
		redisUtil.hPut(Constants.MEETING_ROOM_KEY + meetingId, meetingMemberDTO.getUserId().toString(),
				JSONUtil.toJsonStr(meetingMemberDTO));
	}

	public List<MeetingMemberDTO> getMeetingMemberList(Long meetingId) {
		List<Object> objects = redisUtil.hValues(Constants.MEETING_ROOM_KEY + meetingId);
		return objects.stream().map(o -> JSONUtil.toBean(o.toString(), MeetingMemberDTO.class)).sorted(Comparator.comparing(MeetingMemberDTO::getLastJoinTime)).toList();
	}

	public MeetingMemberDTO getMeetingMemberDTO(Long meetingId, Long userId) {
		Object object = redisUtil.hGet(Constants.MEETING_ROOM_KEY + meetingId, userId.toString());
		if (object == null) {
			return null;
		}
		return JSONUtil.toBean((String) object, MeetingMemberDTO.class);
	}

	public void updateTokenUserInfo(TokenUserInfo tokenUserInfo) {
		Long userId = tokenUserInfo.getUserId();
		String token = getToken(userId);
		Long expire = getExpire(UserConstant.TOKEN + userId);
		saveTokenEX(token, tokenUserInfo, expire);
	}

}
