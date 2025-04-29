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
import com.videoclub.models.Channel;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    private final Context context;
    private final List<Channel> channelList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Channel channel);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ChannelAdapter(Context context, List<Channel> channelList) {
        this.context = context;
        this.channelList = channelList;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        Channel channel = channelList.get(position);

        // Load image with Glide
        if (channel.getImageUrl().startsWith("res://")) {
            // It's a resource ID
            int resourceId = Integer.parseInt(channel.getImageUrl().substring(6));
            holder.imageView.setImageResource(resourceId);
        } else {
            Glide.with(context)
                    .load(channel.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image))
                    .into(holder.imageView);
        }

        // Set upload account if available
        if (channel.getUploadAccount() != null && !channel.getUploadAccount().isEmpty()) {
            holder.uploadAccountTextView.setText(channel.getUploadAccount());
            holder.uploadAccountTextView.setVisibility(View.VISIBLE);
        } else {
            holder.uploadAccountTextView.setVisibility(View.GONE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(channel);
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
        return channelList.size();
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView uploadAccountTextView;

        ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.channel_image);
            uploadAccountTextView = itemView.findViewById(R.id.upload_account_text);
        }
    }
}
