package com.videoclub.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.videoclub.CineramaActivity;
import com.videoclub.R;
import com.videoclub.models.Movie;

import java.util.List;

/**
 * Adapter for the movie grid in CineramaActivity
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private final Context context;
    private List<Movie> movies;
    private OnItemClickListener listener;

    public MovieAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    public interface OnItemClickListener {
        void onItemClick(Movie movie);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateMovies(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movies.get(position);

        // Set movie title
        String title = movie.getVideoName();
        if (title.length() > 60) {
            title = title.substring(0, 60) + "...";
        }
        holder.movieTitle.setText(title);

        // Set uploader name if available
        if (!TextUtils.isEmpty(movie.getUploadAccount())) {
            holder.uploaderName.setVisibility(View.VISIBLE);
            holder.uploaderName.setText(movie.getUploadAccount());
        } else {
            holder.uploaderName.setVisibility(View.GONE);
        }

        // Load image
        if (movie.isError()) {
            // Show placeholder if there was an error loading the image
            Glide.with(context)
                    .load(R.drawable.empty_thumbnail)
                    .into(holder.movieImage);
        } else {
            // Load the cover image with error handling
            Glide.with(context)
                    .load(movie.getCoverImage())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.empty_thumbnail)
                            .error(R.drawable.empty_thumbnail)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(holder.movieImage);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(movie);
            }
        });

        // Set error listener for image loading
        holder.movieImage.setOnClickListener(v -> {
            if (!movie.isError() && listener != null) {
                listener.onItemClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView movieImage;
        TextView movieTitle;
        TextView uploaderName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            movieImage = itemView.findViewById(R.id.movie_image);
            movieTitle = itemView.findViewById(R.id.movie_title);
            uploaderName = itemView.findViewById(R.id.uploader_name);
        }
    }
}