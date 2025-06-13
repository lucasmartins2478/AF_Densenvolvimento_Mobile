package com.example.notation;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;


public class AlarmUtils {

    @SuppressLint({"ObsoleteSdkInt", "ScheduleExactAlarm"})
    public static void definirAlarme(Context context, int hour, int minute, int dayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("day", dayOfWeek);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        int requestCode = dayOfWeek;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    // Reagenda os alarmes do usuário quando ele faz login para
    // que os alarmes salvos no firebase sempre toquem quando
    //  ele fizer login no app

    public static void reagendarTodosAlarmes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE);
        String savedAlarms = prefs.getString("saved_alarms", null);

        if (savedAlarms == null) return;

        try {
            JSONArray alarmArray = new JSONArray(savedAlarms);
            for (int i = 0; i < alarmArray.length(); i++) {
                JSONObject alarmJson = alarmArray.getJSONObject(i);
                int hour = alarmJson.getInt("hour");
                int minute = alarmJson.getInt("minute");
                int day = alarmJson.getInt("day");

                definirAlarme(context, hour, minute, day);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Cancela o alarme definido pelo usuário
    public static void cancelarAlarme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE);
        String savedAlarms = prefs.getString("saved_alarms", null);

        if (savedAlarms == null) return;

        try {
            JSONArray alarmArray = new JSONArray(savedAlarms);
            for (int i = 0; i < alarmArray.length(); i++) {
                JSONObject alarmJson = alarmArray.getJSONObject(i);
                int hour = alarmJson.getInt("hour");
                int minute = alarmJson.getInt("minute");
                int day = alarmJson.getInt("day");

                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra("day", day);
                intent.putExtra("hour", hour);
                intent.putExtra("minute", minute);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        day,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users")
                            .document(user.getUid())
                            .collection("alarms")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (var doc : queryDocumentSnapshots) {
                                    db.collection("users")
                                            .document(user.getUid())
                                            .collection("alarms")
                                            .document(doc.getId())
                                            .delete();
                                }
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                            });
                }

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }
            prefs.edit().remove("saved_alarms").apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
