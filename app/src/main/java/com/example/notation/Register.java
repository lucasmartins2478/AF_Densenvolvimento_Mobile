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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void registerUser(View v) {
        EditText edtName = findViewById(R.id.nameInput);
        EditText edtEmail = findViewById(R.id.emailInput);
        EditText edtSenha = findViewById(R.id.passwordInput);
        EditText edtConfirmPassword = findViewById(R.id.confirmPasswordInput);
        if (edtSenha.getText().toString().length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return; // Interrompe a execução da função
        }

        if(!edtConfirmPassword.getText().toString().equals(edtSenha.getText().toString())){
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
        }else{
            mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtSenha.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name", edtName.getText().toString());
                            editor.putString("email", edtEmail.getText().toString());
                            editor.putBoolean("isLogged", true);
                            editor.commit();
                            Toast.makeText(this, "Usuário criado com sucesso", Toast.LENGTH_LONG).show();
                            Log.d("FIREBASE", "Usuário criado com sucesso");
                            Intent intent = new Intent(Register.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Erro ao criar usuário: " + task.getException(), Toast.LENGTH_LONG).show();
                            Log.e("FIREBASE", "Erro ao criar usuário", task.getException());
                        }
                    });
        }
    }


    public void goToLogin(View v){
        Intent intent  = new Intent(Register.this, Login.class);
        startActivity(intent);
    }
}