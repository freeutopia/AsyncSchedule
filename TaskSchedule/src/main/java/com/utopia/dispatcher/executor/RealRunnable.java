package com.utopia.dispatcher.executor;

import android.os.Process;

import com.utopia.dispatcher.TaskDispatcher;
import com.utopia.dispatcher.task.Task;
import com.utopia.dispatcher.task.TaskStatus;

/**
 * 任务真正执行的地方
 */
public class RealRunnable implements Runnable {
    private final Task mTask;
    private TaskDispatcher mTaskDispatcher;

    public RealRunnable(Task task) {
        this.mTask = task;
    }

    public RealRunnable(Task task, TaskDispatcher dispatcher) {
        this.mTask = task;
        this.mTaskDispatcher = dispatcher;
    }

    @Override
    public void run() {
        Platform.get().setThreadPriority(mTask.priority());//更新线程优先级

        //此处用来处理任务间的依赖关系
        mTask.updateStatus(TaskStatus.WAITING);//更新状态机-》等待状态
        mTask.waitToSatisfy();//当前Task等待，依赖的Task先执行

        // 执行Task
        mTask.updateStatus(TaskStatus.RUNNING);//更新状态机-》运行状态
        mTask.run();//执行task任务
        mTask.updateStatus(TaskStatus.FINISHED);

        if (mTaskDispatcher != null) {
            mTaskDispatcher.finish(mTask);
        }
    }
}
