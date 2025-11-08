# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**MovieLib + MovieCritique** is a Final Project (PFC) for DAM 2º (Desarrollo de Aplicaciones Multiplataforma) consisting of:

1. **`:movielib`** - Reusable Android library with UI components and business logic for movie-related functionality
2. **`:app` (MovieCritique)** - Demo application showcasing the library as a simplified movie critique platform

The project integrates with The Movie Database (TMDb) API to search and display movie information, allowing users to maintain a personal library of movies with ratings and reviews.

**IMPORTANT:** See `REQUIREMENTS.md` for complete functional requirements (RF01-RF07) and project specifications.

**Project Structure:**
- `:app` - MovieCritique demo application (Android Application Module)
- `:movielib` - Reusable library with UI components and business logic (Android Library Module)

## Architecture

### Layer Architecture

The library follows a clean architecture pattern with distinct layers:

1. **Data Layer** (`movielib/src/main/java/com/movielib/movielib/`)
   - `api/` - Retrofit service definitions and API client (TMDbService, ApiClient)
   - `database/` - Room database, DAOs (MovieDatabase, MovieDao)
   - `models/` - Data models and entities (Movie entity, API models, converters)
   - `repository/` - Repository pattern implementation (MovieRepository)

2. **Domain Layer**
   - Business logic is handled in the Repository layer
   - Uses Kotlin Flow for reactive data streams
   - ApiResponse sealed class for standardized API response handling

3. **UI Layer** (`movielib/src/main/java/com/movielib/movielib/ui/`)
   - `list/` - Movie list adapters (MovieAdapter - currently placeholder)
   - `details/` - Movie detail screens (MovieDetailActivity - currently placeholder)
   - `search/` - Search functionality (SearchActivity)

### Key Architectural Patterns

**Repository Pattern:**
- `MovieRepository` is the single source of truth
- Coordinates between remote API (TMDb) and local database (Room)
- Implements caching strategy: API results are automatically cached in Room
- Returns Kotlin Flow for reactive updates

**Data Flow:**
- Network calls → MovieRepository → Room database (cache) → UI
- For movie details: checks local cache first, then fetches from API
- Preserves user data (ratings, reviews, library status) when updating from API

**API Response Handling:**
- Uses sealed class `ApiResponse<T>` with states: Loading, Success, Error, NetworkError
- Extension functions for convenient state checking (isSuccess(), getDataOrNull())

### Database Schema

**Movie Entity** (Room):
- Primary key: `id` (Int)
- Movie data: title, overview, posterPath, releaseDate, voteAverage
- Extended data: genres (JSON string), cast (JSON string)
- User data: isInLibrary (Boolean), userRating (Float?), userReview (String?), dateAdded (Long?)

**Important:** User data fields (isInLibrary, userRating, userReview, dateAdded) are preserved when updating movie details from the API.

### TMDb API Integration

**API Configuration:**
- Base URL: https://api.themoviedb.org/3/
- API Key stored in: `movielib/src/main/java/com/movielib/movielib/utils/Constants.kt`
- Default language: Spanish (es-ES)

**Available Endpoints:**
- `searchMovies()` - Search movies by text
- `getPopularMovies()` - Get popular movies
- `getTopRatedMovies()` - Get top-rated movies
- `getNowPlayingMovies()` - Get movies in theaters
- `getMovieDetails()` - Get full movie details with credits

**Image URLs:**
- Posters: Use `TMDbService.getPosterUrl(posterPath, size)` or `Constants.buildPosterUrl()`
- Available sizes: w92, w154, w185, w342, w500, w780, original

## Build Commands

### Gradle Tasks

```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run specific module tests
./gradlew :movielib:test
./gradlew :app:test

# Check dependencies
./gradlew dependencies
```

### Installation

```bash
# Install debug build to connected device
./gradlew installDebug

# Install and run
./gradlew installDebug && adb shell am start -n com.movielib.movielib/.ApiTestActivity
```

## Development Setup

### Prerequisites
- Android Studio (latest stable)
- JDK 11
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 35

### Dependencies (movielib module)

