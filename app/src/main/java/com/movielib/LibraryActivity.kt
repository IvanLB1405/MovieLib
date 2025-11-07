package com.movielib

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.movielib.adapters.MovieAdapter
import com.movielib.movielib.database.MovieDatabase
import com.movielib.movielib.databinding.ActivityLibraryBinding
import com.movielib.movielib.repository.MovieRepository
import com.movielib.movielib.utils.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLibraryBinding
    private lateinit var repository: MovieRepository
    private lateinit var adapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRepository()
        setupToolbar()
        setupRecyclerView()
        loadLibrary()
    }

    override fun onResume() {
        super.onResume()
        // Reload library when returning from detail
        loadLibrary()
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

    private fun setupRecyclerView() {
        adapter = MovieAdapter(MovieAdapter.LayoutType.GRID) { movie ->
            navigateToMovieDetail(movie.id)
        }

        binding.libraryRecyclerView.apply {
            layoutManager = GridLayoutManager(this@LibraryActivity, 3)
            adapter = this@LibraryActivity.adapter
        }
    }

    private fun loadLibrary() {
        showLoading()

        lifecycleScope.launch {
            // Load library stats
            val stats = repository.getLibraryStats()
            displayStats(stats)

            // Load library movies
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

    private fun displayStats(stats: com.movielib.movielib.repository.LibraryStats) {
        binding.totalMoviesText.text = stats.totalMovies.toString()
        binding.averageRatingText.text = String.format("%.1f", stats.averageRating)
        binding.reviewedMoviesText.text = stats.moviesWithReviews.toString()
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.libraryRecyclerView.visibility = View.GONE
    }

    private fun showMovies(movies: List<com.movielib.movielib.models.Movie>) {
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
