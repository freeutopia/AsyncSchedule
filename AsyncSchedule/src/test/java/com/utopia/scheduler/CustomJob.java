package com.utopia.scheduler;

import com.utopia.scheduler.job.Job;

import java.util.concurrent.TimeUnit;


public class CustomJob extends Job {
    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
