package com.utopia.dispatcher;

import android.os.Looper;
import android.os.MessageQueue;

import com.utopia.dispatcher.executor.RealRunnable;
import com.utopia.dispatcher.task.Task;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 延迟初始化分发器
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
    public boolean remove(Task task) {
        return mDelayTasks.remove(task);
    }

    @Override
    public void start(){
        Looper.myQueue().addIdleHandler(mIdleHandler);
    }

    @Override
    public void interrupt() {
        mDelayTasks.clear();
    }

    @Override
    public void await() {
        //...空闲时任务，不支持阻塞
    }

}
