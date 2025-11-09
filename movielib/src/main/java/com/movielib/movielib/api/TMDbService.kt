package com.movielib.movielib.api

import com.movielib.movielib.models.MovieDetailApiModel
import com.movielib.movielib.models.MovieSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de Retrofit para la API de TMDb (The Movie Database)
 *
 * Proporciona acceso a los endpoints principales de TMDb para buscar películas,
 * obtener detalles, listas populares y mejor valoradas. Todas las funciones son
 * suspendibles para uso con Kotlin Coroutines.
 *
 * Documentación oficial de la API: https://developers.themoviedb.org/3
 * Base URL: https://api.themoviedb.org/3/
 *
 * @see ApiClient
 * @see MovieRepository
 */
interface TMDbService {

    /**
     * Buscar películas por texto
     *
     * @param apiKey Clave de API de TMDb
     * @param query Término de búsqueda
     * @param page Número de página (por defecto 1)
     * @param language Idioma de los resultados (por defecto español)
     * @return Response con lista de películas encontradas
     */
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "es-ES"
    ): Response<MovieSearchResponse>

    /**
     * Obtener detalles completos de una película
     *
     * @param movieId ID de la película
     * @param apiKey Clave de API de TMDb
     * @param language Idioma de los resultados
     * @param appendToResponse Información adicional a incluir (credits para reparto)
     * @return Response con detalles completos de la película
     */
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("append_to_response") appendToResponse: String = "credits"
    ): Response<MovieDetailApiModel>

    /**
     * Obtener películas populares
     *
     * @param apiKey Clave de API de TMDb
     * @param page Número de página
     * @param language Idioma de los resultados
     * @return Response con películas populares
     */
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "es-ES"
    ): Response<MovieSearchResponse>

    /**
     * Obtener películas mejor valoradas
     *
     * @param apiKey Clave de API de TMDb
     * @param page Número de página
     * @param language Idioma de los resultados
     * @return Response con películas mejor valoradas
     */
    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "es-ES"
    ): Response<MovieSearchResponse>

    /**
     * Obtener películas que están actualmente en cines
     *
     * @param apiKey Clave de API de TMDb
     * @param page Número de página
     * @param language Idioma de los resultados
     * @return Response con películas en cines
     */
    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "es-ES"
    ): Response<MovieSearchResponse>

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
        const val POSTER_SIZE_W500 = "w500"
        const val POSTER_SIZE_ORIGINAL = "original"

        /**
         * Construir URL completa para imágenes de pósters
         *
         * @param posterPath Ruta del póster (viene de la API)
         * @param size Tamaño deseado (w500 por defecto)
         * @return URL completa de la imagen
         */
        fun getPosterUrl(posterPath: String?, size: String = POSTER_SIZE_W500): String? {
            return if (posterPath != null) {
                "$IMAGE_BASE_URL$size$posterPath"
            } else {
                null
            }
        }
    }
}