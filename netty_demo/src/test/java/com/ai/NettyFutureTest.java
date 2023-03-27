package com.ai;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
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

    @Test
    public void testPromise() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        // promise bind to event loop
        Promise promise = new DefaultPromise(group.next());
        group.submit(() -> {
            log.info("async task starts to run, time is {}", LocalDateTime.now());
            try {
                int i = 1 / 0;
                TimeUnit.SECONDS.sleep(3);
                promise.setSuccess("hello netty promise");
                TimeUnit.SECONDS.sleep(3);
                log.info("async task has finished, time is {}",LocalDateTime.now());
                return;
            } catch (Exception e) {
                promise.setFailure(e);
            }
        });
        promise.addListener(future -> {
            log.info("async task result is: {}", future.get());
        });
        log.info("main thread");
        TimeUnit.SECONDS.sleep(10);
    }
}
