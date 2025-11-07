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
