package com.utopia.scheduler.utils;

import com.utopia.scheduler.job.IJob;

import java.util.List;
import java.util.Set;


public class ArraysUtils {
    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isEmpty(Set<?> objects) {
        return objects == null || objects.isEmpty();
    }

    public static boolean isEmpty(IJob... objects) {
        return objects == null || objects.length == 0;
    }
}
