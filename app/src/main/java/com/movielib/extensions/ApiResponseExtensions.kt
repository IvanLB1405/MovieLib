package com.movielib.extensions

import com.movielib.movielib.api.ApiResponse

/**
 * Funciones de extensión para simplificar el manejo de ApiResponse
 */

/**
 * Ejecuta una acción si la respuesta es Success
 *
 * @param action Acción a ejecutar con los datos
 * @return true si es Success, false en caso contrario
 */
inline fun <T> ApiResponse<T>.onSuccess(crossinline action: (T) -> Unit): ApiResponse<T> {
    if (this is ApiResponse.Success) {
        action(this.data)
    }
    return this
}

/**
 * Ejecuta una acción si la respuesta es Error
 *
 * @param action Acción a ejecutar con el mensaje de error y código
 * @return this para encadenamiento
 */
inline fun <T> ApiResponse<T>.onError(crossinline action: (String, Int?) -> Unit): ApiResponse<T> {
    if (this is ApiResponse.Error) {
        action(this.message, this.code)
    }
    return this
}

/**
 * Ejecuta una acción si la respuesta es NetworkError
 *
 * @param action Acción a ejecutar
 * @return this para encadenamiento
 */
inline fun <T> ApiResponse<T>.onNetworkError(crossinline action: () -> Unit): ApiResponse<T> {
    if (this is ApiResponse.NetworkError) {
        action()
    }
    return this
}

/**
 * Ejecuta una acción si la respuesta es Loading
 *
 * @param action Acción a ejecutar
 * @return this para encadenamiento
 */
inline fun <T> ApiResponse<T>.onLoading(crossinline action: () -> Unit): ApiResponse<T> {
    if (this is ApiResponse.Loading) {
        action()
    }
    return this
}

/**
 * Maneja todos los estados de ApiResponse con lambdas
 *
 * @param onLoading Acción para estado Loading
 * @param onSuccess Acción para estado Success
 * @param onError Acción para estado Error
 * @param onNetworkError Acción para estado NetworkError
 */
inline fun <T> ApiResponse<T>.handle(
    crossinline onLoading: () -> Unit = {},
    crossinline onSuccess: (T) -> Unit,
    crossinline onError: (String, Int?) -> Unit = { _, _ -> },
    crossinline onNetworkError: () -> Unit = {}
) {
    when (this) {
        is ApiResponse.Loading -> onLoading()
        is ApiResponse.Success -> onSuccess(this.data)
        is ApiResponse.Error -> onError(this.message, this.code)
        is ApiResponse.NetworkError -> onNetworkError()
    }
}
