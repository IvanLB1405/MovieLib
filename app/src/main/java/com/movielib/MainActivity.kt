package com.movielib

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.movielib.adapters.MovieAdapter
import com.movielib.movielib.R
import com.movielib.movielib.api.ApiResponse
import com.movielib.movielib.database.MovieDatabase
import com.movielib.movielib.databinding.ActivityMainBinding
import com.movielib.movielib.models.Movie
import com.movielib.movielib.repository.MovieRepository
import com.movielib.movielib.utils.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: MovieRepository

    private lateinit var popularAdapter: MovieAdapter
    private lateinit var topRatedAdapter: MovieAdapter
    private lateinit var favoritesAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRepository()
        setupRecyclerViews()
        setupSearchButton()
        setupHeroSection()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        // Reload favorites when returning to MainActivity
        loadFavorites()
    }

    private fun setupRepository() {
        val database = MovieDatabase.getDatabase(this)
        repository = MovieRepository(database.movieDao(), Constants.TMDB_API_KEY)
    }

    private fun setupRecyclerViews() {
        // Popular movies
        popularAdapter = MovieAdapter(MovieAdapter.LayoutType.HORIZONTAL) { movie ->
            navigateToMovieDetail(movie)
        }
        binding.popularMoviesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = popularAdapter
        }

        // Top rated movies
        topRatedAdapter = MovieAdapter(MovieAdapter.LayoutType.HORIZONTAL) { movie ->
            navigateToMovieDetail(movie)
        }
        binding.topRatedMoviesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = topRatedAdapter
        }

        // Favorites movies
        favoritesAdapter = MovieAdapter(MovieAdapter.LayoutType.HORIZONTAL) { movie ->
            navigateToMovieDetail(movie)
        }
        binding.favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = favoritesAdapter
        }
    }

    private fun setupSearchButton() {
        binding.searchIcon.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.libraryIcon.setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }
    }

    private fun setupHeroSection() {
        binding.rateButton.setOnClickListener {
            // Navigate to the featured movie detail to rate
            val movieId = binding.heroSection.tag as? Int
            movieId?.let { id ->
                val intent = Intent(this, MovieDetailActivity::class.java).apply {
                    putExtra(SearchActivity.EXTRA_MOVIE_ID, id)
                }
                startActivity(intent)
            }
        }

        binding.favoriteButton.setOnClickListener {
            // Toggle favorite for featured movie
            val movieId = binding.heroSection.tag as? Int
            movieId?.let { id ->
                lifecycleScope.launch {
                    repository.addToLibrary(id)
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
                when (response) {
                    is ApiResponse.Loading -> {
                        // Loading handled by main progress bar
                    }
                    is ApiResponse.Success -> {
                        val movies = response.data
                        popularAdapter.submitList(movies)

                        // Use first popular movie as hero if available
                        if (movies.isNotEmpty()) {
                            displayHeroMovie(movies.first())
                        }

                        hideLoading()
                    }
                    is ApiResponse.Error -> {
                        hideLoading()
                    }
                    is ApiResponse.NetworkError -> {
                        hideLoading()
                    }
                }
            }
        }
    }

    private fun loadTopRatedMovies() {
        lifecycleScope.launch {
            repository.getTopRatedMovies().collect { response ->
                when (response) {
                    is ApiResponse.Success -> {
                        topRatedAdapter.submitList(response.data)
                    }
                    else -> {
                        // Handle other states if needed
                    }
                }
            }
        }
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            repository.getLibraryMoviesFlow().collectLatest { movies ->
                if (movies.isNotEmpty()) {
                    binding.favoritesSection.visibility = View.VISIBLE
                    favoritesAdapter.submitList(movies)
                } else {
                    binding.favoritesSection.visibility = View.GONE
                }
            }
        }
    }

    private fun displayHeroMovie(movie: Movie) {
        // Store movie ID in tag for later use
        binding.heroSection.tag = movie.id

        // Set title and overview
        binding.heroTitle.text = movie.title
        binding.heroOverview.text = movie.overview

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
            .into(binding.heroBackdrop)
    }

    private fun navigateToMovieDetail(movie: Movie) {
        val intent = Intent(this, MovieDetailActivity::class.java).apply {
            putExtra(SearchActivity.EXTRA_MOVIE_ID, movie.id)
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
