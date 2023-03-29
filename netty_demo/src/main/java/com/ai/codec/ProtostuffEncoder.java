package com.ai.codec;

import com.ai.model.UserInfo;
import com.ai.utils.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ProtostuffEncoder extends MessageToMessageEncoder<UserInfo>{
    @Override
    protected void encode(ChannelHandlerContext ctx, UserInfo userInfo, List<Object> out)  {
        try {
            byte[] data = ProtostuffUtil.serialize(userInfo);
            ByteBuf buf = ctx.alloc().buffer(data.length);
            buf.writeBytes(data);
            out.add(buf);
        } catch (Exception e) {
            log.error("ProtostuffEncoder error,msg={}",e.getMessage());
        }
    }
}
