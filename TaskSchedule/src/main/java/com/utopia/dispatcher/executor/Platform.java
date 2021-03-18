package com.utopia.dispatcher.executor;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import com.utopia.dispatcher.utils.ArraysUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Platform {
    protected volatile boolean cancel = false;
    List<Runnable> mTasks = new ArrayList<>();

    private static final Platform PLATFORM = findPlatform();

    public static Platform get() {
        return PLATFORM;
    }

    private static Platform findPlatform() {
        try {
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0) {
                return new Android();
            }
        } catch (ClassNotFoundException ignored) {
        }

        return new Java8();
    }

    /**
     * 设置线程优先级
     */
    abstract void setThreadPriority(int priority);

    /**
     * 切换到主线程执行任务
     */
    public final void addTaskToMainThread(Runnable task){
        mTasks.add(task);
    }

    public abstract void execute();
    /**
     * 只能停止未进行的任务，不能终止正在进行的任务
     */
    public void interruptAll(){
        cancel = true;
    }

    static class Java8 extends Platform {

        @Override
        void setThreadPriority(int priority) {

        }

        @Override
        public void execute() {
            if (ArraysUtils.isEmpty(mTasks)) {
                return;
            }

            for (Runnable runnable : mTasks) {
                if (cancel){
                    break;
                }
                runnable.run();
            }
        }
    }

    static class Android extends Platform {

        @Override
        void setThreadPriority(int priority) {
            Process.setThreadPriority(priority);//更新线程优先级
        }

        @Override
        public void execute() {
            if (ArraysUtils.isEmpty(mTasks)) {
                return;
            }

            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {
                for (Runnable runnable : mTasks) {
                    if (cancel){
                        break;
                    }
                    runnable.run();
                }
            });
        }
    }
}
