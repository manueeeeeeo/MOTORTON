package com.clase.motorton.notifications;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.clase.motorton.notifications.NotificacionDiariaWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationUtils {

    public static final String WORK_NAME = "NotificacionDiaria";

    public static void programarNotificacionDiaria(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);

        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long initialDelay = calendar.getTimeInMillis() - System.currentTimeMillis();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                NotificacionDiariaWorker.class,
                24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest);
    }

    public static void cancelarNotificacionDiaria(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}