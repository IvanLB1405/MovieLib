package com.movielib.movielib.database

import androidx.room.*
import com.movielib.movielib.models.Movie
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operaciones de base de datos con películas
 *
 * Proporciona acceso a la base de datos Room para realizar operaciones CRUD
 * sobre películas, gestionar la biblioteca personal del usuario, y obtener
 * estadísticas. Todas las operaciones son suspendibles para uso con Coroutines.
 *
 * @see Movie
 * @see MovieDatabase
 */
@Dao
interface MovieDao {

    // ============ OPERACIONES BÁSICAS ============

    /**
     * Inserta una película en la base de datos
     *
     * Si la película ya existe (mismo ID), se reemplaza con la nueva información.
     *
     * @param movie Película a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    /**
     * Inserta múltiples películas en una sola operación
     *
     * Más eficiente que insertar una por una. Reemplaza películas existentes.
     *
     * @param movies Lista de películas a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    /**
     * Actualiza una película existente en la base de datos
     *
     * @param movie Película con datos actualizados
     */
    @Update
    suspend fun updateMovie(movie: Movie)

    /**
     * Elimina una película de la base de datos
     *
     * @param movie Película a eliminar
     */
    @Delete
    suspend fun deleteMovie(movie: Movie)

    // ============ CONSULTAS DE BÚSQUEDA ============

    /**
     * Obtiene todas las películas almacenadas en la base de datos
     *
     * @return Lista de todas las películas (biblioteca y caché)
     */
    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<Movie>

    /**
     * Obtiene una película específica por su ID
     *
     * @param movieId ID de la película a buscar
     * @return Película encontrada o null si no existe
     */
    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): Movie?

    /**
     * Busca películas por título (búsqueda parcial)
     *
     * @param query Texto a buscar en el título
     * @return Lista de películas que coinciden con la búsqueda
     */
    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%'")
    suspend fun searchMoviesByTitle(query: String): List<Movie>

    // ============ BIBLIOTECA PERSONAL ============

    /**
     * Obtiene películas de la biblioteca como Flow reactivo
     *
     * El Flow emite automáticamente cuando cambia la biblioteca, ideal para observar
     * cambios en tiempo real en la UI.
     *
     * @return Flow que emite lista actualizada ordenada por fecha de añadido (más recientes primero)
     */
    @Query("SELECT * FROM movies WHERE isInLibrary = 1 ORDER BY dateAdded DESC")
    fun getLibraryMoviesFlow(): Flow<List<Movie>>

    /**
     * Obtiene todas las películas de la biblioteca personal (operación única)
     *
     * @return Lista de películas en biblioteca ordenadas por fecha de añadido
     */
    @Query("SELECT * FROM movies WHERE isInLibrary = 1 ORDER BY dateAdded DESC")
    suspend fun getLibraryMovies(): List<Movie>

    /**
     * Verifica si una película específica está en la biblioteca del usuario
     *
     * @param movieId ID de la película a verificar
     * @return true si está en biblioteca, false en caso contrario
     */
    @Query("SELECT EXISTS(SELECT 1 FROM movies WHERE id = :movieId AND isInLibrary = 1)")
    suspend fun isMovieInLibrary(movieId: Int): Boolean

    /**
     * Añade una película a la biblioteca personal del usuario
     *
     * Marca la película como parte de la biblioteca y registra la fecha de añadido.
     *
     * @param movieId ID de la película a añadir
     * @param timestamp Marca de tiempo de cuando se añadió (milisegundos, por defecto ahora)
     */
    @Query("UPDATE movies SET isInLibrary = 1, dateAdded = :timestamp WHERE id = :movieId")
    suspend fun addToLibrary(movieId: Int, timestamp: Long = System.currentTimeMillis())

    /**
     * Elimina una película de la biblioteca personal del usuario
     *
     * También borra la valoración y reseña del usuario asociadas a esta película.
     *
     * @param movieId ID de la película a eliminar de la biblioteca
     */
    @Query("UPDATE movies SET isInLibrary = 0, dateAdded = NULL, userRating = NULL, userReview = NULL WHERE id = :movieId")
    suspend fun removeFromLibrary(movieId: Int)

    // ============ RESEÑAS Y PUNTUACIONES ============

    /**
     * Actualiza la puntuación del usuario para una película
     *
     * @param movieId ID de la película a puntuar
     * @param rating Puntuación del usuario (0.0 - 10.0)
     */
    @Query("UPDATE movies SET userRating = :rating WHERE id = :movieId")
    suspend fun updateUserRating(movieId: Int, rating: Float)

    /**
     * Actualiza o elimina la reseña del usuario para una película
     *
     * @param movieId ID de la película a reseñar
     * @param review Texto de la reseña (null para eliminar reseña existente)
     */
    @Query("UPDATE movies SET userReview = :review WHERE id = :movieId")
    suspend fun updateUserReview(movieId: Int, review: String?)

    /**
     * Obtiene todas las películas que tienen reseñas escritas por el usuario
     *
     * @return Lista de películas con reseñas no vacías
     */
    @Query("SELECT * FROM movies WHERE isInLibrary = 1 AND userReview IS NOT NULL AND userReview != ''")
    suspend fun getMoviesWithReviews(): List<Movie>

    /**
     * Obtiene películas puntuadas por el usuario ordenadas por puntuación
     *
     * @return Lista de películas con rating ordenadas de mayor a menor puntuación
     */
    @Query("SELECT * FROM movies WHERE isInLibrary = 1 AND userRating IS NOT NULL ORDER BY userRating DESC")
    suspend fun getRatedMovies(): List<Movie>

    // ============ ESTADÍSTICAS ============

    /**
     * Cuenta el total de películas en la biblioteca personal
     *
     * @return Número de películas en biblioteca
     */
    @Query("SELECT COUNT(*) FROM movies WHERE isInLibrary = 1")
    suspend fun getLibraryCount(): Int

    /**
     * Calcula la puntuación promedio de todas las películas valoradas por el usuario
     *
     * @return Promedio de puntuaciones del usuario o null si no hay películas puntuadas
     */
    @Query("SELECT AVG(userRating) FROM movies WHERE isInLibrary = 1 AND userRating IS NOT NULL")
    suspend fun getAverageUserRating(): Double?

    // ============ LIMPIEZA ============

    /**
     * Elimina películas que no están en la biblioteca (limpieza de caché)
     *
     * Útil para liberar espacio eliminando películas que solo se guardaron temporalmente
     * como caché de búsquedas o listas populares.
     */
    @Query("DELETE FROM movies WHERE isInLibrary = 0")
    suspend fun clearNonLibraryMovies()
}