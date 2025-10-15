package org.lzg.meeting.service;

import org.lzg.meeting.model.entity.File;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 文件表 服务类
 * </p>
 *
 * @author lzg
 * @since 2025-10-15
 */
public interface IFileService extends IService<File> {

	/**
	 * 上传文件
	 *
	 * @param file   文件
	 * @param userId 用户ID
	 * @param folder 文件夹名称
	 * @return 文件信息
	 */
	File uploadFile(MultipartFile file, Long userId, String folder);

	/**
	 * 删除文件
	 *
	 * @param fileId 文件ID
	 * @param userId 用户ID
	 * @return 是否删除成功
	 */
	Boolean deleteFile(Long fileId, Long userId);

	/**
	 * 根据用户ID获取文件列表
	 *
	 * @param userId 用户ID
	 * @return 文件列表
	 */
	java.util.List<File> listByUserId(Long userId);
}
