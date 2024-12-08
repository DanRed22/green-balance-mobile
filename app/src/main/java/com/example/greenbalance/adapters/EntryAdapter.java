package com.example.greenbalance.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenbalance.R;
import com.example.greenbalance.model.EntryModel;

import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EMPTY = 0;
    private static final int TYPE_ENTRY = 1;
    private List<EntryModel> entriesList;
    private OnEntryClickListener listener;

    public interface OnEntryClickListener {
        void onEntryClick(EntryModel entry);
    }

    public EntryAdapter(List<EntryModel> entriesList, OnEntryClickListener listener) {
        this.entriesList = entriesList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (entriesList.isEmpty()) {
            return TYPE_EMPTY;
        }
        return TYPE_ENTRY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_EMPTY) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.empty_entry_item, parent, false);
            return new EmptyViewHolder(view);
        }
        
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry_item, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmptyViewHolder) {
            // Nothing to bind for empty view
            return;
        }

        EntryViewHolder entryHolder = (EntryViewHolder) holder;
        EntryModel entry = entriesList.get(position);
        entryHolder.titleTextView.setText(entry.getTitle());
        
        entryHolder.dateTextView.setText(entry.getCreatedAt());
        
        float value = entry.getValue();
        if (value < 0) {
            entryHolder.expenseTextView.setText(String.format("%.2f", Math.abs(value)));
            entryHolder.gainedTextView.setText("-");
        } else {
            entryHolder.expenseTextView.setText("-");
            entryHolder.gainedTextView.setText(String.format("%.2f", value));
        }

        entryHolder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEntryClick(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entriesList.isEmpty() ? 1 : entriesList.size();
    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView dateTextView;
        TextView expenseTextView;
        TextView gainedTextView;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.entry_title);
            dateTextView = itemView.findViewById(R.id.entry_date);
            expenseTextView = itemView.findViewById(R.id.entry_expense);
            gainedTextView = itemView.findViewById(R.id.entry_gained);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
} 