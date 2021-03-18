package com.utopia.dispatcher.task;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

@IntDef({TaskStatus.WAITING, TaskStatus.RUNNING, TaskStatus.FINISHED, TaskStatus.DISPATCHERED, TaskStatus.IDEL})
@Retention(RetentionPolicy.SOURCE)
public @interface TaskStatus {
    /**
     * 是否正在等待
     */
    int WAITING = 0;

    /**
     * 是否正在执行
     */
    int RUNNING = 1;

    /**
     * Task是否执行完成
     */
    int FINISHED = 2;

    /**
     * Task是否已经被分发
     */
    int DISPATCHERED = 3;

    /**
     * 空闲状态
     */
    int IDEL = 4;
}
