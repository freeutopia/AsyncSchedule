package com.utopia.dispatcher.task;

public enum TaskStatus {
    /**
     * 是否正在等待
     */
    WAITING,

    /**
     * 是否正在执行
     */
    RUNNING,

    /**
     * Task是否执行完成
     */
    FINISHED,

    /**
     * Task是否已经被分发
     */
    DISPATCHERED,

    /**
     * 空闲状态
     */
    IDEL
}
