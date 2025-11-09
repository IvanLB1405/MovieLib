# MovieLib

[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg?style=flat)](https://kotlinlang.org)

**MovieLib** es una librería Android reutilizable que proporciona componentes UI y lógica de negocio para aplicaciones relacionadas con películas. Integra la API de The Movie Database (TMDb) para búsqueda y visualización de información cinematográfica, con soporte para biblioteca personal, valoraciones y reseñas.

## Características

- 🎬 **Integración completa con TMDb API**
  - Búsqueda de películas
  - Películas populares y mejor valoradas
  - Detalles completos (géneros, reparto, valoraciones)
  - Películas en cines

- 💾 **Persistencia local con Room**
  - Caché automático de resultados
  - Biblioteca personal del usuario
  - Valoraciones y reseñas offline
  - Estrategia de caché inteligente

- 🏗️ **Arquitectura limpia**
  - Patrón Repository
  - Kotlin Coroutines y Flow
  - Manejo de estados (Loading/Success/Error)
  - Separación de capas (Data/Domain/UI)

- 🎨 **Componentes UI incluidos**
  - Adaptadores RecyclerView
  - Pantallas de búsqueda y detalles
  - Layouts personalizables

## Requisitos

- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 35
- **Kotlin:** 1.9+
- **Gradle:** 8.0+

## Instalación

### 1. Añadir la librería al proyecto

En el archivo `settings.gradle.kts`:

```kotlin
include(":movielib")
```

En el `build.gradle.kts` de tu módulo app:

```kotlin
dependencies {
    implementation(project(":movielib"))
}
```

### 2. Configurar la API key de TMDb

Crea una cuenta en [TMDb](https://www.themoviedb.org/) y obtén tu API key.

En `local.properties`:

```properties
TMDB_API_KEY=tu_api_key_aqui
```

En `build.gradle.kts` de tu app:

```kotlin
android {
    defaultConfig {
        val properties = java.util.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }

        val apiKey = properties.getProperty("TMDB_API_KEY") ?: ""
        buildConfigField("String", "TMDB_API_KEY", "\"$apiKey\"")
    }

    buildFeatures {
        buildConfig = true
    }
}
```

### 3. Permisos necesarios

En `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Uso

### Inicializar el Repository

```kotlin
import com.movielib.movielib.repository.MovieRepository
import com.movielib.movielib.database.MovieDatabase
import com.movielib.movielib.utils.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var repository: MovieRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = MovieDatabase.getDatabase(applicationContext)
        repository = MovieRepository(
            movieDao = database.movieDao(),
            apiKey = Constants.TMDB_API_KEY
        )
    }
}
```

### Buscar películas

```kotlin
import com.movielib.movielib.api.ApiResponse
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

lifecycleScope.launch {
    repository.searchMovies("Inception").collect { response ->
        when (response) {
            is ApiResponse.Loading -> {
                // Mostrar indicador de carga
                showLoading()
            }
            is ApiResponse.Success -> {
                // Mostrar resultados
                val movies = response.data
                displayMovies(movies)
            }
            is ApiResponse.Error -> {
                // Mostrar error
                showError(response.message)
            }
            is ApiResponse.NetworkError -> {
                // Sin conexión
                showNetworkError()
            }
        }
    }
}
```

### Obtener películas populares

```kotlin
lifecycleScope.launch {
    repository.getPopularMovies(page = 1).collect { response ->
        when (response) {
            is ApiResponse.Success -> {
                val movies = response.data
                updateUI(movies)
            }
            // ... manejar otros estados
        }
    }
}
```

### Obtener detalles de una película

```kotlin
lifecycleScope.launch {
    repository.getMovieDetails(movieId = 27205).collect { response ->
        when (response) {
            is ApiResponse.Success -> {
                val movie = response.data
                // Acceder a géneros, reparto, etc.
                val genres = movie.genres
                val cast = movie.cast
            }
            // ... manejar otros estados
        }
    }
}
```

### Gestionar biblioteca personal

```kotlin
// Añadir a biblioteca
lifecycleScope.launch {
    val success = repository.addToLibrary(movieId)
    if (success) {
        showMessage("Añadida a biblioteca")
    }
}

