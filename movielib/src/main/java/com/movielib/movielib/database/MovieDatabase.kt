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


    abstract fun movieDao(): MovieDao

    companion object {

        @Volatile
        private var INSTANCE: MovieDatabase? = null

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

        suspend fun getDatabaseAsync(context: Context): MovieDatabase {
            return getDatabase(context)
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}