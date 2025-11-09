# MovieLib - Android Movie Library

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)

A comprehensive Android library for movie discovery and management, integrating with The Movie Database (TMDb) API. Provides ready-to-use UI components, data management, and business logic for building movie-related applications.

## Features

- 🎬 **TMDb Integration** - Search movies, browse popular/top-rated lists, get detailed information
- 💾 **Local Caching** - Room database for offline access and performance
- 📚 **Personal Library** - Users can build their own movie collection
- ⭐ **Ratings & Reviews** - Add personal ratings and reviews to movies
- 🔄 **Reactive Data** - Kotlin Flow for real-time UI updates
- 🖼️ **Image Loading** - Glide integration for efficient poster/backdrop images
- 🧪 **Well Tested** - 36+ unit and instrumented tests
- 📖 **Documented** - Complete KDoc documentation for all public APIs

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [API Reference](#api-reference)
- [Usage Examples](#usage-examples)
- [Requirements](#requirements)
- [Dependencies](#dependencies)
- [Testing](#testing)
- [License](#license)

## Installation

### Step 1: Add the library module

Add the `:movielib` module to your project:

```groovy
// settings.gradle.kts
include(":movielib")
```

### Step 2: Add dependency

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":movielib"))
}
```

### Step 3: Configure TMDb API Key

Create or update `local.properties` in your project root:

```properties
TMDB_API_KEY=your_api_key_here
```

Get your API key from [The Movie Database](https://www.themoviedb.org/settings/api).

### Step 4: Add Permissions

Add required permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Quick Start

### Basic Setup

```kotlin
class MainActivity : AppCompatActivity() {

    private val repository: MovieRepository by lazy {
        val database = MovieDatabase.getDatabase(this)
        MovieRepository(database.movieDao(), BuildConfig.TMDB_API_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            searchMovies("Inception")
        }
    }

    private suspend fun searchMovies(query: String) {
        repository.searchMovies(query).collect { response ->
            when (response) {
                is ApiResponse.Loading -> showLoading()
                is ApiResponse.Success -> displayMovies(response.data)
                is ApiResponse.Error -> showError(response.message)
                is ApiResponse.NetworkError -> showNetworkError()
            }
        }
    }
}
```

### Using Extension Functions (Recommended)

```kotlin
import com.movielib.extensions.handle

repository.searchMovies("Inception").collect { response ->
    response.handle(
        onLoading = { showLoading() },
        onSuccess = { movies -> displayMovies(movies) },
        onError = { message, code -> showError(message) },
        onNetworkError = { showNetworkError() }
    )
}
```

## Architecture

MovieLib follows Clean Architecture principles with distinct layers:

```
movielib/
├── api/              # Retrofit service definitions
│   ├── ApiClient.kt
│   ├── TMDbService.kt
│   └── ApiResponse.kt (sealed class)
├── database/         # Room database
│   ├── MovieDatabase.kt
│   └── MovieDao.kt
├── models/           # Data models
│   └── Movie.kt (entity + API models)
├── repository/       # Repository pattern
│   └── MovieRepository.kt
└── utils/            # Constants and utilities
    └── Constants.kt
```

### Key Components

#### 1. **ApiResponse** (Sealed Class)

Standardized response wrapper for all API operations:

```kotlin
sealed class ApiResponse<out T> {
    object Loading : ApiResponse<Nothing>()
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResponse<Nothing>()
    object NetworkError : ApiResponse<Nothing>()
}
```

#### 2. **MovieRepository** (Single Source of Truth)

Coordinates between remote API and local database:

```kotlin
class MovieRepository(
    private val movieDao: MovieDao,
    private val apiKey: String
) {
    fun searchMovies(query: String): Flow<ApiResponse<List<Movie>>>
    fun getPopularMovies(): Flow<ApiResponse<List<Movie>>>
    fun getMovieDetails(movieId: Int): Flow<ApiResponse<Movie>>

    suspend fun addToLibrary(movieId: Int): Boolean
    suspend fun updateUserRating(movieId: Int, rating: Float): Boolean
    // ... more library operations
}
```

#### 3. **Movie Entity** (Room + API Model)

```kotlin
@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String?,
    val posterPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val genres: String?,      // JSON string
    val cast: String?,        // JSON string

    // User data
    val isInLibrary: Boolean = false,
    val userRating: Float? = null,
    val userReview: String? = null,
    val dateAdded: Long? = null
)
```

## API Reference

### Repository Methods

#### Search & Discovery

```kotlin
// Search movies by text
fun searchMovies(query: String, page: Int = 1): Flow<ApiResponse<List<Movie>>>

// Get popular movies
fun getPopularMovies(page: Int = 1): Flow<ApiResponse<List<Movie>>>

// Get top-rated movies
fun getTopRatedMovies(page: Int = 1): Flow<ApiResponse<List<Movie>>>

// Get detailed movie information
fun getMovieDetails(movieId: Int): Flow<ApiResponse<Movie>>
```

#### Library Management

```kotlin
// Add movie to personal library
suspend fun addToLibrary(movieId: Int): Boolean

// Remove movie from library (also clears rating & review)
suspend fun removeFromLibrary(movieId: Int): Boolean

// Check if movie is in library
suspend fun isMovieInLibrary(movieId: Int): Boolean

// Get library as reactive Flow
fun getLibraryMoviesFlow(): Flow<List<Movie>>

// Get library (one-time operation)
suspend fun getLibraryMovies(): List<Movie>
```

#### Ratings & Reviews

```kotlin
// Update user rating (0.0 - 10.0)
suspend fun updateUserRating(movieId: Int, rating: Float): Boolean

// Add or update review
suspend fun updateUserReview(movieId: Int, review: String?): Boolean

// Get movies with reviews
suspend fun getMoviesWithReviews(): List<Movie>
```

#### Statistics

```kotlin
// Get library statistics
suspend fun getLibraryStats(): LibraryStats

data class LibraryStats(
    val totalMovies: Int,
    val averageRating: Double,
    val moviesWithReviews: Int
)
```

### Extension Functions

Located in `com.movielib.extensions`:

```kotlin
// Chainable handling
response
    .onLoading { showLoading() }
    .onSuccess { data -> display(data) }
    .onError { msg, code -> showError(msg) }
    .onNetworkError { showNetworkError() }

// All-in-one handler (recommended)
response.handle(
    onLoading = { },
    onSuccess = { data -> },
    onError = { message, code -> },
    onNetworkError = { }
)
```

## Usage Examples

### Example 1: Build a Search Screen

```kotlin
class SearchActivity : AppCompatActivity() {

    private lateinit var repository: MovieRepository
    private lateinit var adapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = MovieDatabase.getDatabase(this)
        repository = MovieRepository(database.movieDao(), BuildConfig.TMDB_API_KEY)

        setupSearchBar()
    }

    private fun setupSearchBar() {
        searchView.addTextChangedListener { text ->
            lifecycleScope.launch {
                delay(500) // Debounce
                searchMovies(text.toString())
            }
        }
    }

    private suspend fun searchMovies(query: String) {
        repository.searchMovies(query).collect { response ->
            response.handle(
                onLoading = {
                    progressBar.isVisible = true
                },
                onSuccess = { movies ->
                    progressBar.isVisible = false
                    adapter.submitList(movies)
                },
                onError = { message, _ ->
                    progressBar.isVisible = false
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
```

### Example 2: Movie Details with Library Actions

```kotlin
class MovieDetailActivity : AppCompatActivity() {

    private lateinit var repository: MovieRepository
    private var currentMovie: Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val movieId = intent.getIntExtra("MOVIE_ID", -1)
        loadMovieDetails(movieId)

        favoriteButton.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun loadMovieDetails(movieId: Int) {
        lifecycleScope.launch {
            repository.getMovieDetails(movieId).collect { response ->
                response.handle(
                    onSuccess = { movie ->
                        currentMovie = movie
                        displayMovie(movie)
                        updateFavoriteButton(movie.isInLibrary)
                    }
                )
            }
        }
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            currentMovie?.let { movie ->
                if (movie.isInLibrary) {
                    repository.removeFromLibrary(movie.id)
                    Toast.makeText(this, "Removed from library", Toast.LENGTH_SHORT).show()
                } else {
                    repository.addToLibrary(movie.id)
                    Toast.makeText(this, "Added to library", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
```

### Example 3: Personal Library with Statistics

```kotlin
class LibraryActivity : AppCompatActivity() {

    private lateinit var repository: MovieRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadLibrary()
    }

    private fun loadLibrary() {
        lifecycleScope.launch {
            // Get statistics
            val stats = repository.getLibraryStats()
            displayStats(stats)

            // Observe library changes with Flow
            repository.getLibraryMoviesFlow().collectLatest { movies ->
                if (movies.isEmpty()) {
                    showEmptyState()
                } else {
                    adapter.submitList(movies)
                }
            }
        }
    }

    private fun displayStats(stats: LibraryStats) {
        totalMoviesText.text = stats.totalMovies.toString()
        averageRatingText.text = String.format("%.1f", stats.averageRating)
        reviewedMoviesText.text = stats.moviesWithReviews.toString()
    }
}
```

### Example 4: Rating & Reviewing Movies

```kotlin
private fun showRatingDialog(movie: Movie) {
    val dialogView = layoutInflater.inflate(R.layout.dialog_rating, null)
    val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
    val reviewEditText = dialogView.findViewById<EditText>(R.id.reviewEditText)

    // Pre-fill with existing data
    movie.userRating?.let { ratingBar.rating = it / 2f }
    reviewEditText.setText(movie.userReview ?: "")

    AlertDialog.Builder(this)
        .setTitle("Rate & Review")
        .setView(dialogView)
        .setPositiveButton("Save") { _, _ ->
            val rating = ratingBar.rating * 2f // Convert 0-5 to 0-10
            val review = reviewEditText.text.toString()

            lifecycleScope.launch {
                // Add to library if not already
                if (!movie.isInLibrary) {
                    repository.addToLibrary(movie.id)
                }

                // Save rating and review
                repository.updateUserRating(movie.id, rating)
                repository.updateUserReview(movie.id, review)

                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
            }
        }
        .setNegativeButton("Cancel", null)
        .show()
}
```

## Requirements

- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 35
- **Compile SDK:** 35
- **Kotlin:** 1.9.22+
- **Java:** 11

## Dependencies

### Core Dependencies

```kotlin
// Kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// AndroidX
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")

// Material Design
implementation("com.google.android.material:material:1.11.0")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Image Loading
implementation("com.github.bumptech.glide:glide:4.16.0")
kapt("com.github.bumptech.glide:compiler:4.16.0")
```

### Test Dependencies

```kotlin
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("app.cash.turbine:turbine:1.0.0")
androidTestImplementation("androidx.room:room-testing:2.6.1")
```

## Testing

The library includes comprehensive test coverage:

- **Unit Tests:** `movielib/src/test/`
  - ApiResponse tests
  - Movie model tests
  - Repository tests (with MockK)

- **Instrumented Tests:** `movielib/src/androidTest/`
  - Room DAO tests (in-memory database)

Run tests:

```bash
# Run all tests
./gradlew :movielib:test

# Run only unit tests
./gradlew :movielib:testDebugUnitTest

# Run instrumented tests (requires emulator/device)
./gradlew :movielib:connectedAndroidTest
```

## Security & Performance

### Security Features

- ✅ API key stored in `BuildConfig` (not hardcoded)
- ✅ ProGuard/R8 rules included for release builds
- ✅ Cleartext traffic disabled in release
- ✅ Logging interceptor only in debug builds

### Performance Optimizations

- ✅ Smart caching with Room database
- ✅ Offline-first strategy for movie details
- ✅ Efficient image loading with Glide
- ✅ Kotlin Flow for reactive updates
- ✅ Lazy repository initialization

## Best Practices

### 1. Use BaseMovieActivity

```kotlin
// Instead of AppCompatActivity, extend BaseMovieActivity
class YourActivity : BaseMovieActivity() {
    // repository is already available
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use repository directly
    }
}
```

### 2. Handle All ApiResponse States

```kotlin
// Always handle all possible states
repository.searchMovies(query).collect { response ->
    response.handle(
        onLoading = { /* Show loading indicator */ },
        onSuccess = { /* Update UI */ },
        onError = { msg, code -> /* Show error message */ },
        onNetworkError = { /* Show offline state */ }
    )
}
```

### 3. Use Flow for Reactive Data

```kotlin
// Use Flow for library to get automatic updates
repository.getLibraryMoviesFlow().collectLatest { movies ->
    adapter.submitList(movies)
}
```

### 4. Preserve User Data

```kotlin
// Repository automatically preserves user ratings/reviews when updating from API
// No need to manually merge data
```

## Troubleshooting

### Issue: "API key is required"

**Solution:** Ensure `TMDB_API_KEY` is set in `local.properties` and rebuild project.

### Issue: "Network error" on all requests

**Solution:** Check internet permission in `AndroidManifest.xml` and verify API key is valid.

### Issue: Images not loading

**Solution:** Ensure Glide dependency is added and internet permission is granted.

### Issue: Database migration error

**Solution:** The library uses `fallbackToDestructiveMigration()`. For production, implement proper migrations.

## Contributing

This library was developed as part of a Final Year Project (PFC) for DAM 2º (Desarrollo de Aplicaciones Multiplataforma).

## License

```
MIT License

Copyright (c) 2025 MovieLib

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Resources

- [TMDb API Documentation](https://developers.themoviedb.org/3)
- [Android Room Database](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Retrofit](https://square.github.io/retrofit/)

---

**Made with ❤️ for Android developers**
