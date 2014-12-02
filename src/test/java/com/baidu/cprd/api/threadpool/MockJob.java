package com.baidu.cprd.api.threadpool;

import java.util.concurrent.Callable;

/**
 * 
 */
public class MockJob implements Callable<String> {

    @Override
    public String call()  {
        try {
            Thread.sleep(200);
//            System.out.println(getKey() + " done..");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "result";
    }

}
