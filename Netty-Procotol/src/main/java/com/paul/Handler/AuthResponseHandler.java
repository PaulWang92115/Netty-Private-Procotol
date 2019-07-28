package com.paul.Handler;

import com.paul.Util.SercretUtil;
import com.paul.entity.Header;
import com.paul.entity.NettyMessage;
import com.paul.enums.AuthEnum;
import com.paul.enums.MessageTypeEnum;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthResponseHandler extends SimpleChannelInboundHandler<NettyMessage> {

    private Map<String, Channel> authedNode = new ConcurrentHashMap<>();

    private AuthEnum auth;

    public AuthResponseHandler(AuthEnum auth){
        this.auth = auth;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage) throws Exception {
        if(nettyMessage.getHeader() != null && nettyMessage.getHeader().getType() == MessageTypeEnum.HANDREQ.getType()){
            //检查是否已经是登录状态
            String channelId = channelHandlerContext.channel().id().asLongText();
            NettyMessage response = null;
            if(authedNode.containsKey(channelId)){
                //已经登录了，拒绝
                response = constructResponse((byte) -1);
            }else{
                //登录校验
                if(auth.getType().equals("basic")){
                    String authronization = nettyMessage.getHeader().getAuthorization();
                    // 解码 appkey 和  appsercret 来进行验证，在我们编程时可以采用 普通的 base64加密（不太安全），或者使用自己
                    // 约定好的 md5 + salt 方式，更加安全的就是使用 AES 方式，根据实际情况自己选择。
                    String app = SercretUtil.invokeDecryptEncode(authronization);
                    System.out.println("app:"+ app);
                    String appKey = app.split("\\|")[0];
                    String appSercret = app.split("\\|")[1];
                    System.out.println("appkey:"+appKey +"," +"appsercret:" +appSercret);
                    //正常情况下应该存到数据库里面，这里就直接做验证
                    if(appKey.equals("netty-client") && appSercret.equals("paul")){
                        response = constructResponse((byte) 0);
                    }else{
                        response = constructResponse((byte) -2);
                    }

                }else if(auth.getType().equals("oauth")){
                    String authronization = nettyMessage.getHeader().getAuthorization();

                }else{
                    response = constructResponse((byte) 0);
                }

            }
            if((byte)response.getBody() == 0){
                authedNode.put(channelId,channelHandlerContext.channel());
            }
            System.out.println("Auth response is " + response.toString());
            //写回登录认证状态
            channelHandlerContext.writeAndFlush(response);
        }else{
            channelHandlerContext.fireChannelRead(nettyMessage);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        authedNode.remove(ctx.channel().remoteAddress().toString()); //删除缓存
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;		// 强制类型转换

            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("读空闲...");
                // 读空闲一定时间后关闭那个对应的 Channel
                Channel channel = ctx.channel();
                // 关闭无用的channel，以防资源浪费
                channel.close();
                //清除 map,可以让客户端再次登录
                authedNode.remove(channel.id().asLongText());

            }
        }
    }

    private NettyMessage constructResponse(byte b){
        Header h = new Header();
        h.setType(MessageTypeEnum.HANDRESP.getType());
        NettyMessage nettyMessage = new NettyMessage();
        nettyMessage.setHeader(h);
        nettyMessage.setBody(b);
        return nettyMessage;
    }
}
