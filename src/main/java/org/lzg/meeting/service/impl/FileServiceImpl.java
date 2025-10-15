package org.lzg.meeting.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.lzg.meeting.component.CosComponent;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.mapper.FileMapper;
import org.lzg.meeting.model.entity.File;
import org.lzg.meeting.service.IFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 文件表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-10-15
 */
@Service
@Slf4j
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

	@Resource
	private CosComponent cosComponent;

	@Override
	public File uploadFile(MultipartFile file, Long userId, String folder) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
		}

		// 上传文件到COS
		String fileUrl = cosComponent.uploadFile(file, folder);

		// 保存文件信息到数据库
		File fileEntity = new File();
		fileEntity.setUserId(userId);
		fileEntity.setFileName(file.getName());
		fileEntity.setOriginalName(file.getOriginalFilename());
		fileEntity.setFileSize(file.getSize());
		fileEntity.setFileType(file.getContentType());
		fileEntity.setFilePath(folder);
		fileEntity.setFileUrl(fileUrl);
		fileEntity.setStatus(1);
		fileEntity.setCreateTime(LocalDateTime.now());
		fileEntity.setUpdateTime(LocalDateTime.now());

		boolean saved = this.save(fileEntity);
		if (!saved) {
			// 如果保存失败，删除已上传的文件
			cosComponent.deleteFile(fileUrl);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件信息保存失败");
		}

		return fileEntity;
	}

	@Override
	public Boolean deleteFile(Long fileId, Long userId) {
		if (fileId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件ID不能为空");
		}

		// 查询文件信息
		File file = this.getById(fileId);
		if (file == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
		}

		// 验证文件所属用户
		if (!file.getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该文件");
		}

		// 删除COS上的文件
		cosComponent.deleteFile(file.getFileUrl());

		// 删除数据库记录
		return this.removeById(fileId);
	}

	@Override
	public List<File> listByUserId(Long userId) {
		QueryWrapper<File> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userId", userId);
		queryWrapper.eq("status", 1);
		queryWrapper.orderByDesc("createTime");
		return this.list(queryWrapper);
	}
}
