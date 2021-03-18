package com.utopia.dispatcher;


import com.utopia.dispatcher.task.Task;

public interface Dispatcher {
    /**
     * 添加任务
     */
    Dispatcher add(Task task);

    /**
     * 结束任务
     */
    void finish(Task task);

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
