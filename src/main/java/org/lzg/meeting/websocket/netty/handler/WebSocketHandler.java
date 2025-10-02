package org.lzg.meeting.websocket.netty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
		String text = textWebSocketFrame.text();
		log.info("用户{}发送了消息: {}", channelHandlerContext.channel().id().toString(), text);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info("用户{}连接成功", ctx.channel().id().toString());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("用户{}断开连接", ctx.channel().id().toString());
	}
}
