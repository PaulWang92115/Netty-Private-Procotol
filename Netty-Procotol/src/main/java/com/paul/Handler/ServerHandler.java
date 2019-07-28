package com.paul.Handler;

import com.paul.entity.NettyMessage;
import com.paul.enums.MessageTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerHandler extends SimpleChannelInboundHandler<NettyMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage) throws Exception {
        if(nettyMessage.getHeader() != null && nettyMessage.getHeader().getType() == MessageTypeEnum.REQUEST.getType()){
            //业务请求，根据实际情况自动处理
            System.out.println(nettyMessage.getBody().toString());
        }
    }
}
