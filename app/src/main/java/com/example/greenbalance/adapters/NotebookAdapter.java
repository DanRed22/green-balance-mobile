package com.example.greenbalance.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenbalance.R;
import com.example.greenbalance.model.NotebookItemModel;

import java.util.List;

public class NotebookAdapter extends RecyclerView.Adapter<NotebookAdapter.ViewHolder> {

    private List<NotebookItemModel> notebookList;
    private OnNotebookClickListener listener;

    public interface OnNotebookClickListener {
        void onNotebookClick(NotebookItemModel notebook);
    }

    public NotebookAdapter(List<NotebookItemModel> notebookList, OnNotebookClickListener listener) {
        this.notebookList = notebookList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notebook_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotebookItemModel notebook = notebookList.get(position);
        
        if (notebook.getId().equals("add_new")) {
            // Show plus icon and different styling for "Add New" card
            holder.titleTextView.setText("+");
            if (holder.descriptionTextView != null) {
                holder.descriptionTextView.setText("Add New Notebook");
            }
        } else {
            // Regular notebook binding
            holder.titleTextView.setText(notebook.getTitle());
            if (holder.descriptionTextView != null) {
                holder.descriptionTextView.setText(notebook.getDescription());
            }
        }

        // Set click listener on the whole item view
        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onNotebookClick(notebook);
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.imageButton.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return notebookList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        ImageButton imageButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notebook_title);
            imageButton = itemView.findViewById(R.id.notebookIcon);
            // Either fix the ID to match your layout file or remove if not needed
            // descriptionTextView = itemView.findViewById(R.id.notebook_description);
        }
    }
}