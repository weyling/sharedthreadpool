package com.baidu.cprd.api.threadpool;

import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.baidu.drapi.pool.JobDispatcher;
import com.baidu.drapi.pool.mode.ThreadWeightMode;
import com.baidu.drapi.util.NamedFutureTask;

public class JobMeterTest extends AbstractJavaSamplerClient {
    private JobDispatcher jobDispatcher = null;

    public void setupTest(JavaSamplerContext arg0) {
        jobDispatcher = JobDispatcher.getInstance();
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
        jobDispatcher.stopDispatcher();
    }

    @Override
    public SampleResult runTest(JavaSamplerContext arg) {
        SampleResult sr = new SampleResult();

        String line = arg.getParameter("line", "01");
        int time = arg.getIntParameter("time");
        int num = arg.getIntParameter("num");
        String mode = arg.getParameter("mode", "limit");

        sr.sampleStart();

        final MockJob job = new MockJob(time);
        NamedFutureTask<String> futureTask = new NamedFutureTask<String>(job, line);
        jobDispatcher.addWeightMode(line, num, ThreadWeightMode.getMode(mode));
        jobDispatcher.submitJob(futureTask);
        try {
            futureTask.get();
            Calendar cld = Calendar.getInstance();
            int min = cld.get(Calendar.MINUTE);
            int sec = cld.get(Calendar.SECOND);
            if (min % 2 == 0 && sec < 2)
                System.out.println(jobDispatcher.getCurrentThreadStatus());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        sr.setSuccessful(true);
        sr.sampleEnd();
        return sr;
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        params.addArgument("line", "01");
        params.addArgument("time", "200");
        params.addArgument("num", "100");
        params.addArgument("mode", "limit");
        return params;
    }

    public static void main(String[] args) {
        Calendar cld = Calendar.getInstance();
        int min = cld.get(Calendar.MINUTE);
        int sec = cld.get(Calendar.SECOND);
        if (min % 3 == 0 && sec < 2)
            System.out.println("---");
    }

    class MockJob implements Callable<String> {

        private final String RESULT = "mock result";

        private final int taskIndex;

        public MockJob() {
            this.taskIndex = 200;
        }

        public MockJob(int taskIndex) {
            this.taskIndex = taskIndex;
        }

        @Override
        public String call() throws Exception {
            Thread.sleep(taskIndex);
            return RESULT + "-new-" + taskIndex;
        }
    }
}
