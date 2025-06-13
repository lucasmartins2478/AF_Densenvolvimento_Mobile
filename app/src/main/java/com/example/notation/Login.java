package com.example.notation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
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

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLogged = sharedPreferences.getBoolean("isLogged", false);

        if(isLogged){
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    // verifica os dados do usuário no firebase e retorna
    // não só os dados principais como nome e email, mas
    // também já redefine os alarmes que ele possui cadastrado

    public void login(View v) {
        EditText edtEmail = findViewById(R.id.emailInput);
        EditText edtSenha = findViewById(R.id.passwordInput);

        mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtSenha.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String name = documentSnapshot.getString("name");

                                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("name", name);
                                        editor.putString("email", edtEmail.getText().toString());
                                        editor.putBoolean("isLogged", true);
                                        editor.commit();

                                        Toast.makeText(this, "Login bem-sucedido", Toast.LENGTH_LONG).show();
                                        Log.d("FIREBASE", "Login bem-sucedido");
                                        db.collection("users")
                                                .document(uid)
                                                .collection("alarms")
                                                .get()
                                                .addOnSuccessListener(querySnapshot -> {
                                                    SharedPreferences alarmPrefs = getSharedPreferences("alarms", MODE_PRIVATE);
                                                    SharedPreferences.Editor alarmEditor = alarmPrefs.edit();
                                                    StringBuilder alarmsJson = new StringBuilder("[");

                                                    for (int i = 0; i < querySnapshot.size(); i++) {
                                                        var doc = querySnapshot.getDocuments().get(i);
                                                        long hour = doc.getLong("hour");
                                                        long minute = doc.getLong("minute");
                                                        long day = doc.getLong("day");

                                                        AlarmUtils.definirAlarme(this, (int) hour, (int) minute, (int) day);

                                                        alarmsJson.append(String.format("{\"hour\":%d,\"minute\":%d,\"day\":%d}", hour, minute, day));
                                                        if (i < querySnapshot.size() - 1) alarmsJson.append(",");
                                                    }

                                                    alarmsJson.append("]");
                                                    alarmEditor.putString("saved_alarms", alarmsJson.toString());
                                                    alarmEditor.apply();

                                                    Log.d("ALARMS", "Alarmes restaurados do Firestore");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("ALARMS", "Erro ao restaurar alarmes", e);
                                                });

                                        Intent intent = new Intent(Login.this, MainActivity.class);
                                        startActivity(intent);
                                        finish(); // opcional
                                    } else {
                                        Toast.makeText(this, "Dados do usuário não encontrados", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erro ao buscar nome do usuário", Toast.LENGTH_SHORT).show();
                                    Log.e("FIREBASE", "Erro ao buscar nome", e);
                                });
                    } else {
                        Toast.makeText(this, "Erro no login: " + task.getException(), Toast.LENGTH_LONG).show();
                        Log.e("FIREBASE", "Erro no login", task.getException());
                    }
                });
    }


    // Vai para a tela de cadastro caso não tenha um conta

    public void goToRegister(View v){
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
    }
}