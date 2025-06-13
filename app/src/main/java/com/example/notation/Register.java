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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

    // Função que cadastra o usuário no firebase

    public void registerUser(View v) {
        EditText edtName = findViewById(R.id.nameInput);
        EditText edtEmail = findViewById(R.id.emailInput);
        EditText edtSenha = findViewById(R.id.passwordInput);
        EditText edtConfirmPassword = findViewById(R.id.confirmPasswordInput);
        if (edtSenha.getText().toString().length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!edtConfirmPassword.getText().toString().equals(edtSenha.getText().toString())){
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
        }else{
            mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtSenha.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String name = edtName.getText().toString();
                            String email = edtEmail.getText().toString();

                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name", name);
                            editor.putString("email", email);
                            editor.putBoolean("isLogged", true);
                            editor.commit();

                            // Salva no Firestore
                            String uid = mAuth.getCurrentUser().getUid();
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .document(uid)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Usuário criado com sucesso", Toast.LENGTH_LONG).show();
                                        Log.d("FIREBASE", "Usuário criado e dados salvos");


                                        Intent intent = new Intent(Register.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Erro ao criar usuário: " + task.getException(), Toast.LENGTH_LONG).show();
                            Log.e("FIREBASE", "Erro ao criar usuário", task.getException());
                        }

                    });
        }
    }


    // Função para voltar para a tela de login caso já tenha uma conta

    public void goToLogin(View v){
        Intent intent  = new Intent(Register.this, Login.class);
        startActivity(intent);
    }
}