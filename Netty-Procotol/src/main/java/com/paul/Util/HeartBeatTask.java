package com.paul.Util;

import com.paul.entity.Header;
import com.paul.entity.NettyMessage;
import com.paul.enums.MessageTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import sun.nio.ch.Net;

public class HeartBeatTask implements Runnable {

    private final ChannelHandlerContext ctx;

    public HeartBeatTask(ChannelHandlerContext ctx){
        this.ctx = ctx;
    }

    @Override
    public void run() {
        Header header = new Header();
        NettyMessage nettyMessage = new NettyMessage();
        header.setType(MessageTypeEnum.PING.getType());
        nettyMessage.setHeader(header);
        ctx.writeAndFlush(nettyMessage);
    }
}
