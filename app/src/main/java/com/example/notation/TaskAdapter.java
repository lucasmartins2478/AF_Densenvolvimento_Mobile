package com.example.notation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvTarefa.setText(task.getTarefa());
        holder.tvDescricao.setText(task.getDescricao());
        holder.tvPrioridade.setText("Prioridade: " + task.getPrioridade());
        holder.tvConcluida.setText("Status: " + (task.isConcluida() ? "Concluída" : "Pendente"));

        Context context = holder.itemView.getContext();

        // Define que o clique curto vai abrir a tela de adicionar
        // tarefa mas enviando a tarefa clicada na intent
        // para que o app entenda que vai ser uma atualização

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddTask.class);
            intent.putExtra("id", task.getId());
            intent.putExtra("tarefa", task.getTarefa());
            intent.putExtra("descricao", task.getDescricao());
            intent.putExtra("prioridade", task.getPrioridade());
            intent.putExtra("concluida", task.isConcluida());
            context.startActivity(intent);
        });

        // Define que quando o card de tarefa tiver um clique
        // longo, vai abrir o Menu de exluir tarefa

        holder.itemView.setOnLongClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(
                    new ContextThemeWrapper(context, com.google.android.material.R.style.Theme_Material3_DayNight_Dialog_Alert))
                    .setTitle("Excluir tarefa")
                    .setMessage("Tem certeza que deseja apagar essa tarefa?")
                    .setPositiveButton("Sim", (d, w) -> excluirTarefa(task.getId(), context, position))
                    .setNegativeButton("Cancelar", null)
                    .create();

            dialog.setOnShowListener(d -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
            });

            dialog.show();


            return true;
        });

    }

    // Função de remover a tarefa do firebase

    private void excluirTarefa(String id, Context context, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("tasks")
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    tasks.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Tarefa excluída com sucesso", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Erro ao excluir", Toast.LENGTH_SHORT).show();
                });
    }



    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTarefa, tvDescricao, tvPrioridade, tvConcluida;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTarefa = itemView.findViewById(R.id.tvTarefa);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            tvPrioridade = itemView.findViewById(R.id.tvPrioridade);
            tvConcluida = itemView.findViewById(R.id.tvConcluida);
        }
    }
}
