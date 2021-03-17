package com.utopia.dispatcher;

import com.utopia.dispatcher.executor.RealRunnable;
import com.utopia.dispatcher.schedule.ScheduleUtil;
import com.utopia.dispatcher.task.Task;
import com.utopia.dispatcher.task.TaskStatus;
import com.utopia.dispatcher.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 启动器调用类
 */
public class TaskDispatcher implements Dispatcher{
    private volatile boolean started = false;//一个任务调度器，只能执行一次

    private final List<Future<?>> mFutures = new ArrayList<>();
    private List<Task> mAllTasks = new ArrayList<>();
    private final List<Class<? extends Task>> mClsAllTasks = new ArrayList<>();
    private final List<Task> mMainThreadTasks = new ArrayList<>();
    private CountDownLatch mCountDownLatch;

    /**
     * 需要等待的任务数
     */
    private final AtomicInteger mNeedWaitCount = new AtomicInteger();


    /**
     * 已经结束的Task
     */
    private final List<Class<? extends Task>> mFinishedTasks = new ArrayList<>();

    private final HashMap<Class<? extends Task>, ArrayList<Task>> mDependedHashMap = new HashMap<>();

    /**
     * 启动器分析的次数，统计下分析的耗时；
     */
    private final AtomicInteger mAnalyseCount = new AtomicInteger();


    @Override
    public synchronized Dispatcher add(Task task) {
        if (started)
            throw new IllegalThreadStateException();

        if (task != null) {
            collectDepends(task);
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
    public boolean remove(Task task) {
        if (ifNeedWait(task)) {
            mFinishedTasks.add(task.getClass());
            mCountDownLatch.countDown();
            mNeedWaitCount.getAndDecrement();
        }
        return true;
    }

    @Override
    public synchronized void start() {
        if (started)
            throw new IllegalThreadStateException();
        started = true;

        if (mAllTasks.size() > 0) {
            mAnalyseCount.getAndIncrement();
            mAllTasks = ScheduleUtil.getSortResult(mAllTasks, mClsAllTasks);
            mCountDownLatch = new CountDownLatch(mNeedWaitCount.get());

            sendAndExecuteAsyncTasks();
            executeTaskMain();
        }
    }

    @Override
    public void interrupt() {
        for (Future<?> future : mFutures) {
            future.cancel(true);
        }
    }

    private void collectDepends(Task task) {
        if (task.dependsOn() != null && task.dependsOn().size() > 0) {
            for (Class<? extends Task> cls : task.dependsOn()) {
                if (mDependedHashMap.get(cls) == null) {
                    mDependedHashMap.put(cls, new ArrayList<Task>());
                }
                mDependedHashMap.get(cls).add(task);
                if (mFinishedTasks.contains(cls)) {
                    task.satisfy();
                }
            }
        }
    }

    private boolean ifNeedWait(Task task) {
        return !task.runOnMainThread() && task.needWait();
    }

    private void executeTaskMain() {
        for (Task task : mMainThreadTasks) {
            new RealRunnable(task, this).run();
        }
    }

    /**
     * 发送去并且执行异步任务
     */
    private void sendAndExecuteAsyncTasks() {
        for (Task task : mAllTasks) {
            if (task.runOnMainThread() && !Utils.isMainProcess()) {
                remove(task);
            } else {
                sendTaskReal(task);
            }
            task.updateStatus(TaskStatus.DISPATCHERED);
        }
    }

    /**
     * 通知Children一个前置任务已完成
     */
    public void satisfyChildren(Task launchTask) {
        ArrayList<Task> arrayList = mDependedHashMap.get(launchTask.getClass());
        if (arrayList != null && arrayList.size() > 0) {
            for (Task task : arrayList) {
                task.satisfy();
            }
        }
    }

    /**
     * 发送任务
     */
    private void sendTaskReal(final Task task) {
        if (task.runOnMainThread()) {
            mMainThreadTasks.add(task);
            if (task.needCall()) {
                task.setTaskCallBack(() -> {
                    task.updateStatus(TaskStatus.FINISHED);
                    satisfyChildren(task);
                    remove(task);
                });
            }
        } else {
            // 直接发，是否执行取决于具体线程池
            Future<?> future = task.submit(new RealRunnable(task, this));
            mFutures.add(future);
        }
    }

    @Override
    public void await() {
        try {
            if (mNeedWaitCount.get() > 0) {
                if (mCountDownLatch == null) {
                    throw new RuntimeException("You have to call start() before call await()");
                }
                mCountDownLatch.await(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException ignored) {
        }
    }
}
