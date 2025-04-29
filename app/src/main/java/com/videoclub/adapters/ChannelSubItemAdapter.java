package com.videoclub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.videoclub.R;
import com.videoclub.models.ChannelCategory;

import java.util.List;

public class ChannelSubItemAdapter extends RecyclerView.Adapter<ChannelSubItemAdapter.SubItemViewHolder> {

    private final Context context;
    private final List<ChannelCategory> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ChannelCategory item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ChannelSubItemAdapter(Context context, List<ChannelCategory> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public SubItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel_sub_item, parent, false);
        return new SubItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubItemViewHolder holder, int position) {
        ChannelCategory item = items.get(position);

        // Set item name
        holder.nameText.setText(item.getName3() != null && !item.getName3().isEmpty() ?
                item.getName3() : "Channel " + position);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        // Handle focus for TV navigation
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.itemView.setBackgroundResource(R.drawable.focused_background);
                holder.itemView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
            } else {
                holder.itemView.setBackgroundResource(R.drawable.channel_border);
                holder.itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SubItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;

        SubItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.name_text);
        }
    }
}