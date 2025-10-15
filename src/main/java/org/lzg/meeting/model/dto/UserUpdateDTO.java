package org.lzg.meeting.model.dto;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户信息更新DTO
 *
 * @author lzg
 * @since 2025-10-15
 */
@Data
@Schema(description = "用户信息更新DTO")
public class UserUpdateDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户名称
	 */
	@Schema(description = "用户名称")
	private String userName;

	/**
	 * 用户邮箱
	 */
	@Schema(description = "用户邮箱")
	private String email;
}
