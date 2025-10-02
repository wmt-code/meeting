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
	 * 获取用户ID（示例）
	 */
	public static String getUserId(String token) {
		Map<String, Object> claims = parseToken(token);
		return claims != null ? (String) claims.get("sub") : null;
	}
}
