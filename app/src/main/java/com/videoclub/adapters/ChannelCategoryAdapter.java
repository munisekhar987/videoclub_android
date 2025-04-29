package com.videoclub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.videoclub.R;
import com.videoclub.models.ChannelCategory;

import java.util.List;

public class ChannelCategoryAdapter extends RecyclerView.Adapter<ChannelCategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<ChannelCategory> categories;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ChannelCategory category);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ChannelCategoryAdapter(Context context, List<ChannelCategory> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        ChannelCategory category = categories.get(position);

        // Set category name
        holder.categoryNameText.setText(category.getName());

        // Create list of subcategories
        List<ChannelCategory> subItems = categories; // Use same list for demo; you'd typically use a sublist here

        // Set up grid for subcategories
        ChannelSubItemAdapter subItemAdapter = new ChannelSubItemAdapter(context, subItems);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false);
        holder.subItemsRecyclerView.setLayoutManager(layoutManager);
        holder.subItemsRecyclerView.setAdapter(subItemAdapter);

        // Set click listener for sub-items
        subItemAdapter.setOnItemClickListener(item -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        // Handle focus for TV navigation
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.itemView.setBackgroundResource(R.drawable.focused_background);
            } else {
                holder.itemView.setBackgroundResource(android.R.color.transparent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameText;
        RecyclerView subItemsRecyclerView;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameText = itemView.findViewById(R.id.category_name_text);
            subItemsRecyclerView = itemView.findViewById(R.id.sub_items_recycler_view);
        }
    }
}