package com.baidu.drapi.pool;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.baidu.drapi.pool.counter.JobThreadCounter;
import com.baidu.drapi.pool.mode.ThreadMode;
import com.baidu.drapi.pool.mode.ThreadWeightMode;
import com.baidu.drapi.util.NamedFutureTask;
import com.baidu.drapi.util.NamedThreadFactory;

/**
 * @title JobDispatcher
 * @description 任务分发类，支持根据线程权重模型来分配资源
 * @author weijiuyin
 * @date 2014-11-26
 * @version 1.0
 */
public class JobDispatcher extends Thread {
    private static JobDispatcher instance = new JobDispatcher();

    private JobThreadPoolExecutor threadPool;
    private BlockingQueue<NamedFutureTask<?>> jobQueue;
    // 任务计数
    private JobThreadCounter threadCounter = null;

    // 用于记录在队列中的不同资源类型数量
    private Map<String, AtomicInteger> queueCounterPool = new ConcurrentHashMap<String, AtomicInteger>();
    private AtomicInteger totalCounter = new AtomicInteger(0);
    private int maximumPoolSize = 3000;
    private int maximumQueueSize = 5000;
    private boolean isRunning = true;

    private JobDispatcher() {
        this.init();
        this.start();
    }

    public static JobDispatcher getInstance() {
        return instance;
    }

    private void init() {
        if (threadPool == null) {
            final String name = "jobDispatcher_worker";
            LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>(maximumQueueSize);
            threadPool =
                    new JobThreadPoolExecutor(maximumPoolSize, 0L, TimeUnit.SECONDS, blockingQueue,
                            new NamedThreadFactory(name), this);
        }
        jobQueue = new LinkedBlockingQueue<NamedFutureTask<?>>(maximumQueueSize);
        threadCounter = new JobThreadCounter(maximumPoolSize);
    }

    public void stopDispatcher() {
        if (threadPool != null)
            threadPool.shutdownNow();

        if (jobQueue != null)
            jobQueue.clear();

        if (threadCounter.getCounterPool() != null)
            threadCounter.getCounterPool().clear();

        if (queueCounterPool != null)
            queueCounterPool.clear();

        isRunning = false;
        this.interrupt();
    }

    /**
     * 运行期修改模型
     * 
     * @param weightModel
     */
    public void addWeightMode(String key, int maxLoad, ThreadWeightMode weight) {
        if (key != null && !key.equals("")) {
            if (queueCounterPool.get(key) == null) {
                ThreadMode mode = weight.getMode(key, maxLoad);
                threadCounter.addMode(key, mode);
                queueCounterPool.put(key, new AtomicInteger(0));
                int shareNum = 0;
                for (Entry<String, ThreadMode> entry : threadCounter.getCounterPool().entrySet()) {
                    ThreadMode value = entry.getValue();
                    if (value.getModelType() == ThreadWeightMode.LEAVE) {
                        shareNum += value.getMaxCount();
                    }
                }
                threadCounter.setShareNum(this.maximumPoolSize - shareNum);
            }
        }
    }

    /**
     * 提交任务
     */
    public <T> void submitJob(NamedFutureTask<T> task) {
        // 第一层做总量判断，同时锁定总资源
        if (this.totalCounter.incrementAndGet() > this.maximumPoolSize) {
            this.totalCounter.decrementAndGet();
            pushJob(task);
            return;
        }

        AtomicInteger totalCounter = threadCounter.getTotalCounter();
        final int shareNum = threadCounter.getShareNum();

        ThreadMode mode = threadCounter.getModel(task.getKey());
        boolean hasResource = mode.hasResource(shareNum, totalCounter);
        if (hasResource) {
            threadPool.execute(task);
        } else {
            this.totalCounter.decrementAndGet();
            pushJob(task);
        }
    }

    public <T> void releaseJob(final String key) {
        this.totalCounter.decrementAndGet();

        AtomicInteger totalCounter = threadCounter.getTotalCounter();
        if (threadCounter.getCounterPool().size() == 0) {
            totalCounter.decrementAndGet();
        } else {
            ThreadMode mode = threadCounter.getModel(key);
            mode.resetCounter(totalCounter);
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                NamedFutureTask<?> job = jobQueue.poll(1, TimeUnit.SECONDS);
                if (job != null) {
                    popJob(job);
                    submitJob(job);
                } else {
                    Thread.sleep(100L);
                }
            } catch (InterruptedException e) {
                //
            }
        }
    }

    public <T> void pushJob(NamedFutureTask<T> job) {
        if (!jobQueue.offer(job)) {// 补偿job
            throw new RuntimeException("can't submit job, queue full...");
        } else {
            queueCounterPool.get(job.getKey()).incrementAndGet();
        }
    }

    public <T> void popJob(NamedFutureTask<T> job) {
        if (queueCounterPool.get(job.getKey()) != null) {
            queueCounterPool.get(job.getKey()).decrementAndGet();
        }
    }

    public Map<String, Object> getCurrentThreadStatus() {
        Map<String, Object> status = new HashMap<String, Object>(4);
        status.put("jobCounter", threadCounter.getTotalCounter());
        status.put("shareNum", threadCounter.getShareNum());
        status.put("resource", threadCounter.getCounterPool());
        status.put("queuePool", queueCounterPool);
        return status;
    }

}
