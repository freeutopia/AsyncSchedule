package com.utopia.scheduler;

import com.utopia.scheduler.job.Job;

import java.util.concurrent.TimeUnit;


public class Job2 extends Job {
    @Override
    public void run() {
        try {
            System.out.println("进入了Task2");
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            System.out.println("执行完了Task2");
        }
    }

    @Override
    public boolean needWait() {
        return true;
    }

}
