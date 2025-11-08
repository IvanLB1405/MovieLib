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
 * Implementa el patrón Repository para abstraer las fuentes de datos
 *
 * @param movieDao DAO para operaciones locales
 * @param apiKey Clave de API de TMDb
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

    fun getLibraryMoviesFlow(): Flow<List<Movie>> {
        return movieDao.getLibraryMoviesFlow()
    }

    suspend fun getLibraryMovies(): List<Movie> {
        return movieDao.getLibraryMovies()
    }

    suspend fun getMoviesWithReviews(): List<Movie> {
        return movieDao.getMoviesWithReviews()
    }

    suspend fun addToLibrary(movieId: Int): Boolean {
        return try {
            movieDao.addToLibrary(movieId)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeFromLibrary(movieId: Int): Boolean {
        return try {
            movieDao.removeFromLibrary(movieId)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isMovieInLibrary(movieId: Int): Boolean {
        return movieDao.isMovieInLibrary(movieId)
    }

    suspend fun updateUserRating(movieId: Int, rating: Float): Boolean {
        return try {
            movieDao.updateUserRating(movieId, rating)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserReview(movieId: Int, review: String?): Boolean {
        return try {
            movieDao.updateUserReview(movieId, review)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLibraryStats(): LibraryStats {
        return LibraryStats(
            totalMovies = movieDao.getLibraryCount(),
            averageRating = movieDao.getAverageUserRating() ?: 0.0,
            moviesWithReviews = movieDao.getMoviesWithReviews().size
        )
    }
}

data class LibraryStats(
    val totalMovies: Int,
    val averageRating: Double,
    val moviesWithReviews: Int
)