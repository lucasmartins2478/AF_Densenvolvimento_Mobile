package com.example.notation;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Busca os dados salvos em sharedPreferences
        // e preenche na tela com as informações do usuário

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "Nenhum nome encontrado");
        String email = sharedPreferences.getString("email", "Nenhum email encontrado");

        TextView profileName = findViewById(R.id.profileName);
        TextView profileEmail = findViewById(R.id.profileEmail);
        profileEmail.setText(email);
        profileName.setText(name);
        AppCompatButton btnTaskCount = findViewById(R.id.btnTaskCount);

        // Busca todas as tarefas que o usuário tem
        // salvo no firebase e mostra como forma de
        // alertar ele sobre as tarefas que ele possui

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .collection("tasks")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int count = queryDocumentSnapshots.size();
                        btnTaskCount.setText("Tarefas: " + count);
                    })
                    .addOnFailureListener(e -> {
                        btnTaskCount.setText("Tarefas: erro");
                    });
        }
        btnTaskCount.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, Tasks.class));
        });

        AppCompatButton btnDelete = findViewById(R.id.btnDeleteAccount);
        btnDelete.setOnClickListener(v -> confirmarExclusao());
    }


    // Função que remove todos os dados do usuário de
    // sharedPrefeences e volta ele para a tela de login

    public void logout(View v){
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();

        SharedPreferences alarmPrefs = getSharedPreferences("alarms", MODE_PRIVATE);
        SharedPreferences.Editor alarmEditor = alarmPrefs.edit();
        alarmEditor.clear().apply();

        AlarmUtils.cancelarAlarme(this);

        Intent intent = new Intent(Profile.this, Login.class);
        startActivity(intent);
        finish();
    }

    // Cria o Menu de confirmar a exclusão da conta

    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
                .setTitle("Deletar conta")
                .setMessage("Tem certeza que deseja deletar sua conta? Isso não poderá ser desfeito.")
                .setPositiveButton("Sim", (dialog, which) -> deletarConta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Função que deleta a conta do usuário do firebase

    private void deletarConta() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.delete()
                    .addOnSuccessListener(aVoid -> {
                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear().apply();

                        Toast.makeText(Profile.this, "Conta deletada com sucesso", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(Profile.this, Login.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Profile.this, "Erro ao deletar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}