package com.videoclub.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.videoclub.R;
import com.videoclub.models.TVCategory;

import java.util.List;

public class TVCategoryAdapter extends RecyclerView.Adapter<TVCategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<TVCategory> categoryList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TVCategory category);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TVCategoryAdapter(Context context, List<TVCategory> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        TVCategory category = categoryList.get(position);

        // Load image with Glide
        Glide.with(context)
                .load(category.getImage())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image))
                .into(holder.imageView);

        // Set name if available
        if (category.getName() != null && !category.getName().isEmpty()) {
            holder.nameTextView.setText(category.getName());
            holder.nameTextView.setVisibility(View.VISIBLE);
        } else {
            holder.nameTextView.setVisibility(View.GONE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(category);
            }
        });

        // Set focus change listener for TV navigation
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Scale up or highlight the focused item
                holder.itemView.setBackgroundResource(R.drawable.focused_background);
                holder.itemView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
            } else {
                // Return to normal state
                holder.itemView.setBackgroundResource(0);
                holder.itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.category_image);
            nameTextView = itemView.findViewById(R.id.category_name);
        }
    }
}