package com.movielib.movielib.database

import androidx.room.*
import com.movielib.movielib.models.Movie
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operaciones de base de datos con películas
 */
@Dao
interface MovieDao {

    // ============ OPERACIONES BÁSICAS ============

    /**
     * Insertar una película en la base de datos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    /**
     * Insertar múltiples películas
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    /**
     * Actualizar una película existente
     */
    @Update
    suspend fun updateMovie(movie: Movie)

    /**
     * Eliminar una película de la base de datos
     */
    @Delete
    suspend fun deleteMovie(movie: Movie)

    // ============ CONSULTAS DE BÚSQUEDA ============

    /**
     * Obtener todas las películas
     */
    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<Movie>

    /**
     * Obtener una película por ID
     */
    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): Movie?

    /**
     * Buscar películas por título
     */
    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%'")
    suspend fun searchMoviesByTitle(query: String): List<Movie>

    // ============ BIBLIOTECA PERSONAL ============

    /**
     * Obtener todas las películas de la biblioteca personal (como Flow para observar cambios)
     */
    @Query("SELECT * FROM movies WHERE isInLibrary = 1 ORDER BY dateAdded DESC")
    fun getLibraryMoviesFlow(): Flow<List<Movie>>

    /**
     * Obtener todas las películas de la biblioteca personal
     */
    @Query("SELECT * FROM movies WHERE isInLibrary = 1 ORDER BY dateAdded DESC")
    suspend fun getLibraryMovies(): List<Movie>

    /**
     * Verificar si una película está en la biblioteca
     */
    @Query("SELECT EXISTS(SELECT 1 FROM movies WHERE id = :movieId AND isInLibrary = 1)")
    suspend fun isMovieInLibrary(movieId: Int): Boolean

    /**
     * Añadir película a la biblioteca personal
     */
    @Query("UPDATE movies SET isInLibrary = 1, dateAdded = :timestamp WHERE id = :movieId")
    suspend fun addToLibrary(movieId: Int, timestamp: Long = System.currentTimeMillis())

    /**
     * Quitar película de la biblioteca personal
     */
    @Query("UPDATE movies SET isInLibrary = 0, dateAdded = NULL, userRating = NULL, userReview = NULL WHERE id = :movieId")
    suspend fun removeFromLibrary(movieId: Int)

    // ============ RESEÑAS Y PUNTUACIONES ============

    /**
     * Actualizar la puntuación del usuario para una película
     */
    @Query("UPDATE movies SET userRating = :rating WHERE id = :movieId")
    suspend fun updateUserRating(movieId: Int, rating: Float)

    /**
     * Actualizar la reseña del usuario para una película
     */
    @Query("UPDATE movies SET userReview = :review WHERE id = :movieId")
    suspend fun updateUserReview(movieId: Int, review: String?)

    /**
     * Obtener películas con reseñas del usuario
     */
    @Query("SELECT * FROM movies WHERE isInLibrary = 1 AND userReview IS NOT NULL AND userReview != ''")
    suspend fun getMoviesWithReviews(): List<Movie>

    /**
     * Obtener películas puntuadas por el usuario
     */
    @Query("SELECT * FROM movies WHERE isInLibrary = 1 AND userRating IS NOT NULL ORDER BY userRating DESC")
    suspend fun getRatedMovies(): List<Movie>

    // ============ ESTADÍSTICAS ============

    /**
     * Contar películas en la biblioteca
     */
    @Query("SELECT COUNT(*) FROM movies WHERE isInLibrary = 1")
    suspend fun getLibraryCount(): Int

    /**
     * Obtener puntuación promedio del usuario
     */
    @Query("SELECT AVG(userRating) FROM movies WHERE isInLibrary = 1 AND userRating IS NOT NULL")
    suspend fun getAverageUserRating(): Double?

    // ============ LIMPIEZA ============

    /**
     * Eliminar películas que no están en la biblioteca (para limpieza de caché)
     */
    @Query("DELETE FROM movies WHERE isInLibrary = 0")
    suspend fun clearNonLibraryMovies()
}