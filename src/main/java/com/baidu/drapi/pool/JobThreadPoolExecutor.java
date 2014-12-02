package com.baidu.drapi.pool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baidu.drapi.util.NamedFutureTask;

/**
 * @title JobThreadPoolExecutor
 * @description 扩展线程池基本实现， 在每个任务结束后需要减去任务计数器的值
 * @author weijiuyin
 * @date 2014-11-26
 * @version 1.0
 */
public class JobThreadPoolExecutor extends ThreadPoolExecutor {

    private JobDispatcher jobDispatcher;

    public JobThreadPoolExecutor(int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, JobDispatcher jobDispatcher) {
        super(maximumPoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.jobDispatcher = jobDispatcher;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (r instanceof NamedFutureTask) {
            NamedFutureTask<?> task = (NamedFutureTask<?>) r;
            jobDispatcher.releaseJob(task.getKey());
        }
    }

}
