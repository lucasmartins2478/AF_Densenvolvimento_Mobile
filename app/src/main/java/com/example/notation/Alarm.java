package com.example.notation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notation.databinding.ActivityAlarmBinding;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Alarm extends AppCompatActivity {

    private TextView tvSelectedTime;
    private int selectedHour = -1, selectedMinute = -1;
    private final Map<Integer, CheckBox> dayCheckboxes = new HashMap<>();
    private final int[] daysOfWeek = {
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent); // Isso abre as configurações para o usuário permitir
            }
        }

        setContentView(R.layout.activity_alarm);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        LinearLayout daysContainer = findViewById(R.id.daysContainer);

        for (int day : daysOfWeek) {
            CheckBox cb = new CheckBox(this);
            cb.setText(getDayName(day));
            daysContainer.addView(cb);
            dayCheckboxes.put(day, cb);
        }

        findViewById(R.id.btnSelectTime).setOnClickListener(v -> showTimePicker());
        findViewById(R.id.btnSetAlarm).setOnClickListener(v -> setAlarm());
        findViewById(R.id.btnCancelAlarm).setOnClickListener(v -> AlarmUtils.cancelAllAlarms(this));
    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
                .setTitleText("Select Alarm Time")
                .setPositiveButtonText("OK")
                .setTheme(R.style.ThemeOverlay_Notation_TimePicker)
                .build();



        picker.show(getSupportFragmentManager(), "time_picker");


        picker.addOnPositiveButtonClickListener(view -> {
            selectedHour = picker.getHour();
            selectedMinute = picker.getMinute();
            tvSelectedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
        });
    }


    private void setAlarm() {
        if (selectedHour == -1 || selectedMinute == -1) {
            Toast.makeText(this, "Select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int day : daysOfWeek) {
            if (dayCheckboxes.get(day).isChecked()) {
                AlarmUtils.setWeeklyAlarm(this, selectedHour, selectedMinute, day);
            }
        }

        Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show();
    }

    private String getDayName(int day) {
        return new DateFormatSymbols().getWeekdays()[day];
    }
}
