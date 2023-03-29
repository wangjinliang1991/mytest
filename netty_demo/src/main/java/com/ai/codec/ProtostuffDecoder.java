package com.ai.codec;

import com.ai.model.UserInfo;
import com.ai.utils.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ProtostuffDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        try {
            int length = msg.readableBytes();
            byte[] data = new byte[length];
            msg.readBytes(data);
            UserInfo userInfo = ProtostuffUtil.deserialize(data, UserInfo.class);
            out.add(userInfo);
        } catch (Exception e) {
            log.error("ProtostuffDecoder error, msg={}",e.getMessage());
        }
    }
}
