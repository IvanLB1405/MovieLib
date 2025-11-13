package com.movielib.movielib.repository

import app.cash.turbine.test
import com.movielib.movielib.database.MovieDao
import com.movielib.movielib.models.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para MovieRepository
 *
 * Usa MockK para mockear MovieDao y las respuestas de la API
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MovieRepositoryTest {

    private lateinit var movieDao: MovieDao
    private lateinit var repository: MovieRepository
    private val testApiKey = "test_api_key_123"

    @Before
    fun setup() {
        movieDao = mockk()
        repository = MovieRepository(movieDao, testApiKey)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // Tests de Películas en Biblioteca

    @Test
    fun `getLibraryMoviesFlow returns flow from DAO`() = runTest {
        // Preparar
        val movies = listOf(
            Movie(1, "Movie 1", "Overview 1", "/p1.jpg", "2024-01-01", 7.0, isInLibrary = true),
            Movie(2, "Movie 2", "Overview 2", "/p2.jpg", "2024-02-01", 8.0, isInLibrary = true)
        )
        every { movieDao.getLibraryMoviesFlow() } returns flowOf(movies)

        // Actuar
        repository.getLibraryMoviesFlow().test {
            val emittedMovies = awaitItem()

            // Verificar
            assertEquals(2, emittedMovies.size)
            assertTrue(emittedMovies.all { it.isInLibrary })
            awaitComplete()
        }

        // Verificar llamadas
        verify(exactly = 1) { movieDao.getLibraryMoviesFlow() }
    }

    @Test
    fun `getLibraryMovies returns list from DAO`() = runTest {
        // Preparar
        val movies = listOf(
            Movie(3, "Library Movie", "In library", "/lib.jpg", "2024-03-01", 9.0, isInLibrary = true)
        )
        coEvery { movieDao.getLibraryMovies() } returns movies

        // Actuar
        val result = repository.getLibraryMovies()

        // Verificar
        assertEquals(1, result.size)
        assertEquals("Library Movie", result[0].title)
        assertTrue(result[0].isInLibrary)

        // Verificar llamadas
        coVerify(exactly = 1) { movieDao.getLibraryMovies() }
    }

    @Test
    fun `getMoviesWithReviews returns only movies with reviews`() = runTest {
        // Preparar
        val movies = listOf(
            Movie(
                4,
                "Reviewed Movie",
                "Has review",
                "/rev.jpg",
                "2024-04-01",
                8.5,
                userReview = "Great movie!"
            )
        )
        coEvery { movieDao.getMoviesWithReviews() } returns movies

        // Actuar
        val result = repository.getMoviesWithReviews()

        // Verificar
        assertEquals(1, result.size)
        assertNotNull(result[0].userReview)
        assertEquals("Great movie!", result[0].userReview)

        // Verificar llamadas
        coVerify(exactly = 1) { movieDao.getMoviesWithReviews() }
    }

    // Tests de Estadísticas de Biblioteca

    @Test
    fun `getLibraryStats returns correct statistics`() = runTest {
        // Preparar
        coEvery { movieDao.getLibraryCount() } returns 50
        coEvery { movieDao.getAverageUserRating() } returns 7.8
        val reviewedMovies = List(15) {
            Movie(it, "Movie $it", "Overview", "/p.jpg", "2024-01-01", 7.0, userReview = "Review")
        }
        coEvery { movieDao.getMoviesWithReviews() } returns reviewedMovies

        // Actuar
        val stats = repository.getLibraryStats()

        // Verificar
        assertEquals(50, stats.totalMovies)
        assertEquals(7.8, stats.averageRating, 0.001)
        assertEquals(15, stats.moviesWithReviews)

        // Verificar llamadas
        coVerify(exactly = 1) { movieDao.getLibraryCount() }
        coVerify(exactly = 1) { movieDao.getAverageUserRating() }
        coVerify(exactly = 1) { movieDao.getMoviesWithReviews() }
    }

    @Test
    fun `getLibraryStats handles null average rating`() = runTest {
        // Preparar
        coEvery { movieDao.getLibraryCount() } returns 0
        coEvery { movieDao.getAverageUserRating() } returns null
        coEvery { movieDao.getMoviesWithReviews() } returns emptyList()

        // Actuar
        val stats = repository.getLibraryStats()

        // Verificar
        assertEquals(0, stats.totalMovies)
        assertEquals(0.0, stats.averageRating, 0.001)
        assertEquals(0, stats.moviesWithReviews)
    }

    @Test
    fun `getLibraryStats with no reviews returns zero`() = runTest {
        // Preparar
        coEvery { movieDao.getLibraryCount() } returns 10
        coEvery { movieDao.getAverageUserRating() } returns 8.0
        coEvery { movieDao.getMoviesWithReviews() } returns emptyList()

        // Actuar
        val stats = repository.getLibraryStats()

        // Verificar
        assertEquals(10, stats.totalMovies)
        assertEquals(8.0, stats.averageRating, 0.001)
        assertEquals(0, stats.moviesWithReviews)
    }

    // Tests de Clase de Datos LibraryStats

    @Test
    fun `LibraryStats data class holds correct values`() {
        // Preparar y Actuar
        val stats = LibraryStats(
            totalMovies = 100,
            averageRating = 8.5,
            moviesWithReviews = 75
        )

        // Verificar
        assertEquals(100, stats.totalMovies)
        assertEquals(8.5, stats.averageRating, 0.001)
        assertEquals(75, stats.moviesWithReviews)
    }

    @Test
    fun `LibraryStats with zero values is valid`() {
        // Preparar y Actuar
        val stats = LibraryStats(
            totalMovies = 0,
            averageRating = 0.0,
            moviesWithReviews = 0
        )

        // Verificar
        assertEquals(0, stats.totalMovies)
        assertEquals(0.0, stats.averageRating, 0.001)
        assertEquals(0, stats.moviesWithReviews)
    }

    @Test
    fun `isMovieInLibrary delegates to DAO`() = runTest {
        // Preparar
        val movieId = 202
        coEvery { movieDao.isMovieInLibrary(movieId) } returns true

        // Actuar
        val result = repository.isMovieInLibrary(movieId)

        // Verificar
        assertTrue(result)
        coVerify(exactly = 1) { movieDao.isMovieInLibrary(movieId) }
    }

    @Test
    fun `isMovieInLibrary returns false when not in library`() = runTest {
        // Preparar
        val movieId = 303
        coEvery { movieDao.isMovieInLibrary(movieId) } returns false

        // Actuar
        val result = repository.isMovieInLibrary(movieId)

        // Verificar
        assertFalse(result)
        coVerify(exactly = 1) { movieDao.isMovieInLibrary(movieId) }
    }

    // Tests de Comportamiento de Flow 

    @Test
    fun `getLibraryMoviesFlow emits multiple values`() = runTest {
        // Preparar
        val initialMovies = listOf(
            Movie(500, "Movie 1", "O", "/p.jpg", "2024-01-01", 7.0, isInLibrary = true)
        )
        val updatedMovies = listOf(
            Movie(500, "Movie 1", "O", "/p.jpg", "2024-01-01", 7.0, isInLibrary = true),
            Movie(501, "Movie 2", "O", "/p.jpg", "2024-01-01", 8.0, isInLibrary = true)
        )

        every { movieDao.getLibraryMoviesFlow() } returns flowOf(initialMovies, updatedMovies)

        // Actuar y Verificar
        repository.getLibraryMoviesFlow().test {
            val first = awaitItem()
            assertEquals(1, first.size)

            val second = awaitItem()
            assertEquals(2, second.size)

            awaitComplete()
        }
    }

    @Test
    fun `getLibraryMoviesFlow emits empty list when no library movies`() = runTest {
        // Preparar
        every { movieDao.getLibraryMoviesFlow() } returns flowOf(emptyList())

        // Actuar y Verificar
        repository.getLibraryMoviesFlow().test {
            val movies = awaitItem()
            assertTrue(movies.isEmpty())
            awaitComplete()
        }
    }
}

