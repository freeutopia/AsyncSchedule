package com.utopia.dispatcher.executor;

import com.utopia.dispatcher.TaskDispatcher;
import com.utopia.dispatcher.task.Task;

import androidx.annotation.NonNull;

/**
 * 任务真正执行的地方
 */
public class RealRunnable implements Runnable {
    private final Task mTask;
    private final TaskDispatcher mTaskDispatcher;

    public RealRunnable(@NonNull Task task) {
        this(task,null);
    }

    public RealRunnable(@NonNull Task task, TaskDispatcher dispatcher) {
        this.mTask = task;
        this.mTaskDispatcher = dispatcher;
    }

    @Override
    public void run() {
        Platform.get().setThreadPriority(mTask.priority());//更新线程优先级

        //此处用来处理任务间的依赖关系
        mTask.waitToSatisfy();//当前Task等待，依赖的Task先执行

        mTask.run();//执行task任务

        if (mTaskDispatcher != null) {
            //通知分发器，执行后续任务
            mTaskDispatcher.finish(mTask);
        }
    }
}
