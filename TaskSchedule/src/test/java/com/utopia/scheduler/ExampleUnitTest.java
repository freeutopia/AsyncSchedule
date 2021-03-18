package com.utopia.scheduler;

import com.utopia.scheduler.job.Job;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void taskDispatchTest() {
        Scheduler dispatcher = new Scheduler();
        Job1 task1 = new Job1();
        Job2 task2 = new Job2();
        task1.addDepends(task1,task2);

        dispatcher.add(task1).add(task2).add(new Job() {
            @Override
            public void run() {
                try {
                    System.out.println("进入了Task");
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    System.out.println("执行完了Task");
                }
            }
        });

        dispatcher.start();


        try {
            dispatcher.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}