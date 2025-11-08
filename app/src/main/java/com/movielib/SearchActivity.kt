package com.movielib

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.movielib.adapters.MovieAdapter
import com.movielib.movielib.R
import com.movielib.movielib.api.ApiResponse
import com.movielib.movielib.database.MovieDatabase
import com.movielib.movielib.databinding.ActivitySearchBinding
import com.movielib.movielib.models.Movie
import com.movielib.movielib.repository.MovieRepository
import com.movielib.movielib.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var repository: MovieRepository
    private lateinit var adapter: MovieAdapter
    private var searchJob: Job? = null

    companion object {
        const val EXTRA_MOVIE_ID = "extra_movie_id"
        private const val GRID_COLUMN_COUNT = 3
        private const val SEARCH_DEBOUNCE_DELAY = 500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRepository()
        setupRecyclerView()
        setupSearchBar()
        setupBackButton()
    }

    private fun setupRepository() {
        val database = MovieDatabase.getDatabase(this)
        repository = MovieRepository(database.movieDao(), Constants.TMDB_API_KEY)
    }

    private fun setupRecyclerView() {
        adapter = MovieAdapter(MovieAdapter.LayoutType.GRID) { movie ->
            navigateToMovieDetail(movie)
        }

        binding.searchResultsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@SearchActivity, GRID_COLUMN_COUNT)
            adapter = this@SearchActivity.adapter
        }
    }

    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Cancel previous search job
                searchJob?.cancel()

                val query = s?.toString()?.trim() ?: ""

                if (query.isEmpty()) {
                    showEmptyState()
                } else {
                    // Debounce search
                    searchJob = lifecycleScope.launch {
                        delay(SEARCH_DEBOUNCE_DELAY)
                        searchMovies(query)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Set focus on search field
        binding.searchEditText.requestFocus()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun searchMovies(query: String) {
        lifecycleScope.launch {
            repository.searchMovies(query).collect { response ->
                when (response) {
                    is ApiResponse.Loading -> {
                        showLoadingState()
                    }
                    is ApiResponse.Success -> {
                        val movies = response.data
                        if (movies.isEmpty()) {
                            showNoResultsState(query)
                        } else {
                            showResultsState(movies)
                        }
                    }
                    is ApiResponse.Error -> {
                        showNoResultsState(query)
                    }
                    is ApiResponse.NetworkError -> {
                        showNoResultsState(query)
                    }
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.noResultsLayout.visibility = View.GONE
        binding.searchResultsRecyclerView.visibility = View.GONE
        binding.searchProgressBar.visibility = View.GONE
    }

    private fun showLoadingState() {
        binding.emptyStateLayout.visibility = View.GONE
        binding.noResultsLayout.visibility = View.GONE
        binding.searchResultsRecyclerView.visibility = View.GONE
        binding.searchProgressBar.visibility = View.VISIBLE
    }

    private fun showNoResultsState(query: String) {
        binding.emptyStateLayout.visibility = View.GONE
        binding.noResultsLayout.visibility = View.VISIBLE
        binding.searchResultsRecyclerView.visibility = View.GONE
        binding.searchProgressBar.visibility = View.GONE

        binding.noResultsDescription.text = getString(R.string.no_results_for_query, query)
    }

    private fun showResultsState(movies: List<Movie>) {
        binding.emptyStateLayout.visibility = View.GONE
        binding.noResultsLayout.visibility = View.GONE
        binding.searchResultsRecyclerView.visibility = View.VISIBLE
        binding.searchProgressBar.visibility = View.GONE

        adapter.submitList(movies)
    }

    private fun navigateToMovieDetail(movie: Movie) {
        val intent = Intent(this, MovieDetailActivity::class.java).apply {
            putExtra(EXTRA_MOVIE_ID, movie.id)
        }
        startActivity(intent)
    }
}
