package org.lzg.meeting.model.dto;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户密码修改DTO
 *
 * @author lzg
 * @since 2025-10-15
 */
@Data
@Schema(description = "用户密码修改DTO")
public class UserPasswordDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 旧密码
	 */
	@Schema(description = "旧密码", required = true)
	private String oldPassword;

	/**
	 * 新密码
	 */
	@Schema(description = "新密码", required = true)
	private String newPassword;

	/**
	 * 确认新密码
	 */
	@Schema(description = "确认新密码", required = true)
	private String confirmPassword;
}
