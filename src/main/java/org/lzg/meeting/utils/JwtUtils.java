package org.lzg.meeting.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Data
public class JwtUtils {
	// 密钥
	private static final String SECRET_KEY = "meeting-secret-key";

	// token 有效期（毫秒）
	public static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000; // 7 天

	// 刷新token的提前时间（毫秒），当token剩余时间少于此值时可以刷新
	public static final long REFRESH_THRESHOLD = 2 * 24 * 60 * 60 * 1000; // 2 天

	// 签名器
	private static final JWTSigner SIGNER = JWTSignerUtil.hs256(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

	/**
	 * 生成 JWT Token
	 *
	 * @param userId      用户ID
	 * @param extraClaims 自定义附加数据
	 * @return token字符串
	 */
	public static String generateToken(String userId, Map<String, Object> extraClaims) {
		long now = System.currentTimeMillis();
		Date expireDate = new Date(now + EXPIRE_TIME);

		Map<String, Object> payload = new HashMap<>();
		payload.put("jti", IdUtil.simpleUUID());   // 唯一ID
		payload.put("sub", userId);                // 主题-用户标识
		payload.put("iat", now / 1000);            // 签发时间（秒）
		payload.put("exp", expireDate.getTime() / 1000); // 过期时间（秒）

		if (extraClaims != null) {
			payload.putAll(extraClaims);
		}

		return JWTUtil.createToken(payload, SIGNER);
	}

	/**
	 * 生成 JWT Token（无额外声明）
	 *
	 * @param userId 用户ID
	 * @return token字符串
	 */
	public static String generateToken(String userId) {
		return generateToken(userId, null);
	}

	/**
	 * 验证并解析 JWT
	 *
	 * @param token token字符串
	 * @return payload数据（验证失败返回null）
	 */
	public static Map<String, Object> parseToken(String token) {
		try {
			JWT jwt = JWTUtil.parseToken(token);

			boolean verify = jwt.setSigner(SIGNER).verify();    // 校验签名
			boolean validate = jwt.validate(0);                 // 校验过期时间

			if (verify && validate) {
				return jwt.getPayloads();
			}
		} catch (Exception e) {
			System.err.println("JWT 解析失败: " + e.getMessage());
		}
		return null;
	}

	/**
	 * 验证token是否有效（不解析payload）
	 *
	 * @param token token字符串
	 * @return true-有效，false-无效
	 */
	public static boolean isTokenValid(String token) {
		return parseToken(token) != null;
	}

	/**
	 * 检查token是否需要刷新
	 * 当token剩余有效时间少于阈值时，返回true
	 *
	 * @param token token字符串
	 * @return true-需要刷新，false-不需要刷新
	 */
	public static boolean needRefresh(String token) {
		try {
			JWT jwt = JWTUtil.parseToken(token);
			if (jwt.setSigner(SIGNER).verify()) {
				Object expObj = jwt.getPayload("exp");
				if (expObj != null) {
					long expTime = Long.parseLong(expObj.toString()) * 1000; // 转换为毫秒
					long currentTime = System.currentTimeMillis();
					long remainingTime = expTime - currentTime;

					// 如果剩余时间少于刷新阈值，则需要刷新
					return remainingTime > 0 && remainingTime < REFRESH_THRESHOLD;
				}
			}
		} catch (Exception e) {
			System.err.println("检查token刷新状态失败: " + e.getMessage());
		}
		return false;
	}

	/**
	 * 刷新token - 基于旧token生成新token
	 * 保留原有的用户信息和自定义声明，重新设置过期时间
	 *
	 * @param oldToken 旧的token
	 * @return 新的token字符串，如果旧token无效则返回null
	 */
	public static String refreshToken(String oldToken) {
		Map<String, Object> claims = parseToken(oldToken);
		if (claims == null) {
			return null;
		}

		// 提取用户ID
		String userId = (String) claims.get("sub");
		if (userId == null) {
			return null;
		}

		// 提取自定义声明（排除标准声明）
		Map<String, Object> extraClaims = new HashMap<>(claims);
		extraClaims.remove("jti");  // 移除JWT ID，会重新生成
		extraClaims.remove("sub");  // 移除主题，会重新设置
		extraClaims.remove("iat");  // 移除签发时间，会重新设置
		extraClaims.remove("exp");  // 移除过期时间，会重新设置

		// 生成新token
		return generateToken(userId, extraClaims.isEmpty() ? null : extraClaims);
	}

	/**
	 * 更新token内容但不重置过期时间
	 *
	 * @param token       原token
	 * @param extraClaims 新增的附加数据
	 * @return 新的token字符串（保持原过期时间）
	 */
	public static String updateTokenWithoutResetExpiration(String token, Map<String, Object> extraClaims) {
		Map<String, Object> claims = parseToken(token);
		if (claims == null) {
			return null;
		}

		// 获取原有的过期时间
		Object expObj = claims.get("exp");
		if (expObj == null) {
			return null;
		}

		long originalExp = ((Number) expObj).longValue();

		// 构建新的payload
		Map<String, Object> newPayload = new HashMap<>(claims);

		// 更新jti（生成新的唯一ID）
		newPayload.put("jti", IdUtil.simpleUUID());

		// 保持原有的过期时间
		newPayload.put("exp", originalExp);

		// 添加新的数据
		if (extraClaims != null) {
			newPayload.putAll(extraClaims);
		}

		return JWTUtil.createToken(newPayload, SIGNER);
	}

	/**
	 * 获取用户ID
	 *
	 * @param token token字符串
	 * @return 用户ID，获取失败返回null
	 */
	public static String getUserId(String token) {
		Map<String, Object> claims = parseToken(token);
		return claims != null ? (String) claims.get("sub") : null;
	}

	/**
	 * 获取token的过期时间
	 *
	 * @param token token字符串
	 * @return 过期时间（Date对象），获取失败返回null
	 */
	public static Date getExpireTime(String token) {
		try {
			JWT jwt = JWTUtil.parseToken(token);
			if (jwt.setSigner(SIGNER).verify()) {
				Object expObj = jwt.getPayload("exp");
				if (expObj != null) {
					long expTime = Long.parseLong(expObj.toString()) * 1000; // 转换为毫秒
					return new Date(expTime);
				}
			}
		} catch (Exception e) {
			System.err.println("获取token过期时间失败: " + e.getMessage());
		}
		return null;
	}

	/**
	 * 获取token的剩余有效时间（毫秒）
	 *
	 * @param token token字符串
	 * @return 剩余时间（毫秒），token无效或已过期返回0
	 */
	public static long getRemainingTime(String token) {
		Date expireTime = getExpireTime(token);
		if (expireTime != null) {
			long remaining = expireTime.getTime() - System.currentTimeMillis();
			return Math.max(0, remaining);
		}
		return 0;
	}

	/**
	 * 从token中获取指定的声明值
	 *
	 * @param token     token字符串
	 * @param claimName 声明名称
	 * @return 声明值，获取失败返回null
	 */
	public static Object getClaim(String token, String claimName) {
		Map<String, Object> claims = parseToken(token);
		return claims != null ? claims.get(claimName) : null;
	}

	/**
	 * 检查token是否即将过期（在指定时间内）
	 *
	 * @param token       token字符串
	 * @param thresholdMs 阈值时间（毫秒）
	 * @return true-即将过期，false-不会在指定时间内过期
	 */
	public static boolean isTokenExpiringSoon(String token, long thresholdMs) {
		long remainingTime = getRemainingTime(token);
		return remainingTime > 0 && remainingTime < thresholdMs;
	}
}