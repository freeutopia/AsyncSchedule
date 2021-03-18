package com.utopia.scheduler;


import com.utopia.scheduler.job.IJob;

public interface IScheduler {
    /**
     * 添加任务
     */
    IScheduler add(IJob job);

    /**
     * 结束任务
     */
    void finish(IJob job);

    /**
     * 执行任务
     */
    void start();

    /**
     * 中断任务
     */
    void interruptAll();

    /**
     * 等待任务（阻塞方法，慎用）
     */
    default void await() throws InterruptedException {}
}
