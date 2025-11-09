package com.movielib.base

import androidx.appcompat.app.AppCompatActivity
import com.movielib.movielib.database.MovieDatabase
import com.movielib.movielib.repository.MovieRepository
import com.movielib.movielib.utils.Constants

/**
 * Base activity for all movie-related screens
 *
 * Provides shared functionality for all activities that need access to movie data:
 * - Lazy initialization of [MovieRepository] (created only when first accessed)
 * - Access to Room database via [MovieDatabase]
 * - Centralized repository management (DRY principle)
 *
 * All activities that interact with movies should extend this class instead of
 * [AppCompatActivity] directly.
 *
 * @see MovieRepository
 * @see MovieDatabase
 */
abstract class BaseMovieActivity : AppCompatActivity() {

    /**
     * Movie repository instance shared across all activities
     *
     * Lazily initialized to avoid creating unnecessary instances. Provides access to:
     * - TMDb API operations
     * - Local database (Room) operations
     * - Library management functions
     */
    protected val repository: MovieRepository by lazy {
        val database = MovieDatabase.getDatabase(this)
        MovieRepository(database.movieDao(), Constants.TMDB_API_KEY)
    }
}
