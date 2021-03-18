package com.utopia.dispatcher.task;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

@IntDef({ScheduleOn.IO, ScheduleOn.CPU, ScheduleOn.UI})
@Retention(RetentionPolicy.SOURCE)
public @interface ScheduleOn {
    int IO = 0;
    int CPU = 1;
    int UI = 2;
}
