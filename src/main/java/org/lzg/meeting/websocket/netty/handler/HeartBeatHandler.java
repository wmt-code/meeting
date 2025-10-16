package org.lzg.meeting.websocket.netty.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 心跳检测器 读空闲超时6秒后自动关闭连接
 */
@Slf4j
public class HeartBeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            // 6秒内没有读事件 则关闭连接
            if (e.state() == IdleState.READER_IDLE) {
                // 如果是读事件 则关闭连接
                Attribute<String> attr = ctx.channel().attr(AttributeKey.valueOf(ctx.channel().id().toString()));
                String userId = attr.get();
                log.info("用户{}心跳超时，关闭连接", userId);
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) {
                // 如果是写事件 则发送心跳包
                ctx.writeAndFlush("ping");
            }
        }
    }
}
