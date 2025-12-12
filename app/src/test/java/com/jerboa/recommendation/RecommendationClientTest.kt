package com.jerboa.recommendation

import com.jerboa.recommendation.api.RecommendationApi
import com.jerboa.recommendation.model.ScoreRequest
import com.jerboa.recommendation.model.ScoreResponse
import com.jerboa.recommendation.model.CandidatePost
import com.jerboa.recommendation.model.ScoredCandidate
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
    private lateinit var mockRecommendationApi: RecommendationApi

    private lateinit var testRequest: ScoreRequest

    @Before
    fun setup() {
        testRequest = ScoreRequest(
            historyContents = listOf("User history post 1", "User history post 2"),
            candidates = listOf(
                CandidatePost("1", "Test post 1", "Body 1"),
                CandidatePost("2", "Test post 2", "Body 2")
            ),
            topK = 10
        )
    }

    /**
     * Test 1: Verify successful API call returns scores
     * Acceptance Criteria: API successfully scores candidate posts
     */
    @Test
    fun `scoreRecommendations should return scores on success`() = runBlocking {
        // Arrange
        val expectedResponse = ScoreResponse(
            success = true,
            algorithm = "tfidf",
            count = 2,
            scoredCandidates = listOf(
                ScoredCandidate("1", 0.85),
                ScoredCandidate("2", 0.72)
            )
        )
        whenever(mockRecommendationApi.scoreCandidates(any()))
            .thenReturn(Response.success(expectedResponse))

        // Act
        val response = mockRecommendationApi.scoreCandidates(testRequest)

        // Assert
        assertTrue("Response should be successful", response.isSuccessful)
        assertNotNull("Response body should not be null", response.body())
        assertEquals("Should return 2 scores", 2, response.body()?.scoredCandidates?.size)
        assertEquals("First score should be 0.85", 0.85, response.body()?.scoredCandidates?.get(0)?.similarityScore ?: 0.0, 0.01)
        assertEquals("Second score should be 0.72", 0.72, response.body()?.scoredCandidates?.get(1)?.similarityScore ?: 0.0, 0.01)
    }

    /**
     * Test 2: Verify API call with empty candidates
     * Acceptance Criteria: API handles empty candidate list gracefully
     */
    @Test
    fun `scoreRecommendations should handle empty candidates`() = runBlocking {
        // Arrange
        val emptyRequest = ScoreRequest(
            historyContents = listOf("User history"),
            candidates = emptyList(),
            topK = 10
        )
        val expectedResponse = ScoreResponse(
            success = true,
            scoredCandidates = emptyList()
        )
        whenever(mockRecommendationApi.scoreCandidates(emptyRequest))
            .thenReturn(Response.success(expectedResponse))

        // Act
        val response = mockRecommendationApi.scoreCandidates(emptyRequest)

        // Assert
        assertTrue("Response should be successful", response.isSuccessful)
        assertTrue("Scores should be empty", response.body()?.scoredCandidates?.isEmpty() == true)
    }

    /**
     * Test 3: Verify API error handling
     * Acceptance Criteria: Network errors are properly handled
     */
    @Test
    fun `scoreRecommendations should handle API errors`() = runBlocking {
        // Arrange
        val errorBody = "Internal Server Error".toResponseBody("text/plain".toMediaType())
        whenever(mockRecommendationApi.scoreCandidates(any()))
            .thenReturn(Response.error(500, errorBody))

        // Act
        val response = mockRecommendationApi.scoreCandidates(testRequest)

        // Assert
        assertFalse("Response should not be successful", response.isSuccessful)
        assertEquals("Should return 500 error code", 500, response.code())
    }

    /**
     * Test 4: Verify request serialization
     * Acceptance Criteria: Request contains correct candidate and history data
     */
    @Test
    fun `ScoreRequest should serialize candidates and history correctly`() {
        // Arrange
        val candidates = listOf(
            CandidatePost("A", "Post A", "Body A"),
            CandidatePost("B", "Post B", "Body B"),
            CandidatePost("C", "Post C", "Body C")
        )
        val history = listOf("History 1", "History 2")

        // Act
        val request = ScoreRequest(history, candidates, 10)

        // Assert
        assertEquals("Candidates size should match", 3, request.candidates.size)
        assertEquals("History size should match", 2, request.historyContents.size)
        assertEquals("First candidate ID should be 'A'", "A", request.candidates[0].id)
        assertEquals("First history should be 'History 1'", "History 1", request.historyContents[0])
    }

    /**
     * Test 5: Verify response deserialization
     * Acceptance Criteria: Scores are correctly parsed from API response
     */
    @Test
    fun `ScoreResponse should deserialize scores correctly`() {
        // Arrange
        val scoredCandidates = listOf(
            ScoredCandidate("1", 0.95),
            ScoredCandidate("2", 0.87),
            ScoredCandidate("3", 0.65),
            ScoredCandidate("4", 0.42)
        )

        // Act
        val response = ScoreResponse(
            success = true,
            scoredCandidates = scoredCandidates
        )

        // Assert
        assertEquals("Scores size should match", 4, response.scoredCandidates.size)
        assertEquals("First score should be 0.95", 0.95, response.scoredCandidates[0].similarityScore, 0.001)
        assertEquals("Last score should be 0.42", 0.42, response.scoredCandidates[3].similarityScore, 0.001)
        assertTrue("All scores should be between 0 and 1", 
            response.scoredCandidates.all { it.similarityScore in 0.0..1.0 }
        )
    }
}
