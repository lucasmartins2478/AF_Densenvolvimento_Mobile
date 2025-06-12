package com.example.notation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Map.Entry<Long, String>> reminderList;
    private OnReminderLongClickListener longClickListener;
    private OnReminderClickListener clickListener;

    public interface OnReminderLongClickListener {
        void onReminderLongClick(long dateInMillis, String text, int position);
    }
    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ReminderViewHolder(view);
    }

    public interface OnReminderClickListener {
        void onReminderClick(long dateInMillis, String text, int position);
    }

    public ReminderAdapter(List<Map.Entry<Long, String>> reminderList,
                           OnReminderClickListener clickListener,
                           OnReminderLongClickListener longClickListener) {
        this.reminderList = reminderList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Map.Entry<Long, String> reminder = reminderList.get(position);
        Date date = new Date(reminder.getKey());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = sdf.format(date);

        holder.text1.setText(dateStr);
        holder.text2.setText(reminder.getValue());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onReminderClick(reminder.getKey(), reminder.getValue(), position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onReminderLongClick(reminder.getKey(), reminder.getValue(), position);
                return true;
            }
            return false;
        });
    }


    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView text1;
        TextView text2;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}


