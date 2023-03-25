package com.ai.handler.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ServerOutboundHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log.info("ServerOutboundHandler write executed ");
        log.info("write back to client data: " + ((ByteBuf) msg).toString(Charset.defaultCharset()));

        super.write(ctx, msg, promise);

        ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(" append ".getBytes(StandardCharsets.UTF_8)));
    }
}
