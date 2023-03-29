package com.ai.handler.server;

import com.ai.model.UserInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

@Slf4j
public class TcpStickHalfHandler extends ChannelInboundHandlerAdapter {
    int count = 0;
    ExecutorService service;

    public TcpStickHalfHandler(ExecutorService service) {
        this.service = service;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       /* ByteBuf buf = (ByteBuf) msg;
        count++;
        log.info("server receives the {} data: {}",count, buf.toString(StandardCharsets.UTF_8));*/

        count++;
        log.info("server receives the {} data: {}",count,(UserInfo)msg);

        super.channelRead(ctx, msg);
    }
}
