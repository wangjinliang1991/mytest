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
        log.info("==================");
        log.info("main thread submit callable task, first style");
        // submit callable task, you can get the result of async task
        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                log.info("thread pool async task is executing");
                TimeUnit.SECONDS.sleep(3);
                log.info("async task executed, return result");
                return "callable async task result";
            }
        });
        // block to wait for getting result of async thread
        try {
            log.info("main thread waiting for result of async task");
            String result = future.get();
            log.info("async task result is: {}", result);
        }  catch (ExecutionException e) {
            log.error("block to wait for result of async task error,{}",e.getMessage());
        }

        log.info("main thread submit callable task, second style");
        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                log.info("thread pool's async task is running");
                TimeUnit.SECONDS.sleep(3);
                log.info("async task executed, return result");
                return "callable async task second result";
            }
        });
        executorService.submit(task);
        try {
            log.info("main thread is waiting for callable task return result");
            String result = task.get();
            log.info("sub thread task result is: {}", result);
        } catch (ExecutionException e) {
           log.error("block to wait for async task result error,{}",e.getMessage());
        }
    }
}
