package com.baidu.drapi.pool.mode;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @title ThreadMode
 * @description 当前支持两类 1. 根据某一类key可以预留多少资源独享。 2. 根据某一类key可以限制最多使用多少资源。
 * @author weijiuyin
 * @date 2014-11-26
 * @version 1.0
 */
public abstract class ThreadMode {
    private String key;
    private int maxCount;
    private AtomicInteger count = new AtomicInteger(0);

    public ThreadMode(String key, int maxCount) {
        this.key = key;
        this.maxCount = maxCount;
    }

    public String getKey() {
        return key;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public AtomicInteger getCount() {
        return count;
    }

    public void setCount(AtomicInteger count) {
        this.count = count;
    }

    /**
     * 
     * @return 线程模式
     */
    public abstract ThreadWeightMode getModelType();

    /**
     * 是否有可用资源
     * 
     * @param shareNum 线程池共享资源数量
     * @param totalCounter 资源计数器
     * @return boolean
     */
    public abstract boolean hasResource(final int shareNum, AtomicInteger totalCounter);

    /**
     * 执行任务后重置计数器
     * 
     * @param totalCounter
     */
    public abstract void resetCounter(AtomicInteger totalCounter);

}
