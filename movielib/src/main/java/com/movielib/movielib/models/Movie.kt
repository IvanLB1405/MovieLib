package com.movielib.movielib.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa una película en la base de datos local
 */
@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey
    val id: Int,
    val title: String,
    val overview: String?,
    val posterPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val genres: String? = null,        // JSON string de géneros
    val cast: String? = null,          // JSON string del reparto
    val isInLibrary: Boolean = false,  // Si está en la biblioteca personal
    val userRating: Float? = null,     // Puntuación del usuario (1-10)
    val userReview: String? = null,    // Reseña del usuario
    val dateAdded: Long? = null        // Timestamp cuando se añadió a la biblioteca
)

/**
 * Modelo para la respuesta de la API de búsqueda de películas
 */
data class MovieSearchResponse(
    val page: Int,
    val results: List<MovieApiModel>,
    val totalPages: Int,
    val totalResults: Int
)

/**
 * Modelo que representa una película como viene de la API externa
 */
data class MovieApiModel(
    val id: Int,
    val title: String,
    val overview: String?,
    val poster_path: String?,
    val release_date: String?,
    val vote_average: Double,
    val genre_ids: List<Int>? = null
)

/**
 * Modelo para detalles completos de una película desde la API
 */
data class MovieDetailApiModel(
    val id: Int,
    val title: String,
    val overview: String?,
    val poster_path: String?,
    val release_date: String?,
    val vote_average: Double,
    val genres: List<Genre>,
    val credits: Credits?
)

/**
 * Modelo para géneros de películas
 */
data class Genre(
    val id: Int,
    val name: String
)

/**
 * Modelo para créditos (reparto y equipo)
 */
data class Credits(
    val cast: List<CastMember>
)

/**
 * Modelo para miembros del reparto
 */
data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profile_path: String?
)

/**
 * Extensiones para convertir entre modelos de API y entidades Room
 */
fun MovieApiModel.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        overview = this.overview,
        posterPath = this.poster_path,
        releaseDate = this.release_date,
        voteAverage = this.vote_average
    )
}

fun MovieDetailApiModel.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        overview = this.overview,
        posterPath = this.poster_path,
        releaseDate = this.release_date,
        voteAverage = this.vote_average,
        genres = this.genres.joinToString(",") { it.name },
        cast = this.credits?.cast?.take(5)?.joinToString(",") { it.name }
    )
}