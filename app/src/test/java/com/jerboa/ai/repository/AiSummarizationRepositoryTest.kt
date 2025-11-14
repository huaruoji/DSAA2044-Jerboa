package com.jerboa.ai.repository

import com.google.common.truth.Truth.assertThat
import com.jerboa.ai.api.SiliconFlowApiClient
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for AiSummarizationRepository.
 * 
 * These tests use dependency injection to mock the SiliconFlowApiClient,
 * allowing tests to run without requiring an actual API key.
 */
class AiSummarizationRepositoryTest {

    private lateinit var mockApiClient: SiliconFlowApiClient
    private lateinit var repository: AiSummarizationRepository

    @Before
    fun setUp() {
        // Create a mock API client
        mockApiClient = mock()
        // Mock the close() method to do nothing
        doNothing().whenever(mockApiClient).close()
        // Create repository with mocked API client
        repository = AiSummarizationRepository(mockApiClient)
    }

    @After
    fun tearDown() {
        // Clean up if needed
        try {
            repository.cleanup()
        } catch (e: Exception) {
            // Ignore cleanup errors in tests
        }
    }

    @Test
    fun `generatePostSummary with empty title and body returns failure`() = runTest {
        // Arrange
        val title = ""
        val body = ""

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isFailure).isTrue()
        result.onFailure { error ->
            assertThat(error.message).isNotNull()
            assertThat(error.message).contains("empty")
        }
    }

    @Test
    fun `generatePostSummary with valid content calls API client`() = runTest {
        // Arrange
        val title = "Test Post Title"
        val body = "Test post body content"
        val expectedSummary = "This is a test summary"

        // Mock the API client to return success
        whenever(mockApiClient.generateSummary(any(), any()))
            .thenReturn(Result.success(expectedSummary))

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isSuccess).isTrue()
        result.onSuccess { summary ->
            assertThat(summary).isEqualTo(expectedSummary)
        }
        // Verify API client was called with processed parameters
        verify(mockApiClient).generateSummary("Test Post Title", "Test post body content")
    }

    @Test
    fun `generatePostSummary trims whitespace from inputs`() = runTest {
        // Arrange
        val title = "  Test Title  "
        val body = "  Test Body  "
        val expectedSummary = "Summary"

        // Mock the API client
        whenever(mockApiClient.generateSummary(any(), any()))
            .thenReturn(Result.success(expectedSummary))

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isSuccess).isTrue()
        // Verify that trimmed values are passed to API
        verify(mockApiClient).generateSummary("Test Title", "Test Body")
    }

    @Test
    fun `generatePostSummary uses title as fallback when body is empty`() = runTest {
        // Arrange
        val title = "Test Title"
        val body = ""
        val expectedSummary = "Summary"

        // Mock the API client
        whenever(mockApiClient.generateSummary(any(), any()))
            .thenReturn(Result.success(expectedSummary))

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isSuccess).isTrue()
        // Verify that when body is empty, title is used as body (fallback logic)
        // effectiveTitle = cleanTitle (since it's not empty) = "Test Title"
        // effectiveBody = cleanTitle (since cleanBody is empty) = "Test Title"
        verify(mockApiClient).generateSummary("Test Title", "Test Title")
    }

    @Test
    fun `generatePostSummary uses default title when title is empty`() = runTest {
        // Arrange
        val title = ""
        val body = "Test body content"
        val expectedSummary = "Summary"

        // Mock the API client
        whenever(mockApiClient.generateSummary(any(), any()))
            .thenReturn(Result.success(expectedSummary))

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isSuccess).isTrue()
        // Verify that "Post Summary" is used as title when title is empty
        verify(mockApiClient).generateSummary("Post Summary", "Test body content")
    }

    @Test
    fun `generatePostSummary handles API exceptions`() = runTest {
        // Arrange
        val title = "Test Title"
        val body = "Test Body"
        val exception = Exception("Network error")

        // Mock API to return failure
        whenever(mockApiClient.generateSummary(any(), any()))
            .thenReturn(Result.failure(exception))

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isFailure).isTrue()
        result.onFailure { error ->
            assertThat(error).isNotNull()
            assertThat(error.message).isNotNull()
            assertThat(error.message).contains("Network error")
        }
    }

    @Test
    fun `generatePostSummary returns success with summary text`() = runTest {
        // Arrange
        val title = "Test Title"
        val body = "Test Body"
        val expectedSummary = "This is the generated summary"

        // Mock successful API response
        whenever(mockApiClient.generateSummary(any(), any()))
            .thenReturn(Result.success(expectedSummary))

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isSuccess).isTrue()
        result.onSuccess { summary ->
            assertThat(summary).isNotNull()
            assertThat(summary).isEqualTo(expectedSummary)
        }
    }

    @Test
    fun `generatePostSummary handles both title and body empty with whitespace`() = runTest {
        // Arrange
        val title = "   "
        val body = "   "

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isFailure).isTrue()
        result.onFailure { error ->
            assertThat(error.message).isNotNull()
            assertThat(error.message).contains("empty")
        }
    }

    @Test
    fun `generatePostSummary handles API client throwing exception`() = runTest {
        // Arrange
        val title = "Test Title"
        val body = "Test Body"
        val exception = RuntimeException("API connection failed")

        // Mock API to throw exception
        whenever(mockApiClient.generateSummary(any(), any()))
            .thenThrow(exception)

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isFailure).isTrue()
        result.onFailure { error ->
            assertThat(error).isNotNull()
            assertThat(error.message).isNotNull()
        }
    }
}
