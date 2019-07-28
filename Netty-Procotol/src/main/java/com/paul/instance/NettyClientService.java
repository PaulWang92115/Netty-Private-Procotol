package com.paul.instance;


import com.google.common.base.StandardSystemProperty;
import com.paul.Handler.AuthRequestHandler;
import com.paul.Handler.AuthResponseHandler;
import com.paul.Handler.ClientHandler;
import com.paul.Handler.HeartBeatReqHandler;
import com.paul.entity.Header;
import com.paul.entity.NettyMessage;
import com.paul.enums.MessageTypeEnum;
import com.paul.serializer.NettyDecoderHandler;
import com.paul.serializer.NettyEncoderHandler;
import com.paul.serializer.SerializeType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class NettyClientService implements NettyClient {


    private static final String OS_NAME = "Linux";

    private static final int port = 30115;

    //此处可自主选择序列化协议
    private SerializeType serializeType = SerializeType.queryByType("ProtoStuff");

    private Bootstrap b;
    EventLoopGroup worker;

    //用于主动发送消息的channel
    private Channel channel;


    //等待Netty握手成功通知信号
    private Lock lock = new ReentrantLock();
    private Condition signal = lock.newCondition();




    @Override
    public void start(String host) {
        b = new Bootstrap();
        if(StandardSystemProperty.OS_NAME.value().equals(OS_NAME)){
            worker = new EpollEventLoopGroup();
            b.group(worker).channel(EpollSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new NettyClientInitializer(this,serializeType));
        }else{
            worker = new NioEventLoopGroup();
            b.group(worker).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new NettyClientInitializer(this,serializeType));
        }

        connect(host);

    }

    @Override
    public void connect(final String host) {
        System.out.println("重连");
        if (channel != null && channel.isActive()) {
            return;
        }
        ChannelFuture future = b.connect(host,port);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(!channelFuture.isSuccess()){
                    channelFuture.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            connect(host);
                        }
                    },5, TimeUnit.SECONDS);
                }
            }
        });
    }

    private void stop() {
        worker.shutdownGracefully();
    }

    public void send(Object body) throws InterruptedException {
        try{
            lock.lock();
            if(null == channel){
                signal.await();
            }
            NettyMessage n = new NettyMessage();
            Header h = new Header();
            h.setType(MessageTypeEnum.REQUEST.getType());
            h.setDate(new Date());
            h.setHost("localhost");
            h.setId(UUID.randomUUID().toString());
            n.setHeader(h);
            n.setBody(body);
            channel.writeAndFlush(n);
        }finally {
            lock.unlock();
        }


    }

    public void setChannel(Channel channel) {
        try {
            lock.lock();
            this.channel = channel;
            //唤醒所有等待的发送
            signal.signalAll();
        } finally {
            lock.unlock();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        NettyClient client = new NettyClientService();
        client.start("localhost");
        ((NettyClientService) client).send("你好");
    }

}
