package com.movielib.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.movielib.movielib.api.ApiResponse
import com.movielib.movielib.repository.MovieRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
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

        Log.d("API_TEST", "🚀 ApiTestActivity iniciada")

        try {
            // Inicializar el repository (del módulo movielib)
            val database = MovieDatabase.getDatabase(this)
            val movieDao = database.movieDao()
            movieRepository = MovieRepository(movieDao, Constants.TMDB_API_KEY)
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

            movieRepository.searchMovies("Avengers").collect { response ->
                when (response) {
                    is ApiResponse.Loading -> {
                        Log.d("API_TEST", "🔄 Cargando búsqueda...")
                    }
                    is ApiResponse.Success -> {
                        val movies = response.data
                        if (movies.isNotEmpty()) {
                            Log.d("API_TEST", "✅ Búsqueda exitosa! Encontradas ${movies.size} películas")
                            movies.take(3).forEach { movie ->
                                Log.d("API_TEST", "🎬 ${movie.title} (${movie.releaseDate}) - Rating: ${movie.voteAverage}")
                            }
                        } else {
                            Log.w("API_TEST", "⚠️ No se encontraron películas en la búsqueda")
                        }
                    }
                    is ApiResponse.Error -> {
                        Log.e("API_TEST", "❌ Error en búsqueda: ${response.message}")
                    }
                    is ApiResponse.NetworkError -> {
                        Log.e("API_TEST", "❌ Error de conexión en búsqueda")
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("API_TEST", "❌ Error en búsqueda: ${e.message}", e)
        }
    }

    private suspend fun testPopularMovies() {
        try {
            Log.d("API_TEST", "🔥 Probando películas populares...")

            movieRepository.getPopularMovies().collect { response ->
                when (response) {
                    is ApiResponse.Loading -> {
                        Log.d("API_TEST", "🔄 Cargando películas populares...")
                    }
                    is ApiResponse.Success -> {
                        val movies = response.data
                        if (movies.isNotEmpty()) {
                            Log.d("API_TEST", "✅ Películas populares obtenidas! Total: ${movies.size}")
                            movies.take(3).forEach { movie ->
                                Log.d("API_TEST", "🎬 ${movie.title} - Rating: ${movie.voteAverage}")
                            }
                        } else {
                            Log.w("API_TEST", "⚠️ No se encontraron películas populares")
                        }
                    }
                    is ApiResponse.Error -> {
                        Log.e("API_TEST", "❌ Error obteniendo películas populares: ${response.message}")
                    }
                    is ApiResponse.NetworkError -> {
                        Log.e("API_TEST", "❌ Error de conexión obteniendo películas populares")
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("API_TEST", "❌ Error obteniendo películas populares: ${e.message}", e)
        }
    }

    private suspend fun testMovieDetails() {
        try {
            Log.d("API_TEST", "🎭 Probando detalles de película específica...")

            // Usar ID de una película conocida (Avengers: Endgame)
            movieRepository.getMovieDetails(299534).collect { response ->
                when (response) {
                    is ApiResponse.Loading -> {
                        Log.d("API_TEST", "🔄 Cargando detalles de película...")
                    }
                    is ApiResponse.Success -> {
                        val movie = response.data
                        Log.d("API_TEST", "✅ Detalles obtenidos exitosamente!")
                        Log.d("API_TEST", "🎬 Título: ${movie.title}")
                        Log.d("API_TEST", "📅 Fecha: ${movie.releaseDate}")
                        Log.d("API_TEST", "⭐ Rating: ${movie.voteAverage}")
                        Log.d("API_TEST", "📝 Resumen: ${movie.overview?.take(100) ?: "Sin resumen"}...")
                    }
                    is ApiResponse.Error -> {
                        Log.e("API_TEST", "❌ Error obteniendo detalles: ${response.message}")
                    }
                    is ApiResponse.NetworkError -> {
                        Log.e("API_TEST", "❌ Error de conexión obteniendo detalles")
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("API_TEST", "❌ Error obteniendo detalles: ${e.message}", e)
        }
    }

    private suspend fun testLocalDatabase() {
        try {
            Log.d("API_TEST", "💾 Probando base de datos local...")

            // Obtener todas las películas de la biblioteca (si existen estos métodos)
            val libraryMovies = movieRepository.getLibraryMovies()
            Log.d("API_TEST", "📊 Películas en biblioteca: ${libraryMovies.size}")

            if (libraryMovies.isNotEmpty()) {
                Log.d("API_TEST", "✅ Base de datos local funcionando correctamente")
                libraryMovies.take(3).forEach { movie ->
                    Log.d("API_TEST", "💾 Local: ${movie.title} - En biblioteca: ${movie.isInLibrary}")
                }
            } else {
                Log.d("API_TEST", "ℹ️ Base de datos local vacía (normal en primera ejecución)")
            }

            // Obtener estadísticas de la biblioteca
            val stats = movieRepository.getLibraryStats()
            Log.d("API_TEST", "📈 Estadísticas - Total: ${stats.totalMovies}, Rating promedio: ${stats.averageRating}, Con reseñas: ${stats.moviesWithReviews}")

        } catch (e: Exception) {
            Log.e("API_TEST", "❌ Error en base de datos local: ${e.message}", e)
        }
    }
}