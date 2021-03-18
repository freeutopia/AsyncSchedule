package com.utopia.dispatcher;

import android.util.Log;

import com.utopia.dispatcher.task.Task;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void taskDispatchTest() {
        TaskDispatcher dispatcher = new TaskDispatcher();
        Task1 task1 = new Task1();
        Task2 task2 = new Task2();
        task1.addDepends(task1,task2);

        dispatcher.add(task1).add(task2).add(new Task() {
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