package com.ai;

import com.ai.handler.client.ClientInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;

import java.nio.charset.StandardCharsets;

public class NettyClient {
    public static void main(String[] args) {
        NettyClient nettyClient = new NettyClient();
        nettyClient.connect("127.0.0.1",8889);
    }

    private void connect(String host, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // codec should be first outbound handler
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new ClientInboundHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port).sync();

            Channel channel = future.channel();
            ByteBuf buffer = channel.alloc().buffer();
            String msg = "hello netty server, I am netty client!";
            buffer.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
            channel.writeAndFlush(buffer);

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