// Eliminar de biblioteca
lifecycleScope.launch {
    val success = repository.removeFromLibrary(movieId)
}

// Verificar si está en biblioteca
lifecycleScope.launch {
    val isInLibrary = repository.isMovieInLibrary(movieId)
}

// Observar cambios en biblioteca
lifecycleScope.launch {
    repository.getLibraryMoviesFlow().collect { movies ->
        updateLibraryUI(movies)
    }
}
```

### Valoraciones y reseñas

```kotlin
// Añadir valoración (0.0 - 10.0)
lifecycleScope.launch {
    repository.updateUserRating(movieId, rating = 8.5f)
}

// Añadir reseña
lifecycleScope.launch {
    repository.updateUserReview(movieId, "Excelente película!")
}

// Obtener películas con reseñas
lifecycleScope.launch {
    val moviesWithReviews = repository.getMoviesWithReviews()
}
```

### Estadísticas de biblioteca

```kotlin
lifecycleScope.launch {
    val stats = repository.getLibraryStats()

    println("Total películas: ${stats.totalMovies}")
    println("Rating promedio: ${stats.averageRating}")
    println("Con reseñas: ${stats.moviesWithReviews}")
}
```

### Cargar imágenes de pósters

```kotlin
import com.bumptech.glide.Glide
import com.movielib.movielib.api.TMDbService

// Construir URL del póster
val posterUrl = TMDbService.getPosterUrl(
    posterPath = movie.posterPath,
    size = TMDbService.POSTER_SIZE_W500
)

// Cargar con Glide
Glide.with(context)
    .load(posterUrl)
    .placeholder(R.drawable.placeholder_movie)
    .into(imageView)
```

## Arquitectura

### Capas

```
┌─────────────────────────────────────┐
│           UI Layer                  │
│  (Activities, Adapters, Views)      │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│        Repository Layer             │
│  (MovieRepository, ApiResponse)     │
└─────┬───────────────────┬───────────┘
      │                   │
┌─────▼──────────┐  ┌────▼──────────┐
│  Data Layer    │  │  Data Layer   │
│  (TMDb API)    │  │  (Room DB)    │
│  - Retrofit    │  │  - MovieDao   │
│  - ApiClient   │  │  - Movie      │
└────────────────┘  └───────────────┘
```

### Clases principales

- **MovieRepository**: Coordina entre API y base de datos
- **TMDbService**: Interfaz Retrofit con endpoints de TMDb
- **ApiClient**: Cliente HTTP singleton con configuración
- **MovieDao**: Acceso a datos locales con Room
- **Movie**: Entidad que representa una película
- **ApiResponse**: Sealed class para estados de la API

## Dependencias

La librería incluye:

- **Retrofit 2.9.0** - Cliente HTTP
- **Room 2.6.1** - Base de datos local
- **Glide 4.16.0** - Carga de imágenes
- **Kotlin Coroutines 1.7.3** - Programación asíncrona
- **Gson 2.9.0** - Serialización JSON
- **Material Components** - Diseño UI

## Ejemplo de Aplicación

Ver el módulo `:app` (MovieCritique) para un ejemplo completo de integración.

## Licencia

Este proyecto es parte de un Proyecto Final de Ciclo (PFC) para DAM 2º.

## Contribuir

Para reportar bugs o sugerir mejoras, por favor abre un issue en el repositorio.

## Recursos

- [TMDb API Documentation](https://developers.themoviedb.org/3)
- [Android Room Guide](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Retrofit](https://square.github.io/retrofit/)

---

**Desarrollado como parte del PFC - DAM 2º**
