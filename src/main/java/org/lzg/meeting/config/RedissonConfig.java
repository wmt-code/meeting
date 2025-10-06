package org.lzg.meeting.config;

import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.constant.Constants;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类
 * 只有当配置项message.handle.channel的值为redis时，才会加载该配置类
 */
@Configuration
@ConditionalOnProperty(name = Constants.MESSAGE_HANDLE_CHANNEL_KEY, havingValue =
		Constants.MESSAGE_HANDLE_CHANNEL_REDIS)
@Slf4j
public class RedissonConfig {
	@Value("${spring.data.redis.host:}")
	private String host;
	@Value("${spring.data.redis.port:}")
	private Integer port;
	@Value("${spring.data.redis.password:}")
	private String password;

	@Bean(name = "redissonClient", destroyMethod = "shutdown")
	public RedissonClient redissonClient() {
		try {
			Config config = new Config();
			config.useSingleServer()
					.setAddress("redis://" + host + ":" + port)
					.setPassword(password);
			return Redisson.create(config);
		} catch (Exception e) {
			log.error("redisson初始化失败", e);
		}
		return null;
	}
}
