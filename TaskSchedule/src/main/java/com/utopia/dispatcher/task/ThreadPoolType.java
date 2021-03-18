package com.utopia.dispatcher.task;

import androidx.annotation.IntDef;

@IntDef({ThreadPoolType.IO,ThreadPoolType.CPU})
public @interface ThreadPoolType {
    int IO = 0;
    int CPU = 1;
}
