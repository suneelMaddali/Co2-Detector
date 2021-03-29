package com.example.cod;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class Co2NotifierService extends JobIntentService {

    Thread task;
    private static final String TAG = "Co2NotifierService";

    private static final int JOB_ID = 4;


    @Override
    public void onCreate() {
        super.onCreate();

        if (task != null) task.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    public static void runNotifier(Context context, Intent intent) {
        enqueueWork(context, Co2NotifierService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        task = new Thread(() -> {
            while (true) {
                startJob();
            }
        });

        task.start();
    }

    private void startJob() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Toast.makeText(Co2NotifierService.this, "Toasting every 3 seconds!", Toast.LENGTH_SHORT).show();
        }, 0);

    }
}
