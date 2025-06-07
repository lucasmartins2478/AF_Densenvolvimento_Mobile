package com.example.notation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddNote extends AppCompatActivity {
    private EditText etTarefa, etDescricao;
    private Spinner spPrioridade;
    private CheckBox cbConcluida;
    private AppCompatButton btnSalvar;
    private String noteId = null;

    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        etTarefa = findViewById(R.id.etTarefa);
        etDescricao = findViewById(R.id.etDescricao);
        spPrioridade = findViewById(R.id.spPrioridade);
        cbConcluida = findViewById(R.id.cbConcluida);
        btnSalvar = findViewById(R.id.btnSalvar);

        db = FirebaseFirestore.getInstance();

        // Preenche o Spinner
        String[] prioridades = {"Alta", "Média", "Baixa"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, prioridades);
        spPrioridade.setAdapter(adapter);

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("id")) {
            noteId = intent.getStringExtra("id");
            etTarefa.setText(intent.getStringExtra("tarefa"));
            etDescricao.setText(intent.getStringExtra("descricao"));
            spPrioridade.setSelection(getSpinnerIndex(intent.getStringExtra("prioridade")));
            cbConcluida.setChecked(intent.getBooleanExtra("concluida", false));
        }


        boolean isEdicao = getIntent().hasExtra("tarefa");

        btnSalvar.setOnClickListener(v -> {
            if (isEdicao) {
                atualizarNota();
            } else {
                salvarNota();
            }
        });
    }

    private void salvarNota() {
        String tarefa = etTarefa.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();
        String prioridade = spPrioridade.getSelectedItem().toString();
        boolean concluida = cbConcluida.isChecked();

        if (tarefa.isEmpty()) {
            etTarefa.setError("Digite a tarefa");
            return;
        }

        Map<String, Object> nota = new HashMap<>();
        nota.put("tarefa", tarefa);
        nota.put("descricao", descricao);
        nota.put("prioridade", prioridade);
        nota.put("concluida", concluida);

        db.collection("notes")
                .add(nota)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Nota salva!", Toast.LENGTH_SHORT).show();
                    finish(); // Volta pra tela anterior
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void atualizarNota(){

        String tarefa = etTarefa.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();
        String prioridade = spPrioridade.getSelectedItem().toString();
        boolean concluida = cbConcluida.isChecked();

        if (tarefa.isEmpty()) {
            etTarefa.setError("Digite a tarefa");
            return;
        }

        Map<String, Object> nota = new HashMap<>();
        nota.put("tarefa", tarefa);
        nota.put("descricao", descricao);
        nota.put("prioridade", prioridade);
        nota.put("concluida", concluida);

        db.collection("notes").document(noteId)
                .set(nota)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Nota atualizada com sucesso", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao atualizar nota", Toast.LENGTH_SHORT).show();
                });
    }

    private int getSpinnerIndex(String prioridade) {
        for (int i = 0; i < spPrioridade.getCount(); i++) {
            if (spPrioridade.getItemAtPosition(i).toString().equalsIgnoreCase(prioridade)) {
                return i;
            }
        }
        return 0;
    }

}