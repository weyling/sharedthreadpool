package com.baidu.drapi.pool.mode;

/**
 * @title ThreadWeightMode
 * @description 当前支持两类 1. 根据某一类key可以预留多少资源独享。 2. 根据某一类key可以限制最多使用多少资源。
 * @author weijiuyin
 * @date 2014-11-26
 * @version 1.0
 */
public enum ThreadWeightMode {

    LEAVE {
        @Override
        public ThreadMode getMode(String key, int maxLoad) {
            if (maxLoad > 300) {
                maxLoad = 300;
            }
            if (maxLoad < 20) {
                maxLoad = 20;
            }
            return new LeaveThreadMode(key, maxLoad);
        }
    },
    LIMIT {
        @Override
        public ThreadMode getMode(String key, int maxLoad) {
            if (maxLoad > 500) {
                maxLoad = 500;
            }
            if (maxLoad < 50) {
                maxLoad = 50;
            }
            return new LimitThreadMode(key, maxLoad);
        }
    };
    
    public abstract ThreadMode getMode(String key, int maxLoad);

    public static ThreadWeightMode getMode(String mode) {
        if (mode == null) {
            return LIMIT;
        } else if ("leave".equalsIgnoreCase(mode)) {
            return LEAVE;
        } else if ("limit".equalsIgnoreCase(mode)) {
            return LIMIT;
        }
        return LIMIT;
    }

}
