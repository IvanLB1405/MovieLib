package com.movielib.movielib.utils

import com.movielib.movielib.BuildConfig

/**
 * Constantes globales de la librería MovieLib
 */
object Constants {

    /**
     * API Key de TMDb - Leída de forma segura desde BuildConfig
     */
    val TMDB_API_KEY: String = BuildConfig.TMDB_API_KEY

    // URLs de la API
    const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

    // Tamaños de imagen disponibles
    const val IMAGE_SIZE_W92 = "w92"
    const val IMAGE_SIZE_W154 = "w154"
    const val IMAGE_SIZE_W185 = "w185"
    const val IMAGE_SIZE_W342 = "w342"
    const val IMAGE_SIZE_W500 = "w500"
    const val IMAGE_SIZE_W780 = "w780"
    const val IMAGE_SIZE_ORIGINAL = "original"

    // Configuración de paginación
    const val DEFAULT_PAGE_SIZE = 20
    const val FIRST_PAGE = 1

    // Configuración de base de datos
    const val DATABASE_NAME = "movie_database"
    const val DATABASE_VERSION = 1

    // Timeouts de red (en segundos)
    const val NETWORK_TIMEOUT = 30L

    // Validación de API Key
    fun isApiKeyValid(): Boolean {
        return TMDB_API_KEY.isNotBlank()
    }

    /**
     * Construir URL completa para pósters de películas
     */
    fun buildPosterUrl(posterPath: String?, size: String = IMAGE_SIZE_W500): String? {
        return if (!posterPath.isNullOrBlank()) {
            "$TMDB_IMAGE_BASE_URL$size$posterPath"
        } else {
            null
        }
    }

}
