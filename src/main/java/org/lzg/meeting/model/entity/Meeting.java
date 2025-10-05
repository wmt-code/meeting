package org.lzg.meeting.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 会议表
 * </p>
 *
 * @author lzg
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("meeting")
public class Meeting implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 会议ID
	 */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	private Integer meetingNo;

	/**
	 * 会议名称
	 */
	private String meetingName;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 创建人ID
	 */
	private Long createUserId;

	/**
	 * 加入会议的类型，例如直接加入或者要密码加入
	 */
	private Integer joinType;

	/**
	 * 加入密码
	 */
	private String joinPassword;

	/**
	 * 会议开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 会议结束时间
	 */
	private LocalDateTime endTime;

	/**
	 * 会议状态 进行中 或 已结束
	 */
	private Integer status;


}
