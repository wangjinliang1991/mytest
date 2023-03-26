package com.ai;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyFutureTest {
    @Test
    public void testFuture() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        Future<String> future = group.submit(() -> {
            log.info("async task starts to execute, time is {}", LocalDateTime.now());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("async task has finished, time is {}", LocalDateTime.now());
            return "hello netty future";
        });

        // netty extra: add listener
        future.addListener(future1 -> {
            log.info("receive notify of async task, result is {}, time is  {}", future1.get(), LocalDateTime.now());
        });
        log.info("main thread");
        TimeUnit.SECONDS.sleep(10);
    }
}
