package com.ai;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.*;

@Slf4j
public class JdkFutureTest {

    @Test
    public void testFuture() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        log.info("main thread submit runnable task.");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                log.info("async thread execute task");
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        log.info("main task go on");
    }
}
