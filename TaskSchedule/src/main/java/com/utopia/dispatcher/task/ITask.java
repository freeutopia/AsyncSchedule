package com.utopia.dispatcher.task;

import android.os.Process;

import com.utopia.dispatcher.Dispatcher;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import androidx.annotation.IntRange;

public interface ITask {
    /**
     * 优先级的范围，可根据Task重要程度及工作量指定；之后根据实际情况决定是否有必要放更大
     */
    @IntRange(from = Process.THREAD_PRIORITY_FOREGROUND, to = Process.THREAD_PRIORITY_LOWEST)
    int priority();

    /**
     * 真正任务入口，子类需要执行的任务入口
     */
    void run();

    /**
     * 提交任务到线程池
     */
    Future<?> submitOn(Dispatcher dispatcher);

    /**
     * 在哪个线程中执行，默认是io线程
     */
    @ScheduleOn int scheduleOn();

    /**
     * 异步线程执行的Task是否需要在被调用await的时候等待，默认不需要
     */
    boolean needWait();

}
