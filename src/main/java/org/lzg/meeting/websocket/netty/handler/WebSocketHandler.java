package org.lzg.meeting.websocket.netty.handler;

import cn.hutool.json.JSONUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.component.RedisComponent;
import org.lzg.meeting.enums.MessageTypeEnum;
import org.lzg.meeting.enums.MsgSendTypeEnum;
import org.lzg.meeting.model.dto.PeerConnectionDataDTO;
import org.lzg.meeting.model.dto.PeerMessageDTO;
import org.lzg.meeting.model.dto.SendMsgDTO;
import org.lzg.meeting.model.dto.TokenUserInfo;
import org.lzg.meeting.websocket.message.MsgHandler;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	@Resource
	private RedisComponent redisComponent;
	@Resource
	private MsgHandler msgHandler;

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
		String text = textWebSocketFrame.text();
		if ("ping".equalsIgnoreCase(text)) {
			return;
		}
		PeerConnectionDataDTO peerConnectionDataDTO = JSONUtil.toBean(text, PeerConnectionDataDTO.class);
		String token = peerConnectionDataDTO.getToken();
		Long sendUserId = peerConnectionDataDTO.getSendUserId();
		Long receiveUserId = peerConnectionDataDTO.getReceiveUserId();
		String signalType = peerConnectionDataDTO.getSignalType();
		String signalData = peerConnectionDataDTO.getSignalData();
		TokenUserInfo tokenUserInfo = redisComponent.getTokenUserInfo(token);
		if (null == tokenUserInfo) return;
		SendMsgDTO sendMsgDTO = new SendMsgDTO();
		PeerMessageDTO peerMessageDTO = new PeerMessageDTO();
		peerMessageDTO.setSignalType(signalType);
		peerMessageDTO.setSignalData(signalData);
		sendMsgDTO.setMsgSendType(MsgSendTypeEnum.USER.getValue());
		sendMsgDTO.setMeetingId(tokenUserInfo.getMeetingId());
		sendMsgDTO.setMsgType(MessageTypeEnum.PEER.getType());
		sendMsgDTO.setSenderId(sendUserId);
		sendMsgDTO.setMsgContent(peerMessageDTO);
		sendMsgDTO.setReceiverId(receiveUserId);
		msgHandler.sendMessage(sendMsgDTO);
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
