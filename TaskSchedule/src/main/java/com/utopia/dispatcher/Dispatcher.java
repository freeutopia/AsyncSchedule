package com.utopia.dispatcher;


import com.utopia.dispatcher.task.Task;

public interface Dispatcher {
    /**
     * 添加任务
     */
    Dispatcher add(Task task);

    /**
     * 移除任务
     */
    boolean remove(Task task);

    /**
     * 执行任务
     */
    void start();

    /**
     * 中断任务
     */
    void interrupt();

    /**
     * 等待任务（阻塞方法，慎用）
     */
    void await();
}
