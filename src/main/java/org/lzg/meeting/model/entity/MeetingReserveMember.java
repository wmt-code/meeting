package org.lzg.meeting.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 邀请用户表
 * </p>
 *
 * @author lzg
 * @since 2025-10-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("meeting_reserve_member")
public class MeetingReserveMember implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 会议ID
	 */
	@TableId(value = "meetingId", type = IdType.ASSIGN_ID)
	private Long meetingId;

	/**
	 * 受邀用户的ID
	 */
	private Long invitateUserId;


}
