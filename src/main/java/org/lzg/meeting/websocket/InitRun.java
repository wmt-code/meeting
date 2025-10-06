package org.lzg.meeting.websocket;

import cn.hutool.setting.yaml.YamlUtil;
import jakarta.annotation.Resource;
import org.lzg.meeting.constant.Constants;
import org.lzg.meeting.websocket.message.MsgHandler;
import org.lzg.meeting.websocket.netty.NettyWebsocketStarter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InitRun implements ApplicationRunner {
	@Resource
	private NettyWebsocketStarter nettyWebsocketStarter;
	@Resource
	private MsgHandler msgHandler;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 启动netty
		new Thread(nettyWebsocketStarter).start();
		// 如果是redis则进行 启动消息监听
		// 从 resources 目录读取 application.yml
		Map<String, Object> yamlMap = YamlUtil.loadByPath("application-prod.yml");
		// 访问层级配置
		Map<String, Object> message = (Map<String, Object>) yamlMap.get("message");
		Map<String, Object> handle = (Map<String, Object>) message.get("handle");
		String channel = (String) handle.get("channel");
		if (channel != null && channel.equals(Constants.MESSAGE_HANDLE_CHANNEL_REDIS)) {
			new Thread(() -> {
				msgHandler.listener();
			}).start();
		}
	}
}
