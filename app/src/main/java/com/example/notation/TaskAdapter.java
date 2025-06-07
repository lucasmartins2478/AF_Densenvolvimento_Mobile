package com.example.notation;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        // Clique normal para editar
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddTask.class);
            intent.putExtra("id", task.getId());
            intent.putExtra("tarefa", task.getTarefa());
            intent.putExtra("descricao", task.getDescricao());
            intent.putExtra("prioridade", task.getPrioridade());
            intent.putExtra("concluida", task.isConcluida());
            context.startActivity(intent);
        });

        // Long click para excluir
        holder.itemView.setOnLongClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Excluir tarefa")
                    .setMessage("Tem certeza que deseja apagar essa tarefa?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        excluirTarefa(task.getId(), context, position);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        });
    }

    private void excluirTarefa(String id, Context context, int position) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("tasks")
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    tasks.remove(position); // remove da lista local
                    notifyItemRemoved(position); // atualiza a RecyclerView
                    android.widget.Toast.makeText(context, "Tarefa excluída com sucesso", android.widget.Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(context, "Erro ao excluir", android.widget.Toast.LENGTH_SHORT).show();
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
