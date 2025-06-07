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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Journal extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JournalAdapter adapter;
    private List<Note> noteList;

    private FirebaseFirestore db;
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

        AppCompatButton btnAdd = findViewById(R.id.add_note);
        recyclerView = findViewById(R.id.recyclerViewNotes);

        noteList = new ArrayList<>();
        adapter = new JournalAdapter(noteList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadNotes();

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(Journal.this, AddNote.class));
        });
    }

    private void loadNotes() {
        db.collection("notes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    noteList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Log.d("FIREBASE_RAW", doc.getData().toString()); // mostra os dados do Firebase
                        Note note = doc.toObject(Note.class);
                        note.setId(doc.getId()); // Aqui você pega o ID do documento
                        Log.d("FIREBASE_NOTE", "Tarefa: " + note.getTarefa() + ", Prioridade: " + note.getPrioridade());
                        noteList.add(note);
                    }
                    Log.d("FIREBASE_LIST", "Total de notas carregadas: " + noteList.size());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_ERROR", "Erro ao buscar notas", e);
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadNotes(); // Atualiza a lista toda vez que volta pra tela principal
    }

}