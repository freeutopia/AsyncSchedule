package com.utopia.dispatcher;

import com.utopia.dispatcher.task.Task;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Task2 extends Task {
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
