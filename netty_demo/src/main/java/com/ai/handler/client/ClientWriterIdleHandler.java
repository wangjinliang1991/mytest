package com.ai.handler.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ClientWriterIdleHandler extends IdleStateHandler {
    public ClientWriterIdleHandler() {
        super(0, 5, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (evt == IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT) {
            log.info("ClientWriterIdleHandler send keepalive msg");
            ctx.channel().writeAndFlush("this is keepalive msg");
        }
        super.channelIdle(ctx, evt);
    }
}
