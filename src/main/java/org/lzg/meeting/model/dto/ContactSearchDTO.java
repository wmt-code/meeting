package org.lzg.meeting.model.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 联系人搜索DTO
 */
@Data
public class ContactSearchDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 搜索关键词（支持用户名、账号模糊搜索）
	 */
	private String keyword;

	/**
	 * 当前页码
	 */
	private Integer current = 1;

	/**
	 * 页面大小
	 */
	private Integer pageSize = 10;
}
