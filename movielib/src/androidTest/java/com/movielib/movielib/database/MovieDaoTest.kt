package com.movielib.movielib.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.movielib.movielib.models.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests instrumentados para MovieDao
 *
 * Estos tests requieren un contexto de Android y se ejecutan en un dispositivo/emulador
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MovieDaoTest {

    private lateinit var database: MovieDatabase
    private lateinit var movieDao: MovieDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            MovieDatabase::class.java
        ).allowMainThreadQueries().build()

        movieDao = database.movieDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    // ===== Insert Tests =====

    @Test
    fun insertMovie_andGetById_returnsCorrectMovie() = runTest {
        // Arrange
        val movie = Movie(
            id = 1,
            title = "Test Movie",
            overview = "Test Overview",
            posterPath = "/test.jpg",
            releaseDate = "2024-01-01",
            voteAverage = 8.5
        )

        // Act
        movieDao.insertMovie(movie)
        val retrieved = movieDao.getMovieById(1)

        // Assert
        assertNotNull(retrieved)
        assertEquals(movie.id, retrieved?.id)
        assertEquals(movie.title, retrieved?.title)
        assertEquals(movie.overview, retrieved?.overview)
        assertEquals(8.5, retrieved?.voteAverage ?: 0.0, 0.001)
    }

    @Test
    fun insertMovies_insertsMultipleMovies() = runTest {
        // Arrange
        val movies = listOf(
            Movie(1, "Movie 1", "Overview 1", "/p1.jpg", "2024-01-01", 7.0),
            Movie(2, "Movie 2", "Overview 2", "/p2.jpg", "2024-02-01", 8.0),
            Movie(3, "Movie 3", "Overview 3", "/p3.jpg", "2024-03-01", 9.0)
        )

        // Act
        movieDao.insertMovies(movies)
        val movie1 = movieDao.getMovieById(1)
        val movie2 = movieDao.getMovieById(2)
        val movie3 = movieDao.getMovieById(3)

        // Assert
        assertNotNull(movie1)
        assertNotNull(movie2)
        assertNotNull(movie3)
        assertEquals("Movie 1", movie1?.title)
        assertEquals("Movie 2", movie2?.title)
        assertEquals("Movie 3", movie3?.title)
    }

    @Test
    fun insertMovie_withSameId_replacesExisting() = runTest {
        // Arrange
        val originalMovie = Movie(
            id = 100,
            title = "Original Title",
            overview = "Original Overview",
            posterPath = "/original.jpg",
            releaseDate = "2024-01-01",
            voteAverage = 7.0
        )

        val updatedMovie = Movie(
            id = 100,
            title = "Updated Title",
            overview = "Updated Overview",
            posterPath = "/updated.jpg",
            releaseDate = "2024-02-01",
            voteAverage = 9.0
        )

        // Act
        movieDao.insertMovie(originalMovie)
        val beforeUpdate = movieDao.getMovieById(100)

        movieDao.insertMovie(updatedMovie)
        val afterUpdate = movieDao.getMovieById(100)

        // Assert
        assertEquals("Original Title", beforeUpdate?.title)
        assertEquals("Updated Title", afterUpdate?.title)
        assertEquals(9.0, afterUpdate?.voteAverage ?: 0.0, 0.001)
    }

    // ===== Library Operations Tests =====

    @Test
    fun addToLibrary_setsIsInLibraryToTrue() = runTest {
        // Arrange
        val movie = Movie(
            id = 200,
            title = "Library Movie",
            overview = "Will be added to library",
            posterPath = "/lib.jpg",
            releaseDate = "2024-04-01",
            voteAverage = 8.0,
            isInLibrary = false
        )
        movieDao.insertMovie(movie)

        // Act
        movieDao.addToLibrary(200)
        val updated = movieDao.getMovieById(200)

        // Assert
        assertTrue(updated?.isInLibrary ?: false)
        assertNotNull(updated?.dateAdded)
    }

    @Test
    fun removeFromLibrary_clearsLibraryData() = runTest {
        // Arrange
        val movie = Movie(
            id = 300,
            title = "Remove Movie",
            overview = "Will be removed",
            posterPath = "/rem.jpg",
            releaseDate = "2024-05-01",
            voteAverage = 7.5,
            isInLibrary = true,
            userRating = 9.0f,
            userReview = "Great movie!",
            dateAdded = 1704067200000L
        )
        movieDao.insertMovie(movie)

        // Act
        movieDao.removeFromLibrary(300)
        val updated = movieDao.getMovieById(300)

        // Assert
        assertFalse(updated?.isInLibrary ?: true)
        assertNull(updated?.userRating)
        assertNull(updated?.userReview)
        assertNull(updated?.dateAdded)
    }

    @Test
    fun isMovieInLibrary_returnsCorrectStatus() = runTest {
        // Arrange
        val inLibraryMovie = Movie(
            id = 400,
            title = "In Library",
            overview = "In library",
            posterPath = "/in.jpg",
            releaseDate = "2024-06-01",
            voteAverage = 8.0,
            isInLibrary = true
        )
        val notInLibraryMovie = Movie(
            id = 401,
            title = "Not In Library",
            overview = "Not in library",
            posterPath = "/out.jpg",
            releaseDate = "2024-07-01",
            voteAverage = 7.0,
            isInLibrary = false
        )

        movieDao.insertMovies(listOf(inLibraryMovie, notInLibraryMovie))

        // Act & Assert
        assertTrue(movieDao.isMovieInLibrary(400))
        assertFalse(movieDao.isMovieInLibrary(401))
        assertFalse(movieDao.isMovieInLibrary(999)) // Non-existent movie
    }

    @Test
    fun getLibraryMovies_returnsOnlyLibraryMovies() = runTest {
        // Arrange
        val movies = listOf(
            Movie(500, "Library 1", "In lib", "/l1.jpg", "2024-01-01", 8.0, isInLibrary = true),
            Movie(501, "Not in Library", "Not in lib", "/nl.jpg", "2024-02-01", 7.0, isInLibrary = false),
            Movie(502, "Library 2", "In lib", "/l2.jpg", "2024-03-01", 9.0, isInLibrary = true)
        )
        movieDao.insertMovies(movies)

        // Act
        val libraryMovies = movieDao.getLibraryMovies()

        // Assert
        assertEquals(2, libraryMovies.size)
        assertTrue(libraryMovies.all { it.isInLibrary })
        assertTrue(libraryMovies.any { it.title == "Library 1" })
        assertTrue(libraryMovies.any { it.title == "Library 2" })
    }

    @Test
    fun getLibraryMoviesFlow_emitsUpdates() = runTest {
        // Arrange
        val movie = Movie(
            id = 600,
            title = "Flow Movie",
            overview = "Test flow",
            posterPath = "/flow.jpg",
            releaseDate = "2024-08-01",
            voteAverage = 8.5,
            isInLibrary = false
        )
        movieDao.insertMovie(movie)

        // Act
        val initialLibrary = movieDao.getLibraryMoviesFlow().first()
        assertEquals(0, initialLibrary.size)

        movieDao.addToLibrary(600)
        val updatedLibrary = movieDao.getLibraryMoviesFlow().first()

        // Assert
        assertEquals(1, updatedLibrary.size)
        assertEquals("Flow Movie", updatedLibrary[0].title)
    }

    @Test
    fun getLibraryCount_returnsCorrectCount() = runTest {
        // Arrange
        val movies = List(10) { index ->
            Movie(
                id = 700 + index,
                title = "Movie $index",
                overview = "Overview",
                posterPath = "/p.jpg",
                releaseDate = "2024-01-01",
                voteAverage = 7.0,
                isInLibrary = index < 5 // Only first 5 in library
            )
        }
        movieDao.insertMovies(movies)

        // Act
        val count = movieDao.getLibraryCount()

        // Assert
        assertEquals(5, count)
    }

    // ===== User Rating Tests =====

    @Test
    fun updateUserRating_updatesRating() = runTest {
        // Arrange
        val movie = Movie(
            id = 800,
            title = "Rating Movie",
            overview = "Test rating",
            posterPath = "/rate.jpg",
            releaseDate = "2024-09-01",
            voteAverage = 7.0,
            userRating = null
        )
        movieDao.insertMovie(movie)

        // Act
        movieDao.updateUserRating(800, 9.5f)
        val updated = movieDao.getMovieById(800)

        // Assert
        assertEquals(9.5f, updated?.userRating ?: 0f, 0.01f)
    }

    @Test
    fun getAverageUserRating_calculatesCorrectly() = runTest {
        // Arrange
        val movies = listOf(
            Movie(900, "Movie 1", "O", "/p.jpg", "2024-01-01", 7.0, userRating = 8.0f),
            Movie(901, "Movie 2", "O", "/p.jpg", "2024-01-01", 7.0, userRating = 6.0f),
            Movie(902, "Movie 3", "O", "/p.jpg", "2024-01-01", 7.0, userRating = 10.0f),
            Movie(903, "Movie 4", "O", "/p.jpg", "2024-01-01", 7.0, userRating = null)
        )
        movieDao.insertMovies(movies)

        // Act
        val average = movieDao.getAverageUserRating()

        // Assert
        // Average of 8.0, 6.0, 10.0 = 24.0 / 3 = 8.0
        assertNotNull(average)
        assertEquals(8.0, average!!, 0.01)
    }

    @Test
    fun getAverageUserRating_withNoRatings_returnsNull() = runTest {
        // Arrange
        val movie = Movie(
            id = 1000,
            title = "No Rating",
            overview = "No rating",
            posterPath = "/nr.jpg",
            releaseDate = "2024-10-01",
            voteAverage = 7.0,
            userRating = null
        )
        movieDao.insertMovie(movie)

        // Act
        val average = movieDao.getAverageUserRating()

        // Assert
        assertNull(average)
    }

    // ===== User Review Tests =====

    @Test
    fun updateUserReview_updatesReview() = runTest {
        // Arrange
        val movie = Movie(
            id = 1100,
            title = "Review Movie",
            overview = "Test review",
            posterPath = "/rev.jpg",
            releaseDate = "2024-11-01",
            voteAverage = 8.0,
            userReview = null
        )
        movieDao.insertMovie(movie)

        // Act
        val review = "This is an excellent movie! Highly recommended."
        movieDao.updateUserReview(1100, review)
        val updated = movieDao.getMovieById(1100)

        // Assert
        assertEquals(review, updated?.userReview)
    }

    @Test
    fun updateUserReview_withNull_clearsReview() = runTest {
        // Arrange
        val movie = Movie(
            id = 1200,
            title = "Clear Review",
            overview = "Test clear",
            posterPath = "/clear.jpg",
            releaseDate = "2024-12-01",
            voteAverage = 7.5,
            userReview = "Old review"
        )
        movieDao.insertMovie(movie)

        // Act
        movieDao.updateUserReview(1200, null)
        val updated = movieDao.getMovieById(1200)

        // Assert
        assertNull(updated?.userReview)
    }

    @Test
    fun getMoviesWithReviews_returnsOnlyReviewedMovies() = runTest {
        // Arrange
        val movies = listOf(
            Movie(1300, "Reviewed 1", "O", "/p.jpg", "2024-01-01", 8.0, userReview = "Good"),
            Movie(1301, "No Review", "O", "/p.jpg", "2024-01-01", 7.0, userReview = null),
            Movie(1302, "Reviewed 2", "O", "/p.jpg", "2024-01-01", 9.0, userReview = "Great")
        )
        movieDao.insertMovies(movies)

        // Act
        val reviewedMovies = movieDao.getMoviesWithReviews()

        // Assert
        assertEquals(2, reviewedMovies.size)
        assertTrue(reviewedMovies.all { it.userReview != null })
        assertTrue(reviewedMovies.any { it.title == "Reviewed 1" })
        assertTrue(reviewedMovies.any { it.title == "Reviewed 2" })
    }

    // ===== Complex Scenarios =====

    @Test
    fun movieLifecycle_addToLibrary_rate_review_remove() = runTest {
        // Arrange
        val movie = Movie(
            id = 1400,
            title = "Lifecycle Movie",
            overview = "Test full lifecycle",
            posterPath = "/life.jpg",
            releaseDate = "2025-01-01",
            voteAverage = 8.0
        )
        movieDao.insertMovie(movie)

        // Act & Assert - Initial state
        val initial = movieDao.getMovieById(1400)
        assertFalse(initial?.isInLibrary ?: true)
        assertNull(initial?.userRating)
        assertNull(initial?.userReview)

        // Add to library
        movieDao.addToLibrary(1400)
        val inLibrary = movieDao.getMovieById(1400)
        assertTrue(inLibrary?.isInLibrary ?: false)

        // Add rating
        movieDao.updateUserRating(1400, 9.0f)
        val rated = movieDao.getMovieById(1400)
        assertEquals(9.0f, rated?.userRating ?: 0f, 0.01f)

        // Add review
        movieDao.updateUserReview(1400, "Amazing movie!")
        val reviewed = movieDao.getMovieById(1400)
        assertEquals("Amazing movie!", reviewed?.userReview)

        // Remove from library
        movieDao.removeFromLibrary(1400)
        val removed = movieDao.getMovieById(1400)
        assertFalse(removed?.isInLibrary ?: true)
        assertNull(removed?.userRating)
        assertNull(removed?.userReview)
    }

    @Test
    fun emptyDatabase_queries_returnEmptyOrNull() = runTest {
        // Act & Assert
        assertNull(movieDao.getMovieById(9999))
        assertEquals(0, movieDao.getLibraryMovies().size)
        assertEquals(0, movieDao.getLibraryCount())
        assertEquals(0, movieDao.getMoviesWithReviews().size)
        assertNull(movieDao.getAverageUserRating())
        assertFalse(movieDao.isMovieInLibrary(9999))
    }
}