**Core:**
- Kotlin
- AndroidX Core KTX, AppCompat
- Material Design Components
- RecyclerView, ConstraintLayout

**Networking:**
- Retrofit 2.9.0
- Gson converter
- OkHttp logging interceptor

**Database:**
- Room 2.6.1 (runtime, KTX, compiler via kapt)

**Image Loading:**
- Glide 4.16.0

**Async:**
- Kotlin Coroutines 1.7.3

**Architecture Components:**
- Lifecycle ViewModel KTX 2.7.0
- LiveData KTX 2.7.0
- Fragment KTX

### Important Configuration Details

**View Binding:**
- Enabled in `movielib` module
- Use for all new UI code

**KAPT:**
- Required for Room and Glide
- Both modules have kapt enabled

**Network Configuration:**
- Internet and network state permissions required
- Uses cleartext traffic (development)
- 30-second timeout for all requests

## Working with the Codebase

### Adding New Features

When adding new features to the library:

1. **Models:** Add data classes in `models/Movie.kt` or create new model files
2. **API Endpoints:** Add methods to `api/TMDbService.kt`
3. **Database Operations:** Add queries to `database/MovieDao.kt`
4. **Business Logic:** Implement in `repository/MovieRepository.kt`
5. **UI Components:** Create in appropriate `ui/` subdirectories

### Working with Movie Data

**To fetch movies:**
```kotlin
val repository = MovieRepository(movieDao, Constants.TMDB_API_KEY)
repository.searchMovies("query").collect { response ->
    when (response) {
        is ApiResponse.Loading -> // Show loading
        is ApiResponse.Success -> // Update UI with response.data
        is ApiResponse.Error -> // Show error
        is ApiResponse.NetworkError -> // Handle no connection
    }
}
```

**User Library Operations:**
- `addToLibrary(movieId)` - Add movie to personal library
- `removeFromLibrary(movieId)` - Remove from library (clears user data)
- `updateUserRating(movieId, rating)` - Set user rating (1-10)
- `updateUserReview(movieId, review)` - Add/update review
- `getLibraryMoviesFlow()` - Observe library changes with Flow
- `getLibraryStats()` - Get statistics (count, average rating, reviews)

### Important Notes

**API Key Management:**
- API key is currently hardcoded in `Constants.kt:17`
- For production, should be moved to `local.properties` or build config
- Never commit API keys to version control (currently it is committed)

**Database Migrations:**
- Currently uses `fallbackToDestructiveMigration()` which deletes data on schema changes
- For production, implement proper Room migrations

**Error Handling:**
- All repository methods return Flow with ApiResponse
- Network errors are caught and emit ApiResponse.NetworkError
- HTTP errors include status codes
- Local cache fallback for offline access

**Testing:**
- Unit tests in `src/test/`
- Instrumented tests in `src/androidTest/`
- ApiClient has `clearInstance()` method for test cleanup

### Current State

The project is in active development:
- Core data layer is fully implemented
- Repository pattern is complete with caching
- UI components (MovieAdapter, MovieDetailActivity, SearchActivity) are placeholder classes
- Layout files exist but activities are not implemented
- ApiTestActivity is the current launcher activity for testing

When implementing UI features, refer to the existing layout files:
- `activity_main.xml` - Main screen
- `activity_search.xml` - Search screen
- `activity_movie_detail.xml` - Movie details
- `item_movie_horizontal.xml` - Horizontal movie item
- `item_movie_grid.xml` - Grid movie item

---

## 🔧 MEJORAS PENDIENTES PARA VERSIÓN 2.0 (Auditoría Senior Developer)

> **Nota:** Esta sección documenta mejoras técnicas identificadas en auditoría de código (Enero 2025).
> La versión actual (v1.0) es funcional y cumple con los requisitos del PFC.
> Estas mejoras se implementarán en futuras versiones para producción.

### 🔐 CRÍTICO - Seguridad (Prioridad Alta)

#### SEC-01: API Key Hardcodeada ⚠️ CRÍTICO
**Problema:**
- API key de TMDb expuesta en código fuente (`Constants.kt:17`)
- Comprometida en repositorio Git
- Riesgo de abuso y límite de rate excedido

