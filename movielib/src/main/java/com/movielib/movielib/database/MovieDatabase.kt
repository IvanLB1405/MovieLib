package com.movielib.movielib.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.movielib.movielib.models.Movie

/**
 * Base de datos principal usando Room (capa de abstracción sobre SQLite)
 *
 * ANOTACIÓN @Database:
 * - entities: Array de clases que representan las tablas de la base de datos
 * - version: Número de versión del schema (se incrementa cuando cambia la estructura)
 * - exportSchema: Si es true, Room exporta el schema a un archivo JSON (útil para tracking)
 *
 * PATRÓN SINGLETON:
 * Esta clase implementa el patrón Singleton para asegurar que solo existe UNA instancia
 * de la base de datos en toda la aplicación. Esto es crítico porque:
 * 1. Evita múltiples conexiones que podrían causar problemas de concurrencia
 * 2. Mejora el rendimiento al reutilizar la misma instancia
 * 3. Ahorra memoria al no crear objetos duplicados
 *
 * @see Movie La entidad principal que se almacena
 * @see MovieDao Interface para operaciones de base de datos
 */
@Database(
    entities = [Movie::class],  // Lista de todas las tablas (entities) de la BD
    version = 1,                // Versión actual del schema
    exportSchema = false        // No exportar schema
)
abstract class MovieDatabase : RoomDatabase() {

    /**
     * Proporciona acceso al DAO (Data Access Object) para operaciones con películas
     *
     * Room genera automáticamente la implementación de esta función abstracta.
     * El DAO contiene todos los métodos para hacer operaciones CRUD en la tabla movies.
     *
     * @return Instancia del MovieDao implementada automáticamente por Room
     */
    abstract fun movieDao(): MovieDao

    companion object {
        /**
         * Instancia única de la base de datos (Singleton pattern)
         *
         * @Volatile asegura que los cambios a INSTANCE sean visibles automaticamente
         * para todos los threads. Necesario en multi-hilo para evitar
         * que un hilo vea una versión desactualizada de la variable.
         *
         * Sin @Volatile, un thread podría cachear el valor de INSTANCE y no ver
         * que otro thread ya la inicializó, creando mas instancias de las que hacen falta.
         */
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos
         *
         * PATRÓN: Double-Checked Locking para inicialización thread-safe
         *
         * FUNCIONAMIENTO:
         * 1. Primera verificación: Si INSTANCE ya existe, la devuelve inmediatamente
         * 2. Si es null, entra en bloque sincronizado para evitar que múltiples threads creen instancias
         * 3. Segunda verificación: Verifica de nuevo porque otro thread podría haberla creado
         * 4. Si sigue siendo null, crea la instancia con Room.databaseBuilder
         *
         * SYNCHRONIZED:
         * - Asegura que solo un thread pueda ejecutar el bloque a la vez
         * - Previene que dos threads puedan crear dos instancias
         *
         * @param context Contexto de Android necesario para crear la base de datos
         * @return Instancia única de MovieDatabase
         */
        fun getDatabase(context: Context): MovieDatabase {
            // Primer check: si ya existe, retornar inmediatamente
            return INSTANCE ?: synchronized(this) {
                // Segundo check: dentro del bloque sincronizado por si otro thread la creó
                val instance = Room.databaseBuilder(
                    context.applicationContext,  // Usar applicationContext para evitar leaks de memoria
                    MovieDatabase::class.java,    // Clase de la base de datos
                    "movie_database"              // Nombre del archivo SQLite en el dispositivo
                )
                    // IMPORTANTE: fallbackToDestructiveMigration() elimina y recrea la BD
                    // si detecta un cambio de versión sin una migración definida.
                    .fallbackToDestructiveMigration()
                    .build()

                // Guardamos la instancia en INSTANCE para futuras llamadas
                INSTANCE = instance
                // Retornar la instancia recién creada
                instance
            }
        }

        /**
         * Versión asíncrona de getDatabase() para compatibilidad con código suspendible
         *
         * En realidad simplemente llama a getDatabase() que ya es seguro llamar desde
         * cualquier contexto.
         *
         * @param context Contexto de Android
         * @return Instancia de la base de datos
         */
        suspend fun getDatabaseAsync(context: Context): MovieDatabase {
            return getDatabase(context)
        }

        /**
         * Cierra la base de datos y limpia la instancia
         *
         * Solo para casos específicos como:
         * - Tests unitarios que necesitan reiniciar el estado
         * - Cuando la app se cierra completamente
         *
         * NO llamar en código normal porque:
         * - Room gestiona automáticamente las conexiones
         * - Cerrar la BD podría causar crashes si hay operaciones pendientes
         * - La próxima llamada tendría que recrear toda la instancia
         */
        fun closeDatabase() {
            INSTANCE?.close()  // Cerrar la base de datos si existe
            INSTANCE = null    // Limpiar la referencia para que se pueda crear una nueva
        }
    }

}
