package com.baidu.drapi.pool.counter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.baidu.drapi.pool.mode.DefaultThreadMode;
import com.baidu.drapi.pool.mode.ThreadMode;

public class JobThreadCounter {
    // 线程池初始化后可用的资源数
    private int shareNum;
    // 线程池可用线程计数
    private AtomicInteger totalCounter = new AtomicInteger(0);
    // 各个任务资源使用计数
    private Map<String, ThreadMode> counterPool = new ConcurrentHashMap<String, ThreadMode>();

    public JobThreadCounter(int shareNum) {
        this.shareNum = shareNum;
    }

    public int getShareNum() {
        return shareNum;
    }

    public void setShareNum(int shareNum) {
        this.shareNum = shareNum;
    }

    public AtomicInteger getTotalCounter() {
        return totalCounter;
    }

    public Map<String, ThreadMode> getCounterPool() {
        return counterPool;
    }

    public ThreadMode getModel(String key) {
        if (key == null || "".equals(key)) {
            return new DefaultThreadMode(null, 0);
        }
        return this.counterPool.get(key);
    }

    public void addMode(String key, ThreadMode model) {
        counterPool.put(key, model);
    }
}
