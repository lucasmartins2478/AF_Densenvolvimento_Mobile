package com.example.notation;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "alarm_channel";
    private Ringtone ringtone;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNotification = intent.getBooleanExtra("notification", false);

        if (isNotification) {
            createNotificationChannel(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.alarm_icon)
                    .setContentTitle("Alarme em breve!")
                    .setContentText("Seu alarme tocará em alguns minutos.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(2, builder.build());
        } else {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            try {
                ringtone = RingtoneManager.getRingtone(context, alarmUri);
                if (ringtone != null) {
                    ringtone.play();
                } else {
                    Log.e("AlarmReceiver", "Não foi possível obter o Ringtone.");
                }
            } catch (Exception e) {
                Log.e("AlarmReceiver", "Erro ao tocar o Ringtone: " + e.getMessage());
            }

            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(1000);
            }

            createNotificationChannel(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.alarm_icon)
                    .setContentTitle("Alarme!")
                    .setContentText("Seu alarme está tocando.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(null, true)
                    .setSound(alarmUri)
                    .setVibrate(new long[]{1000, 1000, 1000})
                    .setAutoCancel(false) // Não cancelar automaticamente ao tocar
                    .setDeleteIntent(createDismissIntent(context))
                    .addAction(R.drawable.alarm_icon, "Soneca", createSnoozeIntent(context)); // Adiciona o botão de soneca

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(1, builder.build());
        }
    }

    private PendingIntent createDismissIntent(Context context) {
        Intent dismissIntent = new Intent(context, AlarmDismissReceiver.class);
        return PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent createSnoozeIntent(Context context) {
        Intent snoozeIntent = new Intent(context, SnoozeReceiver.class);
        return PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Channel";
            String description = "Channel for Alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}