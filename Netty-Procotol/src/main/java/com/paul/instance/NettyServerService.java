package com.paul.instance;

import com.google.common.base.StandardSystemProperty;
import com.paul.Handler.*;
import com.paul.entity.NettyMessage;
import com.paul.enums.AuthEnum;
import com.paul.serializer.NettyDecoderHandler;
import com.paul.serializer.NettyEncoderHandler;
import com.paul.serializer.SerializeType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NettyServerService implements NettyServer {

    private static final String OS_NAME = "Linux";

    private static final int port = 30115;

    //此处可自主选择序列化协议
    private SerializeType serializeType = SerializeType.queryByType("ProtoStuff");


    //服务端的认证方式
    private AuthEnum auth;

    public NettyServerService(AuthEnum auth){
        this.auth = auth;
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Override
    public void start() throws InterruptedException{
        ServerBootstrap b = new ServerBootstrap();
        if(StandardSystemProperty.OS_NAME.value().equals(OS_NAME)){
            bossGroup = new EpollEventLoopGroup();
            workerGroup = new EpollEventLoopGroup();
            b.group(bossGroup,workerGroup)
                    .channel(EpollServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new IdleStateHandler(8,0,0));
                            // 自定义的空闲状态检测,放在了AuthResponseHandler
                            pipeline.addLast(new NettyDecoderHandler(NettyMessage.class, serializeType));
                            //注册编码器NettyEncoderHandler
                            pipeline.addLast(new NettyEncoderHandler(serializeType));
                            pipeline.addLast(new AuthResponseHandler(auth));
                            pipeline.addLast(new HeartBeatRespHandler());
                            pipeline.addLast(new ServerHandler());

                        }
                    });
        }else{
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new IdleStateHandler(8,0,0));
                            // 自定义的空闲状态检测,放在了AuthResponseHandler
                            pipeline.addLast(new NettyDecoderHandler(NettyMessage.class, serializeType));
                            //注册编码器NettyEncoderHandler
                            pipeline.addLast(new NettyEncoderHandler(serializeType));
                            pipeline.addLast(new AuthResponseHandler(auth));
                            pipeline.addLast(new HeartBeatRespHandler());
                            pipeline.addLast(new ServerHandler());

                        }
                    });
        }

        try {
            ChannelFuture future = b.bind(port).sync();
            System.out.println("server start at: " +port);
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args){
        NettyServer server = new NettyServerService(AuthEnum.BASIC);
        try {
            server.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
