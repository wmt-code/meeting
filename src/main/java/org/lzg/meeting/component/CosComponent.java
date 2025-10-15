package org.lzg.meeting.component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.lzg.meeting.config.CosConfig;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;

import cn.hutool.core.util.IdUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯云COS操作组件
 *
 * @author lzg
 * @since 2025-10-15
 */
@Component
@Slf4j
public class CosComponent {

	@Resource
	private COSClient cosClient;

	@Resource
	private CosConfig cosConfig;

	/**
	 * 上传文件到COS
	 *
	 * @param file     文件
	 * @param folder   文件夹名称（如：avatar、document等）
	 * @return 文件访问URL
	 */
	public String uploadFile(MultipartFile file, String folder) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
		}

		try {
			// 获取原始文件名和扩展名
			String originalFilename = file.getOriginalFilename();
			String extension = "";
			if (originalFilename != null && originalFilename.contains(".")) {
				extension = originalFilename.substring(originalFilename.lastIndexOf("."));
			}

			// 生成文件路径：folder/yyyy-MM-dd/uuid.ext
			String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String fileName = IdUtil.simpleUUID() + extension;
			String key = folder + "/" + date + "/" + fileName;

			// 设置文件元数据
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(file.getSize());
			metadata.setContentType(file.getContentType());

			// 上传文件
			InputStream inputStream = file.getInputStream();
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					cosConfig.getBucket(), key, inputStream, metadata);
			cosClient.putObject(putObjectRequest);
			inputStream.close();

			// 返回文件访问URL
			return cosConfig.getHost() + "/" + key;
		} catch (IOException e) {
			log.error("文件上传失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
		}
	}

	/**
	 * 删除COS上的文件
	 *
	 * @param fileUrl 文件URL
	 */
	public void deleteFile(String fileUrl) {
		if (fileUrl == null || fileUrl.isEmpty()) {
			return;
		}

		try {
			// 从URL中提取key
			String key = fileUrl.replace(cosConfig.getHost() + "/", "");
			cosClient.deleteObject(cosConfig.getBucket(), key);
			log.info("删除文件成功: {}", key);
		} catch (Exception e) {
			log.error("删除文件失败: {}", fileUrl, e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文件失败");
		}
	}

	/**
	 * 批量删除文件
	 *
	 * @param fileUrls 文件URL列表
	 */
	public void deleteFiles(String... fileUrls) {
		if (fileUrls == null || fileUrls.length == 0) {
			return;
		}

		for (String fileUrl : fileUrls) {
			deleteFile(fileUrl);
		}
	}
}
