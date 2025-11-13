package com.movielib

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.movielib.adapters.MovieAdapter
import com.movielib.adapters.MovieReviewAdapter
import com.movielib.base.BaseMovieActivity
import com.movielib.movielib.databinding.ActivityLibraryBinding
import com.movielib.movielib.models.Movie
import com.movielib.movielib.repository.LibraryStats
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Pantalla de biblioteca que muestra la colección personal de películas del usuario
 *
 * Características:
 * - Sección de estadísticas (total de películas, valoración promedio, total de reseñas)
 * - Vista en cuadrícula de todas las películas en la biblioteca
 * - Sección separada para películas con reseñas
 * - Estado vacío cuando la biblioteca está vacía
 * - Actualización automática al volver desde la pantalla de detalles (onResume)
 *
 * @see BaseMovieActivity
 * @see LibraryStats
 */
class LibraryActivity : BaseMovieActivity() {

    private lateinit var binding: ActivityLibraryBinding
    private lateinit var adapter: MovieAdapter
    private lateinit var reviewsAdapter: MovieReviewAdapter

    companion object {
        private const val GRID_COLUMN_COUNT = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerViews()
        loadLibrary()
    }

    override fun onResume() {
        super.onResume()
        loadLibrary()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerViews() {
        adapter = MovieAdapter(MovieAdapter.LayoutType.GRID) { movie ->
            navigateToMovieDetail(movie.id)
        }

        binding.libraryRecyclerView.apply {
            layoutManager = GridLayoutManager(this@LibraryActivity, GRID_COLUMN_COUNT)
            adapter = this@LibraryActivity.adapter
        }

        reviewsAdapter = MovieReviewAdapter { movie ->
            navigateToMovieDetail(movie.id)
        }

        binding.reviewsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LibraryActivity)
            adapter = this@LibraryActivity.reviewsAdapter
        }
    }

    private fun loadLibrary() {
        showLoading()

        lifecycleScope.launch {
            val stats = repository.getLibraryStats()
            displayStats(stats)

            loadReviews()

            repository.getLibraryMoviesFlow().collectLatest { movies ->
                hideLoading()

                if (movies.isEmpty()) {
                    showEmptyState()
                } else {
                    showMovies(movies)
                }
            }
        }
    }

    private fun loadReviews() {
        lifecycleScope.launch {
            val reviewedMovies = repository.getMoviesWithReviews()

            binding.reviewsSection.visibility = if (reviewedMovies.isNotEmpty()) {
                binding.reviewsCountText.text = reviewedMovies.size.toString()
                reviewsAdapter.submitList(reviewedMovies)
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun displayStats(stats: LibraryStats) {
        binding.totalMoviesText.text = stats.totalMovies.toString()
        binding.averageRatingText.text = String.format("%.1f", stats.averageRating)
        binding.reviewedMoviesText.text = stats.moviesWithReviews.toString()
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.libraryRecyclerView.visibility = View.GONE
    }

    private fun showMovies(movies: List<Movie>) {
        binding.emptyStateLayout.visibility = View.GONE
        binding.libraryRecyclerView.visibility = View.VISIBLE
        adapter.submitList(movies)
    }

    private fun navigateToMovieDetail(movieId: Int) {
        val intent = Intent(this, MovieDetailActivity::class.java).apply {
            putExtra(SearchActivity.EXTRA_MOVIE_ID, movieId)
        }
        startActivity(intent)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }
}
