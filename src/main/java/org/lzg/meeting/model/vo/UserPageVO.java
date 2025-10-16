package org.lzg.meeting.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户分页VO
 *
 * @author lzg
 * @since 2025-10-16
 */
@Data
public class UserPageVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户列表
	 */
	private List<UserVO> records;

	/**
	 * 总记录数
	 */
	private Long total;

	/**
	 * 当前页码
	 */
	private Long current;

	/**
	 * 每页大小
	 */
	private Long size;

	/**
	 * 总页数
	 */
	private Long pages;
}
