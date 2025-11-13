package com.movielib.base

import androidx.appcompat.app.AppCompatActivity
import com.movielib.movielib.database.MovieDatabase
import com.movielib.movielib.repository.MovieRepository
import com.movielib.movielib.utils.Constants

/**
 * Activity base para todas las pantallas relacionadas con películas
 *
 *  Funcionalidad compartida para todas las activities que necesitan acceso a datos de películas:
 * - Inicialización perezosa de [MovieRepository] (creado solo cuando se accede por primera vez)
 * - Acceso a la base de datos Room mediante [MovieDatabase]
 * - Gestión centralizada del repository para evitar repetirnos
 *
 * Todas las activities que interactúan con películas deben extender esta clase en lugar de
 * [AppCompatActivity] directamente.
 *
 * @see MovieRepository
 * @see MovieDatabase
 */
abstract class BaseMovieActivity : AppCompatActivity() {

    /**
     * Instancia del repositorio de películas compartida entre todas las activities
     *
     * Inicializada en lazy para evitar crear instancias innecesarias. Proporciona acceso a:
     * - Operaciones de la API de TMDb
     * - Operaciones de base de datos local (Room)
     * - Funciones de gestión de biblioteca
     */
    protected val repository: MovieRepository by lazy {
        val database = MovieDatabase.getDatabase(this)
        MovieRepository(database.movieDao(), Constants.TMDB_API_KEY)
    }
}

