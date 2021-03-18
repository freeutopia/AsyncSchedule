package com.utopia.dispatcher.task;

import android.os.Process;

import com.utopia.dispatcher.Dispatcher;
import com.utopia.dispatcher.executor.ThreadPool;
import com.utopia.dispatcher.executor.Platform;
import com.utopia.dispatcher.executor.RealRunnable;
import com.utopia.dispatcher.utils.ArraysUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public abstract class Task implements ITask {
    private final Set<Task> dependsTasks = new HashSet<>();

    // 当前Task依赖的Task数量（需要等待被依赖的Task执行完毕才能执行自己），默认没有依赖
    private CountDownLatch mDepends = null;

    /**
     * 当前Task等待，让依赖的Task先执行
     */
    public final void waitToSatisfy() {
        if (mDepends != null) {
            try {
                mDepends.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 依赖的Task执行完一个
     */
    public final void satisfy() {
        if (mDepends != null) {
            mDepends.countDown();
        }
    }

    /**
     * 是否需要尽快执行，解决特殊场景的问题：一个Task耗时非常多但是优先级却一般，很有可能开始的时间较晚，
     * 导致最后只是在等它，这种可以早开始。
     */
    public boolean needRunAsSoon() {
        return false;
    }

    /**
     * Task的优先级，运行在主线程则不要去改优先级
     */
    @Override
    public int priority() {
        return Process.THREAD_PRIORITY_BACKGROUND;
    }

    /**
     * 任务默认在非UI线程中执行
     */
    @Override
    public @ScheduleOn int scheduleOn() {
        return ScheduleOn.IO;
    }

    /**
     * Task执行在哪个线程池，默认在IO的线程池；
     * CPU 密集型的一定要切换到DispatcherExecutor.getCPUExecutor();
     */
    @Override
    public final Future<?> submitOn(Dispatcher dispatcher) {
        Runnable runnable = new RealRunnable(this, dispatcher);
        switch (scheduleOn()){
            case ScheduleOn.IO:
                ThreadPool.getIOExecutor().submit(runnable);
                break;
            case ScheduleOn.CPU:
                ThreadPool.getCPUExecutor().submit(runnable);
                break;
            case ScheduleOn.UI:
                //执行当前线程任务(平台相关：如果是Android则切换到UI线程执行),先加入线程队列，最后运行
                Platform.get().addTaskToMainThread(runnable);
                break;
        }
        return null;
    }

    /**
     * 异步线程执行的Task是否需要在被调用await的时候等待，默认不需要
     */
    @Override
    public boolean needWait() {
        return false;
    }

    /**
     * 添加依赖关系
     */
    public final void addDepends(Task... tasks){
        if (!ArraysUtils.isEmpty(tasks)){
            for (Task task : tasks){
                if (task != this) {//禁止添加循环依赖
                    dependsTasks.add(task);
                }
            }
            mDepends = new CountDownLatch(dependsTasks.size());
        }

    }

    /**
     * 当前Task依赖的Task集合（需要等待被依赖的Task执行完毕才能执行自己），默认没有依赖
     */
    public final Set<Task> getDependTasks() {
        return dependsTasks;
    }
}
