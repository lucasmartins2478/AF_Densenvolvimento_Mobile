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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddTask extends AppCompatActivity {
    private EditText etTarefa, etDescricao;
    private Spinner spPrioridade;
    private CheckBox cbConcluida;
    private AppCompatButton btnSalvar;
    private String taskId = null;

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

        // Captura os campos de dados da tela

        etTarefa = findViewById(R.id.etTarefa);
        etDescricao = findViewById(R.id.etDescricao);
        spPrioridade = findViewById(R.id.spPrioridade);
        cbConcluida = findViewById(R.id.cbConcluida);
        btnSalvar = findViewById(R.id.btnSalvar);
        db = FirebaseFirestore.getInstance();

        // Define os valores do spinner de prioridade da tarefa

        String[] prioridades = {"Alta", "Média", "Baixa"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, prioridades);
        spPrioridade.setAdapter(adapter);

        Intent intent = getIntent();

        // Verifica se uma tarefa foi enviada na intent
        // se sim, os dados devem ir para os campos,se não
        // faz a função de cadastro normal com os campos vazios

        if (intent != null && intent.hasExtra("id")) {
            taskId = intent.getStringExtra("id");
            etTarefa.setText(intent.getStringExtra("tarefa"));
            etDescricao.setText(intent.getStringExtra("descricao"));
            spPrioridade.setSelection(getSpinnerIndex(intent.getStringExtra("prioridade")));
            cbConcluida.setChecked(intent.getBooleanExtra("concluida", false));
        }


        boolean isEdicao = getIntent().hasExtra("tarefa");

        btnSalvar.setOnClickListener(v -> {
            if (isEdicao) {
                atualizarTarefa();
            } else {
                salvarTarefa();
            }
        });
    }

    // FUnção usada para salvar as tarefas no firebase, ela salva as tarefas dentro do
    // próprio usuário, ou seja, sempre as tarefas que forem adicionadas já vão estar
    // relacionadas a um usuário específico, que torna mais fácil mostrar na tela de tarefas

    private void salvarTarefa() {

        // Verifica se o usuário ta autenticado e logado
        // se não estiver, retorna

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String tarefa = etTarefa.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();
        String prioridade = spPrioridade.getSelectedItem().toString();
        boolean concluida = cbConcluida.isChecked();

        if (tarefa.isEmpty()) {
            etTarefa.setError("Digite a tarefa");
            return;
        }

        Map<String, Object> task = new HashMap<>();
        task.put("tarefa", tarefa);
        task.put("descricao", descricao);
        task.put("prioridade", prioridade);
        task.put("concluida", concluida);
        db.collection("users").document(user.getUid()).collection("tasks").add(task)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Tarefa salva!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Função de atualizar a tarefa, pega os dados que foram adicionados e envia
    // para o firebase no mesmo caminho usado para criar as tarefas e altera ela

    private void atualizarTarefa(){

        // Verifica se o usuário ta autenticado e logado
        // se não estiver, retorna

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String tarefa = etTarefa.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();
        String prioridade = spPrioridade.getSelectedItem().toString();
        boolean concluida = cbConcluida.isChecked();

        if (tarefa.isEmpty()) {
            etTarefa.setError("Digite a tarefa");
            return;
        }

        Map<String, Object> task = new HashMap<>();
        task.put("tarefa", tarefa);
        task.put("descricao", descricao);
        task.put("prioridade", prioridade);
        task.put("concluida", concluida);

        db.collection("users").document(user.getUid()).collection("tasks").document(taskId).set(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tarefa atualizada com sucesso", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao atualizar tarefa", Toast.LENGTH_SHORT).show();
                });
    }


    // Função de pegar o item que está selecionado no spinner

    private int getSpinnerIndex(String prioridade) {
        for (int i = 0; i < spPrioridade.getCount(); i++) {
            if (spPrioridade.getItemAtPosition(i).toString().equalsIgnoreCase(prioridade)) {
                return i;
            }
        }
        return 0;
    }

}