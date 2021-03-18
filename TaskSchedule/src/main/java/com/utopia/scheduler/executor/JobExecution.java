package com.utopia.scheduler.executor;

import com.utopia.scheduler.IScheduler;
import com.utopia.scheduler.job.IJob;

import androidx.annotation.NonNull;

/**
 * 任务真正执行的地方
 */
public class JobExecution implements Runnable {
    private final IJob mTask;
    private final IScheduler mTaskIScheduler;

    public JobExecution(@NonNull IJob task) {
        this(task,null);
    }

    public JobExecution(@NonNull IJob task, IScheduler IScheduler) {
        this.mTask = task;
        this.mTaskIScheduler = IScheduler;
    }

    @Override
    public void run() {
        Platform.get().setThreadPriority(mTask.priority());//更新线程优先级

        //此处用来处理任务间的依赖关系
        mTask.waitToSatisfy();//当前Task等待，依赖的Task先执行

        mTask.run();//执行task任务

        if (mTaskIScheduler != null) {
            //通知分发器，执行后续任务
            mTaskIScheduler.finish(mTask);
        }
    }
}
