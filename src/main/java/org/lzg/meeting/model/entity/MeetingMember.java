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
 * 会议成员关联表
 * </p>
 *
 * @author lzg
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("meeting_member")
public class MeetingMember implements Serializable {

    @Serial
	private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

    /**
     * 会议ID
     */
    private Long meetingId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
	 * 用户昵称
     */
    private String nickName;

    /**
     * 最后加入时间
     */
    private LocalDateTime lastJoinTime;

    /**
     * 成员状态 被踢出、拉入黑名单
     */
    private Integer status;

    /**
     * 成员类型 主持人、普通成员
     */
    private Integer memberType;

    /**
     * 会议状态 进行中、已结束
     */
    private Integer meetingStatus;


}
