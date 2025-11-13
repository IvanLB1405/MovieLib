package com.movielib.extensions

import com.movielib.movielib.api.ApiResponse

/**
 * Funciones de extensión para simplificar el manejo de ApiResponse
 *
 * FUNCIONES DE EXTENSIÓN (Extension Functions):
 * Añade funciones a clases que ya existen sin heredar ni modificar el codigo
 * Aquí añado funciones a ApiResponse<T> que simplifican el manejo de respuestas.
 *
 * INLINE FUNCTIONS:
 * Están marcadas con "inline" para mejorar el rendimiento.
 * El compilador copiaría el código de la función inline directamente en el lugar de llamada,
 * evitando crear objetos para las lambdas.
 */

/**
 * Ejecuta una acción si la respuesta es Success
 *
 * PATRÓN BUILDER:
 * Retorna "this" para permitir encadenamiento de llamadas:
 * response.onSuccess { ... }.onError { ... }
 *
 * EJEMPLO DE USO:
 * ```kotlin
 * repository.searchMovies("Inception").collect { response ->
 *     response.onSuccess { movies ->
 *         // Hacer algo con las películas
 *         adapter.submitList(movies)
 *     }
 * }
 * ```
 *
 * @param action Lambda que recibe los datos (T) y no retorna nada (Unit)
 * @return this para permitir encadenamiento
 */
inline fun <T> ApiResponse<T>.onSuccess(crossinline action: (T) -> Unit): ApiResponse<T> {
    // Smart cast: Kotlin ya sabe que dentro del if, this seria ApiResponse.Success
    if (this is ApiResponse.Success) {
        action(this.data)
    }
    // Retornar this para encadenar
    return this
}

/**
 * Ejecuta una acción si la respuesta es Error
 *
 * MANEJO DE ERRORES:
 * Proporciona acceso tanto al mensaje de error como al código HTTP (si está disponible).
 *
 * LAMBDA CON PARÁMETROS:
 * La lambda recibe dos parámetros: (String, Int?) -> Unit
 * - String: mensaje de error descriptivo
 * - Int?: código HTTP opcional (null si no es error HTTP)
 *
 * EJEMPLO DE USO:
 * ```kotlin
 * response.onError { message, code ->
 *     showError("Error $code: $message")
 * }
 * ```
 *
 * @param action Lambda que recibe mensaje y código de error
 * @return this para encadenamiento
 */
inline fun <T> ApiResponse<T>.onError(crossinline action: (String, Int?) -> Unit): ApiResponse<T> {
    if (this is ApiResponse.Error) {
        action(this.message, this.code)
    }
    return this
}

/**
 * Ejecuta una acción si la respuesta es NetworkError (sin conexión)
 *
 * ERROR DE RED vs ERROR HTTP:
 * - NetworkError: Dispositivo sin internet, timeout, etc.
 * - Error: Respuesta HTTP con código de error
 *
 * LAMBDA SIN PARÁMETROS:
 * () -> Unit significa una función que no recibe nada y no retorna nada.
 *
 * EJEMPLO DE USO:
 * ```kotlin
 * response.onNetworkError {
 *     showSnackbar("Sin conexión a internet")
 * }
 * ```
 *
 * @param action Lambda a ejecutar cuando hay error de red
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
 * ESTADO DE CARGA:
 * ApiResponse.Loading se emite al inicio de cada operación de red para que la UI
 * pueda mostrar indicadores de progreso (spinners, esqueletos...).
 *
 * EJEMPLO DE USO:
 * ```kotlin
 * response.onLoading {
 *     progressBar.visibility = View.VISIBLE
 * }
 * ```
 *
 * @param action Lambda a ejecutar durante la carga
 * @return this para encadenamiento
 */
inline fun <T> ApiResponse<T>.onLoading(crossinline action: () -> Unit): ApiResponse<T> {
    if (this is ApiResponse.Loading) {
        action()
    }
    return this
}

/**
 * Maneja todos los estados de ApiResponse con lambdas en una sola llamada
 *
 * PARÁMETROS POR DEFECTO:
 * Algunos parámetros tienen valores por defecto ({} = lambda vacía), lo que los hace opcionales.
 * Solo onSuccess es obligatorio porque es lo más habitual.
 *
 * WHEN EXPRESSION:
 * Es como switch de Java:
 * - Es una expresión (retorna valor)
 * - Exhaustiva para sealed classes (compilador verifica todos los casos)
 * - Soporta smart casting automático
 *
 * EJEMPLO DE USO COMPLETO:
 * ```kotlin
 * repository.getMovieDetails(123).collect { response ->
 *     response.handle(
 *         onLoading = { showLoading() },
 *         onSuccess = { movie -> displayMovie(movie) },
 *         onError = { msg, code -> showError(msg) },
 *         onNetworkError = { showOfflineMessage() }
 *     )
 * }
 * ```
 *
 * EJEMPLO DE USO MÍNIMO (solo success):
 * ```kotlin
 * response.handle(
 *     onSuccess = { movies -> adapter.submitList(movies) }
 * )
 * ```
 *
 * @param onLoading Lambda para estado Loading (opcional)
 * @param onSuccess Lambda para estado Success (REQUERIDO)
 * @param onError Lambda para estado Error (opcional)
 * @param onNetworkError Lambda para estado NetworkError (opcional)
 */
inline fun <T> ApiResponse<T>.handle(
    crossinline onLoading: () -> Unit = {},  // Lambda vacía por defecto
    crossinline onSuccess: (T) -> Unit,      // REQUERIDO (sin valor por defecto)
    crossinline onError: (String, Int?) -> Unit = { _, _ -> },  // Ignora parámetros por defecto
    crossinline onNetworkError: () -> Unit = {}
) {
    // When es exhaustivo: el compilador verifica que cubramos todos los casos de ApiResponse
    when (this) {
        is ApiResponse.Loading -> onLoading()
        is ApiResponse.Success -> onSuccess(this.data)  // Smart cast: this.data está disponible
        is ApiResponse.Error -> onError(this.message, this.code)
        is ApiResponse.NetworkError -> onNetworkError()
    }
}

