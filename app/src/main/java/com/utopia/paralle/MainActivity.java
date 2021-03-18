package com.utopia.paralle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.utopia.scheduler.job.Job;
import com.utopia.scheduler.Scheduler;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread().interrupt();

        Log.e("test","我执行了onCreate");
        Scheduler dispatcher = new Scheduler();
        dispatcher.add(new Job() {
            @Override
            public void run() {
                try {
                    Log.e("test","我进入了任务1");
                    TimeUnit.SECONDS.sleep(3);
                    Log.e("test","我完成了任务1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).add(new Job() {
            @Override
            public void run() {
                try {
                    Log.e("test","我进入了任务2");
                    TimeUnit.SECONDS.sleep(6);
                    Log.e("test","我完成了任务2");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).add(new Job() {
            @Override
            public void run() {
                try {
                    Log.e("test","我进入了任务3");
                    TimeUnit.SECONDS.sleep(1);
                    Log.e("test","我完成了任务3");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        dispatcher.start();
    }
}