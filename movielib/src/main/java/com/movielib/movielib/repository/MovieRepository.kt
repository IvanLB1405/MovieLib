package com.movielib.movielib.repository

import com.movielib.movielib.api.ApiClient
import com.movielib.movielib.api.ApiResponse
import com.movielib.movielib.api.TMDbService
import com.movielib.movielib.database.MovieDao
import com.movielib.movielib.models.Movie
import com.movielib.movielib.models.MovieApiModel
import com.movielib.movielib.models.toMovie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository que coordina entre la API remota y la base de datos local
 *
 * Implementa el patrón Repository para abstraer las fuentes de datos y proporciona
 * una interfaz única para acceder a películas desde TMDb API y la base de datos Room.
 * Todos los resultados de la API se cachean automáticamente en la base de datos local.
 *
 * @param movieDao DAO para operaciones locales
 * @param apiKey Clave de API de TMDb
 *
 * @see MovieDao
 * @see TMDbService
 */
class MovieRepository(
    private val movieDao: MovieDao,
    private val apiKey: String
) {

    private val tmdbService: TMDbService = ApiClient.getTMDbService()

    companion object {
        private const val ERROR_EMPTY_RESPONSE = "Empty response from server"
        private const val ERROR_SERVER = "Server error: %d"
        private const val ERROR_HTTP = "HTTP error: %s"
        private const val ERROR_UNEXPECTED = "Unexpected error: %s"
        private const val ERROR_GENERIC = "Error: %s"
        private const val ERROR_MOVIE_NOT_FOUND = "Movie not found"
    }

    /**
     * Busca películas en TMDb por término de búsqueda
     *
     * Realiza una búsqueda en la API de TMDb y cachea automáticamente los resultados
     * en la base de datos local. Emite estados Loading, Success, Error o NetworkError.
     *
     * @param query Término de búsqueda (ej: "Inception", "Matrix")
     * @param page Número de página (por defecto 1)
     * @return Flow que emite estados de la operación con lista de películas
     *
     * @see ApiResponse
     * @see Movie
     */
    fun searchMovies(query: String, page: Int = 1): Flow<ApiResponse<List<Movie>>> = flow {
        emit(ApiResponse.Loading)

        try {
            val response = tmdbService.searchMovies(
                apiKey = apiKey,
                query = query,
                page = page
            )

            if (response.isSuccessful) {
                val movieSearchResponse = response.body()
                if (movieSearchResponse != null) {
                    val movies = movieSearchResponse.results.map { it.toMovie() }

                    // Guardar resultados en caché local
                    movieDao.insertMovies(movies)

                    emit(ApiResponse.Success(movies))
                } else {
                    emit(ApiResponse.Error(ERROR_EMPTY_RESPONSE))
                }
            } else {
                emit(ApiResponse.Error(ERROR_SERVER.format(response.code()), response.code()))
            }
        } catch (e: IOException) {
            emit(ApiResponse.NetworkError)
        } catch (e: HttpException) {
            emit(ApiResponse.Error(ERROR_HTTP.format(e.message()), e.code()))
        } catch (e: Exception) {
            emit(ApiResponse.Error(ERROR_UNEXPECTED.format(e.message)))
        }
    }

    /**
     * Obtiene las películas más populares de TMDb
     *
     * Consulta las películas actualmente populares en TMDb y las cachea localmente.
     * Útil para mostrar contenido destacado en la pantalla principal.
     *
     * @param page Número de página (por defecto 1)
     * @return Flow con lista de películas populares
     */
    fun getPopularMovies(page: Int = 1): Flow<ApiResponse<List<Movie>>> = flow {
        emit(ApiResponse.Loading)

        try {
            val response = tmdbService.getPopularMovies(apiKey, page)

            if (response.isSuccessful) {
                val movieSearchResponse = response.body()
                if (movieSearchResponse != null) {
                    val movies = movieSearchResponse.results.map { it.toMovie() }
                    movieDao.insertMovies(movies)
                    emit(ApiResponse.Success(movies))
                } else {
                    emit(ApiResponse.Error(ERROR_EMPTY_RESPONSE))
                }
            } else {
                emit(ApiResponse.Error(ERROR_SERVER.format(response.code()), response.code()))
            }
        } catch (e: IOException) {
            emit(ApiResponse.NetworkError)
        } catch (e: Exception) {
            emit(ApiResponse.Error(ERROR_GENERIC.format(e.message)))
        }
    }

    /**
     * Obtiene las películas mejor valoradas de TMDb
     *
     * Consulta las películas con mayor puntuación en TMDb según la valoración
     * de la comunidad. Los resultados se cachean localmente.
     *
     * @param page Número de página (por defecto 1)
     * @return Flow con lista de películas mejor valoradas
     */
    fun getTopRatedMovies(page: Int = 1): Flow<ApiResponse<List<Movie>>> = flow {
        emit(ApiResponse.Loading)

        try {
            val response = tmdbService.getTopRatedMovies(apiKey, page)

            if (response.isSuccessful) {
                val movieSearchResponse = response.body()
                if (movieSearchResponse != null) {
                    val movies = movieSearchResponse.results.map { it.toMovie() }
                    movieDao.insertMovies(movies)
                    emit(ApiResponse.Success(movies))
                } else {
                    emit(ApiResponse.Error(ERROR_EMPTY_RESPONSE))
                }
            } else {
                emit(ApiResponse.Error(ERROR_SERVER.format(response.code()), response.code()))
            }
        } catch (e: IOException) {
            emit(ApiResponse.NetworkError)
        } catch (e: Exception) {
            emit(ApiResponse.Error(ERROR_GENERIC.format(e.message)))
        }
    }

    /**
     * Obtiene los detalles completos de una película
     *
     * Implementa estrategia de caché inteligente:
     * 1. Primero verifica la caché local
     * 2. Si existe localmente, emite los datos y luego actualiza desde la API
     * 3. Si hay error de red, devuelve datos cacheados si existen
     * 4. Preserva datos del usuario (rating, review, biblioteca) al actualizar
     *
     * @param movieId ID de la película en TMDb
     * @return Flow con los detalles de la película incluyendo géneros y reparto
     */
    fun getMovieDetails(movieId: Int): Flow<ApiResponse<Movie>> = flow {
        emit(ApiResponse.Loading)

        try {
            // Primero intentar obtener de la base de datos local
            val localMovie = movieDao.getMovieById(movieId)

            // Si tenemos datos completos localmente, los emitimos
            if (localMovie != null && !localMovie.genres.isNullOrEmpty()) {
                emit(ApiResponse.Success(localMovie))
            }

            // Siempre intentar obtener datos actualizados de la API
            val response = tmdbService.getMovieDetails(movieId, apiKey)

            if (response.isSuccessful) {
                val movieDetail = response.body()
                if (movieDetail != null) {
                    val movie = movieDetail.toMovie()

                    // Si la película ya existe localmente, preservar datos del usuario
                    val updatedMovie = if (localMovie != null) {
                        movie.copy(
                            isInLibrary = localMovie.isInLibrary,
                            userRating = localMovie.userRating,
                            userReview = localMovie.userReview,
                            dateAdded = localMovie.dateAdded
                        )
                    } else {
                        movie
                    }

                    movieDao.insertMovie(updatedMovie)
                    emit(ApiResponse.Success(updatedMovie))
                } else {
                    if (localMovie != null) {
                        emit(ApiResponse.Success(localMovie))
                    } else {
                        emit(ApiResponse.Error(ERROR_MOVIE_NOT_FOUND))
                    }
                }
            } else {
                if (localMovie != null) {
                    emit(ApiResponse.Success(localMovie))
                } else {
                    emit(ApiResponse.Error(ERROR_SERVER.format(response.code()), response.code()))
                }
            }
        } catch (e: IOException) {
            // Si hay error de red, intentar obtener de la base de datos local
            val localMovie = movieDao.getMovieById(movieId)
            if (localMovie != null) {
                emit(ApiResponse.Success(localMovie))
            } else {
                emit(ApiResponse.NetworkError)
            }
        } catch (e: Exception) {
            emit(ApiResponse.Error(ERROR_GENERIC.format(e.message)))
        }
    }

    /**
     * Obtiene un Flow reactivo de las películas en la biblioteca personal
     *
     * Este Flow se actualiza automáticamente cuando cambia el contenido de la biblioteca.
     * Útil para observar cambios en tiempo real en la UI.
     *
     * @return Flow que emite la lista actualizada de películas en biblioteca
     */
    fun getLibraryMoviesFlow(): Flow<List<Movie>> {
        return movieDao.getLibraryMoviesFlow()
    }

    /**
     * Obtiene la lista actual de películas en la biblioteca (operación única)
     *
     * @return Lista de películas en la biblioteca del usuario
     */
    suspend fun getLibraryMovies(): List<Movie> {
        return movieDao.getLibraryMovies()
    }

    /**
     * Obtiene solo las películas que tienen reseñas del usuario
     *
     * @return Lista de películas con reseñas escritas
     */
    suspend fun getMoviesWithReviews(): List<Movie> {
        return movieDao.getMoviesWithReviews()
    }

    /**
     * Añade una película a la biblioteca personal del usuario
     *
     * @param movieId ID de la película a añadir
     * @return true si se añadió correctamente, false en caso de error
     */
    suspend fun addToLibrary(movieId: Int): Boolean {
        return try {
            movieDao.addToLibrary(movieId)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Elimina una película de la biblioteca personal del usuario
     *
     * NOTA: Esto también borra la valoración y reseña del usuario para esta película.
     *
     * @param movieId ID de la película a eliminar
     * @return true si se eliminó correctamente, false en caso de error
     */
    suspend fun removeFromLibrary(movieId: Int): Boolean {
        return try {
            movieDao.removeFromLibrary(movieId)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica si una película está en la biblioteca del usuario
     *
     * @param movieId ID de la película a verificar
     * @return true si está en biblioteca, false en caso contrario
     */
    suspend fun isMovieInLibrary(movieId: Int): Boolean {
        return movieDao.isMovieInLibrary(movieId)
    }

    /**
     * Actualiza la valoración del usuario para una película
     *
     * @param movieId ID de la película a valorar
     * @param rating Valoración del usuario (0.0 - 10.0)
     * @return true si se actualizó correctamente, false en caso de error
     */
    suspend fun updateUserRating(movieId: Int, rating: Float): Boolean {
        return try {
            movieDao.updateUserRating(movieId, rating)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Actualiza o elimina la reseña del usuario para una película
     *
     * @param movieId ID de la película a reseñar
     * @param review Texto de la reseña (null para eliminar reseña existente)
     * @return true si se actualizó correctamente, false en caso de error
     */
    suspend fun updateUserReview(movieId: Int, review: String?): Boolean {
        return try {
            movieDao.updateUserReview(movieId, review)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene estadísticas de la biblioteca personal del usuario
     *
     * @return Objeto con estadísticas: total películas, rating promedio, total reseñas
     * @see LibraryStats
     */
    suspend fun getLibraryStats(): LibraryStats {
        return LibraryStats(
            totalMovies = movieDao.getLibraryCount(),
            averageRating = movieDao.getAverageUserRating() ?: 0.0,
            moviesWithReviews = movieDao.getMoviesWithReviews().size
        )
    }
}

/**
 * Estadísticas de la biblioteca personal del usuario
 *
 * @property totalMovies Total de películas en la biblioteca
 * @property averageRating Valoración promedio del usuario (0.0 si no hay valoraciones)
 * @property moviesWithReviews Cantidad de películas con reseñas escritas
 */
data class LibraryStats(
    val totalMovies: Int,
    val averageRating: Double,
    val moviesWithReviews: Int
)