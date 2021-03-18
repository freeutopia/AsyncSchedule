package com.utopia.dispatcher;

import com.utopia.dispatcher.executor.Platform;
import com.utopia.dispatcher.executor.RealRunnable;
import com.utopia.dispatcher.sort.ScheduleUtil;
import com.utopia.dispatcher.task.Task;
import com.utopia.dispatcher.task.TaskStatus;
import com.utopia.dispatcher.utils.ArraysUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 启动器调用类
 */
public class TaskDispatcher implements Dispatcher {
    private volatile boolean started = false;//一个任务调度器，只能执行一次

    private final List<Future<?>> mFutures = new ArrayList<>();
    private List<Task> mAllTasks = new ArrayList<>();//所有任务
    private final List<Class<? extends Task>> mClsAllTasks = new ArrayList<>();

    //处理任务间依赖关系
    private final List<Class<? extends Task>> mFinishedTasks = new ArrayList<>();//已经结束的Task
    private final HashMap<Class<? extends Task>, List<Task>> mDependedHashMap = new HashMap<>();//任务间依赖树

    //配合task的ifNeedWait方法和Dispatcher的await方法使用，用来完成线程等待
    private final AtomicInteger mNeedWaitCount = new AtomicInteger();
    private CountDownLatch mCountDownLatch;

    @Override
    public synchronized Dispatcher add(Task task) {
        if (started)
            throw new IllegalThreadStateException();

        if (task != null) {
            buildDepends(task);
            mAllTasks.add(task);
            mClsAllTasks.add(task.getClass());
            // 非主线程且需要wait的，主线程不需要CountDownLatch也是同步的
            if (ifNeedWait(task)) {
                mNeedWaitCount.getAndIncrement();
            }
        }
        return this;
    }

    @Override
    public void finish(Task task) {
        if (!started)
            throw new IllegalThreadStateException();

        if (ifNeedWait(task)) {
            mFinishedTasks.add(task.getClass());

            mNeedWaitCount.getAndDecrement();
            mCountDownLatch.countDown();
        }

        //给任务打上结束标签
        task.updateStatus(TaskStatus.FINISHED);

        //通知分发器，前置任务已完成
        preTaskFinished(task);
    }

    @Override
    public synchronized void start() {
        if (started || mAllTasks.isEmpty())
            throw new IllegalThreadStateException();
        started = true;

        mAllTasks = ScheduleUtil.getSortResult(mAllTasks, mClsAllTasks);
        mCountDownLatch = new CountDownLatch(mNeedWaitCount.get());

        //分发任务
        dispatherTasks();
    }

    @Override
    public void interruptAll() {
        Platform.get().interruptAll();//停止主线程后续任务执行
        for (Future<?> future : mFutures) {//停止子线程后续任务执行
            future.cancel(true);
        }
    }

    /**
     * 构建任务间依赖关系
     */
    private void buildDepends(Task task) {
        if (!ArraysUtils.isEmpty(task.dependsOn())) {
            for (Class<? extends Task> cls : task.dependsOn()) {
                List<Task> tasks = mDependedHashMap.get(cls);
                if (tasks == null) {
                    mDependedHashMap.put(cls, Collections.singletonList(task));
                }else{
                    tasks.add(task);
                }

                if (mFinishedTasks.contains(cls)) {
                    task.satisfy();
                }
            }
        }
    }

    private boolean ifNeedWait(Task task) {
        return !task.runOnMainThread() && task.needWait();
    }

    /**
     * 开始分发任务
     */
    private synchronized void dispatherTasks() {
        List<Runnable> mainThreadTasks = new ArrayList<>();//主线程任务

        for (Task task : mAllTasks) {
            task.updateStatus(TaskStatus.DISPATCHERED);//更新任务状态->已分发
            Runnable runnable = new RealRunnable(task, this);

            if (task.runOnMainThread()) {
                mainThreadTasks.add(runnable);
            } else {
                mFutures.add(task.submit(runnable));
            }
        }

        //执行当前线程任务(平台相关：如果是Android则切换到UI线程执行)
        Platform.get().executeOnMainThread(mainThreadTasks);
    }

    /**
     * 通知Children一个前置任务已完成
     */
    private void preTaskFinished(Task launchTask) {
        List<Task> tasks = mDependedHashMap.get(launchTask.getClass());
        if (tasks != null && tasks.size() > 0) {
            for (Task task : tasks) {
                task.satisfy();
            }
        }
    }



    @Override
    public void await() throws InterruptedException {
        if (!started || mCountDownLatch == null) {
            throw new InterruptedException("You have to call start() before call await()");
        }

        if (mNeedWaitCount.get() > 0) {
            mCountDownLatch.await(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
    }
}
