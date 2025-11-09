# 📚 Guía Educativa Completa de MovieLib
## Aprende Arquitectura Android con Kotlin - De Cero a Experto

> **Autor:** Profesor Especialista en Android & Kotlin
> **Nivel:** Intermedio (se requieren conocimientos básicos de programación orientada a objetos)
> **Duración estimada:** 6-8 horas de estudio
> **Última actualización:** Enero 2025

---

## 🎯 Objetivos de Aprendizaje

Al finalizar esta guía, comprenderás:
1. Cómo funciona la arquitectura Clean Architecture en Android
2. El patrón Repository y por qué es importante
3. Programación reactiva con Kotlin Flow
4. Manejo de base de datos con Room
5. Consumo de APIs REST con Retrofit
6. Testing en Android (unitario e instrumentado)

---

## 📖 Tabla de Contenidos

1. [Introducción: La Analogía del Restaurante](#introducción-la-analogía-del-restaurante)
2. [Arquitectura General](#arquitectura-general)
3. [Capa de Datos (Data Layer)](#capa-de-datos-data-layer)
   - [Movie.kt - El Modelo de Datos](#1-moviekt---el-modelo-de-datos)
   - [ApiResponse.kt - El Sobre de Respuestas](#2-apiresponsekt---el-sobre-de-respuestas)
   - [TMDbService.kt - El Camarero del Restaurante](#3-tmdbservicekt---el-camarero-del-restaurante)
   - [ApiClient.kt - El Chef de la Cocina](#4-apiclientkt---el-chef-de-la-cocina)
   - [MovieDao.kt - El Archivero](#5-moviedaokt---el-archivero)
   - [MovieDatabase.kt - El Almacén](#6-moviedatabasekt---el-almacén)
4. [Capa de Dominio (Business Logic)](#capa-de-dominio-business-logic)
   - [MovieRepository.kt - El Gerente del Restaurante](#7-movierepositorykt---el-gerente-del-restaurante)
5. [Capa de Presentación (UI Layer)](#capa-de-presentación-ui-layer)
   - [BaseMovieActivity.kt - El Plano Base](#8-basemovieactivitykt---el-plano-base)
   - [MainActivity.kt - La Pantalla Principal](#9-mainactivitykt---la-pantalla-principal)
   - [SearchActivity.kt - La Búsqueda](#10-searchactivitykt---la-búsqueda)
   - [MovieDetailActivity.kt - Los Detalles](#11-moviedetailactivitykt---los-detalles)
   - [LibraryActivity.kt - La Biblioteca Personal](#12-libraryactivitykt---la-biblioteca-personal)
6. [Utilidades y Extensiones](#utilidades-y-extensiones)
   - [ApiResponseExtensions.kt - Atajos Mágicos](#13-apiresponseextensionskt---atajos-mágicos)
   - [Constants.kt - La Configuración](#14-constantskt---la-configuración)
7. [Testing](#testing)
8. [Conceptos Clave de Kotlin](#conceptos-clave-de-kotlin)
9. [Flujo Completo de Datos](#flujo-completo-de-datos)
10. [Preguntas Frecuentes](#preguntas-frecuentes)

---

## Introducción: La Analogía del Restaurante

Imagina que **MovieLib es un restaurante de películas**. Vamos a usar esta analogía a lo largo de toda la guía:

- **Los clientes** (usuarios) vienen a buscar películas
- **El menú** es la interfaz de usuario (Activities)
- **Los camareros** (TMDbService) toman los pedidos y los llevan a la cocina
- **La cocina** (TMDb API) prepara los datos frescos
- **El almacén** (Room Database) guarda ingredientes para uso posterior
- **El gerente** (MovieRepository) coordina todo: camareros, cocina y almacén
- **Las recetas** (Models) describen cómo debe verse cada plato

Con esta analogía en mente, ¡comencemos!

---

## Arquitectura General

### El Edificio de Tres Plantas

```
┌─────────────────────────────────────────────────────┐
│          PLANTA 3: UI Layer (Presentación)          │
│  [Activities] - Los clientes ven el menú aquí      │
│     MainActivity, SearchActivity, etc.              │
└─────────────────────────────────────────────────────┘
                        ↕
┌─────────────────────────────────────────────────────┐
│       PLANTA 2: Domain Layer (Lógica Negocio)       │
│  [MovieRepository] - El gerente que coordina todo   │
└─────────────────────────────────────────────────────┘
                        ↕
┌─────────────────────────────────────────────────────┐
│          PLANTA 1: Data Layer (Datos)               │
│  ┌──────────────┐              ┌──────────────┐    │
│  │ API (Retrofit)│              │ DB (Room)    │    │
│  │ Internet     │              │ Local        │    │
│  └──────────────┘              └──────────────┘    │
└─────────────────────────────────────────────────────┘
```

**Principio fundamental:** La información fluye de abajo hacia arriba, y las instrucciones fluyen de arriba hacia abajo.

---

## Capa de Datos (Data Layer)

Esta es la planta baja de nuestro edificio. Aquí viven los datos.

---

### 1. Movie.kt - El Modelo de Datos

**Archivo:** `movielib/src/main/java/com/movielib/movielib/models/Movie.kt`

#### Analogía: La Ficha Técnica de una Película

Imagina que tienes una **ficha de cartulina** donde anotas toda la información de una película. Esa ficha es la clase `Movie`.

#### Código Explicado:

```kotlin
@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String?,
    val posterPath: String?,
    // ... más campos
)
```

**Desglose:**

1. **`@Entity(tableName = "movies")`**
   - **Qué significa:** Esta clase representa una tabla en la base de datos
   - **Analogía:** Es como decir "esta ficha va en el archivador etiquetado 'movies'"
   - **En otros lenguajes:** Similar a `@Table` en Hibernate (Java) o `[Table("movies")]` en C# Entity Framework

2. **`data class`**
   - **Qué significa:** Una clase especial de Kotlin que viene con superpoderes
   - **Superpoderes automáticos:**
     - `equals()` - Comparar dos películas
     - `hashCode()` - Identificación única
     - `toString()` - Convertir a texto legible
     - `copy()` - Crear una copia modificada
   - **En Java:** Tendrías que escribir estos métodos manualmente (¡50+ líneas!)

3. **`@PrimaryKey val id: Int`**
   - **`@PrimaryKey`:** Identificador único (como el DNI de una persona)
   - **`val`:** Valor inmutable (no se puede cambiar después de crearlo)
   - **`Int`:** Número entero
   - **Analogía:** El número de expediente en una ficha

4. **`String?` (con interrogación)**
   - **Qué significa:** Este campo puede ser `null` (vacío)
   - **Ejemplo:** Una película puede no tener descripción
   - **En Java:** Sería `@Nullable String`
   - **Kotlin Safety:** El compilador te obliga a manejar el caso nulo

#### Ejemplo de Uso:

```kotlin
// Crear una película
val movie = Movie(
    id = 550,
    title = "Fight Club",
    overview = "Un oficinista insomne...",
    posterPath = "/poster.jpg",
    releaseDate = "1999-10-15",
    voteAverage = 8.4,
    genres = "Drama, Thriller",
    cast = "Brad Pitt, Edward Norton",
    isInLibrary = false,
    userRating = null,
    userReview = null,
    dateAdded = null
)

// Crear una copia con cambios (inmutabilidad)
val movieEnBiblioteca = movie.copy(
    isInLibrary = true,
    dateAdded = System.currentTimeMillis()
)

// El original NO cambia
println(movie.isInLibrary) // false
println(movieEnBiblioteca.isInLibrary) // true
```

#### Conceptos Importantes:

**Immutability (Inmutabilidad):**
```kotlin
// ❌ INCORRECTO - no compila
movie.title = "Nuevo título" // Error: val cannot be reassigned

// ✅ CORRECTO - crear nueva instancia
val updatedMovie = movie.copy(title = "Nuevo título")
```

**Nullable Types:**
```kotlin
val overview: String? = movie.overview

// Sin el ?, esto podría causar NullPointerException
// Kotlin te obliga a manejar el null
if (overview != null) {
    println(overview.length)
} else {
    println("Sin descripción")
}

// O usando el operador seguro ?. (Elvis)
println(overview?.length ?: 0) // Si es null, retorna 0
```

---

### 2. ApiResponse.kt - El Sobre de Respuestas

**Archivo:** `movielib/src/main/java/com/movielib/movielib/api/ApiResponse.kt`

#### Analogía: Los Diferentes Estados de un Pedido de Comida

Cuando pides comida a domicilio, tu pedido puede estar en diferentes estados:
- 📦 **Loading:** "Preparando tu pedido..."
- ✅ **Success:** "¡Pedido entregado! Aquí está tu pizza"
- ❌ **Error:** "Lo sentimos, no tenemos ingredientes" (error 404)
- 🔌 **NetworkError:** "Sin conexión a internet"

#### Código Explicado:

```kotlin
sealed class ApiResponse<out T> {
    object Loading : ApiResponse<Nothing>()
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResponse<Nothing>()
    object NetworkError : ApiResponse<Nothing>()
}
```

**Desglose:**

1. **`sealed class`**
   - **Qué significa:** Una clase con un número limitado de subclases conocidas
   - **Ventaja:** El compilador sabe TODAS las posibilidades
   - **En otros lenguajes:** Similar a Enum pero más potente
   - **Analogía:** Un semáforo solo puede estar en rojo, amarillo o verde (no puede estar en azul)

2. **`<out T>`**
   - **T es un genérico:** Puede ser cualquier tipo (Movie, List<Movie>, etc.)
   - **`out` (covarianza):** Solo puede salir, no entrar
   - **Ejemplo:** `ApiResponse<Movie>`, `ApiResponse<List<Movie>>`
   - **En Java:** Sería `? extends T`

3. **`object Loading`**
   - **Singleton:** Solo existe UNA instancia en toda la aplicación
   - **No lleva datos:** Solo indica estado
   - **En Java:** Tendrías que usar patrón Singleton manualmente

4. **`data class Success<T>(val data: T)`**
   - **Lleva datos:** El resultado exitoso
   - **Genérico:** Puede llevar cualquier tipo de dato

#### Ejemplo de Uso Completo:

```kotlin
// Función que retorna ApiResponse
suspend fun searchMovies(query: String): ApiResponse<List<Movie>> {
    return try {
        // 1. Indicar que está cargando
        emit(ApiResponse.Loading)

        // 2. Hacer petición a la API
        val response = api.searchMovies(query)

        // 3. Si es exitoso
        if (response.isSuccessful) {
            val movies = response.body()?.results ?: emptyList()
            ApiResponse.Success(movies) // ✅
        } else {
            ApiResponse.Error("Error ${response.code()}", response.code()) // ❌
        }
    } catch (e: IOException) {
        ApiResponse.NetworkError // 🔌
    }
}

// Consumir la respuesta
when (val response = searchMovies("Inception")) {
    is ApiResponse.Loading -> {
        // Mostrar spinner de carga
        showLoadingSpinner()
    }
    is ApiResponse.Success -> {
        // Tenemos datos! response.data es List<Movie>
        val movies = response.data
        displayMovies(movies)
    }
    is ApiResponse.Error -> {
        // Mostrar error al usuario
        showError("Error: ${response.message}")
    }
    is ApiResponse.NetworkError -> {
        // Sin internet
        showError("Sin conexión a internet")
    }
}
```

#### Ventaja sobre Excepciones:

**❌ Enfoque tradicional (con excepciones):**
```kotlin
try {
    val movies = searchMovies("Inception")
    displayMovies(movies)
} catch (e: NetworkException) {
    showError("Sin conexión")
} catch (e: ServerException) {
    showError("Error del servidor")
} catch (e: Exception) {
    showError("Error desconocido")
}
```

**✅ Enfoque con ApiResponse (más seguro):**
```kotlin
// El compilador te OBLIGA a manejar todos los casos
when (val response = searchMovies("Inception")) {
    is ApiResponse.Success -> displayMovies(response.data)
    is ApiResponse.Error -> showError(response.message)
    is ApiResponse.NetworkError -> showError("Sin conexión")
    is ApiResponse.Loading -> showLoading()
    // Si falta un caso, el compilador da error
}
```

---

### 3. TMDbService.kt - El Camarero del Restaurante

**Archivo:** `movielib/src/main/java/com/movielib/movielib/api/TMDbService.kt`

#### Analogía: El Camarero que Toma Pedidos

El camarero (TMDbService) no cocina, solo toma tu pedido y lo lleva a la cocina (API de TMDb en Internet). Luego trae la comida de vuelta.

#### Código Explicado:

```kotlin
interface TMDbService {

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "es-ES"
    ): Response<MovieSearchResponse>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("append_to_response") appendToResponse: String = "credits"
    ): Response<MovieDetailApiModel>
}
```

**Desglose:**

1. **`interface`**
   - **Qué significa:** Solo define el contrato, no la implementación
   - **Quién implementa:** Retrofit genera automágicamente el código
   - **Analogía:** Es el menú del restaurante, no la receta

2. **`@GET("search/movie")`**
   - **Anotación de Retrofit:** Indica método HTTP GET
   - **Ruta:** Se añade a la URL base
   - **URL completa:** `https://api.themoviedb.org/3/search/movie`
   - **En otros frameworks:** Similar a `[HttpGet("search/movie")]` en ASP.NET

3. **`suspend fun`**
   - **Función suspendible:** Puede pausarse y reanudarse
   - **Para qué:** Operaciones largas sin bloquear la UI
   - **Analogía:** Como poner una lavadora y hacer otras cosas mientras lava
   - **Sin suspend:** La app se "congelaría" esperando la respuesta

4. **`@Query("api_key")`**
   - **Parámetro en URL:** `?api_key=TU_CLAVE`
   - **Ejemplo completo:** `search/movie?api_key=abc123&query=Inception&page=1`

5. **`@Path("movie_id")`**
   - **Parámetro en la ruta:** `/movie/550`
   - **Uso:** Para identificadores únicos

#### Construcción de URLs:

```kotlin
// Llamada:
searchMovies(
    apiKey = "abc123",
    query = "Inception",
    page = 1,
    language = "es-ES"
)

// URL resultante:
// https://api.themoviedb.org/3/search/movie?api_key=abc123&query=Inception&page=1&language=es-ES

// Llamada con defaults:
searchMovies(apiKey = "abc123", query = "Matrix")
// Usa page = 1 y language = "es-ES" automáticamente
```

#### Corutinas Explicadas (suspend):

```kotlin
// ❌ SIN suspend - bloqueante (malo)
fun searchMovies(query: String): List<Movie> {
    // Esto bloquea el hilo durante 2 segundos
    Thread.sleep(2000) // La UI se congela 😱
    return api.searchMovies(query) // Error: no puede llamar a suspend
}

// ✅ CON suspend - no bloqueante (bueno)
suspend fun searchMovies(query: String): List<Movie> {
    delay(2000) // Pausa SIN bloquear la UI ✅
    return api.searchMovies(query) // Puede llamar a otras suspend functions
}

// Uso:
lifecycleScope.launch { // Lanzar corutina
    val movies = searchMovies("Inception") // Se pausa aquí
    displayMovies(movies) // Continúa cuando termina
    // La UI sigue respondiendo todo el tiempo
}
```

---

### 4. ApiClient.kt - El Chef de la Cocina

**Archivo:** `movielib/src/main/java/com/movielib/movielib/api/ApiClient.kt`

#### Analogía: El Chef que Prepara las Herramientas

Antes de cocinar, el chef prepara sus cuchillos, sartenes y especias. ApiClient prepara Retrofit para hacer las llamadas HTTP.

#### Código Explicado:

```kotlin
object ApiClient {

    private var instance: TMDbService? = null

    fun getTMDbService(): TMDbService {
        return instance ?: synchronized(this) {
            instance ?: buildService().also { instance = it }
        }
    }

    private fun buildService(): TMDbService {
        val okHttpClient = OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    val loggingInterceptor = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                    addInterceptor(loggingInterceptor)
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(TMDbService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TMDbService::class.java)
    }
}
```

**Desglose:**

1. **`object ApiClient`**
   - **Singleton:** Solo existe UNA instancia
   - **Patrón:** Garantiza que solo haya un Retrofit
   - **Por qué:** Retrofit es pesado de crear, mejor reutilizarlo

2. **Double-Checked Locking (seguridad en threads):**
```kotlin
return instance ?: synchronized(this) {
    instance ?: buildService().also { instance = it }
}
```

**Paso a paso:**
```kotlin
// 1. ¿Ya existe instance?
if (instance != null) {
    return instance // Retorna rápidamente
}

// 2. No existe, pero... ¿y si dos threads llegan al mismo tiempo?
synchronized(this) { // Solo un thread a la vez puede entrar aquí
    // 3. Doble verificación (puede que otro thread la creó mientras esperábamos)
    if (instance == null) {
        instance = buildService() // Crear por primera vez
    }
    return instance
}
```

**En diagrama:**
```
Thread 1: ¿instance? No → synchronized → crear → done
Thread 2: ¿instance? No → synchronized → [ESPERA] → ¿instance? Sí → usar existente
```

3. **OkHttpClient - El Transportista:**
```kotlin
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS) // Tiempo máximo para conectar
    .readTimeout(30, TimeUnit.SECONDS)    // Tiempo máximo leyendo respuesta
    .writeTimeout(30, TimeUnit.SECONDS)   // Tiempo máximo enviando datos
    .build()
```

**Analogía:** Como darle instrucciones a un mensajero:
- "Si no te atienden en 30 segundos, vuelve"
- "Si tardan más de 30 segundos en darte el paquete, vuelve"

4. **Logging Interceptor (solo en DEBUG):**
```kotlin
if (BuildConfig.DEBUG) {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    addInterceptor(loggingInterceptor)
}
```

**Qué hace:** Imprime en la consola las peticiones HTTP

**Salida en Logcat:**
```
--> GET https://api.themoviedb.org/3/search/movie?query=Inception
--> END GET

<-- 200 OK (326ms)
Content-Type: application/json
{
  "results": [
    {
      "id": 27205,
      "title": "Inception",
      ...
    }
  ]
}
<-- END HTTP
```

**Por qué solo en DEBUG:** En producción no queremos llenar los logs ni reducir performance.

5. **Retrofit Builder:**
```kotlin
return Retrofit.Builder()
    .baseUrl(TMDbService.BASE_URL) // URL base: https://api.themoviedb.org/3/
    .client(okHttpClient)           // Usa nuestro OkHttp configurado
    .addConverterFactory(GsonConverterFactory.create()) // JSON → Objetos Kotlin
    .build()
    .create(TMDbService::class.java) // Genera la implementación de la interfaz
```

**Gson Converter:**
```
JSON de la API          →  Gson  →  Objeto Kotlin
{                                    Movie(
  "id": 550,                           id = 550,
  "title": "Fight Club"                title = "Fight Club"
}                                    )
```

---

### 5. MovieDao.kt - El Archivero

**Archivo:** `movielib/src/main/java/com/movielib/movielib/database/MovieDao.kt`

#### Analogía: El Archivero de una Biblioteca

El archivero (DAO = Data Access Object) sabe exactamente dónde están los libros (películas) y puede:
- Guardar nuevos libros
- Buscar libros por título
- Eliminar libros viejos
- Contarlos

#### Código Explicado:

```kotlin
@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): Movie?

    @Query("SELECT * FROM movies WHERE isInLibrary = 1 ORDER BY dateAdded DESC")
    fun getLibraryMoviesFlow(): Flow<List<Movie>>

    @Query("UPDATE movies SET isInLibrary = 1, dateAdded = :timestamp WHERE id = :movieId")
    suspend fun addToLibrary(movieId: Int, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM movies WHERE isInLibrary = 1")
    suspend fun getLibraryCount(): Int
}
```

**Desglose:**

1. **`@Dao`**
   - **Data Access Object:** Patrón de diseño
   - **Responsabilidad:** SOLO acceder a datos, sin lógica de negocio
   - **Room genera:** La implementación automáticamente

2. **`@Insert(onConflict = OnConflictStrategy.REPLACE)`**
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertMovie(movie: Movie)
```

**Qué hace:**
- Inserta la película en la tabla `movies`
- Si ya existe (mismo ID), la REEMPLAZA

**Estrategias de conflicto:**
```kotlin
// REPLACE: Reemplazar la existente
insertMovie(Movie(id = 550, title = "Fight Club v2"))
// Si existe id=550, se sobrescribe

// IGNORE: No hacer nada si existe
@Insert(onConflict = OnConflictStrategy.IGNORE)

// ABORT: Lanzar error si existe (default)
@Insert(onConflict = OnConflictStrategy.ABORT)
```

3. **`@Query` - SQL Directo:**
```kotlin
@Query("SELECT * FROM movies WHERE id = :movieId")
suspend fun getMovieById(movieId: Int): Movie?
```

**Traducción:**
```sql
-- Kotlin:
getMovieById(550)

-- SQL generado:
SELECT * FROM movies WHERE id = 550
```

**Null Safety:**
```kotlin
val movie: Movie? = getMovieById(550)
//              ^ Puede ser null si no existe

// Debes manejar el null
if (movie != null) {
    println(movie.title)
} else {
    println("Película no encontrada")
}
```

4. **Flow vs suspend:**
```kotlin
// ❌ suspend - operación única
@Query("SELECT * FROM movies WHERE isInLibrary = 1")
suspend fun getLibraryMovies(): List<Movie>

// Uso:
val movies = dao.getLibraryMovies() // Se ejecuta UNA vez
displayMovies(movies)

// ✅ Flow - stream reactivo (observable)
@Query("SELECT * FROM movies WHERE isInLibrary = 1")
fun getLibraryMoviesFlow(): Flow<List<Movie>>

// Uso:
dao.getLibraryMoviesFlow().collect { movies ->
    displayMovies(movies) // Se ejecuta CADA vez que cambia la tabla
}
// Si añades/eliminas películas, este código se ejecuta automáticamente
```

**Analogía de Flow:**
```
suspend = Comprar el periódico de hoy (una vez)
Flow    = Suscribirse al periódico (recibes cada edición nueva automáticamente)
```

5. **Parámetros en Queries:**
```kotlin
@Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%'")
suspend fun searchByTitle(query: String): List<Movie>

// Uso:
searchByTitle("Fight")

// SQL generado:
// SELECT * FROM movies WHERE title LIKE '%Fight%'
// Encuentra: "Fight Club", "Fighting", "The Fight"
```

**Concatenación en SQL:**
```kotlin
// En Kotlin: "Hola" + " " + "Mundo"
// En SQL:    '%' || :query || '%'
//            ↑     ↑        ↑
//           parte param   parte
```

6. **Valores por defecto:**
```kotlin
@Query("UPDATE movies SET isInLibrary = 1, dateAdded = :timestamp WHERE id = :movieId")
suspend fun addToLibrary(
    movieId: Int,
    timestamp: Long = System.currentTimeMillis()
)

// Uso 1: Con timestamp personalizado
addToLibrary(movieId = 550, timestamp = 1234567890)

// Uso 2: Sin timestamp (usa el actual)
addToLibrary(movieId = 550)
// timestamp = System.currentTimeMillis() se evalúa automáticamente
```

---

### 6. MovieDatabase.kt - El Almacén

**Archivo:** `movielib/src/main/java/com/movielib/movielib/database/MovieDatabase.kt`

#### Analogía: El Edificio del Almacén

El almacén (Database) contiene múltiples secciones (tablas). El encargado del almacén (MovieDatabase) te da acceso a cada sección (DAOs).

#### Código Explicado:

```kotlin
@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getDatabase(context: Context): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**Desglose:**

1. **`@Database` - Configuración:**
```kotlin
@Database(
    entities = [Movie::class],  // Qué tablas tiene
    version = 1,                // Versión del schema
    exportSchema = false        // No exportar schema (solo para dev)
)
```

**entities = [Movie::class]:**
```
Database
  └─ Table "movies" (de Movie::class)
       ├─ Columna: id
       ├─ Columna: title
       ├─ Columna: overview
       └─ ...
```

**version = 1:**
```kotlin
// Versión 1:
@Database(entities = [Movie::class], version = 1)
// Tabla: movies (10 columnas)

// Más tarde, añades un campo nuevo:
data class Movie(..., val newField: String)

// Versión 2:
@Database(entities = [Movie::class], version = 2)
// Tabla: movies (11 columnas)
// Necesitas migración para actualizar la base de datos existente
```

2. **`abstract class` y `abstract fun`:**
```kotlin
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao // Room genera la implementación
}

// Room genera internamente algo como:
class MovieDatabase_Impl : MovieDatabase() {
    override fun movieDao(): MovieDao {
        return MovieDao_Impl(this)
    }
}
```

3. **Singleton Pattern (Thread-Safe):**
```kotlin
@Volatile // Visible para todos los threads
private var INSTANCE: MovieDatabase? = null

fun getDatabase(context: Context): MovieDatabase {
    return INSTANCE ?: synchronized(this) {
        // Solo un thread a la vez puede crear la instancia
        val instance = Room.databaseBuilder(...).build()
        INSTANCE = instance
        instance
    }
}
```

**@Volatile explicado:**
```kotlin
// Sin @Volatile:
Thread 1: INSTANCE = database // Puede que Thread 2 no vea este cambio inmediatamente
Thread 2: INSTANCE == null?   // Podría leer valor antiguo (null) de su caché

// Con @Volatile:
Thread 1: INSTANCE = database // Todos los threads ven el cambio INMEDIATAMENTE
Thread 2: INSTANCE == null?   // Lee el valor actualizado
```

4. **Room.databaseBuilder:**
```kotlin
Room.databaseBuilder(
    context.applicationContext,      // Contexto de la app
    MovieDatabase::class.java,       // Qué clase de BD
    "movie_database"                 // Nombre del archivo físico
)
.fallbackToDestructiveMigration()   // ⚠️ Cuidado con esto
.build()
```

**fallbackToDestructiveMigration():**
```kotlin
// Sin esto:
// App versión 1 → usuario guarda 100 películas
// Actualizas app a versión 2 (cambios en schema)
// App crashea: "Migration not found"

// Con esto:
// App versión 1 → usuario guarda 100 películas
// Actualizas app a versión 2
// Room BORRA toda la BD y la crea de nuevo
// Usuario pierde sus 100 películas 😱

// Para producción, necesitas migraciones:
.addMigrations(MIGRATION_1_2)
```

**Migración ejemplo:**
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Añadir columna nueva sin perder datos
        database.execSQL(
            "ALTER TABLE movies ADD COLUMN newField TEXT DEFAULT ''"
        )
    }
}
```

---

## Capa de Dominio (Business Logic)

Esta es la planta intermedia donde vive la lógica de negocio.

---

### 7. MovieRepository.kt - El Gerente del Restaurante

**Archivo:** `movielib/src/main/java/com/movielib/movielib/repository/MovieRepository.kt`

#### Analogía: El Gerente que Coordina Todo

El gerente (Repository) es el cerebro de la operación:
- Coordina entre el camarero (API) y el almacén (Database)
- Decide si usar ingredientes frescos (API) o del almacén (caché)
- Maneja errores y problemas
- Da órdenes pero no hace el trabajo sucio

#### Código Explicado:

```kotlin
class MovieRepository(
    private val movieDao: MovieDao,
    private val apiKey: String
) {

    private val tmdbService: TMDbService = ApiClient.getTMDbService()

    fun searchMovies(query: String, page: Int = 1): Flow<ApiResponse<List<Movie>>> = flow {
        emit(ApiResponse.Loading)

        try {
            val response = tmdbService.searchMovies(apiKey, query, page)

            if (response.isSuccessful) {
                val movies = response.body()?.results?.map { it.toMovie() } ?: emptyList()
                movieDao.insertMovies(movies) // Guardar en caché
                emit(ApiResponse.Success(movies))
            } else {
                emit(ApiResponse.Error("Server error: ${response.code()}", response.code()))
            }
        } catch (e: IOException) {
            emit(ApiResponse.NetworkError)
        }
    }

    fun getMovieDetails(movieId: Int): Flow<ApiResponse<Movie>> = flow {
        emit(ApiResponse.Loading)

        // 1. Primero, buscar en caché
        val localMovie = movieDao.getMovieById(movieId)
        if (localMovie != null && !localMovie.genres.isNullOrEmpty()) {
            emit(ApiResponse.Success(localMovie)) // Mostrar datos cacheados
        }

        try {
            // 2. Actualizar desde API
            val response = tmdbService.getMovieDetails(movieId, apiKey)

            if (response.isSuccessful) {
                val movieFromApi = response.body()?.toMovie()

                if (movieFromApi != null) {
                    // 3. Preservar datos del usuario
                    val updatedMovie = if (localMovie != null) {
                        movieFromApi.copy(
                            isInLibrary = localMovie.isInLibrary,
                            userRating = localMovie.userRating,
                            userReview = localMovie.userReview
                        )
                    } else {
                        movieFromApi
                    }

                    movieDao.insertMovie(updatedMovie)
                    emit(ApiResponse.Success(updatedMovie))
                }
            }
        } catch (e: IOException) {
            // Si falla la red pero tenemos caché, eso es suficiente
            if (localMovie == null) {
                emit(ApiResponse.NetworkError)
            }
        }
    }
}
```

**Desglose:**

1. **Flow Builder:**
```kotlin
fun searchMovies(...): Flow<ApiResponse<List<Movie>>> = flow {
    // Aquí emitimos valores a lo largo del tiempo
    emit(ApiResponse.Loading)    // Emisión 1
    emit(ApiResponse.Success(...)) // Emisión 2
}
```

**Analogía de Flow:**
```
Flow es como una manguera de agua:
  Source (flow {...})  →  Pipe (transformaciones)  →  Collector (collect {})

  emit(Loading)     →  →  →  →  →  →  →  →  collect { showLoading() }
  emit(Success(...)) →  →  →  →  →  →  →  →  collect { showData() }
```

**Ejemplo de uso:**
```kotlin
// En la Activity:
lifecycleScope.launch {
    repository.searchMovies("Inception").collect { response ->
        // Este bloque se ejecuta CADA vez que se emite un valor
        when (response) {
            is ApiResponse.Loading -> showLoadingSpinner()
            is ApiResponse.Success -> displayMovies(response.data)
            is ApiResponse.Error -> showError(response.message)
            is ApiResponse.NetworkError -> showOfflineMessage()
        }
    }
}
```

2. **Estrategia de Caché Inteligente (getMovieDetails):**

```
┌─────────────────────────────────────────────┐
│ 1. Usuario pide detalles de "Inception"    │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│ 2. Buscar en caché local (Room)             │
│    ¿Existe y tiene datos completos?         │
└─────────────────────────────────────────────┘
      ↓ Sí                        ↓ No
┌──────────────┐         ┌─────────────────┐
│ 3a. Emitir   │         │ 3b. Esperar API │
│ datos cacheados│         └─────────────────┘
│ (RÁPIDO)     │                 ↓
└──────────────┘         ┌─────────────────┐
      ↓                  │ Usuario ve      │
┌──────────────┐         │ spinner         │
│ 4. Actualizar│         └─────────────────┘
│ desde API    │
│ (BACKGROUND) │
└──────────────┘
      ↓
┌──────────────────────────┐
│ 5. Preservar datos usuario│
│    - Rating             │
│    - Review             │
│    - isInLibrary        │
└──────────────────────────┘
      ↓
┌──────────────────────────┐
│ 6. Guardar en caché      │
│ 7. Emitir datos frescos  │
└──────────────────────────┘
```

**Código de preservación de datos:**
```kotlin
val updatedMovie = if (localMovie != null) {
    // HAY datos locales - preservar información del usuario
    movieFromApi.copy(
        isInLibrary = localMovie.isInLibrary,  // ← Del caché
        userRating = localMovie.userRating,    // ← Del caché
        userReview = localMovie.userReview,    // ← Del caché
        // Todo lo demás viene de la API (fresco)
    )
} else {
    // NO hay datos locales - usar todo de la API
    movieFromApi
}
```

**Por qué es importante:**
```kotlin
// Sin preservar:
Usuario: *puntúa película con 5 estrellas*
App: *actualiza desde API*
App: *sobrescribe rating del usuario con null*
Usuario: "¡Mi puntuación desapareció!" 😡

// Con preservar:
Usuario: *puntúa película con 5 estrellas*
App: *actualiza desde API*
App: *mantiene el rating del usuario*
Usuario: "¡Genial, sigue ahí!" 😊
```

3. **Manejo de Errores por Capas:**

```kotlin
try {
    val response = tmdbService.searchMovies(...)

    if (response.isSuccessful) {
        // ✅ Código 200-299 (éxito)
        emit(ApiResponse.Success(...))
    } else {
        // ❌ Código 400-599 (error del servidor)
        emit(ApiResponse.Error("Server error: ${response.code()}", response.code()))
    }
} catch (e: IOException) {
    // 🔌 Error de red (sin internet, timeout, etc.)
    emit(ApiResponse.NetworkError)
} catch (e: HttpException) {
    // ⚠️ Error HTTP (raro, Retrofit lo maneja)
    emit(ApiResponse.Error("HTTP error: ${e.message}", e.code()))
} catch (e: Exception) {
    // 💥 Error inesperado
    emit(ApiResponse.Error("Unexpected error: ${e.message}"))
}
```

**Códigos HTTP explicados:**
```
200-299: Éxito ✅
  200: OK
  201: Created
  204: No Content

400-499: Error del cliente ❌
  400: Bad Request (pedido mal formado)
  401: Unauthorized (sin autenticación)
  403: Forbidden (sin permiso)
  404: Not Found (no existe)

500-599: Error del servidor 💥
  500: Internal Server Error
  502: Bad Gateway
  503: Service Unavailable
```

4. **Métodos de Biblioteca:**

```kotlin
suspend fun addToLibrary(movieId: Int): Boolean {
    return try {
        movieDao.addToLibrary(movieId)
        true // ✅ Éxito
    } catch (e: Exception) {
        false // ❌ Error
    }
}
```

**Uso:**
```kotlin
// En la Activity:
lifecycleScope.launch {
    val success = repository.addToLibrary(550)

    if (success) {
        Toast.makeText(this, "Añadida a biblioteca", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(this, "Error al añadir", Toast.LENGTH_SHORT).show()
    }
}
```

---

## Capa de Presentación (UI Layer)

Esta es la planta superior, donde los usuarios interactúan con la app.

---

### 8. BaseMovieActivity.kt - El Plano Base

**Archivo:** `app/src/main/java/com/movielib/base/BaseMovieActivity.kt`

#### Analogía: El Plano Arquitectónico Base

Imagina que estás construyendo 5 casas en un vecindario. Todas comparten el mismo diseño de cimientos, plomería y electricidad. En lugar de repetir ese trabajo 5 veces, creas un "plano base" y cada casa lo extiende.

#### Código Explicado:

```kotlin
abstract class BaseMovieActivity : AppCompatActivity() {

    protected val repository: MovieRepository by lazy {
        val database = MovieDatabase.getDatabase(this)
        MovieRepository(database.movieDao(), Constants.TMDB_API_KEY)
    }
}
```

**Desglose:**

1. **`abstract class`**
   - No se puede instanciar directamente
   - Sirve como base para otras clases
   - **Analogía:** Es el plano, no la casa construida

2. **`protected`**
   - Solo accesible desde esta clase y sus hijos
   - **En Java:** Igual
   - **vs private:** Private no sería accesible desde MainActivity

3. **`by lazy`**
   - Inicialización perezosa (lazy initialization)
   - Se crea solo la primera vez que se usa
   - Se guarda para reusar

```kotlin
// Sin lazy:
val repository: MovieRepository = MovieRepository(...) // Se crea SIEMPRE

// Con lazy:
val repository: MovieRepository by lazy {
    MovieRepository(...) // Se crea solo cuando se usa por primera vez
}

// Ejemplo:
class MainActivity : BaseMovieActivity() {
    override fun onCreate(...) {
        // repository aún NO se ha creado

        // Primera vez que se usa:
        repository.searchMovies("Inception") // ← Aquí se crea

        // Siguientes usos:
        repository.getPopularMovies() // ← Usa la instancia ya creada
    }
}
```

**Ventajas:**

```kotlin
// ❌ Sin BaseMovieActivity (código duplicado 5 veces):

class MainActivity : AppCompatActivity() {
    private val repository by lazy {
        val db = MovieDatabase.getDatabase(this)
        MovieRepository(db.movieDao(), Constants.TMDB_API_KEY)
    }
}

class SearchActivity : AppCompatActivity() {
    private val repository by lazy {  // ← Repetido
        val db = MovieDatabase.getDatabase(this)
        MovieRepository(db.movieDao(), Constants.TMDB_API_KEY)
    }
}

class MovieDetailActivity : AppCompatActivity() {
    private val repository by lazy {  // ← Repetido
        val db = MovieDatabase.getDatabase(this)
        MovieRepository(db.movieDao(), Constants.TMDB_API_KEY)
    }
}

// ... y así 2 veces más

// ✅ Con BaseMovieActivity (DRY - Don't Repeat Yourself):

abstract class BaseMovieActivity : AppCompatActivity() {
    protected val repository by lazy { ... } // ← Una sola vez
}

class MainActivity : BaseMovieActivity() {
    // repository ya disponible ✅
}

class SearchActivity : BaseMovieActivity() {
    // repository ya disponible ✅
}

class MovieDetailActivity : BaseMovieActivity() {
    // repository ya disponible ✅
}
```

---

### 9. MainActivity.kt - La Pantalla Principal

**Archivo:** `app/src/main/java/com/movielib/MainActivity.kt`

#### Analogía: El Vestíbulo del Cine

El vestíbulo (MainActivity) es lo primero que ves al entrar al cine. Muestra:
- Las películas en cartelera (Popular)
- Las mejor valoradas (Top Rated)
- Tus favoritas (Biblioteca)

#### Código Explicado (Simplificado):

```kotlin
class MainActivity : BaseMovieActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var popularAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        setupClickListeners()
        loadData()
    }

    private fun loadPopularMovies() {
        lifecycleScope.launch {
            repository.getPopularMovies().collect { response ->
                response.handle(
                    onSuccess = { movies ->
                        popularAdapter.submitList(movies)
                        if (movies.isNotEmpty()) {
                            displayHeroMovie(movies.first())
                        }
                    },
                    onError = { message, _ ->
                        showError(message)
                    }
                )
            }
        }
    }
}
```

**Desglose:**

1. **View Binding:**
```kotlin
private lateinit var binding: ActivityMainBinding
```

**lateinit (late initialization):**
```kotlin
// Sin lateinit - necesitas inicializar inmediatamente:
private var binding: ActivityMainBinding = null // Error: no puede ser null
private var binding: ActivityMainBinding? = null // ✅ Pero ahora es nullable

// Uso:
binding?.recyclerView?.adapter = adapter // Feo

// Con lateinit:
private lateinit var binding: ActivityMainBinding

// onCreate:
binding = ActivityMainBinding.inflate(layoutInflater) // Inicializar después

// Uso:
binding.recyclerView.adapter = adapter // ✅ Limpio, sin ?
```

**View Binding vs findViewById:**
```kotlin
// ❌ Forma antigua (findViewById):
val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
recyclerView.adapter = adapter
// Problemas:
// - Puede retornar null si el ID es incorrecto
// - Sin type safety
// - Propenso a crashes

// ✅ Forma moderna (View Binding):
binding.recyclerView.adapter = adapter
// Ventajas:
// - Type-safe (el compilador verifica que exista)
// - Null-safe
// - Autocomplete en el IDE
```

2. **lifecycleScope.launch:**
```kotlin
lifecycleScope.launch {
    // Código asíncrono aquí
}
```

**Qué es:**
- Un CoroutineScope vinculado al ciclo de vida de la Activity
- Se cancela automáticamente cuando la Activity muere

**Ciclo de vida:**
```
onCreate → onStart → onResume
  ↓          ↓         ↓
  lifecycleScope está activo
  ↓          ↓         ↓
onPause → onStop → onDestroy
                      ↓
              lifecycleScope se cancela
              (todas las corutinas se detienen)
```

**Ejemplo del problema que resuelve:**
```kotlin
// ❌ Sin lifecycleScope:
override fun onCreate(...) {
    Thread {
        val movies = repository.getMovies() // En hilo separado
        runOnUiThread {
            displayMovies(movies) // Actualizar UI
        }
    }.start()
}

override fun onDestroy() {
    super.onDestroy()
    // El thread sigue corriendo aunque la Activity murió! 💥
    // Si intenta actualizar UI → CRASH
}

// ✅ Con lifecycleScope:
override fun onCreate(...) {
    lifecycleScope.launch {
        val movies = repository.getMovies()
        displayMovies(movies) // Seguro
    }
}

override fun onDestroy() {
    super.onDestroy()
    // lifecycleScope automáticamente cancela todas las corutinas ✅
}
```

3. **RecyclerView y Adapter:**

```kotlin
private fun setupRecyclerViews() {
    popularAdapter = MovieAdapter(MovieAdapter.LayoutType.HORIZONTAL) { movie ->
        navigateToMovieDetail(movie)
    }

    binding.popularMoviesRecyclerView.apply {
        layoutManager = LinearLayoutManager(
            this@MainActivity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        adapter = popularAdapter
    }
}
```

**Analogía:** RecyclerView es como una cinta transportadora en un sushi bar:
- **RecyclerView:** La cinta que gira
- **Adapter:** El chef que pone platos en la cinta
- **ViewHolder:** Los platos individuales
- **LayoutManager:** El motor que mueve la cinta (horizontal/vertical/grid)

**Flujo:**
```
1. RecyclerView: "Necesito mostrar elemento en posición 5"
2. Adapter: "Aquí está el ViewHolder para posición 5"
3. RecyclerView: "Muéstralo en pantalla"

Usuario hace scroll ↓

4. RecyclerView: "Elemento 0 ya no es visible, recíclalo"
5. Adapter: "Reutilizaré ese ViewHolder para posición 10"
```

**Lambda como parámetro (onClick):**
```kotlin
MovieAdapter(MovieAdapter.LayoutType.HORIZONTAL) { movie ->
    navigateToMovieDetail(movie)
}

// Es equivalente a:
MovieAdapter(
    layoutType = MovieAdapter.LayoutType.HORIZONTAL,
    onMovieClick = { movie -> navigateToMovieDetail(movie) }
)

// En el Adapter:
class MovieAdapter(
    private val layoutType: LayoutType,
    private val onMovieClick: (Movie) -> Unit // Función que recibe Movie y no retorna nada
) {
    fun onBindViewHolder(...) {
        itemView.setOnClickListener {
            onMovieClick(movie) // Llamar al lambda
        }
    }
}
```

4. **Extension handle():**

```kotlin
response.handle(
    onSuccess = { movies -> displayMovies(movies) },
    onError = { message, _ -> showError(message) }
)

// Sin extension (más verboso):
when (response) {
    is ApiResponse.Success -> displayMovies(response.data)
    is ApiResponse.Error -> showError(response.message)
    is ApiResponse.NetworkError -> showError("Sin conexión")
    is ApiResponse.Loading -> showLoading()
}
```

---

### 10. SearchActivity.kt - La Búsqueda

**Archivo:** `app/src/main/java/com/movielib/SearchActivity.kt`

#### Analogía: El Buscador de una Biblioteca

Como el catálogo de búsqueda de una biblioteca: escribes un término y te muestra resultados en tiempo real.

#### Código Explicado:

```kotlin
class SearchActivity : BaseMovieActivity() {

    private var searchJob: Job? = null

    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel() // Cancelar búsqueda anterior

                val query = s?.toString()?.trim() ?: ""

                if (query.isEmpty()) {
                    showEmptyState()
                } else {
                    searchJob = lifecycleScope.launch {
                        delay(500) // Debounce: esperar 500ms
                        searchMovies(query)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
```

**Desglose:**

1. **TextWatcher - Observador de Texto:**
```kotlin
object : TextWatcher {
    override fun beforeTextChanged(...) {
        // ANTES de que cambie el texto
    }

    override fun onTextChanged(...) {
        // MIENTRAS cambia el texto (cada tecla presionada)
    }

    override fun afterTextChanged(...) {
        // DESPUÉS de que cambió el texto
    }
}
```

**Ejemplo:**
```
Usuario escribe: "Inception"

I → onTextChanged("I")
In → onTextChanged("In")
Inc → onTextChanged("Inc")
Ince → onTextChanged("Ince")
Incep → onTextChanged("Incep")
...
```

2. **Debouncing - El Truco de Espera:**

**Problema sin debounce:**
```
Usuario escribe: "Inception" (9 letras)
App hace: 9 búsquedas a la API
  "I" → API ❌ (innecesario)
  "In" → API ❌ (innecesario)
  "Inc" → API ❌ (innecesario)
  ...
  "Inception" → API ✅ (la única necesaria)
```

**Solución con debounce:**
```kotlin
searchJob?.cancel() // Cancelar temporizador anterior
searchJob = lifecycleScope.launch {
    delay(500) // Esperar 500ms
    searchMovies(query) // Solo buscar si pasaron 500ms sin más cambios
}
```

**Visualización temporal:**
```
t=0ms:   Usuario escribe "I"
         → searchJob inicia, espera 500ms

t=100ms: Usuario escribe "n"
         → searchJob.cancel() ❌
         → Nuevo searchJob inicia, espera 500ms

t=200ms: Usuario escribe "c"
         → searchJob.cancel() ❌
         → Nuevo searchJob inicia, espera 500ms

...

t=900ms: Usuario escribe última letra
         → searchJob inicia, espera 500ms

t=1400ms: Sin más cambios
         → searchMovies("Inception") ✅ (solo UNA búsqueda)
```

3. **Job Management:**

```kotlin
private var searchJob: Job? = null

// Uso:
searchJob = lifecycleScope.launch {
    // trabajo asíncrono
}

// Cancelar:
searchJob?.cancel()
```

**Por qué nullable (Job?):**
```kotlin
// Primera búsqueda:
searchJob == null // No hay búsqueda previa que cancelar

// Segunda búsqueda:
searchJob != null // Hay búsqueda previa, cancelarla
searchJob?.cancel()
```

4. **Estados de la UI:**

```kotlin
private fun setViewsVisibility(
    emptyState: Boolean,
    noResults: Boolean,
    results: Boolean,
    loading: Boolean
) {
    binding.emptyStateLayout.visibility = if (emptyState) View.VISIBLE else View.GONE
    binding.noResultsLayout.visibility = if (noResults) View.VISIBLE else View.GONE
    binding.searchResultsRecyclerView.visibility = if (results) View.VISIBLE else View.GONE
    binding.searchProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
}
```

**Estados mutuamente excluyentes:**
```
┌──────────────┬─────────┬───────────┬─────────┬─────────┐
│   Estado     │ Empty   │ NoResults │ Results │ Loading │
├──────────────┼─────────┼───────────┼─────────┼─────────┤
│ Inicial      │  ✅     │     ❌    │    ❌   │    ❌   │
│ Buscando     │  ❌     │     ❌    │    ❌   │    ✅   │
│ Sin resultados│  ❌     │     ✅    │    ❌   │    ❌   │
│ Con resultados│  ❌     │     ❌    │    ✅   │    ❌   │
└──────────────┴─────────┴───────────┴─────────┴─────────┘
```

**Uso:**
```kotlin
// Estado inicial (nada en el buscador):
setViewsVisibility(emptyState = true, noResults = false, results = false, loading = false)

// Buscando:
setViewsVisibility(emptyState = false, noResults = false, results = false, loading = true)

// Encontró resultados:
setViewsVisibility(emptyState = false, noResults = false, results = true, loading = false)

// No encontró nada:
setViewsVisibility(emptyState = false, noResults = true, results = false, loading = false)
```

---

### 11. MovieDetailActivity.kt - Los Detalles

**Archivo:** `app/src/main/java/com/movielib/MovieDetailActivity.kt`

#### Analogía: La Ficha Técnica Completa

Como mirar la ficha completa de un libro en una biblioteca: título, autor, resumen, críticas, etc.

#### Código Explicado:

```kotlin
class MovieDetailActivity : BaseMovieActivity() {

    private var currentMovie: Movie? = null

    companion object {
        private const val RATING_BAR_MAX = 5f
        private const val TMDB_RATING_MAX = 10f
        private const val RATING_SCALE_FACTOR = TMDB_RATING_MAX / RATING_BAR_MAX
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val movieId = intent.getIntExtra(SearchActivity.EXTRA_MOVIE_ID, -1)
        if (movieId != -1) {
            loadMovieDetails(movieId)
        } else {
            finish() // Cerrar activity si no hay ID
        }
    }

    private fun toggleFavorite(movie: Movie) {
        lifecycleScope.launch {
            if (movie.isInLibrary) {
                repository.removeFromLibrary(movie.id)
                currentMovie = movie.copy(
                    isInLibrary = false,
                    userRating = null,
                    userReview = null
                )
                updateFavoriteUI(false)
            } else {
                repository.addToLibrary(movie.id)
                currentMovie = movie.copy(isInLibrary = true)
                updateFavoriteUI(true)
            }
        }
    }

    private fun showRatingDialog(movie: Movie) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rating_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)

        movie.userRating?.let { rating ->
            ratingBar.rating = rating / RATING_SCALE_FACTOR
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.rate_review_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val rating = ratingBar.rating * RATING_SCALE_FACTOR
                saveRatingAndReview(movie, rating, review)
            }
            .show()
    }
}
```

**Desglose:**

1. **companion object - El Equivalente de static:**

```kotlin
companion object {
    private const val RATING_BAR_MAX = 5f
    private const val TMDB_RATING_MAX = 10f
    private const val RATING_SCALE_FACTOR = 2f
}

// En Java sería:
public class MovieDetailActivity {
    private static final float RATING_BAR_MAX = 5f;
    private static final float TMDB_RATING_MAX = 10f;
    private static final float RATING_SCALE_FACTOR = 2f;
}

// Uso:
val scale = RATING_SCALE_FACTOR // Dentro de la clase

val scale = MovieDetailActivity.RATING_SCALE_FACTOR // Desde fuera
```

**const vs val:**
```kotlin
// const: Constante en tiempo de compilación (primitivos y Strings)
const val MAX_RATING = 10f // Se sustituye en compilación

// val: Constante en tiempo de ejecución (cualquier tipo)
val repository = MovieRepository(...) // Se crea al correr
```

2. **Intent Extras - Pasar Datos Entre Activities:**

```kotlin
// Activity origen (SearchActivity):
val intent = Intent(this, MovieDetailActivity::class.java)
intent.putExtra(EXTRA_MOVIE_ID, 550) // Poner dato
startActivity(intent)

// Activity destino (MovieDetailActivity):
val movieId = intent.getIntExtra(EXTRA_MOVIE_ID, -1)
//                                                 ↑
//                                        valor por defecto
```

**Tipos de extras:**
```kotlin
intent.putExtra("key_int", 123)
intent.putExtra("key_string", "Hola")
intent.putExtra("key_boolean", true)
intent.putExtra("key_parcelable", movie) // Si Movie implementa Parcelable

val int = intent.getIntExtra("key_int", 0)
val str = intent.getStringExtra("key_string") // Nullable
val bool = intent.getBooleanExtra("key_boolean", false)
```

3. **let - Scope Function:**

```kotlin
movie.userRating?.let { rating ->
    ratingBar.rating = rating / RATING_SCALE_FACTOR
}

// Sin let (más verboso):
val userRating = movie.userRating
if (userRating != null) {
    ratingBar.rating = userRating / RATING_SCALE_FACTOR
}
```

**let explicado:**
```kotlin
// Si movie.userRating NO es null:
movie.userRating?.let { rating ->
    // Aquí rating es NOT NULL
    // rating es smart-cast automáticamente
    println(rating + 1) // ✅ Funciona
}

// Si movie.userRating ES null:
// El bloque let NO se ejecuta

// Otro ejemplo:
val result = movie.overview?.let { overview ->
    overview.take(100) // Primeros 100 caracteres
} ?: "Sin descripción" // Elvis operator: valor si es null
```

4. **AlertDialog - Diálogos:**

```kotlin
AlertDialog.Builder(this)
    .setTitle("Título")
    .setMessage("Mensaje")
    .setPositiveButton("OK") { dialog, which ->
        // Usuario presionó OK
    }
    .setNegativeButton("Cancelar") { dialog, which ->
        // Usuario presionó Cancelar
    }
    .setNeutralButton("Más info") { dialog, which ->
        // Tercer botón
    }
    .setCancelable(false) // No se puede cancelar tocando fuera
    .show()
```

**Lambda parameters:**
```kotlin
.setPositiveButton("OK") { dialog, which ->
    // dialog: DialogInterface - el diálogo mismo
    // which: Int - qué botón se presionó
}

// Si no usas los parámetros:
.setPositiveButton("OK") { _, _ ->
    doSomething()
}
```

5. **copy() - Inmutabilidad:**

```kotlin
val movie = Movie(id = 1, title = "Film", isInLibrary = false)

// ❌ Mutar (no permitido con val):
movie.isInLibrary = true // Error de compilación

// ✅ Crear copia con cambios:
val updatedMovie = movie.copy(isInLibrary = true)

// movie sigue igual:
println(movie.isInLibrary) // false

// updatedMovie tiene el cambio:
println(updatedMovie.isInLibrary) // true

// Todo lo demás es igual:
println(movie.title == updatedMovie.title) // true
```

---

### 12. LibraryActivity.kt - La Biblioteca Personal

**Archivo:** `app/src/main/java/com/movielib/LibraryActivity.kt`

#### Analogía: Tu Estantería Personal

Como tu estantería en casa: solo las películas que has elegido guardar.

#### Código Explicado:

```kotlin
class LibraryActivity : BaseMovieActivity() {

    private fun loadLibrary() {
        lifecycleScope.launch {
            val stats = repository.getLibraryStats()
            displayStats(stats)

            repository.getLibraryMoviesFlow().collectLatest { movies ->
                if (movies.isEmpty()) {
                    showEmptyState()
                } else {
                    showMovies(movies)
                }
            }
        }
    }

    private fun displayStats(stats: LibraryStats) {
        binding.totalMoviesText.text = stats.totalMovies.toString()
        binding.averageRatingText.text = String.format("%.1f", stats.averageRating)
        binding.reviewedMoviesText.text = stats.moviesWithReviews.toString()
    }

    override fun onResume() {
        super.onResume()
        loadLibrary() // Recargar cada vez que se muestra
    }
}
```

**Desglose:**

1. **onResume() - Ciclo de Vida:**

```
Flujo de navegación:
MainActivity → LibraryActivity → MovieDetailActivity

En MovieDetailActivity, usuario añade película a biblioteca

Usuario presiona "atrás" → vuelve a LibraryActivity

LibraryActivity.onResume() se llama → recarga la lista ✅

Usuario ve la nueva película en su biblioteca
```

**Ciclo de vida completo:**
```
onCreate  → onStart → onResume → [Activity visible]
                        ↑              ↓
                        └── onPause ←──┘
                               ↓
                            onStop
                               ↓
                           onDestroy

onResume: Se llama cada vez que la activity vuelve a ser visible
```

2. **collectLatest vs collect:**

```kotlin
// collect: Procesa TODAS las emisiones
repository.getLibraryMoviesFlow().collect { movies ->
    displayMovies(movies) // Se ejecuta para cada emisión
}

// collectLatest: Solo procesa la ÚLTIMA emisión
repository.getLibraryMoviesFlow().collectLatest { movies ->
    displayMovies(movies) // Cancela el anterior si llega uno nuevo
}
```

**Ejemplo:**
```
Flow emite:    A → B → C → D
               ↓   ↓   ↓   ↓
collect:       A   B   C   D  (procesa todos)

Flow emite:    A → B → C → D
               ↓   ✗   ✗   ↓
collectLatest: A [cancel] [cancel] D (solo el último)
```

**Cuándo usar cada uno:**
```kotlin
// collect: Cuando necesitas todos los valores
repository.searchMovies(query).collect { response ->
    // Quiero ver Loading, luego Success
}

// collectLatest: Cuando solo importa el último
repository.getLibraryMoviesFlow().collectLatest { movies ->
    // Solo quiero la lista más actualizada
}
```

3. **String.format - Formateo:**

```kotlin
val rating = 8.666666

// Sin formato:
binding.text.text = rating.toString() // "8.666666"

// Con formato:
binding.text.text = String.format("%.1f", rating) // "8.7"
//                                   ↑
//                            1 decimal
```

**Formatos comunes:**
```kotlin
// Decimales:
String.format("%.2f", 3.14159) // "3.14"
String.format("%.0f", 3.14159) // "3"

// Enteros:
String.format("%d", 42) // "42"
String.format("%03d", 5) // "005" (rellenar con ceros)

// Strings:
String.format("%s", "Hola") // "Hola"
String.format("%-10s", "Hi") // "Hi        " (10 caracteres, alineado a izquierda)

// Múltiples valores:
String.format("Usuario: %s, Edad: %d, Altura: %.2f", "Juan", 25, 1.75)
// "Usuario: Juan, Edad: 25, Altura: 1.75"
```

---

## Utilidades y Extensiones

---

### 13. ApiResponseExtensions.kt - Atajos Mágicos

**Archivo:** `app/src/main/java/com/movielib/extensions/ApiResponseExtensions.kt`

#### Analogía: Atajos de Teclado

Como en un editor de texto: en lugar de hacer clic en Menú → Editar → Copiar, presionas Ctrl+C. Las extensions son atajos para código común.

#### Código Explicado:

```kotlin
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
```

**Desglose:**

1. **Extension Function:**

```kotlin
// Función normal:
fun handleResponse(response: ApiResponse<List<Movie>>) {
    when (response) { ... }
}

// Uso:
handleResponse(response)

// Extension function:
fun <T> ApiResponse<T>.handle(...) {
    when (this) { ... }
}

// Uso:
response.handle(...)
//       ↑
//    Como si fuera un método de ApiResponse
```

**Cómo funciona:**
```kotlin
fun String.greet() = "Hola, $this"

// Uso:
val greeting = "Juan".greet() // "Hola, Juan"

// "Juan" se convierte en "this" dentro de greet()
```

2. **inline y crossinline:**

**inline:**
```kotlin
// Sin inline:
fun doSomething(action: () -> Unit) {
    action()
}

doSomething {
    println("Hola")
}

// Bytecode generado:
// 1. Crear objeto Function
// 2. Llamar a doSomething
// 3. doSomething llama a action()

// Con inline:
inline fun doSomething(action: () -> Unit) {
    action()
}

// Bytecode generado:
// 1. Código de action() se inserta directamente
println("Hola") // No hay llamada a función extra
```

**Ventaja:** Más rápido, menos objetos creados.

**crossinline:**
- Permite que el lambda sea llamado en otro contexto
- Previene `return` dentro del lambda

```kotlin
inline fun doSomething(crossinline action: () -> Unit) {
    Thread {
        action() // Llamado desde otro thread
    }.start()
}
```

3. **Parámetros con valores por defecto:**

```kotlin
fun handle(
    onLoading: () -> Unit = {},     // Default: no hacer nada
    onSuccess: (T) -> Unit,         // Obligatorio
    onError: (String, Int?) -> Unit = { _, _ -> }, // Default: no hacer nada
    onNetworkError: () -> Unit = {} // Default: no hacer nada
)

// Uso 1: Solo manejar éxito
response.handle(
    onSuccess = { movies -> displayMovies(movies) }
)

// Uso 2: Manejar éxito y error
response.handle(
    onSuccess = { movies -> displayMovies(movies) },
    onError = { message, _ -> showError(message) }
)

// Uso 3: Todo
response.handle(
    onLoading = { showSpinner() },
    onSuccess = { movies -> displayMovies(movies) },
    onError = { message, code -> showError(message) },
    onNetworkError = { showOffline() }
)
```

4. **() -> Unit explicado:**

```kotlin
// Tipo de función:
val myFunction: () -> Unit
//               ↑     ↑
//          sin params retorna nada

// Ejemplos:
val greet: () -> Unit = {
    println("Hola")
}

val add: (Int, Int) -> Int = { a, b ->
    a + b
}

val transform: (String) -> String = { str ->
    str.uppercase()
}

// Uso:
greet() // "Hola"
val sum = add(5, 3) // 8
val upper = transform("hola") // "HOLA"
```

---

### 14. Constants.kt - La Configuración

**Archivo:** `movielib/src/main/java/com/movielib/movielib/utils/Constants.kt`

#### Analogía: El Archivo de Configuración

Como el archivo `settings.ini` de un programa: todas las configuraciones en un solo lugar.

#### Código Explicado:

```kotlin
object Constants {

    val TMDB_API_KEY: String = BuildConfig.TMDB_API_KEY

    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
    const val IMAGE_SIZE_W92 = "w92"
    const val IMAGE_SIZE_W154 = "w154"
    const val IMAGE_SIZE_W342 = "w342"
    const val IMAGE_SIZE_W780 = "w780"
    const val IMAGE_SIZE_ORIGINAL = "original"

    fun buildPosterUrl(posterPath: String?, size: String = IMAGE_SIZE_W500): String? {
        return if (posterPath != null) {
            "$IMAGE_BASE_URL$size$posterPath"
        } else {
            null
        }
    }
}
```

**Desglose:**

1. **object vs class:**

```kotlin
// object: Singleton automático
object Constants {
    const val MAX_ITEMS = 100
}

// Uso directo:
val max = Constants.MAX_ITEMS

// class: Necesitas crear instancias
class Settings {
    val maxItems = 100
}

// Uso:
val settings = Settings()
val max = settings.maxItems
```

2. **BuildConfig:**

```kotlin
// En build.gradle.kts:
buildConfigField("String", "TMDB_API_KEY", "\"${project.findProperty("TMDB_API_KEY")}\"")

// Genera:
public final class BuildConfig {
    public static final String TMDB_API_KEY = "tu_clave_aqui";
}

// En Kotlin:
val key = BuildConfig.TMDB_API_KEY
```

**Por qué es mejor que hardcodear:**
```kotlin
// ❌ Hardcoded (malo):
const val API_KEY = "abc123xyz" // Expuesto en código fuente

// ✅ BuildConfig (bueno):
val API_KEY = BuildConfig.TMDB_API_KEY // Valor en local.properties
                                        // No se sube a Git
```

3. **String Templates:**

```kotlin
val name = "Juan"
val age = 25

// Interpolación simple:
val message = "Hola, $name" // "Hola, Juan"

// Expresión compleja:
val message2 = "En 10 años tendrás ${age + 10}" // "En 10 años tendrás 35"

// buildPosterUrl usa concatenación:
fun buildPosterUrl(posterPath: String?, size: String): String? {
    return "$IMAGE_BASE_URL$size$posterPath"
    //      ↑               ↑    ↑
    //      "https://..."   "w500" "/abc.jpg"
    // Resultado: "https://image.tmdb.org/t/p/w500/abc.jpg"
}
```

---

## Testing

### Tipos de Tests en MovieLib

#### 1. Tests Unitarios (JUnit + MockK)

**Archivo:** `movielib/src/test/java/com/movielib/movielib/repository/MovieRepositoryTest.kt`

```kotlin
@Test
fun `getLibraryMoviesFlow returns flow from DAO`() = runTest {
    // Arrange (Preparar)
    val movies = listOf(
        Movie(id = 1, title = "Movie 1", isInLibrary = true),
        Movie(id = 2, title = "Movie 2", isInLibrary = true)
    )
    every { movieDao.getLibraryMoviesFlow() } returns flowOf(movies)

    // Act (Actuar)
    repository.getLibraryMoviesFlow().test {
        val emittedMovies = awaitItem()

        // Assert (Afirmar)
        assertEquals(2, emittedMovies.size)
        assertEquals("Movie 1", emittedMovies[0].title)
        awaitComplete()
    }
}
```

**Desglose:**

1. **@Test:**
   - Marca un método como test
   - JUnit lo ejecuta automáticamente

2. **runTest:**
   - Scope para tests de corutinas
   - Controla el tiempo virtual

```kotlin
runTest {
    delay(1000) // En tests, no espera 1 segundo real
                // Avanza el tiempo virtual instantáneamente
}
```

3. **MockK (every/verify):**

```kotlin
// Mock: Objeto falso que simula comportamiento
val mockDao = mockk<MovieDao>()

// every: Definir comportamiento
every { mockDao.getMovieById(1) } returns Movie(id = 1, title = "Test")

// Uso:
val movie = mockDao.getMovieById(1)
// Retorna el Movie que definimos

// verify: Verificar que se llamó
verify { mockDao.getMovieById(1) }
```

4. **Turbine (.test):**

```kotlin
// Sin Turbine (difícil):
val results = mutableListOf<Movie>()
val job = launch {
    flow.collect { results.add(it) }
}
job.cancel()
assertEquals(expected, results[0])

// Con Turbine (fácil):
flow.test {
    val item = awaitItem()     // Esperar primer item
    assertEquals(expected, item)
    awaitComplete()             // Verificar que terminó
}
```

#### 2. Tests Instrumentados (AndroidJUnit)

**Archivo:** `movielib/src/androidTest/java/com/movielib/movielib/database/MovieDaoTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class MovieDaoTest {

    private lateinit var database: MovieDatabase
    private lateinit var movieDao: MovieDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MovieDatabase::class.java
        ).build()
        movieDao = database.movieDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveMovie() = runTest {
        // Arrange
        val movie = Movie(id = 1, title = "Test Movie", ...)

        // Act
        movieDao.insertMovie(movie)
        val retrieved = movieDao.getMovieById(1)

        // Assert
        assertEquals(movie.title, retrieved?.title)
    }
}
```

**Desglose:**

1. **@RunWith(AndroidJUnit4::class):**
   - Ejecuta en emulador/dispositivo Android
   - Tiene acceso a APIs de Android

2. **@Before y @After:**

```kotlin
@Before // Se ejecuta ANTES de cada test
fun setup() {
    // Preparar recursos
}

@Test
fun test1() {
    // Usa recursos
}

@Test
fun test2() {
    // Usa recursos
}

@After // Se ejecuta DESPUÉS de cada test
fun cleanup() {
    // Limpiar recursos
}

// Orden de ejecución:
// setup() → test1() → cleanup()
// setup() → test2() → cleanup()
```

3. **In-Memory Database:**

```kotlin
// Base de datos normal:
Room.databaseBuilder(..., "real_database.db") // Crea archivo en disco

// Base de datos en memoria (para tests):
Room.inMemoryDatabaseBuilder(...) // Solo en RAM, se borra al terminar
```

**Ventajas:**
- Más rápido (RAM vs Disco)
- Aislado (no afecta datos reales)
- Se limpia automáticamente

---

## Conceptos Clave de Kotlin

### 1. Null Safety

**El problema del billón de dólares (Tony Hoare):**
```java
// Java/C#:
String nombre = null;
int length = nombre.length(); // NullPointerException 💥
```

**Solución de Kotlin:**
```kotlin
// Tipos no-nullable (default):
val nombre: String = "Juan"
val length = nombre.length // ✅ Siempre funciona

// Tipos nullable (explicit):
val apellido: String? = null
val length = apellido.length // ❌ Error de compilación

// Manejo seguro:
val length = apellido?.length // ✅ Retorna null si apellido es null

// Con valor por defecto:
val length = apellido?.length ?: 0 // ✅ Retorna 0 si es null

// Assertion (!!) - úsalo solo si estás 100% seguro:
val length = apellido!!.length // Si es null → crash
```

### 2. Data Classes

```kotlin
// Sin data class:
class Person(val name: String, val age: Int) {
    override fun equals(other: Any?): Boolean { ... } // 10 líneas
    override fun hashCode(): Int { ... }               // 5 líneas
    override fun toString(): String { ... }            // 3 líneas
    fun copy(...): Person { ... }                      // 8 líneas
}

// Con data class:
data class Person(val name: String, val age: Int)
// ↑ 26 líneas → 1 línea
```

### 3. Sealed Classes

```kotlin
sealed class Result {
    data class Success(val data: String) : Result()
    data class Error(val message: String) : Result()
    object Loading : Result()
}

// El compilador conoce TODAS las subclases:
fun handle(result: Result) = when (result) {
    is Result.Success -> println(result.data)
    is Result.Error -> println(result.message)
    is Result.Loading -> println("Loading...")
    // Si falta un caso → error de compilación ✅
}
```

### 4. Coroutines vs Threads

```kotlin
// ❌ Threads (vieja escuela):
Thread {
    val result = heavyWork() // Bloquea el thread
    runOnUiThread {
        updateUI(result)
    }
}.start()

// ✅ Coroutines (moderna):
lifecycleScope.launch {
    val result = heavyWork() // No bloquea, solo suspende
    updateUI(result)         // En el mismo scope, sin cambios
}
```

**Diferencias:**
```
Thread:
  - Pesado (~1MB de memoria)
  - Bloqueante
  - Difícil de cancelar

Coroutine:
  - Ligera (~KB de memoria)
  - Suspendible
  - Fácil de cancelar
```

### 5. Flow vs LiveData

```kotlin
// LiveData (viejo):
val movies: LiveData<List<Movie>> = liveData {
    emit(repository.getMovies())
}

// Flow (nuevo):
val movies: Flow<List<Movie>> = flow {
    emit(repository.getMovies())
}
```

**Flow ventajas:**
- Más operadores (map, filter, combine, etc.)
- Cold stream (no se ejecuta hasta que alguien lo observa)
- Mejor para casos complejos

**LiveData ventajas:**
- Lifecycle-aware por defecto
- Más simple para casos básicos

---

## Flujo Completo de Datos

### Ejemplo: Búsqueda de "Inception"

```
1. USUARIO
   ↓
   Escribe "Inception" en SearchActivity
   ↓
2. UI LAYER (SearchActivity)
   ↓
   lifecycleScope.launch {
       repository.searchMovies("Inception")
   }
   ↓
3. DOMAIN LAYER (MovieRepository)
   ↓
   fun searchMovies(query: String): Flow<ApiResponse<List<Movie>>> = flow {
       emit(ApiResponse.Loading) ────────────────┐
       ↓                                          │
       val response = tmdbService.search(query)  │
       ↓                                          │
       if (response.isSuccessful) {              │
           val movies = response.body()          │
           movieDao.insertMovies(movies) ←─ CACHE│
           emit(ApiResponse.Success(movies)) ────┤
       }                                          │
   }                                              │
   ↓                                              │
4. DATA LAYER                                     │
   ↓                                              │
   TMDbService (Retrofit)                         │
   ↓                                              │
   HTTP GET https://api.themoviedb.org/3/search/movie?query=Inception
   ↓                                              │
   JSON Response                                  │
   ↓                                              │
   Gson Converter → List<MovieApiModel>           │
   ↓                                              │
   movieApiModel.toMovie() → List<Movie>          │
   ↓                                              │
5. VUELTA A UI                                    │
   ↓                                              │
   Flow emite valores ───────────────────────────┘
   ↓
   SearchActivity.collect { response ->
       when (response) {
           is Loading → showSpinner() ← Primera emisión
           is Success → displayMovies(response.data) ← Segunda emisión
       }
   }
```

---

## Preguntas Frecuentes

### ¿Por qué usar Repository en lugar de llamar a la API directamente?

**Sin Repository:**
```kotlin
class MainActivity {
    fun loadMovies() {
        // Llamada directa a API
        val movies = api.getMovies()
        // ¿Qué pasa si quiero caché?
        // ¿Qué pasa si quiero cambiar de API a base de datos?
        // Necesito cambiar código en TODAS las Activities
    }
}
```

**Con Repository:**
```kotlin
class MainActivity {
    fun loadMovies() {
        val movies = repository.getMovies()
        // Repository decide: ¿API? ¿Caché? ¿Ambos?
        // Si cambio la implementación, solo modifico el Repository
    }
}
```

**Ventajas:**
1. **Single Source of Truth:** Un solo lugar para decisiones de datos
2. **Testeable:** Puedo hacer mock del Repository fácilmente
3. **Mantenible:** Cambios en una sola clase
4. **Escalable:** Fácil añadir más fuentes de datos

### ¿Cuándo usar Flow vs suspend?

```kotlin
// suspend: Operación única
suspend fun getMovie(id: Int): Movie {
    return api.getMovie(id) // Se ejecuta UNA vez
}

// Flow: Stream de valores
fun getMovies(): Flow<List<Movie>> = flow {
    while (true) {
        delay(5000)
        emit(api.getMovies()) // Emite cada 5 segundos
    }
}
```

**Usa suspend cuando:**
- Solo necesitas el resultado una vez
- Es una operación simple (get, post, delete)

**Usa Flow cuando:**
- Observas cambios en tiempo real
- Múltiples emisiones de valores
- Necesitas operadores (map, filter, etc.)

### ¿Por qué data class en lugar de class?

```kotlin
// Sin data class:
class Movie(val id: Int, val title: String)

val movie1 = Movie(1, "Inception")
val movie2 = Movie(1, "Inception")

println(movie1 == movie2) // false (compara referencias)
println(movie1)           // "Movie@1a2b3c" (hash code)

// Con data class:
data class Movie(val id: Int, val title: String)

val movie1 = Movie(1, "Inception")
val movie2 = Movie(1, "Inception")

println(movie1 == movie2) // true (compara valores)
println(movie1)           // "Movie(id=1, title=Inception)"

val movie3 = movie1.copy(title = "Matrix")
println(movie3) // "Movie(id=1, title=Matrix)"
```

### ¿Qué es el patrón MVVM y por qué no está en este proyecto?

**MVVM = Model-View-ViewModel**

```
View (Activity) ←→ ViewModel ←→ Model (Repository)
```

**Este proyecto usa:**
```
View (Activity) ←→ Model (Repository)
```

**¿Por qué?**
- Proyecto educativo/demo
- Lógica de UI es simple
- Para producción, se recomienda añadir ViewModel

**Con ViewModel sería:**
```kotlin
class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    fun searchMovies(query: String) {
        viewModelScope.launch {
            repository.searchMovies(query).collect { response ->
                if (response is ApiResponse.Success) {
                    _movies.value = response.data
                }
            }
        }
    }
}

// Activity:
class SearchActivity : AppCompatActivity() {
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.movies.collectLatest { movies ->
            adapter.submitList(movies)
        }
    }
}
```

---

## Conclusión

Has aprendido:

✅ Arquitectura en capas (Data, Domain, UI)
✅ Patrón Repository
✅ Room Database (SQLite)
✅ Retrofit (API REST)
✅ Kotlin Coroutines y Flow
✅ Testing (unitario e instrumentado)
✅ Conceptos avanzados de Kotlin

**Próximos pasos:**
1. Implementar ViewModels (MVVM)
2. Añadir Dependency Injection (Hilt/Koin)
3. Migrar a Jetpack Compose
4. Implementar paginación

---

**¡Felicidades! Ahora entiendes MovieLib de principio a fin.** 🎉

---

*Documento creado con fines educativos - Enero 2025*
