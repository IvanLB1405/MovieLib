package com.movielib

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.movielib.adapters.MovieAdapter
import com.movielib.base.BaseMovieActivity
import com.movielib.extensions.handle
import com.movielib.movielib.R
import com.movielib.movielib.databinding.ActivityMainBinding
import com.movielib.movielib.models.Movie
import com.movielib.movielib.utils.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main screen activity displaying popular movies, top rated, and user's library
 *
 * Features:
 * - Hero section with featured movie
 * - Horizontal lists of popular and top-rated movies
 * - User's personal library section (visible only if not empty)
 * - Navigation to search and library screens
 *
 * @see BaseMovieActivity
 */
class MainActivity : BaseMovieActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var popularAdapter: MovieAdapter
    private lateinit var topRatedAdapter: MovieAdapter
    private lateinit var favoritesAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        setupClickListeners()
        setupHeroSection()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun setupRecyclerViews() {
        popularAdapter = createHorizontalAdapter()
        topRatedAdapter = createHorizontalAdapter()
        favoritesAdapter = createHorizontalAdapter()

        setupRecyclerView(binding.popularMoviesRecyclerView, popularAdapter)
        setupRecyclerView(binding.topRatedMoviesRecyclerView, topRatedAdapter)
        setupRecyclerView(binding.favoritesRecyclerView, favoritesAdapter)
    }

    /**
     * Helper function to create horizontal movie adapters (DRY principle)
     */
    private fun createHorizontalAdapter() = MovieAdapter(MovieAdapter.LayoutType.HORIZONTAL) { movie ->
        navigateToMovieDetail(movie)
    }

    /**
     * Helper function to setup horizontal RecyclerViews (DRY principle)
     */
    private fun setupRecyclerView(recyclerView: RecyclerView, adapter: MovieAdapter) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
    }

    private fun setupClickListeners() {
        binding.searchIcon.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.libraryIcon.setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }
    }

    private fun setupHeroSection() {
        // Lambda to get movie ID from hero section
        val getHeroMovieId = { binding.heroSection.tag as? Int }

        binding.rateButton.setOnClickListener {
            getHeroMovieId()?.let { movieId ->
                navigateToMovieDetail(movieId)
            }
        }

        binding.favoriteButton.setOnClickListener {
            getHeroMovieId()?.let { movieId ->
                lifecycleScope.launch {
                    repository.addToLibrary(movieId)
                    loadFavorites()
                }
            }
        }
    }

    private fun loadData() {
        showLoading()
        loadPopularMovies()
        loadTopRatedMovies()
        loadFavorites()
    }

    private fun loadPopularMovies() {
        lifecycleScope.launch {
            repository.getPopularMovies().collect { response ->
                response.handle(
                    onSuccess = { movies ->
                        popularAdapter.submitList(movies)
                        if (movies.isNotEmpty()) {
                            displayHeroMovie(movies.first())
                        }
                        hideLoading()
                    },
                    onError = { _, _ -> hideLoading() },
                    onNetworkError = { hideLoading() }
                )
            }
        }
    }

    private fun loadTopRatedMovies() {
        lifecycleScope.launch {
            repository.getTopRatedMovies().collect { response ->
                response.handle(
                    onSuccess = { movies ->
                        topRatedAdapter.submitList(movies)
                    }
                )
            }
        }
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            repository.getLibraryMoviesFlow().collectLatest { movies ->
                binding.favoritesSection.visibility = if (movies.isNotEmpty()) {
                    favoritesAdapter.submitList(movies)
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun displayHeroMovie(movie: Movie) {
        binding.heroSection.tag = movie.id
        binding.heroTitle.text = movie.title
        binding.heroOverview.text = movie.overview

        val backdropUrl = Constants.buildPosterUrl(
            movie.posterPath,
            Constants.IMAGE_SIZE_W780
        )

        Glide.with(this)
            .load(backdropUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder_movie)
            .error(R.drawable.placeholder_movie)
            .into(binding.heroBackdrop)
    }

    private fun navigateToMovieDetail(movie: Movie) {
        navigateToMovieDetail(movie.id)
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
