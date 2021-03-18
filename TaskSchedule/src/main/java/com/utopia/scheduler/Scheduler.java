package com.utopia.scheduler;

import com.utopia.scheduler.executor.Platform;
import com.utopia.scheduler.job.IJob;
import com.utopia.scheduler.sort.ScheduleUtil;
import com.utopia.scheduler.job.ScheduleOn;
import com.utopia.scheduler.utils.ArraysUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class Scheduler implements IScheduler {
    private volatile boolean started = false;//一个任务调度器，只能执行一次

    private List<IJob> mAllTasks = new ArrayList<>();//所有任务
    private final List<Future<?>> mFutures = new ArrayList<>();//所有异步任务

    //处理任务间依赖关系
    private final List<IJob> mFinishedTasks = new ArrayList<>();//已经结束的Task
    private final HashMap<IJob, List<IJob>> mDependedHashMap = new HashMap<>();//任务间依赖树

    //配合task的ifNeedWait方法和Dispatcher的await方法使用，用来完成线程等待
    private final AtomicInteger mNeedWaitCount = new AtomicInteger();
    private CountDownLatch mCountDownLatch;

    @Override
    public synchronized IScheduler add(IJob job) {
        if (started)
            throw new IllegalThreadStateException();

        if (job != null) {
            buildDepends(job);
            mAllTasks.add(job);

            // 非主线程且需要wait的，主线程不需要CountDownLatch也是同步的
            if (ifNeedWait(job)) {
                mNeedWaitCount.getAndIncrement();
            }
        }
        return this;
    }

    @Override
    public void finish(IJob task) {
        if (!started)
            throw new IllegalThreadStateException();

        if (ifNeedWait(task)) {
            mFinishedTasks.add(task);

            mNeedWaitCount.getAndDecrement();
            mCountDownLatch.countDown();
        }

        //通知分发器，前置任务已完成
        preTaskFinished(task);
    }

    @Override
    public synchronized void start() {
        if (started || mAllTasks.isEmpty())
            throw new IllegalThreadStateException();
        started = true;

        mAllTasks = ScheduleUtil.getSortResult(mAllTasks);
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
    private void buildDepends(IJob task) {
        if (ArraysUtils.isEmpty(task.getDependTasks())) {
            return;
        }

        for (IJob dependTask : task.getDependTasks()) {
            List<IJob> tasks = mDependedHashMap.get(dependTask);
            if (tasks == null) {
                mDependedHashMap.put(dependTask, Collections.singletonList(task));
            }else{
                tasks.add(task);
            }

            if (mFinishedTasks.contains(dependTask)) {
                task.satisfy();
            }
        }
    }

    private boolean ifNeedWait(IJob task) {
        return task.needWait() && !(task.scheduleOn() == ScheduleOn.UI);
    }

    /**
     * 开始分发任务
     */
    private synchronized void dispatherTasks() {
        for (IJob task : mAllTasks) {
            Future<?> submit = task.submitOn(this);
            if (submit != null) {
                mFutures.add(submit);
            }
        }
        //执行主线程任务
        Platform.get().execute();
    }

    /**
     * 通知Children一个前置任务已完成
     */
    private void preTaskFinished(IJob launchTask) {
        List<IJob> tasks = mDependedHashMap.get(launchTask);
        if (tasks != null && tasks.size() > 0) {
            for (IJob task : tasks) {
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
