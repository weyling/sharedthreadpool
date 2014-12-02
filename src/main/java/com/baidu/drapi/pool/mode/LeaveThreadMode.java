package com.baidu.drapi.pool.mode;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @title LeaveThreadMode
 * @description 独占模式,可以使用线程池未分配完的资源
 * @author weijiuyin
 * @date 2014-11-26
 * @version 1.0
 */
public class LeaveThreadMode extends ThreadMode {
    private boolean privateUseFirst = false;

    public LeaveThreadMode(String key, int maxCount) {
        super(key, maxCount);
    }

    @Override
    public ThreadWeightMode getModelType() {
        return ThreadWeightMode.LEAVE;
    }

    public boolean hasResource(final int shareNum, AtomicInteger totalCounter) {
        Integer threshold = this.getMaxCount();
        AtomicInteger count = this.getCount();

        boolean hasResource = false;
        if (privateUseFirst) {
            // 优先使用私有资源
            if (count.incrementAndGet() > threshold) {
                count.decrementAndGet();
                if (totalCounter.incrementAndGet() > shareNum) {
                    totalCounter.decrementAndGet();
                } else {
                    hasResource = true;
                }
            } else {
                hasResource = true;
            }
        } else {
            if (totalCounter.incrementAndGet() > shareNum) {
                totalCounter.decrementAndGet();
                if (count.incrementAndGet() > threshold) {
                    count.decrementAndGet();
                } else {
                    hasResource = true;
                }
            } else {
                hasResource = true;
            }
        }
        return hasResource;
    }

    public void resetCounter(AtomicInteger totalCounter) {
        AtomicInteger count = this.getCount();
        if (privateUseFirst) {
            if (totalCounter.decrementAndGet() < 0) {
                totalCounter.incrementAndGet();
                count.decrementAndGet();
            }
        } else {
            if (count.decrementAndGet() < 0) {
                count.incrementAndGet();
                totalCounter.decrementAndGet();
            }
        }
    }

    @Override
    public String toString() {
        return "[type=" + this.getModelType() + ", key=" + this.getKey() + ", maxCount=" + this.getMaxCount()
                + ", count=" + this.getCount() + "]";
    }
}
