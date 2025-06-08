package com.example.notation;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CalendarActivity extends AppCompatActivity {

    private Map<Long, String> reminders = new HashMap<>();
    private CalendarView calendarView;
    private RecyclerView reminderRecyclerView;
    private ReminderAdapter reminderAdapter;
    private List<Map.Entry<Long, String>> reminderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        reminderRecyclerView = findViewById(R.id.recyclerView);

        // Configura RecyclerView
        reminderList = new ArrayList<>();
        reminderAdapter = new ReminderAdapter(reminderList);
        reminderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reminderRecyclerView.setAdapter(reminderAdapter);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long selectedDateInMillis = calendar.getTimeInMillis();

                showAddReminderDialog(selectedDateInMillis);
            }
        });
        carregarLembretes();

        updateReminderList();
    }

    private void showAddReminderDialog(final long dateInMillis) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar lembrete");

        final EditText input = new EditText(this);
        input.setHint("Digite seu lembrete");
        builder.setView(input);

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String reminderText = input.getText().toString().trim();
                if (!reminderText.isEmpty()) {
                    // Converte a data do tipo long para string (ex: "yyyy-MM-dd")
                    String dateString = convertMillisToDateString(dateInMillis);

                    salvarLembrete(dateString, reminderText);

                    // Atualiza localmente só se quiser (ou espera carregar do Firebase)
                    reminders.put(dateInMillis, reminderText);
                    updateReminderList();

                } else {
                    Toast.makeText(CalendarActivity.this, "Lembrete vazio!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { dialog.cancel(); }
        });

        builder.show();
    }

    private void salvarLembrete(String date, String textoLembrete) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (textoLembrete.isEmpty()) {
            Toast.makeText(this, "Digite o lembrete", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> reminder = new HashMap<>();
        reminder.put("date", date);  // Pode ser string "yyyy-MM-dd" ou Timestamp
        reminder.put("text", textoLembrete);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .collection("reminders")
                .add(reminder)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Lembrete salvo!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void carregarLembretes() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .collection("reminders")
                .orderBy("date")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reminders.clear(); // Limpa os lembretes locais antes

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String dateString = doc.getString("date");
                        String text = doc.getString("text");

                        long dateInMillis = convertDateStringToMillis(dateString);
                        if (dateInMillis != -1 && text != null) {
                            reminders.put(dateInMillis, text);
                        }
                    }
                    updateReminderList();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar lembretes", Toast.LENGTH_SHORT).show();
                });
    }

    private long convertDateStringToMillis(String dateString) {
        try {
            String[] parts = dateString.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // Janeiro = 0
            int day = Integer.parseInt(parts[2]);

            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return cal.getTimeInMillis();
        } catch (Exception e) {
            return -1;
        }
    }

    private String convertMillisToDateString(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;  // Janeiro = 0
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", year, month, day);
    }





    private void updateReminderList() {
        reminderList.clear();

        // Ordena lembretes por data crescente
        List<Map.Entry<Long, String>> sortedEntries = new ArrayList<>(reminders.entrySet());
        Collections.sort(sortedEntries, new Comparator<Map.Entry<Long, String>>() {
            @Override
            public int compare(Map.Entry<Long, String> o1, Map.Entry<Long, String> o2) {
                return Long.compare(o1.getKey(), o2.getKey());
            }
        });

        reminderList.addAll(sortedEntries);
        reminderAdapter.notifyDataSetChanged();
    }
}

