package com.ai;

import com.ai.codec.ProtostuffDecoder;
import com.ai.handler.server.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class NettyServer {
    public static void main(String[] args) {
        NettyServer server = new NettyServer();
        server.start(8888);
    }

    private void start(int port) {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        // build bootstrap
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // every channel execute initChannel when init, so after connection, c/s entry init a channel, a pipeline, thread isolate
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                           /* pipeline.addLast(new ServerOutboundHandler());
                            pipeline.addLast(new ServerInboundHandler());
                            pipeline.addLast(new SimpleServerInboundHandler());*/
//                            pipeline.addLast(new DelimiterBasedFrameDecoder(65536, socketChannel.alloc().buffer().writeBytes("$".getBytes(StandardCharsets.UTF_8))));
//                            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4,0,4));
//                            pipeline.addLast(new ProtostuffDecoder());
//                            pipeline.addLast(new TcpStickHalfHandler());

                            //todo http cannot run
                            pipeline.addLast(new HttpResponseEncoder());
                            pipeline.addLast(new MyHttpServerHandler());
                            pipeline.addLast(new HttpObjectAggregator(1024 * 1024 * 8));
                        }
                    });
            // bind port
            ChannelFuture future = serverBootstrap.bind(port).sync();
            // listen port closed
            future.channel().closeFuture().sync();
        }  catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // release connect resource
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
