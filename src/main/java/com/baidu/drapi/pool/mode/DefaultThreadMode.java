package com.baidu.drapi.pool.mode;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadMode extends ThreadMode {

    public DefaultThreadMode(String key, int maxCount) {
        super(key, maxCount);
    }

    @Override
    public ThreadWeightMode getModelType() {
        return null;
    }

    public boolean hasResource(final int shareNum, AtomicInteger totalCounter) {
        boolean hasResource = false;
        if (totalCounter.incrementAndGet() > shareNum) {
            totalCounter.decrementAndGet();
        } else {
            hasResource = true;
        }
        return hasResource;
    }

    public void resetCounter(AtomicInteger totalCounter) {
        totalCounter.decrementAndGet();
    }

}
