## AsyncSchedule


Android异步任务调度器，通过有向无环图进行任务执行顺序排序，后续通过JUC线程调度完成线程间通信。框架核心是线程池任务调度，实现线程间最大限度并行发生。

## 框架说明
|关键角色|角色功能|描述|
|---|---|---|
|`Scheduler`|任务调度器|对任务进行增加和执行等。内部实现DAG排序算法和任务调度算法，核心角色|
|`Job`|任务抽象接口|任务模板类，由子类实现run方法，内部提供对任务优先级、依赖关系和任务类型的配置信息
|`Executor`|任务执行器|内部提供三种任务执行器，包括IO密集型任务、CPU密集型任务和主线程任务执行策略|


## 依赖配置

```groovy

//需要在使用到本库的模块添加以下依赖
dependencies {
    ...
    implementation 'com.utopia:schedule:1.0.0'
}

```

## 使用示例
1、本框架和Thread框架类似，配置好异步任务只能执行一次，主要是用来解决Anrdoid系统生命周期onCreate耗时任务卡顿优化：
````java
//生成调度器
Scheduler dispatcher = new Scheduler();
````

2、自定义CustomJob，必须继承Job抽象类或者实现IJob接口，建议继承Job类，里面配置了默认数据，用户只需实现run方法：
````java
public class CustomJob extends Job {   
        @Override   
        public void run() {       
                try {           
                        TimeUnit.SECONDS.sleep(3);       
                } catch (InterruptedException e) {           
                        e.printStackTrace();       
                }   
        }
}

````

3、开启作业调度：
````java
Job1 task1 = new Job1();
Job2 task2 = new Job2();
dispatcher
        .add(task1)
        .add(task2)
        .add(new Job() {   
                @Override   
                public void run() {          
                        System.out.println("执行完了匿名任务);     
                }
       } );
dispatcher.start();


````

## DAG构建优先级任务+抢占任务图解
![Image text](https://raw.githubusercontent.com/freeutopia/AsyncSchedule/main/images/schedule.jpg)

````java

假设作业入队顺序是：job1->job2->job3->job4->job5->job6

step1：构建先行发生关系，作业3依赖作业1和作业2；
            当前任务队列数据排序关系：job1->job2->job3
step2：检测作业优先级配置，图例中作业5优先级高于普通作业job3；
            当前任务队列数据排序关系：job1->job2->job5->job3
step3：Job4依赖与Job3
            当前任务队列数据排序关系：job1->job2->job5->job3->Job4
step4：最后剩下的普通作业，根据入队顺序排序
            当前任务队列数据排序关系：job1->job2->job5->job3->Job4->Job6

最终优先级任务调度器排序后的任务执行顺序为：ob1->job2->job5->job3->Job4->Job6
````


## 开源协议
```text
Copyright 2021 freeutopia.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```