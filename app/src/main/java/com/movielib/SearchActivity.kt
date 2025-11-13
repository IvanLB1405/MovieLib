package com.movielib

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.movielib.adapters.MovieAdapter
import com.movielib.base.BaseMovieActivity
import com.movielib.extensions.handle
import com.movielib.movielib.R
import com.movielib.movielib.databinding.ActivitySearchBinding
import com.movielib.movielib.models.Movie
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de búsqueda de películas por texto
 *
 * Características:
 * - Búsqueda en tiempo real con debouncing (retraso de 500ms)
 * - Diseño en cuadrícula para los resultados de búsqueda
 * - Estados: vacío, cargando, sin resultados y éxito
 * - Navegación directa a detalles de película
 *
 * @see BaseMovieActivity
 */
class SearchActivity : BaseMovieActivity() {

    private lateinit var binding: ActivitySearchBinding
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

        setupRecyclerView()
        setupSearchBar()
        setupBackButton()
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
                searchJob?.cancel()

                val query = s?.toString()?.trim() ?: ""

                if (query.isEmpty()) {
                    showEmptyState()
                } else {
                    searchJob = lifecycleScope.launch {
                        delay(SEARCH_DEBOUNCE_DELAY)
                        searchMovies(query)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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
                response.handle(
                    onLoading = { showLoadingState() },
                    onSuccess = { movies ->
                        if (movies.isEmpty()) {
                            showNoResultsState(query)
                        } else {
                            showResultsState(movies)
                        }
                    },
                    onError = { _, _ -> showNoResultsState(query) },
                    onNetworkError = { showNoResultsState(query) }
                )
            }
        }
    }

    private fun showEmptyState() {
        setViewsVisibility(
            emptyState = true,
            noResults = false,
            results = false,
            loading = false
        )
    }

    private fun showLoadingState() {
        setViewsVisibility(
            emptyState = false,
            noResults = false,
            results = false,
            loading = true
        )
    }

    private fun showNoResultsState(query: String) {
        setViewsVisibility(
            emptyState = false,
            noResults = true,
            results = false,
            loading = false
        )
        binding.noResultsDescription.text = getString(R.string.no_results_for_query, query)
    }

    private fun showResultsState(movies: List<Movie>) {
        setViewsVisibility(
            emptyState = false,
            noResults = false,
            results = true,
            loading = false
        )
        adapter.submitList(movies)
    }

    /**
     * Función auxiliar para gestionar todos los estados de vista en un solo lugar (principio DRY)
     */
    private fun setViewsVisibility(
        emptyState: Boolean,
        noResults: Boolean,
        results: Boolean,
        loading: Boolean
    ) {
        binding.emptyStateLayout.visibility = if (emptyState) View.VISIBLE else View.GONE
        binding.noResultsLayout.visibility = if (noResults) View.VISIBLE else View.GONE
        binding.searchResultsRecyclerView.visibility = if (results) View.VISIBLE else View.GONE
        binding.searchProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun navigateToMovieDetail(movie: Movie) {
        val intent = Intent(this, MovieDetailActivity::class.java).apply {
            putExtra(EXTRA_MOVIE_ID, movie.id)
        }
        startActivity(intent)
    }
}
