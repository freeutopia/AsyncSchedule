package com.utopia.scheduler.job;

import com.utopia.scheduler.IScheduler;
import com.utopia.scheduler.executor.ExecutorFactory;
import com.utopia.scheduler.executor.Platform;
import com.utopia.scheduler.executor.JobExecution;
import com.utopia.scheduler.utils.ArraysUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public abstract class Job implements IJob {
    private final Set<IJob> mDepends = new HashSet<>();

    // 当前Task依赖的Task数量（需要等待被依赖的Task执行完毕才能执行自己），默认没有依赖
    private CountDownLatch mCountDownLatch = null;

    @Override
    public final void waitToSatisfy() {
        if (mCountDownLatch != null) {
            try {
                mCountDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public final void satisfy() {
        if (mCountDownLatch != null) {
            mCountDownLatch.countDown();
        }
    }

    /**
     * 执行线程池，默认在IO的线程池；
     */
    @Override
    public final Future<?> submitOn(IScheduler scheduler) {
        Runnable runnable = new JobExecution(this, scheduler);
        switch (scheduleOn()){
            case ScheduleOn.IO:
                ExecutorFactory.getIOExecutor().submit(runnable);
                break;
            case ScheduleOn.CPU:
                ExecutorFactory.getCPUExecutor().submit(runnable);
                break;
            case ScheduleOn.UI:
                //执行当前线程任务(平台相关：如果是Android则切换到UI线程执行),先加入线程队列，最后运行
                Platform.get().addToMainThread(runnable);
                break;
        }
        return null;
    }


    /**
     * 添加依赖关系
     */
    public final void addDepends(Job... tasks){
        if (!ArraysUtils.isEmpty(tasks)){
            for (Job task : tasks){
                if (task != this) {//禁止添加循环依赖
                    mDepends.add(task);
                }
            }
            mCountDownLatch = new CountDownLatch(mDepends.size());
        }

    }

    @Override
    public final Set<IJob> getDependTasks() {
        return mDepends;
    }
}
