package org.test;

import java.util.concurrent.*;

public class Concurrency {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        Callable<Integer> task1 = () -> {
            try {
                System.out.println("task1 started...");
                TimeUnit.SECONDS.sleep(3);
                System.out.println("task1 ended");
                latch.countDown();
                return 1;
            }
            catch (Exception e) {
                System.out.println(e.getStackTrace());
                return 0;
//                throw new IllegalStateException("task interrupted", e);
            }
        };
        Callable<Integer> task2 = () -> {
            try {
                System.out.println("task2 started...");
                TimeUnit.SECONDS.sleep(7);
                System.out.println("task2 ended");
                latch.countDown();
                return 2;
            }
            catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };

         executor.submit(task1);
         executor.submit(task2);



        executor.shutdown();
        if (executor.isTerminated()) System.out.println("TERMINATED");
        System.out.println("executor.shutdown");

        latch.await();
        System.out.println("END");

    }



}