**Solución:**
```kotlin
// En build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "TMDB_API_KEY", "\"${project.findProperty("TMDB_API_KEY")}\"")
    }
}

// En local.properties (NO commitear)
TMDB_API_KEY=tu_clave_aqui

// Usar en código
BuildConfig.TMDB_API_KEY
```

**Archivos afectados:**
- `movielib/src/main/java/com/movielib/movielib/utils/Constants.kt`
- `build.gradle.kts` (ambos módulos)
- `.gitignore` (añadir `local.properties`)

#### SEC-02: Cleartext Traffic en Producción
**Problema:**
- `usesCleartextTraffic="true"` en AndroidManifest (línea 18)
- Permite tráfico HTTP no cifrado
- Vulnerabilidad de seguridad en producción

**Solución:**
```xml
<!-- Solo para debug -->
<application
    android:usesCleartextTraffic="false"
    ...>
```

**Archivo:** `app/src/main/AndroidManifest.xml`

#### SEC-03: Logging Interceptor en Release
**Problema:**
- `HttpLoggingInterceptor` con `Level.BODY` siempre activo
- Expone datos sensibles en logs de producción
- Impacto en performance

**Solución:**
```kotlin
private fun getOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder().apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        // timeouts...
    }.build()
}
```

**Archivo:** `movielib/src/main/java/com/movielib/movielib/api/ApiClient.kt:21-24`

#### SEC-04: ProGuard/R8 No Configurado
**Problema:**
- `isMinifyEnabled = false` en release build
- Código sin ofuscar expone lógica de negocio
- APK más grande de lo necesario

**Solución:**
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

**Archivo:** `app/build.gradle.kts:22-28`

#### SEC-05: Backup Sin Cifrado
**Problema:**
- `allowBackup="true"` sin configuración de cifrado
- Datos de usuario expuestos en backups
- Potencial fuga de reseñas y ratings privados

**Solución:**
```xml
<application
    android:allowBackup="true"
    android:fullBackupContent="@xml/backup_rules"
    android:dataExtractionRules="@xml/data_extraction_rules"
    ...>
```

En `backup_rules.xml`:
```xml
<full-backup-content>
    <exclude domain="database" path="movie_database" />
</full-backup-content>
```

**Archivo:** `app/src/main/AndroidManifest.xml:10-12`

#### SEC-06: Certificate Pinning No Implementado
**Recomendación:**
- Implementar SSL pinning para API TMDb
- Prevenir ataques Man-in-the-Middle
- Usar OkHttp CertificatePinner

**Prioridad:** Media (para producción)

---

### 🏗️ Arquitectura y Diseño (Prioridad Alta)

#### ARCH-01: Falta Capa ViewModel (MVVM Incompleto)
**Problema:**
- Activities manejan lógica de negocio directamente
- No hay separación entre UI y lógica
- Dificulta testing y mantenimiento

**Solución:**
Implementar ViewModels para cada pantalla:
```kotlin
class MainViewModel(private val repository: MovieRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun loadMovies() {
        viewModelScope.launch {
            repository.getPopularMovies().collect { response ->
                _uiState.value = when (response) {
                    is ApiResponse.Success -> MainUiState.Success(response.data)
                    is ApiResponse.Error -> MainUiState.Error(response.message)
                    // ...
                }
            }
        }
    }
}
```

**Archivos a crear:**
- `app/src/main/java/com/movielib/viewmodels/MainViewModel.kt`
- `app/src/main/java/com/movielib/viewmodels/SearchViewModel.kt`
- `app/src/main/java/com/movielib/viewmodels/MovieDetailViewModel.kt`
- `app/src/main/java/com/movielib/viewmodels/LibraryViewModel.kt`

#### ARCH-02: Inyección de Dependencias Manual
**Problema:**
- Singleton manual en ApiClient y MovieDatabase
- Repository creado directamente en Activities
- Acoplamiento fuerte y difícil testing

