package com.example.notation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    AppCompatButton btnProfile, btnConfiguration, btnAlarm, btnTasks, btnCalendar, btnJournal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );


        btnAlarm = findViewById(R.id.btnAlarm);
        btnTasks = findViewById(R.id.btnTasks);
        btnConfiguration = findViewById(R.id.btnConfiguration);
        btnCalendar = findViewById(R.id.btnCalendar);
        btnProfile = findViewById(R.id.btnProfile);
        btnJournal = findViewById(R.id.btnJournal);

    }

    public void openProfile(View v){
        Intent intent = new Intent(MainActivity.this, Profile.class);
        startActivity(intent);
    }
    public void openJournal(View v){
        Intent intent = new Intent(MainActivity.this, Journal.class);
        startActivity(intent);
    }
    public void openCalendar(View v){
        Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
        startActivity(intent);

    }
    public void openConfiguration(View v){
        Intent intent = new Intent(MainActivity.this, Configuration.class);
        startActivity(intent);
    }
    public void openAlarm(View v){
        Intent intent = new Intent(MainActivity.this, Alarm.class);
        startActivity(intent);
    }
    public void openTasks(View v){
        Intent intent = new Intent(MainActivity.this, Tasks.class);
        startActivity(intent);
    }
}