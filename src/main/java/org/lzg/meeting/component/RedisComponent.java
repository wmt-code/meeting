package org.lzg.meeting.component;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.lzg.meeting.constant.Constants;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.enums.MeetingMemberStatusEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.model.dto.MeetingMemberDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.utils.RedisUtil;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis相关操作组件，封装了Token、验证码、会议成员等的缓存逻辑
 */
@Component
public class RedisComponent {
	@Resource
	private RedisUtil redisUtil;

	public TokenUserInfo getTokenUserInfo(String token) {
		// 根据token从Redis获取用户信息
		String redisToken = redisUtil.get(UserConstant.TOKEN + token);
		if (redisToken == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "token无效,请重新登录");
		}
		return JSONUtil.toBean(redisToken, TokenUserInfo.class);
	}

	/**
	 * 根据用户ID获取token
	 */
	public String getToken(Long userId) {
		return redisUtil.get(UserConstant.TOKEN + userId);
	}

	/**
	 * 保存token到Redis，设置过期时间
	 */
	public void saveToken(Long userId, String token) {
		redisUtil.setEx(UserConstant.TOKEN + userId, token, UserConstant.TOKEN_EXPIRE_TIME
				, TimeUnit.DAYS);
	}

	/**
	 * 保存token对应的用户信息到Redis，设置过期时间
	 */
	public void saveTokenUserInfo(String token, TokenUserInfo tokenUserInfo) {
		redisUtil.setEx(UserConstant.TOKEN + token, JSONUtil.toJsonStr(tokenUserInfo), UserConstant.TOKEN_EXPIRE_TIME
				, TimeUnit.DAYS);
	}

	/**
	 * 获取指定key的过期时间
	 */
	public Long getExpire(String key) {
		return redisUtil.getExpire(key);
	}

	/**
	 * 保存token对应的用户信息到Redis，指定过期时间（秒）
	 */
	public void saveTokenEX(String token, TokenUserInfo tokenUserInfo, Long expire) {
		redisUtil.setEx(UserConstant.TOKEN + token, JSONUtil.toJsonStr(tokenUserInfo), expire
				, TimeUnit.SECONDS);
	}

	/**
	 * 保存验证码到Redis，1分钟过期
	 */
	public void saveCaptcha(String captchaKey, String code) {
		redisUtil.setEx(captchaKey, code, 1, TimeUnit.MINUTES);
	}

	/**
	 * 获取指定key的值
	 */
	public String get(String key) {
		return redisUtil.get(key);
	}

	/**
	 * 将成员信息添加到会议房间的Redis哈希表
	 */
	public void add2Meeting(Long meetingId, MeetingMemberDTO meetingMemberDTO) {
		redisUtil.hPut(Constants.MEETING_ROOM_KEY + meetingId, meetingMemberDTO.getUserId().toString(),
				JSONUtil.toJsonStr(meetingMemberDTO));
	}

	/**
	 * 获取会议成员列表，并按最后加入时间排序
	 */
	public List<MeetingMemberDTO> getMeetingMemberList(Long meetingId) {
		List<Object> objects = redisUtil.hValues(Constants.MEETING_ROOM_KEY + meetingId);
		return objects.stream().map(o -> JSONUtil.toBean(o.toString(), MeetingMemberDTO.class)).sorted(Comparator.comparing(MeetingMemberDTO::getLastJoinTime)).toList();
	}

	/**
	 * 获取指定会议成员信息
	 */
	public MeetingMemberDTO getMeetingMemberDTO(Long meetingId, Long userId) {
		Object object = redisUtil.hGet(Constants.MEETING_ROOM_KEY + meetingId, userId.toString());
		if (object == null) {
			return null;
		}
		return JSONUtil.toBean((String) object, MeetingMemberDTO.class);
	}

	/**
	 * 更新用户的token信息并保持原有过期时间
	 */
	public void updateTokenUserInfo(TokenUserInfo tokenUserInfo) {
		Long userId = tokenUserInfo.getUserId();
		String token = getToken(userId);
		Long expire = getExpire(UserConstant.TOKEN + userId);
		saveTokenEX(token, tokenUserInfo, expire);
	}

	/**
	 * 会议成员退出会议，更新其状态
	 */
	public boolean exitMeeting(Long meetingId, Long userId, MeetingMemberStatusEnum meetingMemberStatusEnum) {
		MeetingMemberDTO meetingMemberDTO = getMeetingMemberDTO(meetingId, userId);
		if (meetingMemberDTO == null) return false;
		meetingMemberDTO.setStatus(meetingMemberStatusEnum.getStatus());
		add2Meeting(meetingId, meetingMemberDTO);
		return true;
	}

	/**
	 * 根据用户ID获取token对应的用户信息
	 */
	public TokenUserInfo getTokenUserInfoByUserId(Long userId) {
		String token = getToken(userId);
		if (token == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "token无效,请重新登录");
		}
		return getTokenUserInfo(token);
	}

	/**
	 * 清空会议成员列表
	 */
	public void clearMeetingMemberList(Long meetingId) {
		List<MeetingMemberDTO> meetingMemberList = getMeetingMemberList(meetingId);
		List<String> userIdList =
				meetingMemberList.stream().map(m -> String.valueOf(m.getUserId())).distinct().toList();
		redisUtil.hDelete(Constants.MEETING_ROOM_KEY + meetingId, userIdList.toArray(new String[userIdList.size()]));
	}
}
