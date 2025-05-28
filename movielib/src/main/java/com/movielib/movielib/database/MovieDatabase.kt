package com.movielib.movielib.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.movielib.movielib.models.Movie

/**
 * Base de datos principal usando Room
 *
 * @Database: Anotación que define esta clase como una base de datos Room
 * - entities: Lista de entidades (tablas) que contiene la base de datos
 * - version: Versión de la base de datos (importante para migraciones)
 * - exportSchema: false para evitar warnings en desarrollo
 */
@Database(
    entities = [Movie::class],
    version = 1,
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {

    /**
     * Método abstracto que retorna el DAO
     * Room implementará este método automáticamente
     */
    abstract fun movieDao(): MovieDao

    companion object {
        /**
         * Instancia singleton de la base de datos
         * @Volatile asegura que el valor sea visible para todos los threads
         */
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        /**
         * Obtiene la instancia de la base de datos (patrón Singleton)
         *
         * @param context Contexto de la aplicación
         * @return Instancia de MovieDatabase
         */
        fun getDatabase(context: Context): MovieDatabase {
            // Si la instancia no es null, la retorna
            // Si es null, sincroniza y crea una nueva instancia
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                )
                    .fallbackToDestructiveMigration() // En caso de cambios de esquema sin migración
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Método para obtener la base de datos de forma asíncrona
         * Útil para testing o casos especiales
         */
        suspend fun getDatabaseAsync(context: Context): MovieDatabase {
            return getDatabase(context)
        }

        /**
         * Método para limpiar la instancia (útil para testing)
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}