package com.paul.Handler;

import com.paul.Util.HeartBeatTask;
import com.paul.entity.NettyMessage;
import com.paul.enums.MessageTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HeartBeatReqHandler extends SimpleChannelInboundHandler<NettyMessage> {
    private volatile ScheduledFuture<?> heartBeat;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage) throws Exception {
        //登录成功后，会讲那个 message 继续传递过来，握手成功后，触发定时心跳
        if(nettyMessage.getHeader() != null && nettyMessage.getHeader().getType() == MessageTypeEnum.HANDRESP.getType()){
            System.out.println("1111111111111");
            heartBeat = channelHandlerContext.executor().scheduleAtFixedRate(new HeartBeatTask(channelHandlerContext),0,5000, TimeUnit.MILLISECONDS);
        }else if(nettyMessage.getHeader() != null && nettyMessage.getHeader().getType() == MessageTypeEnum.PONG.getType()){
            System.out.println("receive heart beat from server" + nettyMessage.toString());
        }else{
            channelHandlerContext.fireChannelRead(nettyMessage);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(heartBeat != null){
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireChannelRead(cause);
    }
}
