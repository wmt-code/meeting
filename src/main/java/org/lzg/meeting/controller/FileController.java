package org.lzg.meeting.controller;

import java.util.List;

import org.lzg.meeting.annotation.GlobalInterceptor;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.model.entity.File;
import org.lzg.meeting.service.IFileService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 文件管理 前端控制器
 * </p>
 *
 * @author lzg
 * @since 2025-10-15
 */
@RestController
@RequestMapping("/file")
@Slf4j
@Tag(name = "文件管理", description = "文件上传、删除等接口")
public class FileController extends BaseController {

	@Resource
	private IFileService fileService;

	/**
	 * 上传文件
	 *
	 * @param file   文件
	 * @param folder 文件夹名称（可选，默认为document）
	 * @return 文件信息
	 */
	@PostMapping("/upload")
	@Operation(summary = "上传文件")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<File> uploadFile(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "folder", defaultValue = "document") String folder) {
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
		
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		File uploadedFile = fileService.uploadFile(file, tokenUserInfo.getUserId(), folder);
		return ResultUtils.success(uploadedFile);
	}

	/**
	 * 删除文件
	 *
	 * @param fileId 文件ID
	 * @return 是否删除成功
	 */
	@DeleteMapping("/{fileId}")
	@Operation(summary = "删除文件")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<Boolean> deleteFile(@PathVariable Long fileId) {
		ThrowUtils.throwIf(fileId == null, ErrorCode.PARAMS_ERROR, "文件ID不能为空");
		
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		Boolean result = fileService.deleteFile(fileId, tokenUserInfo.getUserId());
		return ResultUtils.success(result);
	}

	/**
	 * 获取当前用户的文件列表
	 *
	 * @return 文件列表
	 */
	@GetMapping("/list")
	@Operation(summary = "获取当前用户的文件列表")
	@GlobalInterceptor(checkLogin = true)
	public BaseResponse<List<File>> listFiles() {
		TokenUserInfo tokenUserInfo = getTokenUserInfo();
		List<File> files = fileService.listByUserId(tokenUserInfo.getUserId());
		return ResultUtils.success(files);
	}
}