**Solución:**
Implementar Hilt/Koin para DI:
```kotlin
@HiltAndroidApp
class MovieCritiqueApp : Application()

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideMovieDatabase(@ApplicationContext context: Context): MovieDatabase {
        return MovieDatabase.getDatabase(context)
    }

    @Provides
    fun provideMovieRepository(
        movieDao: MovieDao,
        @ApiKey apiKey: String
    ): MovieRepository {
        return MovieRepository(movieDao, apiKey)
    }
}
```

**Dependencias a añadir:**
```kotlin
// Hilt
implementation("com.google.dagger:hilt-android:2.50")
kapt("com.google.dagger:hilt-compiler:2.50")
```

#### ARCH-03: Sin Navigation Component
**Problema:**
- Navegación con `startActivity()` directamente
- No hay grafo de navegación
- Dificulta deep linking y back stack

**Solución:**
```kotlin
// En build.gradle.kts
implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

// Crear nav_graph.xml
<navigation>
    <fragment id="@+id/mainFragment" ...>
        <action id="@+id/action_to_detail"
                destination="@id/detailFragment" />
    </fragment>
</navigation>
```

**Archivos a crear:**
- `app/src/main/res/navigation/nav_graph.xml`
- Convertir Activities a Fragments

#### ARCH-04: Repository Sin Abstracción
**Problema:**
- MovieRepository clase concreta sin interfaz
- Dificulta testing con mocks
- Acoplamiento con implementación específica

**Solución:**
```kotlin
interface IMovieRepository {
    fun searchMovies(query: String): Flow<ApiResponse<List<Movie>>>
    fun getMovieDetails(id: Int): Flow<ApiResponse<Movie>>
    // ...
}

class MovieRepositoryImpl(
    private val movieDao: MovieDao,
    private val apiKey: String
) : IMovieRepository {
    // implementación actual
}
```

**Archivos a modificar:**
- `movielib/src/main/java/com/movielib/movielib/repository/MovieRepository.kt`

#### ARCH-05: Sin Manejo de Estados UI
**Problema:**
- Estados UI dispersos en Activities
- No hay sealed class para estados
- Código repetitivo para loading/error/success

**Solución:**
```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val code: Int? = null) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}
```

**Archivo a crear:**
- `app/src/main/java/com/movielib/ui/common/UiState.kt`

---

### ⚡ Optimización y Performance (Prioridad Media)

#### PERF-01: Destructive Migration
**Problema:**
- `fallbackToDestructiveMigration()` borra datos en cada cambio de schema
- Usuario pierde biblioteca personal y reseñas
- Inaceptable en producción

**Solución:**
```kotlin
Room.databaseBuilder(...)
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .build()

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE movies ADD COLUMN newField TEXT")
    }
}
```

**Archivo:** `movielib/src/main/java/com/movielib/movielib/database/MovieDatabase.kt:41`

#### PERF-02: Sin Paginación
**Problema:**
- Carga todas las películas de una vez
- Consumo excesivo de memoria y red
- Mal UX con listas largas

**Solución:**
Implementar Paging 3:
```kotlin
// En Repository
fun getPopularMoviesPaged(): Flow<PagingData<Movie>> {
    return Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { MoviePagingSource(tmdbService, apiKey) }
    ).flow
}
```

**Dependencias:**
```kotlin
implementation("androidx.paging:paging-runtime:3.2.1")
```

#### PERF-03: Caché de Imágenes No Configurado
**Problema:**
- Glide usa configuración por defecto
- No hay control de tamaño de caché
- Posible OutOfMemory con muchas imágenes

**Solución:**
```kotlin
@GlideModule
class MovieGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setMemoryCache(LruResourceCache(20 * 1024 * 1024)) // 20MB
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, 100 * 1024 * 1024)) // 100MB
    }
}
```

#### PERF-04: Sin WorkManager para Tareas en Background
**Recomendación:**
- Implementar sincronización periódica de películas populares
- Actualizar caché en background
- Notificaciones de nuevas películas

**Prioridad:** Baja

#### PERF-05: LazyColumn en lugar de RecyclerView
**Recomendación futura:**
- Migrar a Jetpack Compose
- Mejor performance con LazyColumn
- Código más mantenible

**Prioridad:** Baja (Refactor mayor)

