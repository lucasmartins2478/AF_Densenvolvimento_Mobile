package com.example.notation;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
        EdgeToEdge.enable(this);
        calendarView = findViewById(R.id.calendarView);
        reminderRecyclerView = findViewById(R.id.recyclerView);

        // Configura RecyclerView
        reminderList = new ArrayList<>();
        reminderAdapter = new ReminderAdapter(reminderList,
                (dateInMillis, text, position) -> {
                    showEditReminderDialog(dateInMillis, text, position);
                },
                (dateInMillis, text, position) -> {
                    showDeleteReminderDialog(dateInMillis, text, position);
                });
        reminderRecyclerView.setAdapter(reminderAdapter);

        reminderRecyclerView.setAdapter(reminderAdapter);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, com.google.android.material.R.style.Theme_Material3_DayNight_Dialog_Alert)
        );
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
    private void showEditReminderDialog(final long dateInMillis, String currentText, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, com.google.android.material.R.style.Theme_Material3_DayNight_Dialog_Alert)
        );
        builder.setTitle("Editar lembrete");

        final EditText input = new EditText(this);
        input.setText(currentText);
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!newText.isEmpty()) {
                updateReminder(dateInMillis, currentText, newText, position);
            } else {
                Toast.makeText(CalendarActivity.this, "Lembrete vazio!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void updateReminder(long dateInMillis, String oldText, String newText, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateString = convertMillisToDateString(dateInMillis);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Busca o documento com date e oldText
        db.collection("users")
                .document(user.getUid())
                .collection("reminders")
                .whereEqualTo("date", dateString)
                .whereEqualTo("text", oldText)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        doc.getReference()
                                .update("text", newText)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Lembrete atualizado", Toast.LENGTH_SHORT).show();
                                    // Atualiza localmente
                                    reminders.put(dateInMillis, newText);
                                    updateReminderList();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erro ao atualizar lembrete", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Lembrete não encontrado para atualização", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao buscar lembrete para atualização", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteReminderDialog(long dateInMillis, String reminderText, int position) {
        new AlertDialog.Builder(
                new ContextThemeWrapper(this, com.google.android.material.R.style.Theme_Material3_DayNight_Dialog_Alert)
        )
                .setTitle("Excluir lembrete")
                .setMessage("Deseja realmente excluir o lembrete:\n\"" + reminderText + "\"?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    deleteReminder(dateInMillis, reminderText, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
    private void deleteReminder(long dateInMillis, String reminderText, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateString = convertMillisToDateString(dateInMillis);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .collection("reminders")
                .whereEqualTo("date", dateString)
                .whereEqualTo("text", reminderText)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Lembrete excluído", Toast.LENGTH_SHORT).show();
                                    reminders.remove(dateInMillis);
                                    updateReminderList();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erro ao excluir lembrete", Toast.LENGTH_SHORT).show();
                                });
                        break; // remove só o primeiro que achar (deve ser único)
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao buscar lembrete para exclusão", Toast.LENGTH_SHORT).show();
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

