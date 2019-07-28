package com.paul.Handler;

import com.paul.entity.NettyMessage;
import com.paul.enums.MessageTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<NettyMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage) throws Exception {
        if(nettyMessage.getHeader() != null && nettyMessage.getHeader().getType() == MessageTypeEnum.RESPONSE.getType()){
            //业务应答
            //根据实际情况处理
            System.out.println(nettyMessage.getBody().toString());
        }
    }
}
