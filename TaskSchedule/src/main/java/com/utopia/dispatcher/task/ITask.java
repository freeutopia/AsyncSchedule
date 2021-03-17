package com.utopia.dispatcher.task;

import android.os.Process;

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

    void run();

    Future<?> submit(Runnable runnable);

    /**
     * 设置线程执行需要进入的线程池
     */
    ExecutorService getRunThread();

    boolean runOnMainThread();
    /**
     * 依赖关系
     */
    List<Class<? extends Task>> dependsOn();

    /**
     * 异步线程执行的Task是否需要在被调用await的时候等待，默认不需要
     */
    boolean needWait();

    void setTaskCallBack(TaskCallBack callBack);

    boolean needCall();
}