---

### 🧪 Testing y Calidad (Prioridad Alta)

#### TEST-01: Sin Tests Unitarios
**Problema:**
- 0% cobertura de tests
- No hay tests de Repository
- No hay tests de ViewModels (cuando se implementen)

**Solución:**
```kotlin
class MovieRepositoryTest {
    @Test
    fun `searchMovies returns success with valid query`() = runTest {
        // Arrange
        val mockDao = mockk<MovieDao>()
        val repository = MovieRepository(mockDao, "test_key")

        // Act & Assert
        repository.searchMovies("Inception").test {
            assertEquals(ApiResponse.Loading, awaitItem())
            val success = awaitItem() as ApiResponse.Success
            assertTrue(success.data.isNotEmpty())
            awaitComplete()
        }
    }
}
```

**Dependencias necesarias:**
```kotlin
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("app.cash.turbine:turbine:1.0.0")
```

**Archivos a crear:**
- `movielib/src/test/java/com/movielib/movielib/repository/MovieRepositoryTest.kt`
- `movielib/src/test/java/com/movielib/movielib/database/MovieDaoTest.kt`
- `app/src/test/java/com/movielib/viewmodels/*ViewModelTest.kt`

#### TEST-02: Sin Tests de Integración
**Recomendación:**
- Tests de Room con AndroidJUnit
- Tests de Retrofit con MockWebServer
- Tests de UI con Espresso

#### TEST-03: Sin Tests de UI
**Recomendación:**
- Implementar Espresso tests
- Screenshot testing con Paparazzi/Shot
- Accessibility tests

---

### 📝 Documentación y Código Limpio (Prioridad Media)

#### DOC-01: KDoc Incompleto
**Problema:**
- 8 de 22 archivos Kotlin sin documentación KDoc
- Métodos públicos sin documentar
- Dificulta uso de la librería por terceros

**Solución:**
```kotlin
/**
 * Busca películas en la API de TMDb usando un término de búsqueda.
 *
 * Este método realiza una búsqueda paginada en la base de datos de películas,
 * cachea los resultados localmente y emite estados de carga mediante Flow.
 *
 * @param query Término de búsqueda (mínimo 3 caracteres recomendado)
 * @param page Número de página (por defecto 1)
 * @return Flow que emite ApiResponse con estados Loading/Success/Error
 *
 * @sample
 * ```kotlin
 * repository.searchMovies("Inception").collect { response ->
 *     when (response) {
 *         is ApiResponse.Success -> displayMovies(response.data)
 *         is ApiResponse.Error -> showError(response.message)
 *         // ...
 *     }
 * }
 * ```
 *
 * @throws IllegalArgumentException si query está vacío
 * @see ApiResponse
 * @see Movie
 */
fun searchMovies(query: String, page: Int = 1): Flow<ApiResponse<List<Movie>>>
```

**Archivos a documentar:**
- `movielib/src/main/java/com/movielib/movielib/repository/MovieRepository.kt`
- `movielib/src/main/java/com/movielib/movielib/api/TMDbService.kt`
- `movielib/src/main/java/com/movielib/movielib/database/MovieDao.kt`
- Todos los adapters y activities

#### DOC-02: README de la Librería
**Archivo a crear:**
`movielib/README.md` con:
- Instrucciones de integración
- Ejemplos de uso
- API pública documentada
- Requisitos y dependencias

#### DOC-03: Comentarios en Español e Inglés Mezclados
**Problema:**
- Inconsistencia en idioma de comentarios
- Dificulta colaboración internacional

**Solución:**
Estandarizar a inglés para código público:
```kotlin
// Current: "Películas en biblioteca"
// Better: "Movies in user library"
```

#### DOC-04: Strings Hardcodeados
**Problema:**
- Textos directamente en código
- No hay internacionalización
- Dificulta traducción

**Solución:**
```xml
<!-- strings.xml -->
<string name="error_network">Network error. Please check connection.</string>
<string name="empty_library">Your library is empty</string>

// En código
binding.errorText.text = getString(R.string.error_network)
```

