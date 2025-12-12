package com.jerboa.recommendation

import com.jerboa.recommendation.api.RecommendationClient
import com.jerboa.recommendation.api.ScoringRequest
import com.jerboa.recommendation.api.ScoringResponse
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import retrofit2.Response

/**
 * Unit tests for RecommendationClient
 * Tests Story #25: Recommendation API integration
 */
@RunWith(MockitoJUnitRunner::class)
class RecommendationClientTest {

    @Mock
    private lateinit var mockRecommendationClient: RecommendationClient

    private lateinit var testRequest: ScoringRequest

    @Before
    fun setup() {
        testRequest = ScoringRequest(
            candidates = listOf("Test post 1", "Test post 2"),
            history = listOf("User history post 1", "User history post 2")
        )
    }

    /**
     * Test 1: Verify successful API call returns scores
     * Acceptance Criteria: API successfully scores candidate posts
     */
    @Test
    fun `scoreRecommendations should return scores on success`() = runBlocking {
        // Arrange
        val expectedResponse = ScoringResponse(
            scores = listOf(0.85, 0.72)
        )
        whenever(mockRecommendationClient.scoreRecommendations(any()))
            .thenReturn(Response.success(expectedResponse))

        // Act
        val response = mockRecommendationClient.scoreRecommendations(testRequest)

        // Assert
        assertTrue("Response should be successful", response.isSuccessful)
        assertNotNull("Response body should not be null", response.body())
        assertEquals("Should return 2 scores", 2, response.body()?.scores?.size)
        assertEquals("First score should be 0.85", 0.85, response.body()?.scores?.get(0) ?: 0.0, 0.01)
        assertEquals("Second score should be 0.72", 0.72, response.body()?.scores?.get(1) ?: 0.0, 0.01)
    }

    /**
     * Test 2: Verify API call with empty candidates
     * Acceptance Criteria: API handles empty candidate list gracefully
     */
    @Test
    fun `scoreRecommendations should handle empty candidates`() = runBlocking {
        // Arrange
        val emptyRequest = ScoringRequest(
            candidates = emptyList(),
            history = listOf("User history")
        )
        val expectedResponse = ScoringResponse(scores = emptyList())
        whenever(mockRecommendationClient.scoreRecommendations(emptyRequest))
            .thenReturn(Response.success(expectedResponse))

        // Act
        val response = mockRecommendationClient.scoreRecommendations(emptyRequest)

        // Assert
        assertTrue("Response should be successful", response.isSuccessful)
        assertTrue("Scores should be empty", response.body()?.scores?.isEmpty() == true)
    }

    /**
     * Test 3: Verify API error handling
     * Acceptance Criteria: Network errors are properly handled
     */
    @Test
    fun `scoreRecommendations should handle API errors`() = runBlocking {
        // Arrange
        val errorBody = "Internal Server Error".toResponseBody("text/plain".toMediaType())
        whenever(mockRecommendationClient.scoreRecommendations(any()))
            .thenReturn(Response.error(500, errorBody))

        // Act
        val response = mockRecommendationClient.scoreRecommendations(testRequest)

        // Assert
        assertFalse("Response should not be successful", response.isSuccessful)
        assertEquals("Should return 500 error code", 500, response.code())
    }

    /**
     * Test 4: Verify request serialization
     * Acceptance Criteria: Request contains correct candidate and history data
     */
    @Test
    fun `ScoringRequest should serialize candidates and history correctly`() {
        // Arrange
        val candidates = listOf("Post A", "Post B", "Post C")
        val history = listOf("History 1", "History 2")

        // Act
        val request = ScoringRequest(candidates, history)

        // Assert
        assertEquals("Candidates size should match", 3, request.candidates.size)
        assertEquals("History size should match", 2, request.history.size)
        assertEquals("First candidate should be 'Post A'", "Post A", request.candidates[0])
        assertEquals("First history should be 'History 1'", "History 1", request.history[0])
    }

    /**
     * Test 5: Verify response deserialization
     * Acceptance Criteria: Scores are correctly parsed from API response
     */
    @Test
    fun `ScoringResponse should deserialize scores correctly`() {
        // Arrange
        val scores = listOf(0.95, 0.87, 0.65, 0.42)

        // Act
        val response = ScoringResponse(scores)

        // Assert
        assertEquals("Scores size should match", 4, response.scores.size)
        assertEquals("First score should be 0.95", 0.95, response.scores[0], 0.001)
        assertEquals("Last score should be 0.42", 0.42, response.scores[3], 0.001)
        assertTrue("All scores should be between 0 and 1", 
            response.scores.all { it in 0.0..1.0 }
        )
    }
}
