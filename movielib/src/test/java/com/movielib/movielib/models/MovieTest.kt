package com.movielib.movielib.models

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para entidad Movie y modelos relacionados
 */
class MovieTest {

    @Test
    fun `Movie entity is created with correct default values`() {
        val movie = Movie(
            id = 1,
            title = "Test Movie",
            overview = "A test overview",
            posterPath = "/test.jpg",
            releaseDate = "2024-01-01",
            voteAverage = 8.5
        )

        assertEquals(1, movie.id)
        assertEquals("Test Movie", movie.title)
        assertEquals("A test overview", movie.overview)
        assertEquals("/test.jpg", movie.posterPath)
        assertEquals("2024-01-01", movie.releaseDate)
        assertEquals(8.5, movie.voteAverage, 0.001)
        assertNull(movie.genres)
        assertNull(movie.cast)
        assertFalse(movie.isInLibrary)
        assertNull(movie.userRating)
        assertNull(movie.userReview)
        assertNull(movie.dateAdded)
    }

    @Test
    fun `Movie entity with all fields populated is correct`() {
        val movie = Movie(
            id = 2,
            title = "Complete Movie",
            overview = "Full overview",
            posterPath = "/poster.jpg",
            releaseDate = "2024-06-15",
            voteAverage = 9.2,
            genres = "Action,Drama",
            cast = "Actor One,Actor Two,Actor Three",
            isInLibrary = true,
            userRating = 9.0f,
            userReview = "Amazing movie!",
            dateAdded = 1704067200000L
        )

        assertTrue(movie.isInLibrary)
        assertEquals(9.0f, movie.userRating)
        assertEquals("Amazing movie!", movie.userReview)
        assertEquals(1704067200000L, movie.dateAdded)
        assertEquals("Action,Drama", movie.genres)
        assertEquals("Actor One,Actor Two,Actor Three", movie.cast)
    }

    @Test
    fun `MovieApiModel to Movie conversion is correct`() {
        val apiModel = MovieApiModel(
            id = 123,
            title = "API Movie",
            overview = "API Overview",
            poster_path = "/api_poster.jpg",
            release_date = "2024-03-20",
            vote_average = 7.8,
            genre_ids = listOf(28, 12, 878)
        )

        val movie = apiModel.toMovie()

        assertEquals(123, movie.id)
        assertEquals("API Movie", movie.title)
        assertEquals("API Overview", movie.overview)
        assertEquals("/api_poster.jpg", movie.posterPath)
        assertEquals("2024-03-20", movie.releaseDate)
        assertEquals(7.8, movie.voteAverage, 0.001)
        assertNull(movie.genres)
        assertNull(movie.cast)
        assertFalse(movie.isInLibrary)
    }

    @Test
    fun `MovieApiModel with null fields converts correctly`() {
        val apiModel = MovieApiModel(
            id = 456,
            title = "Minimal Movie",
            overview = null,
            poster_path = null,
            release_date = null,
            vote_average = 5.0,
            genre_ids = null
        )

        val movie = apiModel.toMovie()

        assertEquals(456, movie.id)
        assertEquals("Minimal Movie", movie.title)
        assertNull(movie.overview)
        assertNull(movie.posterPath)
        assertNull(movie.releaseDate)
        assertEquals(5.0, movie.voteAverage, 0.001)
    }

    @Test
    fun `MovieDetailApiModel to Movie conversion includes genres`() {
        val genres = listOf(
            Genre(28, "Action"),
            Genre(12, "Adventure"),
            Genre(878, "Science Fiction")
        )

        val detailModel = MovieDetailApiModel(
            id = 789,
            title = "Detailed Movie",
            overview = "Detailed overview",
            poster_path = "/detail_poster.jpg",
            release_date = "2024-05-10",
            vote_average = 8.9,
            genres = genres,
            credits = null
        )

        val movie = detailModel.toMovie()

        assertEquals(789, movie.id)
        assertEquals("Detailed Movie", movie.title)
        assertEquals("Action,Adventure,Science Fiction", movie.genres)
        assertNull(movie.cast)
    }

