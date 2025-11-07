package com.movielib.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.movielib.movielib.R
import com.movielib.movielib.models.Movie
import com.movielib.movielib.utils.Constants

/**
 * Adapter for displaying movies in RecyclerView with support for different layouts
 */
class MovieAdapter(
    private val layoutType: LayoutType,
    private val onMovieClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    enum class LayoutType {
        HORIZONTAL,  // For item_movie_horizontal.xml
        GRID         // For item_movie_grid.xml
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val layoutId = when (layoutType) {
            LayoutType.HORIZONTAL -> R.layout.item_movie_horizontal
            LayoutType.GRID -> R.layout.item_movie_grid
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position)
        holder.bind(movie, onMovieClick)
    }

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moviePoster: ImageView = itemView.findViewById(R.id.moviePoster)
        private val movieTitle: TextView = itemView.findViewById(R.id.movieTitle)
        private val movieRating: TextView = itemView.findViewById(R.id.movieRating)
        private val favoriteIndicator: ImageView = itemView.findViewById(R.id.favoriteIndicator)

        fun bind(movie: Movie, onMovieClick: (Movie) -> Unit) {
            // Set title
            movieTitle.text = movie.title

            // Set rating
            movieRating.text = String.format("%.1f", movie.voteAverage)

            // Load poster image
            val posterUrl = Constants.buildPosterUrl(
                movie.posterPath,
                Constants.IMAGE_SIZE_W342
            )

            Glide.with(itemView.context)
                .load(posterUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .into(moviePoster)

            // Show/hide favorite indicator
            favoriteIndicator.visibility = if (movie.isInLibrary) View.VISIBLE else View.GONE

            // Set click listener
            itemView.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}
