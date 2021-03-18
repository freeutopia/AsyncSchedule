package com.utopia.dispatcher;

import com.utopia.dispatcher.task.Task;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Task1 extends Task {
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
