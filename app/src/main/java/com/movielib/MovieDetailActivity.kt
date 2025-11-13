package com.movielib

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.movielib.base.BaseMovieActivity
import com.movielib.extensions.handle
import com.movielib.movielib.R
import com.movielib.movielib.databinding.ActivityMovieDetailBinding
import com.movielib.movielib.models.Movie
import com.movielib.movielib.utils.Constants
import kotlinx.coroutines.launch

/**
 * Pantalla de detalles de película que muestra información completa sobre una película específica
 *
 * Características:
 * - Detalles completos de la película
 * - Imágenes de fondo y póster
 * - Añadir/eliminar de la biblioteca personal
 * - Valorar y reseñar películas (con diálogo de RatingBar)
 * - Preserva los datos del usuario al actualizar desde la API
 *
 * Recibe el ID de la película mediante Intent extra: SearchActivity.EXTRA_MOVIE_ID
 *
 * @see BaseMovieActivity
 */
class MovieDetailActivity : BaseMovieActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private var currentMovie: Movie? = null

    companion object {
        private const val RATING_BAR_MAX = 5f
        private const val TMDB_RATING_MAX = 10f
        private const val RATING_SCALE_FACTOR = TMDB_RATING_MAX / RATING_BAR_MAX
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupButtons()

        val movieId = intent.getIntExtra(SearchActivity.EXTRA_MOVIE_ID, -1)
        if (movieId != -1) {
            loadMovieDetails(movieId)
        } else {
            finish()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        val favoriteClickListener = View.OnClickListener {
            currentMovie?.let { movie -> toggleFavorite(movie) }
        }

        // Tanto el botón como el icono realizan la misma acción (principio DRY, no repetirse!!)
        binding.favoriteButton.setOnClickListener(favoriteClickListener)
        binding.favoriteIcon.setOnClickListener(favoriteClickListener)

        binding.rateButton.setOnClickListener {
            currentMovie?.let { movie -> showRatingDialog(movie) }
        }
    }

    private fun loadMovieDetails(movieId: Int) {
        showLoading()

        lifecycleScope.launch {
            repository.getMovieDetails(movieId).collect { response ->
                response.handle(
                    onLoading = { showLoading() },
                    onSuccess = { movie ->
                        currentMovie = movie
                        displayMovieDetails(movie)
                        hideLoading()
                    },
                    onError = { message, _ ->
                        hideLoading()
                        showError(getString(R.string.error_loading_movie_details, message))
                    },
                    onNetworkError = {
                        hideLoading()
                        showError(getString(R.string.error_network))
                    }
                )
            }
        }
    }

    private fun displayMovieDetails(movie: Movie) {
        binding.titleTextView.text = movie.title
        binding.collapsingToolbar.title = movie.title

        binding.ratingTextView.text = String.format("%.1f", movie.voteAverage)

        movie.releaseDate?.substringBefore("-")?.let { year ->
            binding.releaseDateTextView.text = year
        }

        setTextOrHide(
            text = movie.genres,
            textView = binding.genresTextView
        )

        binding.overviewTextView.text = movie.overview ?: getString(R.string.no_overview)

        setCastOrHide(movie.cast)

        loadMovieImages(movie.posterPath)

        updateFavoriteUI(movie.isInLibrary)

        movie.userRating?.let { rating ->
            binding.rateButton.text = getString(R.string.rated_label, rating)
        }
    }

    /**
     * Función auxiliar para establecer texto u ocultar vista si es nulo/vacío
     */
    private fun setTextOrHide(text: String?, textView: android.widget.TextView) {
        if (!text.isNullOrEmpty()) {
            textView.text = text
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
    }

    private fun setCastOrHide(cast: String?) {
        if (!cast.isNullOrEmpty()) {
            binding.castSection.visibility = View.VISIBLE
            binding.castTextView.text = cast.replace(",", " • ")
        } else {
            binding.castSection.visibility = View.GONE
        }
    }

    /**
     * Función auxiliar para cargar imágenes de fondo y póster (principio DRY)
     */
    private fun loadMovieImages(posterPath: String?) {
        loadImageIntoView(
            posterPath,
            Constants.IMAGE_SIZE_W780,
            binding.backdropImageView
        )

        loadImageIntoView(
            posterPath,
            Constants.IMAGE_SIZE_W342,
            binding.posterImageView
        )
    }

    private fun loadImageIntoView(
        posterPath: String?,
        imageSize: String,
        imageView: android.widget.ImageView
    ) {
        val imageUrl = Constants.buildPosterUrl(posterPath, imageSize)
        Glide.with(this)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder_movie)
            .error(R.drawable.placeholder_movie)
            .into(imageView)
    }

    private fun toggleFavorite(movie: Movie) {
        lifecycleScope.launch {
            if (movie.isInLibrary) {
                repository.removeFromLibrary(movie.id)
                currentMovie = movie.copy(
                    isInLibrary = false,
                    userRating = null,
                    userReview = null
                )
                updateFavoriteUI(false)
                showSnackbar(getString(R.string.removed_from_library))
            } else {
                repository.addToLibrary(movie.id)
                currentMovie = movie.copy(isInLibrary = true)
                updateFavoriteUI(true)
                showSnackbar(getString(R.string.added_to_library))
            }
        }
    }

    private fun updateFavoriteUI(isInLibrary: Boolean) {
        if (isInLibrary) {
            binding.favoriteButton.apply {
                text = getString(R.string.remove_from_list)
                strokeWidth = 0
            }
            binding.favoriteIcon.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            binding.favoriteButton.apply {
                text = getString(R.string.add_to_list)
                strokeWidth = 2
            }
            binding.favoriteIcon.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    private fun showRatingDialog(movie: Movie) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rating_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val reviewEditText = dialogView.findViewById<EditText>(R.id.reviewEditText)

        movie.userRating?.let { rating ->
            ratingBar.rating = rating / RATING_SCALE_FACTOR
        }
        reviewEditText.setText(movie.userReview ?: "")

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.rate_review_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val rating = ratingBar.rating * RATING_SCALE_FACTOR
                val review = reviewEditText.text.toString().trim()
                saveRatingAndReview(movie, rating, review)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun saveRatingAndReview(movie: Movie, rating: Float, review: String) {
        lifecycleScope.launch {
            if (!movie.isInLibrary) {
                repository.addToLibrary(movie.id)
            }

            if (rating > 0) {
                repository.updateUserRating(movie.id, rating)
            }

            if (review.isNotEmpty()) {
                repository.updateUserReview(movie.id, review)
            }

            currentMovie = movie.copy(
                isInLibrary = true,
                userRating = if (rating > 0) rating else movie.userRating,
                userReview = if (review.isNotEmpty()) review else movie.userReview
            )

            updateFavoriteUI(true)
            binding.rateButton.text = getString(R.string.rated_label, rating)

            showSnackbar(getString(R.string.rating_review_saved))
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
