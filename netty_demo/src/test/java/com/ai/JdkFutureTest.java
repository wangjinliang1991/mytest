package com.ai;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.sql.Time;
import java.time.LocalDateTime;
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

    /**
     *  callable+Future, not real async, get result via get()
     *
     *  jdk8 CompletableFuture provide strong Future function
     */
    @Test
    public void testCompletableFuture() throws InterruptedException {
        // async non-blocking no result task, default use ForkJoinPool.commonPool() as thread pool
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                log.info("start to execute async task");
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("async task finished.");
            }
        });
        log.info("main thread...");
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void testCompletableSupplyAsync() throws Exception {
        // async non-blocking with return result
        Executor executor = Executors.newFixedThreadPool(10);
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            log.info("start async task");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("finished async task");
            return "CompletableFuture returned result.";
        },executor);
        // set callback if executing successfully
        cf.thenAccept(result -> {
            log.info("async notify result is: {}", result);
        }).exceptionally(e -> {
            log.info("async task executed error, {}", e.getMessage());
            return null;
        });
        log.info("main thread needs not to wait, continue to do other things");
        TimeUnit.SECONDS.sleep(10);
    }

    /**
     *  CompletableFuture name rule:
     *  1. xxx() execute in original thread
     *  2. xxxAsync() execute in another thread pool
     *
     *  multi CompletableFuture can serializable or concurrent execute
     */
    @Test
    public void testSerialize() throws Exception {
        // multi serialize
        //first task
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            log.info("first async task executes, time={}", LocalDateTime.now().toString());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("first async task has finished, time={}", LocalDateTime.now().toString());
            return "hello";
        });
        // second task
        CompletableFuture<String> cf2 = cf.thenApply(((pre) -> {
            log.info("second async task executes, receives result of first async task is: {}, time={}", pre, LocalDateTime.now().toString());
            return pre + " word";
        }));

        // set callback
        cf2.thenAccept(result -> {
            log.info("two async task sum result is: {}", result);
        }).exceptionally(e -> {
            log.info("async task error, msg is: {}", e.getMessage());
            return null;
        });

        log.info("main thread needs not to wait, continue to deal with other tasks");
        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void testAnyOf() throws Exception {
        //test multi completableFuture concurrent execute, continue if any one return
        //first task
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            log.info("first async task starts to execute, time is {}", LocalDateTime.now());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("first task has finished, time is {}", LocalDateTime.now());
            return "hello";
        });
        //second
        CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> {
            log.info("second async task starts to execute, time is {}", LocalDateTime.now());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("second task has finished, time is {}", LocalDateTime.now());
            return " world";
        });
        // combine two completableFuture to one new CompletableFuture
        CompletableFuture<Object> cf3 = CompletableFuture.anyOf(cf1, cf2);
        // set callback
        cf3.thenAccept(result -> {
            log.info("two async task result is {}", result);
        }).exceptionally(e -> {
            log.info("async task error, msg is {}", e.getMessage());
            return null;
        });

        log.info("main thread needs not to wait, continue to deal with other tasks");
        TimeUnit.SECONDS.sleep(10);
    }
}
