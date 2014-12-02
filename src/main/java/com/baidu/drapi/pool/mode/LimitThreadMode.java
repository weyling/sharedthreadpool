package com.baidu.drapi.pool.mode;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @title LimitThreadMode
 * @description 限制模式,只能使用自身线程资源
 * @author weijiuyin
 * @date 2014-11-26
 * @version 1.0
 */
public class LimitThreadMode extends ThreadMode {

    public LimitThreadMode(String key, int maxCount) {
        super(key, maxCount);
    }

    @Override
    public ThreadWeightMode getModelType() {
        return ThreadWeightMode.LIMIT;
    }

    public boolean hasResource(final int shareNum, AtomicInteger totalCounter) {
        final int threshold = this.getMaxCount();
        AtomicInteger count = this.getCount();

        boolean hasResource = false;
        if (count.incrementAndGet() > threshold) {
            count.decrementAndGet();
        } else {
            if (totalCounter.incrementAndGet() > shareNum) {
                totalCounter.decrementAndGet();
                count.decrementAndGet();
            } else {
                hasResource = true;
            }
        }
        return hasResource;
    }

    public void resetCounter(AtomicInteger totalCounter) {
        AtomicInteger count = this.getCount();
        count.decrementAndGet();
        totalCounter.decrementAndGet();
    }

    @Override
    public String toString() {
        return "[type=" + this.getModelType() + ", key=" + this.getKey() + ", maxCount=" + this.getMaxCount()
                + ", count=" + this.getCount() + "]";
    }
}
