package com.clase.motorton.notifications;

import android.content.Context;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessaging;

public class FirebaseNotificationDaily {
    private static final String TAG = "NotificacionManager";
    public static final String TEMA_NOTIFICACION_DIARIA = "notificacion_diaria";

    public static void suscribirAnotificacionDiaria(Context context) {
        FirebaseMessaging.getInstance().subscribeToTopic(TEMA_NOTIFICACION_DIARIA)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Suscrito al tema: " + TEMA_NOTIFICACION_DIARIA);
                    } else {
                        Log.e(TAG, "Error al suscribirse al tema: " + TEMA_NOTIFICACION_DIARIA, task.getException());
                    }
                });
    }

    public static void desuscribirDeNotificacionDiaria(Context context) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TEMA_NOTIFICACION_DIARIA)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Desuscrito del tema: " + TEMA_NOTIFICACION_DIARIA);
                    } else {
                        Log.e(TAG, "Error al desuscribirse del tema: " + TEMA_NOTIFICACION_DIARIA, task.getException());
                    }
                });
    }
}