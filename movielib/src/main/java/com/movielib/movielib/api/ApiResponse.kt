package com.movielib.movielib.api

/**
 * Clase sellada para manejar diferentes estados de respuesta de la API
 *
 * @param T Tipo de datos que contiene la respuesta exitosa
 */
sealed class ApiResponse<out T> {

    /**
     * Respuesta exitosa
     *
     * @param data Datos obtenidos de la API
     */
    data class Success<T>(val data: T) : ApiResponse<T>()

    /**
     * Error en la respuesta
     *
     * @param message Mensaje de error
     * @param code Código de error HTTP (opcional)
     */
    data class Error(val message: String, val code: Int? = null) : ApiResponse<Nothing>()

    /**
     * Estado de carga
     */
    object Loading : ApiResponse<Nothing>()

    /**
     * Estado sin conexión a internet
     */
    object NetworkError : ApiResponse<Nothing>()
}

/**
 * Extensión para verificar si la respuesta es exitosa
 */
fun <T> ApiResponse<T>.isSuccess(): Boolean = this is ApiResponse.Success

/**
 * Extensión para verificar si la respuesta es un error
 */
fun <T> ApiResponse<T>.isError(): Boolean = this is ApiResponse.Error

/**
 * Extensión para verificar si está cargando
 */
fun <T> ApiResponse<T>.isLoading(): Boolean = this is ApiResponse.Loading

/**
 * Extensión para obtener los datos si la respuesta es exitosa
 */
fun <T> ApiResponse<T>.getDataOrNull(): T? {
    return if (this is ApiResponse.Success) this.data else null
}

/**
 * Extensión para obtener el mensaje de error si la respuesta es un error
 */
fun <T> ApiResponse<T>.getErrorMessage(): String? {
    return if (this is ApiResponse.Error) this.message else null
}