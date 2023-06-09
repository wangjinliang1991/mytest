package com.ai.handler.client;

import com.ai.model.UserInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ClientInboundHandler extends ChannelInboundHandlerAdapter {
    /**
     * 通道准备就绪
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("ClientInboundHandler channelActive executed");
        // send data patch
        /*for (int i = 0; i < 100; i++) {
            UserInfo userInfo = new UserInfo(i,"name="+i,i+1,(i%2==0)?"man":"woman","beijing");
            ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(userInfo.toString().getBytes(StandardCharsets.UTF_8)));
        }*/

        // protostuff can use userinfo directly
        UserInfo userInfo;
        for (int i = 0; i < 100; i++) {
            userInfo = new UserInfo(i, "name=" + i, i + 1, (i % 2 == 0) ? "man" : "woman", "beijing");
            ctx.writeAndFlush(userInfo);
        }
        super.channelActive(ctx);
    }

    /**
     * 失活，释放资源
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("ClientInboundHandler channelInactive executed");
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("ClientInboundHandler channelRead executed");
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        //transfer to string
        String data = new String(bytes, Charset.defaultCharset());
        log.info("receive data from server: " + data);
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("ClientInboundHandler channelReadComplete executed");

        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("ClientInboundHandler exceptionCaught executed");
        super.exceptionCaught(ctx, cause);
    }
}
