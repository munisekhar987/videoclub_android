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
import com.videoclub.models.Region;

import java.util.List;

public class RegionAdapter extends RecyclerView.Adapter<RegionAdapter.RegionViewHolder> {

    private final Context context;
    private final List<Region> regions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Region region);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public RegionAdapter(Context context, List<Region> regions) {
        this.context = context;
        this.regions = regions;
    }

    @NonNull
    @Override
    public RegionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_region, parent, false);
        return new RegionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegionViewHolder holder, int position) {
        Region region = regions.get(position);

        holder.nameTextView.setText(region.getName());

        // Load image with Glide
        Glide.with(context)
                .load(region.getImageUrl())
                .apply(new RequestOptions()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_dialog_alert))
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(region);
            }
        });

        // Handle focus change for TV navigation
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.itemView.setBackgroundResource(R.drawable.focused_background);
                holder.itemView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
            } else {
                holder.itemView.setBackgroundResource(android.R.color.transparent);
                holder.itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return regions.size();
    }

    static class RegionViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;

        RegionViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.region_image);
            nameTextView = itemView.findViewById(R.id.region_name);
        }
    }
}