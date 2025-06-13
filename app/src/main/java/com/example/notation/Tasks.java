package com.example.notation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Tasks extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;

    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tasks);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }


        AppCompatButton btnAdd = findViewById(R.id.add_task);
        recyclerView = findViewById(R.id.recyclerViewTasks);

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadTasks();

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(Tasks.this, AddTask.class));
        });
    }

    // carrega as tarefas do firebase conforme o usuário que estiver logado

    private void loadTasks() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Log.d("FIREBASE_RAW", doc.getData().toString());
                        Task note = doc.toObject(Task.class);
                        note.setId(doc.getId());
                        Log.d("FIREBASE_NOTE", "Tarefa: " + note.getTarefa() + ", Prioridade: " + note.getPrioridade());
                        taskList.add(note);
                    }
                    Log.d("FIREBASE_LIST", "Total de tarefas carregadas: " + taskList.size());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_ERROR", "Erro ao buscar tarefas", e);
                });
    }



    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }
}