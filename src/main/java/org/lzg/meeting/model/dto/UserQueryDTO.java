package org.lzg.meeting.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author lzg
 * @since 2025-10-16
 */
@Data
public class UserQueryDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户名（模糊查询）
	 */
	private String userName;

	/**
	 * 用户账号（模糊查询）
	 */
	private String userAccount;

	/**
	 * 邮箱（模糊查询）
	 */
	private String email;

	/**
	 * 用户状态 1启用 0禁用
	 */
	private Integer status;

	/**
	 * 用户角色
	 */
	private String userRole;

	/**
	 * 当前页码
	 */
	private Integer current = 1;

	/**
	 * 页面大小
	 */
	private Integer pageSize = 10;

	/**
	 * 排序字段
	 */
	private String sortField;

	/**
	 * 排序顺序 asc/desc
	 */
	private String sortOrder = "desc";
}
