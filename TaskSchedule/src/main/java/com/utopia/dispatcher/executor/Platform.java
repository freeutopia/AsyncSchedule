package com.utopia.dispatcher.executor;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import java.util.List;

public abstract class Platform {
    protected volatile boolean cancel = false;

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

        try {
            Class.forName("java.util.Optional");
            return new Java8();
        } catch (ClassNotFoundException ignored) {
        }

        return new Others();
    }

    /**
     * 设置线程优先级
     */
    abstract void setThreadPriority(int priority);

    /**
     * 切换到主线程执行任务
     */
    public abstract void executeOnMainThread(List<Runnable> tasks);

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
        public void executeOnMainThread(List<Runnable> tasks) {
            if (tasks == null || tasks.isEmpty()) {
                return;
            }

            for (Runnable runnable : tasks) {
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
        public void executeOnMainThread(List<Runnable> tasks) {
            if (tasks == null || tasks.isEmpty()) {
                return;
            }

            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {
                for (Runnable runnable : tasks) {
                    if (cancel){
                        break;
                    }
                    runnable.run();
                }
            });
        }
    }

    static class Others extends Platform {

        @Override
        void setThreadPriority(int priority) {

        }

        @Override
        public void executeOnMainThread(List<Runnable> tasks) {

        }
    }
}
