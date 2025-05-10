package com.videoclub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.videoclub.R;
import com.videoclub.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<Category> categories;
    private OnItemClickListener listener;
    private OnItemClickPositionListener positionListener;
    private int selectedPosition = -1;

    // Interface for legacy code (without position)
    public interface OnItemClickListener {
        void onItemClick(Category category);
    }

    // Interface for new code (with position)
    public interface OnItemClickPositionListener {
        void onItemClick(Category category, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
        this.positionListener = null;
    }

    public void setOnItemClickListener(OnItemClickPositionListener listener) {
        this.positionListener = listener;
        this.listener = null;
    }

    public CategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_side, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        holder.nameTextView.setText(category.getName());

        // Set selected state
        if (selectedPosition == position) {
            holder.itemView.setBackgroundResource(R.color.colorPrimary); // Use an existing color
            holder.nameTextView.setTextColor(context.getResources().getColor(android.R.color.white));
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
            holder.nameTextView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        holder.itemView.setOnClickListener(v -> {
            int oldSelectedPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Update old and new positions
            if (oldSelectedPosition != -1) {
                notifyItemChanged(oldSelectedPosition);
            }
            notifyItemChanged(selectedPosition);

            // Call appropriate listener
            if (listener != null) {
                listener.onItemClick(category);
            }
            if (positionListener != null) {
                positionListener.onItemClick(category, selectedPosition);
            }
        });

        // Handle focus change for TV navigation
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.itemView.setBackgroundResource(R.drawable.focused_background); // Make sure this exists
            } else {
                if (selectedPosition == position) {
                    holder.itemView.setBackgroundResource(R.color.colorPrimary); // Use an existing color
                } else {
                    holder.itemView.setBackgroundResource(android.R.color.transparent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < categories.size()) {
            int oldSelectedPosition = selectedPosition;
            selectedPosition = position;

            // Update old and new positions
            if (oldSelectedPosition != -1) {
                notifyItemChanged(oldSelectedPosition);
            }
            notifyItemChanged(selectedPosition);
        }
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.category_name);
        }
    }
}