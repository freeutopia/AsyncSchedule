package com.utopia.scheduler;

import com.utopia.scheduler.job.Job;

import java.util.concurrent.TimeUnit;


public class Job1 extends Job {
    @Override
    public void run() {
        try {
            System.out.println("进入了Task1");
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            System.out.println("执行完了Task1");
        }
    }

    @Override
    public boolean needWait() {
        return true;
    }
}
