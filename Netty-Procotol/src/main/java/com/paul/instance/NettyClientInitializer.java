package com.paul.instance;

import com.paul.Handler.AuthRequestHandler;
import com.paul.Handler.ClientHandler;
import com.paul.Handler.HeartBeatReqHandler;
import com.paul.entity.NettyMessage;
import com.paul.serializer.NettyDecoderHandler;
import com.paul.serializer.NettyEncoderHandler;
import com.paul.serializer.SerializeType;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private NettyClientService nettyClientService;
    private SerializeType serializeType;

    public NettyClientInitializer(NettyClientService nettyClientService, SerializeType serializeType){
        this.serializeType = serializeType;
        this.nettyClientService = nettyClientService;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new IdleStateHandler(8,0,0));
        // 自定义的空闲状态检测,放在了AuthRequestHandler
        pipeline.addLast(new NettyEncoderHandler(serializeType));
        pipeline.addLast(new NettyDecoderHandler(NettyMessage.class,serializeType));
        pipeline.addLast(new AuthRequestHandler(nettyClientService));
        pipeline.addLast(new HeartBeatReqHandler());
        pipeline.addLast(new ClientHandler());

    }
}
