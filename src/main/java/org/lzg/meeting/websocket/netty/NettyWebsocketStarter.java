package org.lzg.meeting.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.config.WebSocketConfig;
import org.lzg.meeting.websocket.netty.handler.HeartBeatHandler;
import org.lzg.meeting.websocket.netty.handler.TokenHandler;
import org.lzg.meeting.websocket.netty.handler.WebSocketHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NettyWebsocketStarter implements Runnable {
	private EventLoopGroup boss = new NioEventLoopGroup();
	private  EventLoopGroup worker = new NioEventLoopGroup();
	@Resource
	private WebSocketHandler webSocketHandler;
	@Resource
	private TokenHandler tokenHandler;
	@Resource
	private WebSocketConfig webSocketConfig;

	@Override
	public void run() {
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap()
					.group(boss, worker)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.DEBUG))
					.childHandler(new ChannelInitializer<NioSocketChannel>() {
						@Override
						protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
							ChannelPipeline pipeline = nioSocketChannel.pipeline();
							// http编解码器
							pipeline.addLast(new HttpServerCodec());
							// http数据聚合，聚合成完整的FullHttpRequest FullHttpResponse
							pipeline.addLast(new HttpObjectAggregator(64 * 1024));
							// int readerIdleTimeSeconds, 一段时间未收到数据，则触发一次IdleState.READER_IDLE
							// int writerIdleTimeSeconds, 一段时间未发送数据，则触发一次IdleState.WRITER_IDLE
							// int allIdleTimeSeconds 一段时间未读写，则触发一次IdleState.ALL_IDLE
							pipeline.addLast(new IdleStateHandler(6, 0, 0));
							pipeline.addLast(new HeartBeatHandler());

							// token 校验
							pipeline.addLast(tokenHandler);
							// websocket 处理器
							/*
							 * String websocketPath, websocket路径
							 * String subprotocols, 子协议
							 * boolean allowExtensions, 是否允许扩展
							 * int maxFrameSize, 最大帧大小 6553
							 * boolean allowMaskMismatch, 允许掩码不匹配
							 * boolean checkStartsWith, 检查是否以指定的字符串开头
							 * long handshakeTimeoutMillis  握手超时时间
							 */
							pipeline.addLast(new WebSocketServerProtocolHandler(
									"/ws",
									null,
									true,
									6553,
									true,
									true,
									10000L
							));
							pipeline.addLast(webSocketHandler);
						}
					});
			Channel channel = serverBootstrap.bind(Integer.parseInt(webSocketConfig.getPort())).sync().channel();
			log.info("启动成功，监听端口：{}", channel.localAddress());
			channel.closeFuture().sync();
		} catch (Exception e) {
			log.error("netty启动失败：{}",e.getMessage());
		} finally {
			boss.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}

	@PreDestroy
	public void destroy() {
		boss.shutdownGracefully();
		worker.shutdownGracefully();
	}
}
