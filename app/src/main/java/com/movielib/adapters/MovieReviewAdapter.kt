package com.movielib.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.movielib.movielib.R
import com.movielib.movielib.databinding.ItemMovieReviewBinding
import com.movielib.movielib.models.Movie
import com.movielib.movielib.utils.Constants

/**
 * Adapter for displaying movies with user reviews
 */
class MovieReviewAdapter(
    private val onMovieClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieReviewAdapter.ReviewViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemMovieReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReviewViewHolder(
        private val binding: ItemMovieReviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.titleTextView.text = movie.title

            // Set user rating
            movie.userRating?.let { rating ->
                binding.userRatingTextView.text = "★ ${String.format("%.1f", rating)}"
            }

            // Set review text
            binding.reviewTextView.text = movie.userReview ?: ""

            // Load poster
            val posterUrl = Constants.buildPosterUrl(
                movie.posterPath,
                Constants.IMAGE_SIZE_W342
            )

            Glide.with(binding.posterImageView.context)
                .load(posterUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .into(binding.posterImageView)

            // Set click listener
            binding.root.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    private class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}
