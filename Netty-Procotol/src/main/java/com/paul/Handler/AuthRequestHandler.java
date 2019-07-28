package com.paul.Handler;


import com.paul.Util.SercretUtil;
import com.paul.entity.Header;
import com.paul.entity.NettyMessage;
import com.paul.enums.MessageTypeEnum;
import com.paul.instance.NettyClient;
import com.paul.instance.NettyClientService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import sun.nio.ch.Net;

public class AuthRequestHandler extends SimpleChannelInboundHandler<NettyMessage> {

    private NettyClientService nettyClient;

    public AuthRequestHandler(NettyClientService nettyClient){
        this.nettyClient = nettyClient;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyMessage response) throws Exception {
        if(response.getHeader()!=null && response.getHeader().getType() == MessageTypeEnum.HANDRESP.getType()){
            byte res = (byte)response.getBody();
            //判断登录状态
            if(res != 0){
                //握手/认证 失败
                channelHandlerContext.close();
            }else{
                //登录成功
                System.out.println("登录成功");
                nettyClient.setChannel(channelHandlerContext.channel());
                channelHandlerContext.fireChannelRead(response);
            }
        }else{
            //此处有两种情况，消息没有 header，这种消息后面也不会处理，或者消息类型不是握手应答，
            //将消息继续向下一个 handler 传递
            channelHandlerContext.fireChannelRead(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        /* 客户端需要根据服务端的认证的方式添加 Authorization
         * 服务端认证方式为 NONE 则不需要传
         * 服务端认证方式为 BASIC 则需要将 appkey，appSercret 以 BASIC 方式加密传送
         * 服务端认证方式为 OAUTH 则需奥将约定好的 token，sign 等传给服务端
         */
        String app = "netty-client" +"|" + "paul";
        String after = SercretUtil.invokeEncryptEncode(app);
        // 发送握手请求
        Header h = new Header();
        h.setType(MessageTypeEnum.HANDREQ.getType());
        h.setAuthorization(after);
        NettyMessage nettyMessage = new NettyMessage();
        nettyMessage.setHeader(h);
        ctx.writeAndFlush(nettyMessage);

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;		// 强制类型转换

            if (event.state() == IdleState.READER_IDLE) {
                // 读空闲一定时间后关闭那个对应的 Channel
                Channel channel = ctx.channel();
                // 关闭无用的channel，以防资源浪费
                channel.close();
                //定时重连
            }
        }
    }
}
