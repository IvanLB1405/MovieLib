package com.movielib.movielib.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa una película en la base de datos local
 *
 * Esta clase cumple dos funciones principales:
 * 1. Almacena los datos de películas obtenidos de la API de TMDb (en caché)
 * 2. Guarda la información personalizada del usuario (biblioteca, ratings, reseñas)
 *
 * @property id Identificador único de la película en TMDb (clave primaria de la base de datos)
 * @property title Título de la película
 * @property overview Sinopsis o descripción de la película (puede ser null)
 * @property posterPath Ruta relativa del póster en TMDb (se combina con IMAGE_BASE_URL para obtener la URL completa)
 * @property releaseDate Fecha de estreno en formato "YYYY-MM-DD" (puede ser null)
 * @property voteAverage Puntuación promedio de TMDb (0.0 - 10.0)
 * @property genres Géneros de la película como string separado por comas
 * @property cast Reparto principal como string separado por comas
 * @property isInLibrary Flag que indica si el usuario añadió esta película a su biblioteca personal
 * @property userRating Puntuación personal del usuario (0.0 - 10.0), null si no ha puntuado
 * @property userReview Reseña escrita por el usuario, null si no ha escrito una
 * @property dateAdded Timestamp de cuando se añadió a la biblioteca
 *
 * @see MovieDao Para operaciones de base de datos con esta entidad
 * @see MovieApiModel Para el modelo que viene directamente de la API
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
 *
 * TMDb devuelve resultados paginados para no sobrecargar la red.
 *
 * @property page Número de página actual (primera página = 1)
 * @property results Lista de películas en esta página
 * @property totalPages Total de páginas disponibles
 * @property totalResults Total de películas encontradas (en todas las páginas)
 */
data class MovieSearchResponse(
    val page: Int,
    val results: List<MovieApiModel>,
    val totalPages: Int,
    val totalResults: Int
)

/**
 * Modelo que representa una película como viene de la API externa (TMDb)
 *
 * NOTA: Los nombres de propiedades usan snake_case porque vienen así de la API JSON.
 * Gson los convierte automáticamente sin necesidad de anotaciones @SerializedName.
 *
 * @property id Identificador único de la película en TMDb
 * @property title Título de la película
 * @property overview Sinopsis o descripción
 * @property poster_path Ruta relativa del póster (hay que concatenar con BASE_URL)
 * @property release_date Fecha de estreno en formato "YYYY-MM-DD"
 * @property vote_average Puntuación promedio de la comunidad (0.0 - 10.0)
 * @property genre_ids Lista de IDs de géneros (se usa para búsquedas simples)
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
 *
 * Este modelo se usa cuando se obtienen todos los detalles de una película específica,
 * incluyendo géneros (nombres completos) y créditos (reparto).
 *
 * @property id Identificador único de la película
 * @property title Título de la película
 * @property overview Sinopsis o descripción completa
 * @property poster_path Ruta del póster
 * @property release_date Fecha de estreno
 * @property vote_average Puntuación promedio
 * @property genres Lista completa de géneros con ID y nombre
 * @property credits Información del reparto y equipo (opcional)
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
 *
 * @property id Identificador único del género en TMDb
 * @property name Nombre del género (ej: "Acción", "Comedia", "Drama")
 */
data class Genre(
    val id: Int,
    val name: String
)

/**
 * Modelo para créditos (reparto y equipo)
 * @property cast Lista de actores que participan en la película
 */
data class Credits(
    val cast: List<CastMember>
)

/**
 * Modelo para miembros del reparto
 *
 * @property id Identificador único del actor en TMDb
 * @property name Nombre real del actor
 * @property character Nombre del personaje que interpreta
 * @property profile_path Ruta de la foto del actor (puede ser null)
 */
data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profile_path: String?
)

// FUNCIONES DE EXTENSIÓN 

/**
 * Extensión para convertir un modelo simple de API a entidad Room
 *
 * Esta función la usamos cuando obtenemos listas de películas (búsquedas, populares, etc.)
 * donde solo vienen los datos básicos sin géneros ni reparto detallado.
 *
 * Los campos de usuario (isInLibrary, userRating, etc.) se inicializan con valores por defecto.
 * Si la película ya existe en la base de datos, Room usa REPLACE preservando
 * los datos del usuario si se manejan correctamente en el Repository.
 *
 * @return Objeto Movie listo para guardar en la base de datos Room
 */
fun MovieApiModel.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        overview = this.overview,
        posterPath = this.poster_path,
        releaseDate = this.release_date,
        voteAverage = this.vote_average
        // Los demás campos usan valores por defecto de la data class Movie
    )
}

/**
 * Extensión para convertir un modelo detallado de API a entidad Room
 *
 * Esta función la usamos cuando obtenemos detalles completos de una película específica.
 * Incluye la conversión de géneros y reparto a strings separados por comas para
 * almacenamiento simple en SQLite (sin necesidad de tablas relacionales).
 *
 * CONVERSIONES APLICADAS:
 * - Géneros: Lista de objetos Genre → String "Acción,Aventura,Ciencia ficción"
 * - Reparto: Lista de CastMember → String "Actor1,Actor2,Actor3"
 *
 * NOTA: Solo se guardan los primeros 5 actores para ahorrar espacio en DB.
 *
 * @return Objeto Movie con información completa listo para Room
 */
fun MovieDetailApiModel.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        overview = this.overview,
        posterPath = this.poster_path,
        releaseDate = this.release_date,
        voteAverage = this.vote_average,
        // Convertir lista de géneros a string separado por comas
        genres = this.genres.joinToString(",") { it.name },
        // Tomar solo los primeros 5 actores y unir sus nombres con comas
        cast = this.credits?.cast?.take(5)?.joinToString(",") { it.name }
    )

}