    @Test
    fun `MovieDetailApiModel to Movie conversion includes cast`() {
        val cast = listOf(
            CastMember(1, "Actor One", "Hero", "/profile1.jpg"),
            CastMember(2, "Actor Two", "Villain", "/profile2.jpg"),
            CastMember(3, "Actor Three", "Sidekick", "/profile3.jpg"),
            CastMember(4, "Actor Four", "Mentor", "/profile4.jpg"),
            CastMember(5, "Actor Five", "Support", "/profile5.jpg")
        )

        val credits = Credits(cast)
        val genres = listOf(Genre(28, "Action"))

        val detailModel = MovieDetailApiModel(
            id = 999,
            title = "Cast Movie",
            overview = "Movie with cast",
            poster_path = "/cast_poster.jpg",
            release_date = "2024-07-01",
            vote_average = 7.5,
            genres = genres,
            credits = credits
        )

        val movie = detailModel.toMovie()

        assertEquals("Actor One,Actor Two,Actor Three,Actor Four,Actor Five", movie.cast)
    }

    @Test
    fun `MovieDetailApiModel conversion limits cast to 5 members`() {
        val cast = listOf(
            CastMember(1, "Actor 1", "Role 1", null),
            CastMember(2, "Actor 2", "Role 2", null),
            CastMember(3, "Actor 3", "Role 3", null),
            CastMember(4, "Actor 4", "Role 4", null),
            CastMember(5, "Actor 5", "Role 5", null),
            CastMember(6, "Actor 6", "Role 6", null),
            CastMember(7, "Actor 7", "Role 7", null)
        )

        val credits = Credits(cast)
        val detailModel = MovieDetailApiModel(
            id = 888,
            title = "Large Cast Movie",
            overview = "Many actors",
            poster_path = "/large_cast.jpg",
            release_date = "2024-08-15",
            vote_average = 6.5,
            genres = listOf(Genre(18, "Drama")),
            credits = credits
        )

        val movie = detailModel.toMovie()

        val castArray = movie.cast?.split(",")
        assertEquals(5, castArray?.size)
        assertEquals("Actor 1", castArray?.get(0))
        assertEquals("Actor 5", castArray?.get(4))
    }

    @Test
    fun `MovieDetailApiModel with empty genres creates empty string`() {
        val detailModel = MovieDetailApiModel(
            id = 777,
            title = "No Genre Movie",
            overview = "No genres",
            poster_path = "/no_genre.jpg",
            release_date = "2024-09-01",
            vote_average = 5.5,
            genres = emptyList(),
            credits = null
        )

        val movie = detailModel.toMovie()

        assertEquals("", movie.genres)
    }

    @Test
    fun `MovieDetailApiModel with empty cast creates empty string`() {
        val detailModel = MovieDetailApiModel(
            id = 666,
            title = "No Cast Movie",
            overview = "No cast",
            poster_path = "/no_cast.jpg",
            release_date = "2024-10-01",
            vote_average = 4.5,
            genres = listOf(Genre(99, "Documentary")),
            credits = Credits(emptyList())
        )

        val movie = detailModel.toMovie()

        assertEquals("", movie.cast)
    }

    @Test
    fun `MovieSearchResponse is parsed correctly`() {
        val movies = listOf(
            MovieApiModel(1, "Movie 1", "Overview 1", "/p1.jpg", "2024-01-01", 7.0, null),
            MovieApiModel(2, "Movie 2", "Overview 2", "/p2.jpg", "2024-02-01", 8.0, null)
        )

        val response = MovieSearchResponse(
            page = 1,
            results = movies,
            totalPages = 10,
            totalResults = 200
        )

        assertEquals(1, response.page)
        assertEquals(2, response.results.size)
        assertEquals(10, response.totalPages)
        assertEquals(200, response.totalResults)
    }

    @Test
    fun `Genre model is correct`() {
        val genre = Genre(28, "Action")

        assertEquals(28, genre.id)
        assertEquals("Action", genre.name)
    }

    @Test
    fun `CastMember model is correct`() {
        val castMember = CastMember(
            id = 123,
            name = "John Doe",
            character = "Hero",
            profile_path = "/profile.jpg"
        )

        assertEquals(123, castMember.id)
        assertEquals("John Doe", castMember.name)
        assertEquals("Hero", castMember.character)
        assertEquals("/profile.jpg", castMember.profile_path)
    }

    @Test
    fun `Movie copy with updated library status works`() {
        val original = Movie(
            id = 1,
            title = "Original",
            overview = "Test",
            posterPath = null,
            releaseDate = null,
            voteAverage = 7.0,
            isInLibrary = false
        )

        val updated = original.copy(isInLibrary = true, dateAdded = 1704067200000L)

        assertTrue(updated.isInLibrary)
        assertEquals(1704067200000L, updated.dateAdded)
        assertEquals(original.id, updated.id)
        assertEquals(original.title, updated.title)
    }
}
