package com.videoclub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.videoclub.R;
import com.videoclub.models.MenuItem;

import java.util.List;

/**
 * Adapter for left menu in NewHomeScreenActivity
 */
public class LeftMenuAdapter extends RecyclerView.Adapter<LeftMenuAdapter.ViewHolder> {
    private final Context context;
    private final List<MenuItem> menuItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MenuItem item);
    }

    public LeftMenuAdapter(Context context, List<MenuItem> menuItems) {
        this.context = context;
        this.menuItems = menuItems;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_left_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem menuItem = menuItems.get(position);

        // Load image
        String imageUrl = "http://pmedia.msgnaa.info:8082/images/apk/xxxhdpi/" + menuItem.getImage();
        Glide.with(context)
                .load(imageUrl)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.imageView);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(menuItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.menu_image);
        }
    }
}