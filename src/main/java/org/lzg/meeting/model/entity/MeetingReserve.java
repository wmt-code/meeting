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
 * 会议预约表
 * </p>
 *
 * @author lzg
 * @since 2025-10-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("meeting_reserve")
public class MeetingReserve implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 会议ID
	 */
	@TableId(value = "meetingId", type = IdType.ASSIGN_ID)
	private Long meetingId;

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
	 * 预约的会议状态 0-已预约 1-已取消 2-已完成
	 */
	private Integer status;

	/**
	 * 会议开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 会议持续时间 (单位分钟)
	 */
	private Integer duration;


}