**Archivos a limpiar:**
- Todas las Activities
- Todos los Adapters
- Diálogos y Snackbars

#### DOC-05: Magic Numbers
**Problema:**
- Números hardcodeados sin constantes
- Ejemplo: `ratingBar.rating = rating / 2f` (línea MovieDetailActivity.kt:201)

**Solución:**
```kotlin
companion object {
    private const val RATING_BAR_MAX = 5f
    private const val RATING_SCALE = 10f
    private const val RATING_BAR_SCALE = RATING_BAR_MAX / RATING_SCALE
}

ratingBar.rating = rating * RATING_BAR_SCALE
```

---

### 🔄 Refactoring y Mejoras Generales (Prioridad Baja)

#### REF-01: Nombres de Paquetes Inconsistentes
**Problema:**
- `com.movielib.movielib` es redundante
- Debería ser `com.movielib.core` o `com.ivnfrndz.movielib`

**Solución:**
Refactor de paquetes (breaking change)

#### REF-02: Activities Muy Grandes
**Problema:**
- MainActivity con múltiples responsabilidades
- Violación de Single Responsibility Principle

**Solución:**
- Extraer lógica a ViewModels (ARCH-01)
- Crear Fragments para cada sección
- Usar Composables (futuro)

#### REF-03: Sin Manejo de Configuraciones
**Problema:**
- No hay manejo de rotación de pantalla
- Estados UI se pierden
- Posible pérdida de datos en formularios

**Solución:**
```kotlin
override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString("search_query", binding.searchView.query.toString())
}
```

#### REF-04: Sin Analytics
**Recomendación:**
- Firebase Analytics para tracking de uso
- Crashlytics para crashes en producción
- Métricas de engagement

#### REF-05: Sin CI/CD
**Recomendación:**
- GitHub Actions para builds automáticos
- Tests automáticos en PRs
- Deploy automático a Play Store (beta)

---

### 📋 Checklist de Limpieza de Código

Antes de producción, realizar:

- [ ] **Remover código comentado** innecesario
- [ ] **Eliminar imports no usados** (Optimize Imports)
- [ ] **Formatear código** según Kotlin Style Guide
- [ ] **Remover logs de debug** (`println`, `Log.d`)
- [ ] **Actualizar dependencias** a últimas versiones estables
- [ ] **Revisar TODOs** y eliminar o convertir a issues
- [ ] **Verificar lint warnings** (`./gradlew lint`)
- [ ] **Ejecutar detekt** para análisis estático
- [ ] **Remover ApiTestActivity** de producción
- [ ] **Limpiar recursos no usados** (drawables, layouts)
- [ ] **Verificar permisos** necesarios en Manifest
- [ ] **Añadir proguard rules** específicas
- [ ] **Generar documentación** con Dokka
- [ ] **Crear CHANGELOG.md** con versiones

---

### 🎯 Plan de Implementación Sugerido

**Fase 1 - Crítico (Pre-entrega PFC):**
1. SEC-01: Mover API key a build config
2. DOC-01: Completar KDoc en clases públicas
3. DOC-02: Crear README de librería
4. Checklist de limpieza básico

**Fase 2 - Post-entrega (Versión 2.0):**
1. ARCH-01: Implementar ViewModels
2. ARCH-02: Añadir Hilt DI
3. TEST-01: Tests unitarios básicos
4. PERF-01: Migrations de Room

**Fase 3 - Producción (Versión 3.0):**
1. Todas las mejoras de seguridad (SEC-*)
2. ARCH-03: Navigation Component
3. PERF-02: Paginación
4. Tests completos (>80% coverage)
5. CI/CD pipeline

---

### 📚 Recursos de Referencia

**Documentación oficial:**
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Room Migration Guide](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Hilt Documentation](https://dagger.dev/hilt/)

**Mejores prácticas:**
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [Now in Android App](https://github.com/android/nowinandroid) - Ejemplo de arquitectura moderna
- [Android Testing Codelab](https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-basics)

---

**Auditoría realizada:** 2025-01-08
**Versión actual del proyecto:** 1.0 (PFC DAM 2º)
**Próxima versión planificada:** 2.0 (Post-entrega)