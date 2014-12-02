package com.baidu.cprd.api.threadpool;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import com.baidu.drapi.pool.JobDispatcher;
import com.baidu.drapi.pool.mode.ThreadWeightMode;
import com.baidu.drapi.util.NamedFutureTask;

public class JobDispatcherTest {

    private JobDispatcher jobDispatcher = null;

    @Before
    public void setUp() throws Exception {
        jobDispatcher = JobDispatcher.getInstance();
    }

    @Test
    public void dotest() throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(100);
        final MockJob tag0Job = new MockJob();
        for (int i = 0; i < 3; i++) {
            es.submit(new Runnable() {
                public void run() {
                    try {
                        NamedFutureTask<String> futureTask = new NamedFutureTask<String>(tag0Job, "0");
                        jobDispatcher.addWeightMode("0", 1, ThreadWeightMode.LIMIT);
                        jobDispatcher.submitJob(futureTask);
                        System.out.println("----" + futureTask.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        Thread.sleep(3000000L);
    }
}
