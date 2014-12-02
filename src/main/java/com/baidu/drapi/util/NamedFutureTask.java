package com.baidu.drapi.util;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @title NamedFutureTask
 * @description 任务定义，需要支持能够产生key。
 * @author weijiuyin
 * @date 2014-11-26
 * @version 1.0
 */
public class NamedFutureTask<T> extends FutureTask<T> {
    private String key;

    public NamedFutureTask(Callable<T> callable, String key) {
        super(callable);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
