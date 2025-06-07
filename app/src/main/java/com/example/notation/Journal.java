package com.example.notation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Journal extends AppCompatActivity {

    private EditText editTextNote;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private DocumentReference noteRef;

    private boolean isLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextNote = findViewById(R.id.editTextNote);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Usuário não está logado!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        noteRef = db.collection("users").document(user.getUid()).collection("notas").document("bloco");

        carregarNota();

        editTextNote.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isLoading) {
                    salvarNota(s.toString());
                }
            }
        });
    }

    private void carregarNota() {
        noteRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String texto = documentSnapshot.getString("texto");
                        isLoading = true;
                        editTextNote.setText(texto);
                        isLoading = false;
                    } else {
                        Log.d("FIRESTORE", "Nota ainda não criada.");
                        isLoading = false;
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar nota", Toast.LENGTH_SHORT).show();
                    isLoading = false;
                });
    }

    private void salvarNota(String texto) {
        noteRef.set(new Note(texto))
                .addOnFailureListener(e -> Log.e("FIRESTORE", "Erro ao salvar nota", e));
    }


}

