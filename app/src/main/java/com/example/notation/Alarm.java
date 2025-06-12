package com.example.notation;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notation.databinding.ActivityAlarmBinding;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

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
        EdgeToEdge.enable(this);
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
        carregarAlarmesSalvos();

        findViewById(R.id.btnSelectTime).setOnClickListener(v -> showCustomTimePickerDialog());
        findViewById(R.id.btnSetAlarm).setOnClickListener(v -> setAlarm());
        findViewById(R.id.btnCancelAlarm).setOnClickListener(v -> AlarmUtils.cancelAllAlarms(this));
    }

    private void showCustomTimePickerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_picker, null);

        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnOk.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            // Aqui você pode salvar o horário selecionado e atualizar a UI
            selectedHour = hour;
            selectedMinute = minute;
            tvSelectedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }



    private void setAlarm() {
        if (selectedHour == -1 || selectedMinute == -1) {
            Toast.makeText(this, "Selecione um horário", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int day : daysOfWeek) {
            if (dayCheckboxes.get(day).isChecked()) {


                AlarmUtils.setWeeklyAlarm(this, selectedHour, selectedMinute, day);
            }
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            for (int day : daysOfWeek) {
                if (dayCheckboxes.get(day).isChecked()) {
                    Map<String, Object> alarm = new HashMap<>();
                    alarm.put("hour", selectedHour);
                    alarm.put("minute", selectedMinute);
                    alarm.put("day", day);

                    db.collection("users")
                            .document(user.getUid())
                            .collection("alarms")
                            .add(alarm);
                }
            }
        }
        saveAlarmLocally();

        Toast.makeText(this, "Alarm definido", Toast.LENGTH_SHORT).show();
    }

    private void carregarAlarmesSalvos() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("alarms")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        Long hour = doc.getLong("hour");
                        Long minute = doc.getLong("minute");
                        Long day = doc.getLong("day");

                        if (hour != null && minute != null && day != null) {
                            // Marca o checkbox do dia
                            CheckBox cb = dayCheckboxes.get(day.intValue());
                            if (cb != null) cb.setChecked(true);

                            // Atualiza hora selecionada (pode mostrar o último alarme criado)
                            selectedHour = hour.intValue();
                            selectedMinute = minute.intValue();
                            tvSelectedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar alarmes salvos", Toast.LENGTH_SHORT).show();
                });
    }


    private String getDayName(int day) {
        return new DateFormatSymbols().getWeekdays()[day];
    }


    private void saveAlarmLocally() {
        try {
            SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            JSONArray alarmArray = new JSONArray();

            for (int day : daysOfWeek) {
                if (dayCheckboxes.get(day).isChecked()) {
                    JSONObject alarmJson = new JSONObject();
                    alarmJson.put("hour", selectedHour);
                    alarmJson.put("minute", selectedMinute);
                    alarmJson.put("day", day);
                    alarmArray.put(alarmJson);
                }
            }

            editor.putString("saved_alarms", alarmArray.toString());
            editor.apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
