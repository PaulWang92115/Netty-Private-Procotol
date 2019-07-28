package com.paul.Handler;


import com.paul.entity.Header;
import com.paul.entity.NettyMessage;
import com.paul.enums.MessageTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class HeartBeatRespHandler extends SimpleChannelInboundHandler<NettyMessage> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage) throws Exception {
        System.out.println("收到："+ nettyMessage);
        if(null != nettyMessage && nettyMessage.getHeader().getType() == MessageTypeEnum.PING.getType()){
            //收到心跳请求，直接应答
            Header h = new Header();
            h.setType(MessageTypeEnum.PONG.getType());
            NettyMessage nettyMessage1 = new NettyMessage();
            nettyMessage1.setHeader(h);
            channelHandlerContext.writeAndFlush(nettyMessage1);

        }
    }
}
