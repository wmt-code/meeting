package org.lzg.meeting.constant;

public interface UserConstant {
	String USER_ID = "userId";

	String TOKEN = "token:";

	/**
	 * 盐值，混淆密码
	 */
	String SALT = "meeting";

	/**
	 * 用户登录态键
	 */
	String USER_LOGIN_STATE = "user_login";

	//  region 权限

	/**
	 * 默认角色
	 */
	String DEFAULT_ROLE = "user";

	/**
	 * 管理员角色
	 */
	String ADMIN_ROLE = "admin";
	/**
	 * 用户token过期时间，单位：天
	 */
	long TOKEN_EXPIRE_TIME = 30;


	// endregion
}
