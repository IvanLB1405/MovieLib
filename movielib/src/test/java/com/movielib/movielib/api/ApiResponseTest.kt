package com.movielib.movielib.api

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para ApiResponse sealed class
 */
class ApiResponseTest {

    @Test
    fun `Loading state is correct`() {
        val response: ApiResponse<String> = ApiResponse.Loading

        assertTrue(response is ApiResponse.Loading)
        assertFalse(response.isSuccess())
        assertNull(response.getDataOrNull())
    }

    @Test
    fun `Success state with data is correct`() {
        val testData = listOf("Movie1", "Movie2", "Movie3")
        val response = ApiResponse.Success(testData)

        assertTrue(response is ApiResponse.Success)
        assertTrue(response.isSuccess())
        assertEquals(testData, response.data)
        assertEquals(testData, response.getDataOrNull())
    }

    @Test
    fun `Success state with empty list is valid`() {
        val emptyList = emptyList<String>()
        val response = ApiResponse.Success(emptyList)

        assertTrue(response.isSuccess())
        assertEquals(emptyList, response.data)
        assertTrue(response.data.isEmpty())
    }

    @Test
    fun `Error state with message is correct`() {
        val errorMessage = "Network error occurred"
        val response: ApiResponse<String> = ApiResponse.Error(errorMessage)

        assertTrue(response is ApiResponse.Error)
        assertFalse(response.isSuccess())
        assertNull(response.getDataOrNull())

        if (response is ApiResponse.Error) {
            assertEquals(errorMessage, response.message)
            assertNull(response.code)
        }
    }

    @Test
    fun `Error state with message and code is correct`() {
        val errorMessage = "Server error"
        val errorCode = 500
        val response: ApiResponse<String> = ApiResponse.Error(errorMessage, errorCode)

        assertTrue(response is ApiResponse.Error)

        if (response is ApiResponse.Error) {
            assertEquals(errorMessage, response.message)
            assertEquals(errorCode, response.code)
        }
    }

    @Test
    fun `NetworkError state is correct`() {
        val response: ApiResponse<String> = ApiResponse.NetworkError

        assertTrue(response is ApiResponse.NetworkError)
        assertFalse(response.isSuccess())
        assertNull(response.getDataOrNull())
    }

    @Test
    fun `isSuccess extension function returns true only for Success`() {
        val success: ApiResponse<String> = ApiResponse.Success("data")
        val loading: ApiResponse<String> = ApiResponse.Loading
        val error: ApiResponse<String> = ApiResponse.Error("error")
        val networkError: ApiResponse<String> = ApiResponse.NetworkError

        assertTrue(success.isSuccess())
        assertFalse(loading.isSuccess())
        assertFalse(error.isSuccess())
        assertFalse(networkError.isSuccess())
    }

    @Test
    fun `getDataOrNull returns data only for Success`() {
        val testData = "test data"
        val success = ApiResponse.Success(testData)
        val loading: ApiResponse<String> = ApiResponse.Loading
        val error: ApiResponse<String> = ApiResponse.Error("error")
        val networkError: ApiResponse<String> = ApiResponse.NetworkError

        assertEquals(testData, success.getDataOrNull())
        assertNull(loading.getDataOrNull())
        assertNull(error.getDataOrNull())
        assertNull(networkError.getDataOrNull())
    }

    @Test
    fun `multiple Success instances with different data are independent`() {
        val response1 = ApiResponse.Success(1)
        val response2 = ApiResponse.Success(2)

        assertNotEquals(response1.data, response2.data)
        assertEquals(1, response1.data)
        assertEquals(2, response2.data)
    }

    @Test
    fun `ApiResponse works with different generic types`() {
        val stringResponse = ApiResponse.Success("text")
        val intResponse = ApiResponse.Success(42)
        val listResponse = ApiResponse.Success(listOf(1, 2, 3))

        assertEquals("text", stringResponse.data)
        assertEquals(42, intResponse.data)
        assertEquals(3, listResponse.data.size)
    }
}
