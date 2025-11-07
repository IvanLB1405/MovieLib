package com.movielib

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.movielib.movielib.R
import com.movielib.movielib.api.ApiResponse
import com.movielib.movielib.database.MovieDatabase
import com.movielib.movielib.databinding.ActivityMovieDetailBinding
import com.movielib.movielib.models.Movie
import com.movielib.movielib.repository.MovieRepository
import com.movielib.movielib.utils.Constants
import kotlinx.coroutines.launch

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var repository: MovieRepository
    private var currentMovie: Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRepository()
        setupToolbar()
        setupButtons()

        val movieId = intent.getIntExtra(SearchActivity.EXTRA_MOVIE_ID, -1)
        if (movieId != -1) {
            loadMovieDetails(movieId)
        } else {
            finish()
        }
    }

    private fun setupRepository() {
        val database = MovieDatabase.getDatabase(this)
        repository = MovieRepository(database.movieDao(), Constants.TMDB_API_KEY)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        binding.favoriteButton.setOnClickListener {
            currentMovie?.let { movie ->
                toggleFavorite(movie)
            }
        }

        binding.favoriteIcon.setOnClickListener {
            currentMovie?.let { movie ->
                toggleFavorite(movie)
            }
        }

        binding.rateButton.setOnClickListener {
            currentMovie?.let { movie ->
                showRatingDialog(movie)
            }
        }
    }

    private fun loadMovieDetails(movieId: Int) {
        showLoading()

        lifecycleScope.launch {
            repository.getMovieDetails(movieId).collect { response ->
                when (response) {
                    is ApiResponse.Loading -> {
                        showLoading()
                    }
                    is ApiResponse.Success -> {
                        currentMovie = response.data
                        displayMovieDetails(response.data)
                        hideLoading()
                    }
                    is ApiResponse.Error -> {
                        hideLoading()
                        showError("Error loading movie details: ${response.message}")
                    }
                    is ApiResponse.NetworkError -> {
                        hideLoading()
                        showError("Network error. Please check your connection.")
                    }
                }
            }
        }
    }

    private fun displayMovieDetails(movie: Movie) {
        // Set title
        binding.titleTextView.text = movie.title
        binding.collapsingToolbar.title = movie.title

        // Set rating
        binding.ratingTextView.text = String.format("%.1f", movie.voteAverage)

        // Set release date (just year)
        movie.releaseDate?.let { date ->
            val year = date.substringBefore("-")
            binding.releaseDateTextView.text = year
        }

        // Set genres
        movie.genres?.let { genres ->
            binding.genresTextView.text = genres
        } ?: run {
            binding.genresTextView.visibility = View.GONE
        }

        // Set overview
        binding.overviewTextView.text = movie.overview ?: "No overview available."

        // Load backdrop image
        val backdropUrl = Constants.buildPosterUrl(
            movie.posterPath,
            Constants.IMAGE_SIZE_W780
        )
        Glide.with(this)
            .load(backdropUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder_movie)
            .error(R.drawable.placeholder_movie)
            .into(binding.backdropImageView)

        // Load poster image
        val posterUrl = Constants.buildPosterUrl(
            movie.posterPath,
            Constants.IMAGE_SIZE_W342
        )
        Glide.with(this)
            .load(posterUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder_movie)
            .error(R.drawable.placeholder_movie)
            .into(binding.posterImageView)

        // Update favorite status
        updateFavoriteUI(movie.isInLibrary)

        // Show user rating if exists
        movie.userRating?.let { rating ->
            binding.rateButton.text = "★ Puntuado: ${String.format("%.1f", rating)}/10"
        }
    }

    private fun toggleFavorite(movie: Movie) {
        lifecycleScope.launch {
            if (movie.isInLibrary) {
                // Remove from library
                repository.removeFromLibrary(movie.id)
                currentMovie = movie.copy(
                    isInLibrary = false,
                    userRating = null,
                    userReview = null
                )
                updateFavoriteUI(false)
                showSnackbar("Removed from your library")
            } else {
                // Add to library
                repository.addToLibrary(movie.id)
                currentMovie = movie.copy(isInLibrary = true)
                updateFavoriteUI(true)
                showSnackbar("Added to your library")
            }
        }
    }

    private fun updateFavoriteUI(isInLibrary: Boolean) {
        if (isInLibrary) {
            binding.favoriteButton.text = "✓ In My List"
            binding.favoriteButton.strokeWidth = 0
            binding.favoriteIcon.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            binding.favoriteButton.text = "+ My List"
            binding.favoriteButton.strokeWidth = 2
            binding.favoriteIcon.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    private fun showRatingDialog(movie: Movie) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rating_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val reviewEditText = dialogView.findViewById<EditText>(R.id.reviewEditText)

        // Set existing rating and review if available
        movie.userRating?.let { rating ->
            ratingBar.rating = rating / 2f // RatingBar is 0-5, userRating is 0-10
        }
        reviewEditText.setText(movie.userReview ?: "")

        AlertDialog.Builder(this)
            .setTitle("Rate & Review")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val rating = ratingBar.rating * 2f // Convert back to 0-10 scale
                val review = reviewEditText.text.toString().trim()

                saveRatingAndReview(movie, rating, review)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveRatingAndReview(movie: Movie, rating: Float, review: String) {
        lifecycleScope.launch {
            // Make sure movie is in library first
            if (!movie.isInLibrary) {
                repository.addToLibrary(movie.id)
            }

            // Update rating
            if (rating > 0) {
                repository.updateUserRating(movie.id, rating)
            }

            // Update review
            if (review.isNotEmpty()) {
                repository.updateUserReview(movie.id, review)
            }

            // Update current movie
            currentMovie = movie.copy(
                isInLibrary = true,
                userRating = if (rating > 0) rating else movie.userRating,
                userReview = if (review.isNotEmpty()) review else movie.userReview
            )

            // Update UI
            updateFavoriteUI(true)
            binding.rateButton.text = "★ Puntuado: ${String.format("%.1f", rating)}/10"

            showSnackbar("Puntuación y reseña guardadas")
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String) {
        showSnackbar(message)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
