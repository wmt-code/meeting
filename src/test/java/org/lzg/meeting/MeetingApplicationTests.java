package org.lzg.meeting;

import cn.hutool.setting.yaml.YamlUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class MeetingApplicationTests {
	@Test
	void contextLoads() {
		// 从 resources 目录读取 application.yml
		Map<String, Object> yamlMap = YamlUtil.loadByPath("application.yml");

		// 访问层级配置
		Map<String, Object> message = (Map<String, Object>) yamlMap.get("message");
		Map<String, Object> handle = (Map<String, Object>) message.get("handle");
		String channel = (String) handle.get("channel");

		System.out.println("channel = " + channel); // 输出 redis
	}
}
