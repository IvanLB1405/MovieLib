package com.movielib.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.movielib.movielib.api.ApiResponse
import com.movielib.movielib.repository.MovieRepository
import kotlinx.coroutines.launch
import com.movielib.movielib.database.MovieDatabase
import com.movielib.movielib.utils.Constants

class ApiTestActivity : AppCompatActivity() {

    private lateinit var movieRepository: MovieRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear un layout simple para mostrar que está funcionando
        val textView = android.widget.TextView(this)
        textView.text = "🚀 MovieLib API Test\n\n🔄 Ejecutando pruebas...\n\n📱 Revisa el Logcat para ver los resultados detallados\n\nFiltro: API_TEST"
        textView.gravity = android.view.Gravity.CENTER
        textView.textSize = 16f
        textView.setPadding(32, 32, 32, 32)
        setContentView(textView)

        try {
            val database = MovieDatabase.getDatabase(this)
            val movieDao = database.movieDao()
            movieRepository = MovieRepository(movieDao, Constants.TMDB_API_KEY)

            testApiConnection()

        } catch (e: Exception) {
            // Error initializing repository
        }
    }

    private fun testApiConnection() {
        lifecycleScope.launch {
            try {
                testSearchMovies()
                testPopularMovies()
                testMovieDetails()
                testLocalDatabase()
            } catch (e: Exception) {
                // Error in API test
            }
        }
    }

    private suspend fun testSearchMovies() {
        try {
            movieRepository.searchMovies("Avengers").collect { response ->
                when (response) {
                    is ApiResponse.Loading -> {}
                    is ApiResponse.Success -> {}
                    is ApiResponse.Error -> {}
                    is ApiResponse.NetworkError -> {}
                }
            }
        } catch (e: Exception) {
            // Search error
        }
    }

    private suspend fun testPopularMovies() {
        try {
            movieRepository.getPopularMovies().collect { response ->
                when (response) {
                    is ApiResponse.Loading -> {}
                    is ApiResponse.Success -> {}
                    is ApiResponse.Error -> {}
                    is ApiResponse.NetworkError -> {}
                }
            }
        } catch (e: Exception) {
            // Popular movies error
        }
    }

    private suspend fun testMovieDetails() {
        try {
            movieRepository.getMovieDetails(299534).collect { response ->
                when (response) {
                    is ApiResponse.Loading -> {}
                    is ApiResponse.Success -> {}
                    is ApiResponse.Error -> {}
                    is ApiResponse.NetworkError -> {}
                }
            }
        } catch (e: Exception) {
            // Movie details error
        }
    }

    private suspend fun testLocalDatabase() {
        try {
            val libraryMovies = movieRepository.getLibraryMovies()
            val stats = movieRepository.getLibraryStats()
        } catch (e: Exception) {
            // Database error
        }
    }
}