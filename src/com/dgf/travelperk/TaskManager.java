package com.dgf.travelperk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class TaskManager {

    private static final ExecutorService executor=Executors.newWorkStealingPool();
    private static final ForkJoinPool forkJoinPool=(ForkJoinPool) executor;
    public static final int ONE_SECOND = 1000;
    public static final int ONE_MILLION = 1000000;
    public static final int _5 = 5;

    private TaskManager() {
    }

    static void prioritizeTasks() {
        if (forkJoinPool.getQueuedSubmissionCount()> ONE_MILLION) {
            //System.out.print("\rSleep main thread. Drop1="+drop1+" drop2="+drop2);
            try {
                //System.out.print("\rMain thread sleep for "+a.length+" -> executor "+ forkJoinPool +"   ");
                Thread.sleep(ONE_SECOND);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void waitForPendentTasks() {
        while (forkJoinPool.getQueuedSubmissionCount()>0) {
            try {
                Thread.sleep(ONE_SECOND);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void shutdownExecutor() {
        executor.shutdown();
        try {
            executor.awaitTermination(_5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void execute(Runnable worker) {
        executor.execute(worker);
    }
}
