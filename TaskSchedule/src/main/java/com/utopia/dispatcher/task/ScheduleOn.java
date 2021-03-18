package com.utopia.dispatcher.task;

import androidx.annotation.IntDef;

@IntDef({ScheduleOn.IO, ScheduleOn.CPU})
public @interface ScheduleOn {
    int IO = 0;
    int CPU = 1;
}
