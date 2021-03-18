package com.utopia.dispatcher.utils;

import com.utopia.dispatcher.task.Task;

import java.util.List;
import java.util.Set;


public class ArraysUtils {
    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isEmpty(Set<?> objects) {
        return objects == null || objects.isEmpty();
    }

    public static boolean isEmpty(Task... tasks) {
        return tasks == null || tasks.length == 0;
    }
}
