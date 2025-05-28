package com.movielib

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.movielib.movielib.repository.MovieRepository
import kotlinx.coroutines.launch
import com.movielib.movielib.database.MovieDatabase

class ApiTestActivity : AppCompatActivity() {

    private lateinit var movieRepository: MovieRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Como no tienes MainActivity, usamos un layout simple o creamos la vista programáticamente
        // Por ahora, no necesitamos layout para la prueba

        Log.d("API_TEST", "🚀 ApiTestActivity iniciada")

        try {
            // Inicializar el repository (del módulo movielib)
            movieRepository = MovieRepository(this)
            Log.d("API_TEST", "✅ MovieRepository inicializado correctamente")

            // Probar la API
            testApiConnection()

        } catch (e: Exception) {
            Log.e("API_TEST", "❌ Error inicializando MovieRepository: ${e.message}", e)
        }
    }

    private fun testApiConnection() {
        lifecycleScope.launch {
            try {
                Log.d("API_TEST", "🔄 Iniciando prueba de conexión con TMDb API...")

                // Probar búsqueda de películas
                testSearchMovies()

                // Probar películas populares
                testPopularMovies()

                // Probar detalles de una película específica
                testMovieDetails()

                // Probar base de datos local
                testLocalDatabase()

                Log.d("API_TEST", "🎉 ¡Todas las pruebas completadas!")

            } catch (e: Exception) {
                Log.e("API_TEST", "❌ Error en la prueba de API: ${e.message}", e)
            }
        }
    }

    private suspend fun testSearchMovies() {
        try {
            Log.d("API_TEST", "🔍 Probando búsqueda de películas...")
            val movies = movieRepository.searchMovies("Avengers")

            if (movies.isNotEmpty()) {
                Log.d("API_TEST", "✅ Búsqueda exitosa! Encontradas ${movies.size} películas")
                movies.take(3).forEach { movie ->
                    Log.d("API_TEST", "🎬 ${movie.title} (${movie.releaseDate}) - Rating: ${movie.voteAverage}")
                }
            } else {
                Log.w("API_TEST", "⚠️ No se encontraron películas en la búsqueda")
            }

        } catch (e: Exception) {
            Log.e("API_TEST", "❌ Error en búsqueda: ${e.message}", e)
        }
    }

    private suspend fun testPopularMovies() {
        try {
            Log.d("API_TEST", "🔥 Probando películas populares...")
            val movies = movieRepository.getPopularMovies()

            if (movies.isNotEmpty()) {
                Log.d("API_TEST", "✅ Películas populares obtenidas! Total: ${movies.size}")
                movies.take(3).forEach { movie ->
                    Log.d("API_TEST", "🎬 ${movie.title} - Popularidad: ${movie.popularity}")
                }
            } else {
                Log.w("API_TEST", "⚠️ No se encontraron películas populares")
            }

        } catch (e: Exception) {
            Log.e("API_TEST", "❌ Error obteniendo películas populares: ${e.message}", e)
        }
    }

    private suspend fun testMovieDetails() {
        try {
            Log.d("API_TEST", "🎭 Probando detalles de película específica...")
            // Usar ID de una película conocida (Avengers: Endgame)
            val movie = movieRepository.getMovieDetails(299534)

            if (movie != null) {
                Log.d("API_TEST", "✅ Detalles obtenidos exitosamente!")
                Log.d("API_TEST", "🎬 Título: ${movie.title}")
                Log.d("API_TEST", "📅 Fecha: ${movie.releaseDate}")
                Log.d("API_TEST", "⭐ Rating: ${movie.voteAverage}")
                Log.d("API_TEST", "📝 Resumen: ${movie.overview.take(100)}...")
            } else {
                Log.w("API_TEST", "⚠️ No se pudieron obtener los detalles de la película")
            }

        } catch (e: Exception) {
            Log.e("API_TEST", "❌ Error obteniendo detalles: ${e.message}", e)
        }
    }

    private suspend fun testLocalDatabase() {
        try {
            Log.d("API_TEST", "💾 Probando base de datos local...")

            // Obtener todas las películas de la base de datos
            val localMovies = movieRepository.getAllMovies()
            Log.d("API_TEST", "📊 Películas en base de datos local: ${localMovies.size}")

            // Obtener favoritas
            val favorites = movieRepository.getFavoriteMovies()
            Log.d("API_TEST", "❤️ Películas favoritas: ${favorites.size}")

            if (localMovies.isNotEmpty()) {
                Log.d("API_TEST", "✅ Base de datos local funcionando correctamente")
                localMovies.take(3).forEach { movie ->
                    Log.d("API_TEST", "💾 Local: ${movie.title} - Favorito: ${movie.isFavorite}")
                }
            } else {
                Log.d("API_TEST", "ℹ️ Base de datos local vacía (normal en primera ejecución)")
            }

        } catch (e: Exception) {
            Log.e("API_TEST", "❌ Error en base de datos local: ${e.message}", e)
        }
    }
}