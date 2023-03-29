package com.ai.handler.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ServerReadIdleHandler extends IdleStateHandler {
    public ServerReadIdleHandler() {
        super(10,0,0, TimeUnit.SECONDS);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (evt == IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT) {
            //close connect
            log.info("ServerReadIdleHandler close channel");
            ctx.close();
            return;
        }
        super.channelIdle(ctx,evt);
    }
}
