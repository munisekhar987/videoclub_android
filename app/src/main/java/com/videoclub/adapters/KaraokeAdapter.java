package com.videoclub.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.videoclub.R;
import com.videoclub.models.KaraokeItem;

import java.util.List;

public class KaraokeAdapter extends RecyclerView.Adapter<KaraokeAdapter.KaraokeViewHolder> {

    private final Context context;
    private final List<KaraokeItem> karaokeList;
    private OnPlayClickListener playListener;
    private OnRecordClickListener recordListener;

    public interface OnPlayClickListener {
        void onPlayClick(KaraokeItem item);
    }

    public interface OnRecordClickListener {
        void onRecordClick(KaraokeItem item);
    }

    public void setOnPlayClickListener(OnPlayClickListener listener) {
        this.playListener = listener;
    }

    public void setOnRecordClickListener(OnRecordClickListener listener) {
        this.recordListener = listener;
    }

    public KaraokeAdapter(Context context, List<KaraokeItem> karaokeList) {
        this.context = context;
        this.karaokeList = karaokeList;
    }

    @NonNull
    @Override
    public KaraokeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_karaoke, parent, false);
        return new KaraokeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KaraokeViewHolder holder, int position) {
        KaraokeItem item = karaokeList.get(position);

        holder.indexTextView.setText(String.valueOf(position + 1));
        holder.nameTextView.setText(item.getVideoName());

        // Set click listeners
        holder.nameTextView.setOnClickListener(v -> {
            if (playListener != null) {
                playListener.onPlayClick(item);
            }
        });

        holder.recordButton.setOnClickListener(v -> {
            if (recordListener != null) {
                recordListener.onRecordClick(item);
            }
        });

        // Set focus change listeners for TV navigation
        holder.nameTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.nameTextView.setBackgroundResource(R.drawable.focused_background);
            } else {
                holder.nameTextView.setBackgroundResource(R.drawable.normal_background);
            }
        });

        holder.recordButton.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.recordButton.setBackgroundResource(R.drawable.focused_button);
            } else {
                holder.recordButton.setBackgroundResource(R.drawable.normal_button);
            }
        });
    }

    @Override
    public int getItemCount() {
        return karaokeList.size();
    }

    static class KaraokeViewHolder extends RecyclerView.ViewHolder {
        TextView indexTextView;
        TextView nameTextView;
        Button recordButton;

        KaraokeViewHolder(@NonNull View itemView) {
            super(itemView);
            indexTextView = itemView.findViewById(R.id.index_text);
            nameTextView = itemView.findViewById(R.id.karaoke_name);
            recordButton = itemView.findViewById(R.id.record_button);
        }
    }
}
