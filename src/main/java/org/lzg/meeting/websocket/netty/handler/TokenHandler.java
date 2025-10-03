package org.lzg.meeting.websocket.netty.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.utils.JwtUtils;
import org.lzg.meeting.websocket.netty.ChannelContextUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * token 校验
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class TokenHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	@Resource
	private ChannelContextUtils channelContextUtils;

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
		String uri = fullHttpRequest.getUri();
		// 解析uri
		QueryStringDecoder qsd = new QueryStringDecoder(uri);
		// 检查参数是否存在
		if (!qsd.parameters().containsKey("token") || qsd.parameters().get("token").isEmpty()) {
			sendErrorResponse(channelHandlerContext);
			return;
		}
		// 校验 token
		String token = qsd.parameters().get("token").getFirst();
		Map<String, Object> parsedToken = JwtUtils.parseToken(token);
		if (parsedToken == null) {
			log.error("token 校验失败：{}", token);
			sendErrorResponse(channelHandlerContext);
			return;
		}
		Long userId = (Long) parsedToken.get("userId");
		// 继续处理下一个handler
		channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
		// 将 userId 和 channel 关联起来
		channelContextUtils.addUserChannel(userId, channelHandlerContext.channel());
	}

	public static void sendErrorResponse(ChannelHandlerContext ctx) {
		DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.FORBIDDEN,
				Unpooled.copiedBuffer("token 无效", CharsetUtil.UTF_8));
		response.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.headers().add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
}
