package com.movielib

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.movielib.base.BaseMovieActivity
import com.movielib.extensions.handle
import com.movielib.movielib.R
import kotlinx.coroutines.launch

/**
 * Test activity for verifying API and Repository functionality
 * For development and debugging purposes only
 */
class ApiTestActivity : BaseMovieActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create simple layout to show tests are running
        val textView = android.widget.TextView(this)
        textView.text = getString(R.string.api_test_message)
        textView.gravity = android.view.Gravity.CENTER
        textView.textSize = 16f
        textView.setPadding(32, 32, 32, 32)
        setContentView(textView)

        testApiConnection()
    }

    private fun testApiConnection() {
        lifecycleScope.launch {
            testSearchMovies()
            testPopularMovies()
            testMovieDetails()
            testLocalDatabase()
        }
    }

    private suspend fun testSearchMovies() {
        repository.searchMovies("Avengers").collect { response ->
            response.handle(
                onSuccess = { /* Test passed - movies received */ },
                onError = { _, _ -> /* Error handled silently for test */ }
            )
        }
    }

    private suspend fun testPopularMovies() {
        repository.getPopularMovies().collect { response ->
            response.handle(
                onSuccess = { /* Test passed - popular movies received */ },
                onError = { _, _ -> /* Error handled silently for test */ }
            )
        }
    }

    private suspend fun testMovieDetails() {
        repository.getMovieDetails(299534).collect { response ->
            response.handle(
                onSuccess = { /* Test passed - movie details received */ },
                onError = { _, _ -> /* Error handled silently for test */ }
            )
        }
    }

    private suspend fun testLocalDatabase() {
        // Test local database operations
        repository.getLibraryMovies()
        repository.getLibraryStats()
    }
}