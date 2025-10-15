package org.lzg.meeting.model.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 文件表
 * </p>
 *
 * @author lzg
 * @since 2025-10-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file")
public class File implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 文件id
	 */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 用户id
	 */
	@TableField("userId")
	private Long userId;

	/**
	 * 文件名
	 */
	@TableField("fileName")
	private String fileName;

	/**
	 * 文件原始名称
	 */
	@TableField("originalName")
	private String originalName;

	/**
	 * 文件大小（字节）
	 */
	@TableField("fileSize")
	private Long fileSize;

	/**
	 * 文件类型/MIME类型
	 */
	@TableField("fileType")
	private String fileType;

	/**
	 * 文件存储路径
	 */
	@TableField("filePath")
	private String filePath;

	/**
	 * 文件访问URL
	 */
	@TableField("fileUrl")
	private String fileUrl;

	/**
	 * 文件状态 1正常 0已删除
	 */
	@TableField("status")
	private Integer status;

	/**
	 * 创建时间
	 */
	@TableField("createTime")
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	@TableField("updateTime")
	private LocalDateTime updateTime;
}
