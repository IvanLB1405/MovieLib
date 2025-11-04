package com.movielib.movielib.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para manejar las peticiones HTTP a la API de TMDb
 *
 * Implementa patrón Singleton para reutilizar la instancia
 */
object ApiClient {

    private var retrofit: Retrofit? = null

    /**
     * Configuración de OkHttpClient con interceptores
     */
    private fun getOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Obtiene la instancia de Retrofit (Singleton)
     *
     * @return Instancia configurada de Retrofit
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
     * @return Instancia de TMDbService
     */
    fun getTMDbService(): TMDbService {
        return getRetrofit().create(TMDbService::class.java)
    }

    /**
     * Método para limpiar la instancia (útil para testing)
     */
    fun clearInstance() {
        retrofit = null
    }
}