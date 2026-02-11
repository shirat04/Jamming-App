package com.example.jamming.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.jamming.R; // ודאי שזה הנתיב הנכון ל-R שלך

public class NotificationHelper {

    public static void showNotification(Context context, String title, String message) {
        String channelId = "event_updates_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Event Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher) // ודאי שיש לך אייקון כזה בתיקיית drawable, או שמי ic_launcher
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);


        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    public static void showOwnerNotification(Context context, String title, String message) {
        // השם של הערוץ חייב להיות חדש (למשל v2) כדי שהטלפון יאפשר קול מחדש
        String channelId = "owner_alerts_v2";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Event Full Updates",
                    NotificationManager.IMPORTANCE_HIGH // זה מה שמחזיר את הקול והווילון
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        // מזהה ייחודי כדי שההתראה לא תיעלם/תידרס
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}