package com.example.notation;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "Nenhum nome encontrado");
        String email = sharedPreferences.getString("email", "Nenhum email encontrado");

        TextView profileName = findViewById(R.id.profileName);
        TextView profileEmail = findViewById(R.id.profileEmail);
        profileEmail.setText(email);
        profileName.setText(name);

        AppCompatButton btnDelete = findViewById(R.id.btnDeleteAccount);
        btnDelete.setOnClickListener(v -> confirmarExclusao());
    }



    public void logout(View v){
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogged", false);
        editor.commit();
        Intent intent = new Intent(Profile.this, Login.class);
        startActivity(intent);
    }

    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
                .setTitle("Deletar conta")
                .setMessage("Tem certeza que deseja deletar sua conta? Isso não poderá ser desfeito.")
                .setPositiveButton("Sim", (dialog, which) -> deletarConta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletarConta() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.delete()
                    .addOnSuccessListener(aVoid -> {
                        // Limpa o shared preferences
                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear().apply();

                        Toast.makeText(Profile.this, "Conta deletada com sucesso", Toast.LENGTH_SHORT).show();

                        // Vai para a tela de login
                        startActivity(new Intent(Profile.this, Login.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Profile.this, "Erro ao deletar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}