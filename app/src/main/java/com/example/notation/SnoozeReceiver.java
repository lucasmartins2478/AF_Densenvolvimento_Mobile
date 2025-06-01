package com.example.notation;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationManagerCompat;
import java.util.Calendar;

public class SnoozeReceiver extends BroadcastReceiver {
    private Ringtone ringtone;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(1); // Cancela a notificação do alarme principal

        // Parar o som do alarme, se estiver tocando
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(context, alarmUri);
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }

        // Agendar um novo alarme para 5 minutos depois (pode ser configurável)
        Calendar snoozeCalendar = Calendar.getInstance();
        snoozeCalendar.add(Calendar.MINUTE, 5);

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeCalendar.getTimeInMillis(), alarmPendingIntent);
            Log.d("SnoozeReceiver", "Alarme adiado para " + snoozeCalendar.getTime().toString());
        }
    }
}