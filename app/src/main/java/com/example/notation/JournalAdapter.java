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

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.NoteViewHolder> {

    private List<Note> notes;

    public JournalAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_journal, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvTarefa.setText(note.getTarefa());
        holder.tvDescricao.setText(note.getDescricao());
        holder.tvPrioridade.setText("Prioridade: " + note.getPrioridade());
        holder.tvConcluida.setText("Status: " + (note.isConcluida() ? "Concluída" : "Pendente"));

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, AddNote.class);
            intent.putExtra("id", note.getId());
            intent.putExtra("tarefa", note.getTarefa());
            intent.putExtra("descricao", note.getDescricao());
            intent.putExtra("prioridade", note.getPrioridade());
            intent.putExtra("concluida", note.isConcluida());
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTarefa, tvDescricao, tvPrioridade, tvConcluida;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTarefa = itemView.findViewById(R.id.tvTarefa);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            tvPrioridade = itemView.findViewById(R.id.tvPrioridade);
            tvConcluida = itemView.findViewById(R.id.tvConcluida);
        }
    }
}
