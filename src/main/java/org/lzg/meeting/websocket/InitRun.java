package org.lzg.meeting.websocket;

import jakarta.annotation.Resource;
import org.lzg.meeting.websocket.message.MsgHandler;
import org.lzg.meeting.websocket.netty.NettyWebsocketStarter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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
		// 启动消息监听
		new Thread(() -> {
			msgHandler.listener();
		}).start();
	}
}
