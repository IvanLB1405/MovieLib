package com.movielib.movielib.api

import com.movielib.movielib.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para manejar las peticiones HTTP a la API de TMDb
 *
 * Patrón Singleton para reutilizar la instancia de Retrofit y optimizar.
 * Configura automáticamente timeouts y conversión JSON con Gson.
 *
 * Características:
 * - Conversión automática JSON con Gson
 * - Reutilización de conexiones HTTP
 *
 * @see TMDbService
 */
object ApiClient {

    private var retrofit: Retrofit? = null

    /**
     * Configuración de OkHttpClient con interceptores
     *
     * NOTA: El logging interceptor solo se activa en modo DEBUG para evitar
     * exposición de datos sensibles en producción y mejorar el rendimiento
     */
    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // Solo añadir logging en modo debug
            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                addInterceptor(loggingInterceptor)
            }
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
        }.build()
    }

    /**
     * Obtiene la instancia de Retrofit (Singleton)
     *
     * Crea la instancia de Retrofit solo en la primera llamada. Las llamadas
     * posteriores reutilizan la misma instancia para optimizar.
     *
     * @return Instancia configurada de Retrofit con OkHttpClient y Gson
     */
    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(TMDbService.BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    /**
     * Obtiene la instancia del servicio TMDb
     *
     * Método público principal para obtener acceso a la interfaz TMDbService.
     * Todas las llamadas a la API deben hacerse a través de esta instancia.
     *
     * @return Instancia de TMDbService configurada y lista para usar
     * @see TMDbService
     */
    fun getTMDbService(): TMDbService {
        return getRetrofit().create(TMDbService::class.java)
    }

    /**
     * Limpia la instancia de Retrofit para forzar recreación
     *
     * Útil principalmente para testing cuando se necesita reiniciar el estado
     * del cliente HTTP. Si se implementa la libreria no deberia usarse en produccion.
     */
    fun clearInstance() {
        retrofit = null
    }

}
