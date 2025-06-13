package com.example.notation;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private static MediaPlayer staticMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        staticMediaPlayer = mediaPlayer;

        Button btnDismiss = findViewById(R.id.btnDismiss);
        Button btnSnooze = findViewById(R.id.btnSnooze);

        btnDismiss.setOnClickListener(v -> {
            stopAlarmSound();
            finish();
        });

        btnSnooze.setOnClickListener(v -> {
            snoozeAlarm();
            stopAlarmSound();
            finish();
        });
    }

    // Tira o som do alarme

    private void stopAlarmSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    // Define o alarme para tocar novamente em
    // 5 minutos depois de apertar o botão soneca

    @SuppressLint("ScheduleExactAlarm")
    private void snoozeAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 9999, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerAt = SystemClock.elapsedRealtime() + 5 * 60 * 1000;
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent);
    }

    // Para o alarme após apartar o botão

    public static void stopStaticAlarm() {
        if (staticMediaPlayer != null && staticMediaPlayer.isPlaying()) {
            staticMediaPlayer.stop();
            staticMediaPlayer.release();
        }
    }
}
