package com.utopia.dispatcher.sort;

import com.utopia.dispatcher.task.Task;
import com.utopia.dispatcher.utils.ArraysUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;


public class ScheduleUtil {

    /**
     * 任务的有向无环图的拓扑排序
     */
    public static synchronized List<Task> getSortResult(List<Task> originTasks) {

        Set<Integer> dependSet = new ArraySet<>();
        Graph graph = new Graph(originTasks.size());
        for (int i = 0; i < originTasks.size(); i++) {
            Task task = originTasks.get(i);
            Set<Task> dTasks = task.getDependTasks();
            if (ArraysUtils.isEmpty(dTasks)) {
                continue;
            }
            for (Task dTask : dTasks) {
                int indexOfDepend = getIndexOfTask(originTasks, dTask.getClass());
                if (indexOfDepend < 0) {
                    throw new IllegalStateException(task.getClass().getSimpleName() +
                            " depends on " + dTask.getClass().getSimpleName() + " can not be found in task list ");
                }
                dependSet.add(indexOfDepend);
                graph.addEdge(indexOfDepend, i);
            }
        }
        List<Integer> indexList = graph.topologicalSort();

        return getResultTasks(originTasks, dependSet, indexList);
    }

    @NonNull
    private static List<Task> getResultTasks(List<Task> originTasks, Set<Integer> dependSet, List<Integer> indexList) {

        List<Task> newTasksDepended = new ArrayList<>();// 被别人依赖的
        List<Task> newTasksWithOutDepend = new ArrayList<>();// 没有依赖的
        List<Task> newTasksRunAsSoon = new ArrayList<>();// 需要提升自己优先级的，先执行（这个先是相对于没有依赖的先）
        for (int index : indexList) {
            if (dependSet.contains(index)) {
                newTasksDepended.add(originTasks.get(index));
            } else {
                Task task = originTasks.get(index);
                if (task.needRunAsSoon()) {
                    newTasksRunAsSoon.add(task);
                } else {
                    newTasksWithOutDepend.add(task);
                }
            }
        }
        // 顺序：被别人依赖的————》需要提升自己优先级的————》需要被等待的————》没有依赖的
        List<Task> newTasksAll = new ArrayList<>(originTasks.size());
        newTasksAll.addAll(newTasksDepended);
        newTasksAll.addAll(newTasksRunAsSoon);
        newTasksAll.addAll(newTasksWithOutDepend);
        return newTasksAll;
    }

    /**
     * 获取任务在任务列表中的下标
     */
    private static int getIndexOfTask(List<Task> originTasks, Class<? extends Task> taskClass) {
        final int size = originTasks.size();
        for (int i = 0; i < size; i++) {
            if (taskClass == originTasks.get(i).getClass()) {
                return i;
            }
        }
        return 0;
    }
}
