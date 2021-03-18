package com.utopia.dispatcher;

import android.os.Looper;
import android.os.MessageQueue;

import com.utopia.dispatcher.executor.RealRunnable;
import com.utopia.dispatcher.task.Task;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 空闲时任务分发器
 */
public final class DelayDispatcher implements Dispatcher{

    private final Queue<Task> mDelayTasks = new LinkedList<>();

    private final MessageQueue.IdleHandler mIdleHandler = () -> {
        if(mDelayTasks.size()>0){
            Task task = mDelayTasks.poll();
            new RealRunnable(task).run();
        }
        return !mDelayTasks.isEmpty();
    };

    @Override
    public Dispatcher add(Task task){
        mDelayTasks.add(task);
        return this;
    }

    @Override
    public void finish(Task task) {
        mDelayTasks.remove(task);
    }

    @Override
    public void start(){
        Looper.myQueue().addIdleHandler(mIdleHandler);
    }

    @Override
    public void interruptAll() {
        mDelayTasks.clear();
    }
}
