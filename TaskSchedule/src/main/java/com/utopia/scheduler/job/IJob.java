package com.utopia.scheduler.job;

import android.os.Process;

import com.utopia.scheduler.IScheduler;

import java.util.Set;
import java.util.concurrent.Future;

import androidx.annotation.IntRange;

public interface IJob {
    /**
     * 优先级的范围，可根据Task重要程度及工作量指定；之后根据实际情况决定是否有必要放更大
     */
    @IntRange(from = Process.THREAD_PRIORITY_FOREGROUND, to = Process.THREAD_PRIORITY_LOWEST)
    default int priority() {
        return Process.THREAD_PRIORITY_BACKGROUND;
    }

    /**
     * 真正任务入口，子类需要执行的任务入口
     */
    void run();

    /**
     * 提交任务到线程池
     */
    Future<?> submitOn(IScheduler scheduler);

    /**
     * 任务默认在非UI线程中执行
     */
    default  @ScheduleOn int scheduleOn() {
        return ScheduleOn.IO;
    }

    /**
     * 异步线程执行的Task是否需要在被调用await的时候等待，默认不需要
     */
    default boolean needWait() {
        return false;
    }

    /**
     * 依赖的任务执行完一个
     */
    void satisfy();

    /**
     * 当前任务等待，让依赖的任务先执行
     */
    void waitToSatisfy();

    /**
     * 当前Task依赖的Task集合（需要等待被依赖的Task执行完毕才能执行自己），默认没有依赖
     */
    Set<IJob> getDependTasks();

    /**
     * 是否需要尽快执行，解决特殊场景的问题：一个Task耗时非常多但是优先级却一般，很有可能开始的时间较晚，
     * 导致最后只是在等它，这种可以早开始。
     */
    default boolean needRunAsSoon() {
        return false;
    }
}
