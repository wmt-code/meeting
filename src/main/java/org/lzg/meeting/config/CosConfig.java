package org.lzg.meeting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;

import lombok.Data;

/**
 * 腾讯云COS配置类
 *
 * @author lzg
 * @since 2025-10-15
 */
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosConfig {

	/**
	 * COS访问域名
	 */
	private String host;

	/**
	 * 腾讯云SecretId
	 */
	private String secretId;

	/**
	 * 腾讯云SecretKey
	 */
	private String secretKey;

	/**
	 * COS地域
	 */
	private String region;

	/**
	 * 存储桶名称
	 */
	private String bucket;

	/**
	 * 创建COSClient实例
	 *
	 * @return COSClient
	 */
	@Bean
	public COSClient cosClient() {
		// 初始化用户身份信息（secretId, secretKey）
		COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
		// 设置bucket的地域
		ClientConfig clientConfig = new ClientConfig(new Region(region));
		// 生成cos客户端
		return new COSClient(cred, clientConfig);
	}
}
