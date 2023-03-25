package com.ai.handler.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class ServerInboundHandler extends ChannelInboundHandlerAdapter {


    /**
     * 通道准备就绪
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("ServerInboundHandler channelActive executed");
        super.channelActive(ctx);
    }

    /**
     * 失活，释放资源
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("ServerInboundHandler channelInactive executed");
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("ServerInboundHandler channelRead executed");
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        //transfer to string
        String data = new String(bytes, Charset.defaultCharset());
        log.info("receive data from client: " + data);
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("ServerInboundHandler channelReadComplete executed");
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("ServerInboundHandler exceptionCaught executed");
        super.exceptionCaught(ctx, cause);
    }
}
